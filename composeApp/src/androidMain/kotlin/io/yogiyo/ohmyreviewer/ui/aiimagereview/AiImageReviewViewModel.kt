package io.yogiyo.ohmyreviewer.ui.aiimagereview

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import io.yogiyo.ohmyreviewer.data.model.AndroidPlatformImage
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateImageReviewUseCase
import io.yogiyo.ohmyreviewer.ui.base.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiImageReviewViewModel(
    private val contentResolver: ContentResolver,
    private val generateImageReviewUseCase: GenerateImageReviewUseCase,
) : MviViewModel<AiImageReviewContract.State, AiImageReviewContract.Event, AiImageReviewContract.Effect>(
    initialState = AiImageReviewContract.State(),
) {

    override fun handleEvent(event: AiImageReviewContract.Event) {
        when (event) {
            is AiImageReviewContract.Event.OnImageSelected -> onImageSelected(event.uri)
            is AiImageReviewContract.Event.OnGenerateClick -> generateReview()
            is AiImageReviewContract.Event.OnModelSelected -> onModelSelected(event.model)
            is AiImageReviewContract.Event.OnClearImage -> updateState { copy(selectedImage = null, generatedReview = "") }
        }
    }

    private fun onImageSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = decodeBitmap(uri) ?: run {
                sendEffect(AiImageReviewContract.Effect.ShowError("이미지를 불러올 수 없습니다"))
                return@launch
            }
            updateState { copy(selectedImage = bitmap, generatedReview = "") }
        }
    }

    private fun decodeBitmap(uri: Uri): Bitmap? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }.getOrNull()

    private fun onModelSelected(model: GeminiModel) {
        updateState { copy(selectedModel = model) }
    }

    private fun generateReview() {
        val bitmap = state.value.selectedImage ?: return
        val model = state.value.selectedModel

        viewModelScope.launch {
            updateState { copy(isGenerating = true, generatedReview = "") }
            runCatching { withContext(Dispatchers.IO) { generateImageReviewUseCase(model, AndroidPlatformImage.create(bitmap)) } }
                .onSuccess { review ->
                    updateState { copy(generatedReview = review, isGenerating = false) }
                }
                .onFailure { e ->
                    updateState { copy(isGenerating = false) }
                    sendEffect(AiImageReviewContract.Effect.ShowError("AI 이미지 리뷰 생성 실패: ${e.message}"))
                }
        }
    }
}
