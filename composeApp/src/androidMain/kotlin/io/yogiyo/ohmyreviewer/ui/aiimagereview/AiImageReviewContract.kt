package io.yogiyo.ohmyreviewer.ui.aiimagereview

import android.graphics.Bitmap
import android.net.Uri
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object AiImageReviewContract {

    data class State(
        val selectedImage: Bitmap? = null,
        val isGenerating: Boolean = false,
        val generatedReview: String = "",
        val modelStatus: ModelStatus = ModelStatus.UNAVAILABLE,
        val isInitializingModel: Boolean = false,
        val downloadProgress: Float = 0f,
        val isCloudMode: Boolean = false,
        val selectedModel: GeminiModel = GeminiModel.DEFAULT,
    ) : UiState {
        val hasImage: Boolean get() = selectedImage != null
        val hasGeneratedReview: Boolean get() = generatedReview.isNotEmpty()
        val isModelReady: Boolean get() = modelStatus == ModelStatus.SUCCESS
        val isDownloading: Boolean get() = downloadProgress in 0.01f..0.99f
        val canGenerate: Boolean get() = hasImage && isModelReady && !isGenerating
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
