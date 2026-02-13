package io.yogiyo.ohmyreviewer

import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android ML Kit 기반 이미지 분석기 구현
 */
actual class ImageAnalyzer {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.4f)
            .build()
    )

    /**
     * ML Kit을 사용하여 이미지 분석
     */
    actual suspend fun analyzeImage(
        imageBytes: ByteArray,
        width: Int,
        height: Int
    ): List<ImageLabel> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "analyzeImage 시작 - imageBytes size: ${imageBytes.size}, width: $width, height: $height")

        // JPEG 바이트 배열을 Bitmap으로 디코딩
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        if (bitmap == null) {
            Log.e(TAG, "Bitmap 디코딩 실패")
            continuation.resumeWithException(
                IllegalArgumentException("이미지 바이트 배열을 Bitmap으로 디코딩할 수 없습니다.")
            )
            return@suspendCancellableCoroutine
        }

        Log.d(TAG, "Bitmap 디코딩 성공 - ${bitmap.width}x${bitmap.height}")

        // Bitmap에서 InputImage 생성
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                Log.d(TAG, "ML Kit 분석 성공 - 라벨 ${labels.size}개 감지")
                labels.forEach { label ->
                    Log.d(TAG, "  라벨: ${label.text}, 신뢰도: ${label.confidence}, index: ${label.index}")
                }
                val result = labels.map { label ->
                    ImageLabel(
                        text = label.text,
                        confidence = label.confidence
                    )
                }
                continuation.resume(result)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "ML Kit 분석 실패", exception)
                continuation.resumeWithException(exception)
            }

        continuation.invokeOnCancellation {
            Log.d(TAG, "분석 취소됨")
        }
    }

    companion object {
        private const val TAG = "ImageAnalyzer"
    }
}
