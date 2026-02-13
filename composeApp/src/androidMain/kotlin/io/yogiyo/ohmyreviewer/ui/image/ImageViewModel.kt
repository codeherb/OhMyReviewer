package io.yogiyo.ohmyreviewer.ui.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import io.yogiyo.ohmyreviewer.ImageAnalyzer
import io.yogiyo.ohmyreviewer.ui.base.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.yogiyo.ohmyreviewer.util.decodeBitmapWithOrientation
import java.io.ByteArrayOutputStream

class ImageViewModel(
    private val appContext: Context,
    private val imageAnalyzer: ImageAnalyzer,
) : MviViewModel<ImageContract.State, ImageContract.Event, ImageContract.Effect>(
    initialState = ImageContract.State(),
) {

    override fun handleEvent(event: ImageContract.Event) {
        when (event) {
            is ImageContract.Event.OnImageSelected -> onImageSelected(event.uri)
            is ImageContract.Event.OnAnalyzeClick -> analyzeImage()
        }
    }

    private fun onImageSelected(uri: Uri?) {
        if (uri == null) return

        updateState {
            copy(
                isLoading = true,
                selectedImageUri = uri,
                analysisResult = emptyList(),
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    appContext.decodeBitmapWithOrientation(uri)
                }
                updateState { copy(isLoading = false, selectedBitmap = bitmap) }
            } catch (e: Exception) {
                updateState { copy(isLoading = false) }
                sendEffect(ImageContract.Effect.ShowError("이미지를 불러올 수 없습니다: ${e.message}"))
            }
        }
    }

    private fun analyzeImage() {
        val currentBitmap = state.value.selectedBitmap ?: return

        viewModelScope.launch {
            updateState { copy(isAnalyzing = true, errorMessage = null, analysisResult = emptyList()) }

            try {
                val result = withContext(Dispatchers.IO) {
                    val byteArray = bitmapToByteArray(currentBitmap)
                    imageAnalyzer.analyzeImage(
                        imageBytes = byteArray,
                        width = currentBitmap.width,
                        height = currentBitmap.height,
                    )
                }

                if (result.isEmpty()) {
                    updateState {
                        copy(
                            analysisResult = emptyList(),
                            isAnalyzing = false,
                            errorMessage = "이미지에서 라벨을 감지하지 못했습니다. 다른 이미지를 시도해보세요.",
                        )
                    }
                } else {
                    updateState {
                        copy(
                            analysisResult = result,
                            isAnalyzing = false,
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "분석 실패: ${e.message}"
                Log.e(TAG, "analyzeImage error", e)

                updateState {
                    copy(
                        errorMessage = errorMsg,
                        isAnalyzing = false,
                    )
                }
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    companion object {
        private const val TAG = "ImageViewModel"
    }
}
