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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.guava.await

class MLDatasourceImpl(
    private val context: Context,
    private val externalScope: CoroutineScope,
): MLDatasource {

    private var imageDescriber: ImageDescriber? = null

    private var generativeModel: GenerativeModel? = null

    private fun prepareGenerativeModel(): Flow<ModelStatus> = flow {
        val status = generativeModel?.checkStatus() ?: return@flow

        when (status) {
            FeatureStatus.UNAVAILABLE,
            FeatureStatus.DOWNLOADING ->
                emit(ModelStatus.UNAVAILABLE)
            FeatureStatus.AVAILABLE ->
                emit(ModelStatus.READY)
            FeatureStatus.DOWNLOADABLE -> {
                generativeModel?.download()?.collect { status ->
                    when (status) {
                        is DownloadStatus.DownloadStarted ->
                            Log.d(TAG, "starting download for Gemini Nano")

                        is DownloadStatus.DownloadProgress ->
                            Log.d(TAG, "Nano ${status.totalBytesDownloaded} bytes downloaded")

                        DownloadStatus.DownloadCompleted -> {
                            emit(ModelStatus.READY)
                        }
                        is DownloadStatus.DownloadFailed -> {
                            emit(ModelStatus.UNAVAILABLE)
                        }
                    }
                } ?: run {
                    emit(ModelStatus.UNAVAILABLE)
                    close()
                }
            }
        }
    }

    private fun prepareImageDescription(): Flow<ModelStatus> = callbackFlow {
        val featureStatus = imageDescriber?.checkFeatureStatus()?.await()

        when(featureStatus) {
            FeatureStatus.UNAVAILABLE, FeatureStatus.DOWNLOADING -> {
                trySend(ModelStatus.UNAVAILABLE)
            }
            FeatureStatus.AVAILABLE -> {
                trySend(ModelStatus.READY)
            }
            FeatureStatus.DOWNLOADABLE -> {
                imageDescriber?.downloadFeature(object : DownloadCallback {
                    override fun onDownloadStarted(bytesToDownload: Long) { }

                    override fun onDownloadFailed(e: GenAiException) {
                        trySend(ModelStatus.UNAVAILABLE)
                    }

                    override fun onDownloadProgress(totalBytesDownloaded: Long) { }

                    override fun onDownloadCompleted() {
                        trySend(ModelStatus.READY)
                    }
                })?.await()
                    ?: run {
                        trySend(ModelStatus.UNAVAILABLE)
                        close()
                    }
            }
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

            val result = prepareGenerativeModel().combine(prepareImageDescription()) { s1, s2 ->
                s1 == ModelStatus.READY && s2 == ModelStatus.READY
            }.last()
            return@async if (result) ModelStatus.SUCCESS else ModelStatus.UNAVAILABLE
        }
    }

    override fun generateContent(prompt: String): Flow<String> {
        return flow {
            generativeModel?.generateContent(prompt)?.let { response ->
                emit(response.candidates.first().text)
            } ?: run {
                close()
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
            }?.await() ?: run {
                close()
            }
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