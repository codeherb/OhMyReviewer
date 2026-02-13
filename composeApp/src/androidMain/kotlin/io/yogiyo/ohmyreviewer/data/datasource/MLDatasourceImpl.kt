package io.yogiyo.ohmyreviewer.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.imagedescription.ImageDescriber
import com.google.mlkit.genai.imagedescription.ImageDescriberOptions
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriptionRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
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
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine

class MLDatasourceImpl(
    private val context: Context,
    private val externalScope: CoroutineScope,
): MLDatasource {

    private var imageDescriber: ImageDescriber? = null

    private var generativeModel: GenerativeModel? = null

    private var totalBytesToDownload: Long = 0L
    private val _downloadProgress = MutableStateFlow(0f)
    override val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private fun prepareGenerativeModel(): Flow<ModelStatus> = flow {
        val status = generativeModel?.checkStatus() ?: return@flow

        when (status) {
            FeatureStatus.UNAVAILABLE,
            FeatureStatus.DOWNLOADING ->
                emit(ModelStatus.UNAVAILABLE)
            FeatureStatus.AVAILABLE ->
                emit(ModelStatus.READY)
            FeatureStatus.DOWNLOADABLE -> {
                generativeModel?.download()?.collect { downloadStatus ->
                    when (downloadStatus) {
                        is DownloadStatus.DownloadStarted ->
                            Log.d(TAG, "starting download for Gemini Nano")

                        is DownloadStatus.DownloadProgress ->
                            Log.d(TAG, "Nano ${downloadStatus.totalBytesDownloaded} bytes downloaded")

                        DownloadStatus.DownloadCompleted -> emit(ModelStatus.READY)
                        is DownloadStatus.DownloadFailed -> emit(ModelStatus.UNAVAILABLE)
                    }
                } ?: emit(ModelStatus.UNAVAILABLE)
            }
        }
    }

    private suspend fun prepareImageDescription(): ModelStatus {
        val client = imageDescriber ?: return ModelStatus.UNAVAILABLE

        return try {
            val featureStatus = client.checkFeatureStatus().await()
            Log.d(TAG, "ImageDescription featureStatus: $featureStatus")

            when (featureStatus) {
                FeatureStatus.UNAVAILABLE -> ModelStatus.UNAVAILABLE
                FeatureStatus.AVAILABLE -> ModelStatus.READY
                FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                    Log.d(TAG, "ImageDescription waiting for download (status: $featureStatus)")
                    _downloadProgress.value = 0f
                    suspendCancellableCoroutine { continuation ->
                        client.downloadFeature(object : DownloadCallback {
                            override fun onDownloadStarted(bytesToDownload: Long) {
                                Log.d(TAG, "ImageDescription download started: $bytesToDownload bytes")
                                totalBytesToDownload = bytesToDownload
                                _downloadProgress.value = 0.01f
                            }

                            override fun onDownloadFailed(e: GenAiException) {
                                Log.e(TAG, "ImageDescription download failed", e)
                                _downloadProgress.value = 0f
                                if (continuation.isActive) {
                                    continuation.resume(ModelStatus.UNAVAILABLE)
                                }
                            }

                            override fun onDownloadProgress(totalBytesDownloaded: Long) {
                                Log.d(TAG, "ImageDescription download progress: $totalBytesDownloaded bytes")
                                if (totalBytesToDownload > 0) {
                                    _downloadProgress.value = (totalBytesDownloaded.toFloat() / totalBytesToDownload).coerceIn(0.01f, 0.99f)
                                }
                            }

                            override fun onDownloadCompleted() {
                                Log.d(TAG, "ImageDescription download completed")
                                _downloadProgress.value = 1f
                                if (continuation.isActive) {
                                    continuation.resume(ModelStatus.READY)
                                }
                            }
                        })
                    }
                }
                else -> ModelStatus.UNAVAILABLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "prepareImageDescription error", e)
            ModelStatus.UNAVAILABLE
        }
    }

    override fun initialize(): Deferred<ModelStatus> {
        return externalScope.async {
            if (imageDescriber == null) {
                val options = ImageDescriberOptions.builder(context).build()
                imageDescriber = ImageDescription.getClient(options)
            }
            if (generativeModel == null) {
                generativeModel = Generation.getClient()
            }

            val generativeModelStatus = prepareGenerativeModel().last()
            val imageDescriptionStatus = prepareImageDescription()
            val result = generativeModelStatus == ModelStatus.READY && imageDescriptionStatus == ModelStatus.READY
            return@async if (result) ModelStatus.SUCCESS else ModelStatus.UNAVAILABLE
        }
    }

    override fun initializeImageDescription(): Deferred<ModelStatus> {
        return externalScope.async {
            if (imageDescriber == null) {
                val options = ImageDescriberOptions.builder(context).build()
                imageDescriber = ImageDescription.getClient(options)
            }

            val result = prepareImageDescription()
            Log.d(TAG, "initializeImageDescription result: $result")
            return@async if (result == ModelStatus.READY) ModelStatus.SUCCESS else ModelStatus.UNAVAILABLE
        }
    }

    override fun generateContent(prompt: String): Flow<String> {
        return flow {
            generativeModel?.generateContent(prompt)?.let { response ->
                emit(response.candidates.first().text)
            }
        }
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> {
        return callbackFlow {
            val imageDescriptionRequest = ImageDescriptionRequest
                .builder(image.image as Bitmap)
                .build()

            imageDescriber?.runInference(imageDescriptionRequest) { outputText ->
                trySend(outputText)
                channel.close()
            }?.await() ?: run {
                channel.close()
            }

            awaitClose()
        }
    }

    override fun close(): Deferred<Unit> {
        return externalScope.async {
            imageDescriber?.close()
            generativeModel?.close()
            imageDescriber = null
            generativeModel = null
        }
    }

    companion object {
        const val TAG = "MLDatasourceImpl"
    }
}
