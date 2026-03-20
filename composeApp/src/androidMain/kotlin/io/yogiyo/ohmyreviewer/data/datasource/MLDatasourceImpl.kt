package io.yogiyo.ohmyreviewer.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.imagedescription.ImageDescriber
import com.google.mlkit.genai.imagedescription.ImageDescriberOptions
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriptionRequest
import com.google.ai.client.generativeai.GenerativeModel as GeminiCloudModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine

class MLDatasourceImpl(
    private val context: Context,
    private val externalScope: CoroutineScope,
    private val geminiApiKey: String = "",
) : MLDatasource {

    private var imageDescriber: ImageDescriber? = null
    private var generativeModel: GenerativeModel? = null
    private var geminiCloudModel: GeminiCloudModel? = null

    override var isPromptApiAvailable: Boolean = false
        private set

    override var isCloudApiAvailable: Boolean = false
        private set

    override var currentCloudModel: GeminiModel = GeminiModel.DEFAULT
        private set

    private var totalBytesToDownload: Long = 0L
    private val _downloadProgress = MutableStateFlow(0f)
    override val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private suspend fun prepareGenerativeModel(): ModelStatus {
        val model = generativeModel ?: return ModelStatus.UNAVAILABLE

        return runCatching { model.checkStatus() }
            .map { status ->
                when (status) {
                    FeatureStatus.AVAILABLE -> ModelStatus.READY
                    FeatureStatus.DOWNLOADABLE -> downloadGenerativeModel(model)
                    else -> ModelStatus.UNAVAILABLE
                }
            }
            .getOrElse { e ->
                Log.e(TAG, "[GenerativeModel] prepareGenerativeModel error", e)
                ModelStatus.UNAVAILABLE
            }
    }

    private suspend fun downloadGenerativeModel(model: GenerativeModel): ModelStatus {
        var result = ModelStatus.UNAVAILABLE
        model.download()?.collect { downloadStatus ->
            when (downloadStatus) {
                DownloadStatus.DownloadCompleted -> result = ModelStatus.READY
                is DownloadStatus.DownloadFailed -> Log.e(TAG, "[GenerativeModel] download failed", downloadStatus.e)
                else -> Unit
            }
        }
        return result
    }

    private suspend fun prepareImageDescription(): ModelStatus {
        val client = imageDescriber ?: return ModelStatus.UNAVAILABLE

        return runCatching { client.checkFeatureStatus().await() }
            .map { featureStatus ->
                when (featureStatus) {
                    FeatureStatus.AVAILABLE -> ModelStatus.READY
                    FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> downloadImageDescription(client)
                    else -> ModelStatus.UNAVAILABLE
                }
            }
            .getOrElse { e ->
                Log.e(TAG, "prepareImageDescription error", e)
                ModelStatus.UNAVAILABLE
            }
    }

    private suspend fun downloadImageDescription(client: ImageDescriber): ModelStatus {
        _downloadProgress.value = 0f
        return suspendCancellableCoroutine { continuation ->
            client.downloadFeature(object : DownloadCallback {
                override fun onDownloadStarted(bytesToDownload: Long) {
                    totalBytesToDownload = bytesToDownload
                    _downloadProgress.value = 0.01f
                }

                override fun onDownloadFailed(e: GenAiException) {
                    Log.e(TAG, "ImageDescription download failed", e)
                    _downloadProgress.value = 0f
                    if (continuation.isActive) continuation.resume(ModelStatus.UNAVAILABLE)
                }

                override fun onDownloadProgress(totalBytesDownloaded: Long) {
                    if (totalBytesToDownload > 0) {
                        _downloadProgress.value = (totalBytesDownloaded.toFloat() / totalBytesToDownload).coerceIn(0.01f, 0.99f)
                    }
                }

                override fun onDownloadCompleted() {
                    _downloadProgress.value = 1f
                    if (continuation.isActive) continuation.resume(ModelStatus.READY)
                }
            })
        }
    }

    override fun initialize(): Deferred<ModelStatus> = externalScope.async {
        initializeCloudModel()
        initializeOnDeviceModels()

        val generativeModelStatus = prepareGenerativeModel()
        isPromptApiAvailable = generativeModelStatus == ModelStatus.READY

        val imageDescriptionStatus = prepareImageDescription()

        val anyReady = isCloudApiAvailable || isPromptApiAvailable || imageDescriptionStatus == ModelStatus.READY
        if (anyReady) ModelStatus.SUCCESS else ModelStatus.UNAVAILABLE
    }

    private fun initializeCloudModel() {
        if (geminiApiKey.isBlank()) return
        geminiCloudModel = createCloudModel(currentCloudModel)
        isCloudApiAvailable = true
    }

    private fun createCloudModel(model: GeminiModel): GeminiCloudModel =
        GeminiCloudModel(
            modelName = model.modelId,
            apiKey = geminiApiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 1024
            },
        )

    override fun changeCloudModel(model: GeminiModel) {
        if (model == currentCloudModel) return
        currentCloudModel = model
        if (geminiApiKey.isNotBlank()) {
            geminiCloudModel = createCloudModel(model)
            isCloudApiAvailable = true
        }
    }

    private fun initializeOnDeviceModels() {
        if (imageDescriber == null) {
            imageDescriber = ImageDescription.getClient(ImageDescriberOptions.builder(context).build())
        }
        if (generativeModel == null) {
            generativeModel = Generation.getClient()
        }
    }

    override fun initializeImageDescription(): Deferred<ModelStatus> = externalScope.async {
        if (imageDescriber == null) {
            imageDescriber = ImageDescription.getClient(ImageDescriberOptions.builder(context).build())
        }
        val result = prepareImageDescription()
        if (result == ModelStatus.READY) ModelStatus.SUCCESS else ModelStatus.UNAVAILABLE
    }

    override fun generateContent(prompt: String): Flow<String> = flow {
        generativeModel?.generateContent(prompt)?.let { response ->
            emit(response.candidates.first().text)
        }
    }

    override fun generateReview(image: PlatformImage, prompt: String): Flow<String> = flow {
        val bitmap = image.image as Bitmap

        generateWithCloudApi(bitmap, prompt)?.let { text ->
            emit(text)
            return@flow
        }

        generateWithOnDeviceApi(bitmap, prompt)?.let { text ->
            emit(text)
            return@flow
        }

        throw IllegalStateException("사용 가능한 모델이 없습니다")
    }

    private suspend fun generateWithCloudApi(bitmap: Bitmap, prompt: String): String? {
        val cloudModel = geminiCloudModel ?: return null
        if (!isCloudApiAvailable) return null

        return runCatching {
            val content = content {
                image(bitmap)
                text(prompt)
            }
            cloudModel.generateContent(content).text
        }
            .onFailure { Log.e(TAG, "[generate] Cloud API 실패, 폴백 시도", it) }
            .getOrNull()
    }

    private suspend fun generateWithOnDeviceApi(bitmap: Bitmap, prompt: String): String? {
        val onDeviceModel = generativeModel ?: return null
        if (!isPromptApiAvailable) return null

        val request = generateContentRequest(ImagePart(bitmap), TextPart(prompt)) {
            temperature = 0.7f
            maxOutputTokens = 1024
        }
        return onDeviceModel.generateContent(request).candidates.firstOrNull()?.text
    }

    override fun generateTextReview(prompt: String): Flow<String> = flow {
        generateTextWithCloudApi(prompt)?.let { text ->
            emit(text)
            return@flow
        }

        generateTextWithOnDeviceApi(prompt)?.let { text ->
            emit(text)
            return@flow
        }

        throw IllegalStateException("사용 가능한 모델이 없습니다")
    }

    private suspend fun generateTextWithCloudApi(prompt: String): String? {
        val cloudModel = geminiCloudModel ?: return null
        if (!isCloudApiAvailable) return null

        return runCatching { cloudModel.generateContent(prompt).text }
            .onFailure { Log.e(TAG, "[generateText] Cloud API 실패", it) }
            .getOrNull()
    }

    private suspend fun generateTextWithOnDeviceApi(prompt: String): String? {
        val onDeviceModel = generativeModel ?: return null
        if (!isPromptApiAvailable) return null

        val request = generateContentRequest(TextPart(prompt)) {
            temperature = 0.7f
            maxOutputTokens = 1024
        }
        return onDeviceModel.generateContent(request).candidates.firstOrNull()?.text
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> = callbackFlow {
        val bitmap = when (val raw = image.image) {
            is Bitmap -> raw
            is ByteArray -> BitmapFactory.decodeByteArray(raw, 0, raw.size)
            else -> throw IllegalArgumentException("Unsupported image type: ${raw::class}")
        }

        val request = ImageDescriptionRequest.builder(bitmap).build()

        imageDescriber?.runInference(request) { outputText ->
            trySend(outputText)
            channel.close()
        }?.await() ?: channel.close()

        awaitClose()
    }

    override fun close(): Deferred<Unit> = externalScope.async {
        imageDescriber?.close()
        generativeModel?.close()
        imageDescriber = null
        generativeModel = null
        geminiCloudModel = null
    }

    companion object {
        private const val TAG = "MLDatasourceImpl"
    }
}
