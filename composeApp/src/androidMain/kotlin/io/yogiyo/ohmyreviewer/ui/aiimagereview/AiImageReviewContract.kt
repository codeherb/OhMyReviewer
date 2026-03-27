package io.yogiyo.ohmyreviewer.ui.aiimagereview

import android.graphics.Bitmap
import android.net.Uri
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object AiImageReviewContract {

    data class State(
        val selectedImage: Bitmap? = null,
        val isGenerating: Boolean = false,
        val generatedReview: String = "",
        val selectedModel: GeminiModel = GeminiModel.DEFAULT,
    ) : UiState {
        val hasImage: Boolean get() = selectedImage != null
        val hasGeneratedReview: Boolean get() = generatedReview.isNotEmpty()
        val canGenerate: Boolean get() = hasImage && !isGenerating
    }

    sealed interface Event : UiEvent {
        data class OnImageSelected(val uri: Uri) : Event
        data object OnGenerateClick : Event
        data class OnModelSelected(val model: GeminiModel) : Event
        data object OnClearImage : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}
