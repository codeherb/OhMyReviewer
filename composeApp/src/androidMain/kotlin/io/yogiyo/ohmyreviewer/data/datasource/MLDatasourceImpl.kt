package io.yogiyo.ohmyreviewer.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.imagedescription.ImageDescriber
import com.google.mlkit.genai.imagedescription.ImageDescriberOptions
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriptionRequest
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MLDatasourceImpl(
    private val context: Context,
    private val externalScope: CoroutineScope,
) : MLDatasource {

    private var imageDescriber: ImageDescriber? = null
    private var totalBytesToDownload: Long = 0L
    private val _downloadProgress = MutableStateFlow(0f)
    override val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private fun ensureImageDescriber(): ImageDescriber {
        return imageDescriber ?: ImageDescription.getClient(
            ImageDescriberOptions.builder(context).build()
        ).also { imageDescriber = it }
    }

    override fun initializeImageDescription(): Deferred<ModelStatus> = externalScope.async {
        val client = ensureImageDescriber()

        val featureStatus = runCatching {
            suspendCancellableCoroutine<Int> { cont ->
                val future = client.checkFeatureStatus()
                future.addListener({ cont.resume(future.get()) }, { it.run() })
            }
        }.getOrElse {
            return@async ModelStatus.UNAVAILABLE
        }

        when (featureStatus) {
            FeatureStatus.AVAILABLE -> ModelStatus.SUCCESS
            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> downloadImageDescription(client)
            else -> ModelStatus.UNAVAILABLE
        }
    }

    private suspend fun downloadImageDescription(client: ImageDescriber): ModelStatus {
        _downloadProgress.value = 0.01f
        return suspendCancellableCoroutine { continuation ->
            client.downloadFeature(object : DownloadCallback {
                override fun onDownloadStarted(bytesToDownload: Long) {
                    totalBytesToDownload = bytesToDownload
                }

                override fun onDownloadProgress(totalBytesDownloaded: Long) {
                    if (totalBytesToDownload > 0) {
                        _downloadProgress.value =
                            (totalBytesDownloaded.toFloat() / totalBytesToDownload).coerceIn(0.01f, 0.99f)
                    }
                }

                override fun onDownloadCompleted() {
                    _downloadProgress.value = 1f
                    if (continuation.isActive) continuation.resume(ModelStatus.SUCCESS)
                }

                override fun onDownloadFailed(e: GenAiException) {
                    _downloadProgress.value = 0f
                    if (continuation.isActive) continuation.resume(ModelStatus.UNAVAILABLE)
                }
            })
        }
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> = callbackFlow {
        val bitmap = when (val raw = image.image) {
            is Bitmap -> raw
            is ByteArray -> BitmapFactory.decodeByteArray(raw, 0, raw.size)
            else -> throw IllegalArgumentException("Unsupported image type: ${raw::class}")
        }

        val client = ensureImageDescriber()
        val request = ImageDescriptionRequest.builder(bitmap).build()

        client.runInference(request) { outputText ->
            trySend(outputText)
            channel.close()
        }

        awaitClose()
    }
}
