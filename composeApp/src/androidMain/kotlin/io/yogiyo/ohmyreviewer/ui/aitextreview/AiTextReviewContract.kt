package io.yogiyo.ohmyreviewer.ui.aitextreview

import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.ReviewRequestData
import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object AiTextReviewContract {

    data class State(
        val menuInput: String = "",
        val parsedData: ReviewRequestData? = null,
        val isGenerating: Boolean = false,
        val generatedReview: String = "",
        val modelStatus: ModelStatus = ModelStatus.UNAVAILABLE,
        val isInitializingModel: Boolean = false,
        val downloadProgress: Float = 0f,
        val isCloudMode: Boolean = false,
    ) : UiState {
        val hasInput: Boolean get() = menuInput.isNotBlank()
        val isJsonMode: Boolean get() = parsedData != null
        val hasGeneratedReview: Boolean get() = generatedReview.isNotEmpty()
        val isModelReady: Boolean get() = modelStatus == ModelStatus.SUCCESS
        val isDownloading: Boolean get() = downloadProgress in 0.01f..0.99f
        val canGenerate: Boolean get() = hasInput && isModelReady && !isGenerating
    }

    sealed interface Event : UiEvent {
        data class OnMenuInputChanged(val input: String) : Event
        data object OnGenerateClick : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}