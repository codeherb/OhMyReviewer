package io.yogiyo.ohmyreviewer.ui.aitextreview

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
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
        val selectedModel: GeminiModel = GeminiModel.DEFAULT,
    ) : UiState {
        val hasInput: Boolean get() = menuInput.isNotBlank()
        val isJsonMode: Boolean get() = parsedData != null
        val hasGeneratedReview: Boolean get() = generatedReview.isNotEmpty()
        val canGenerate: Boolean get() = hasInput && !isGenerating
    }

    sealed interface Event : UiEvent {
        data class OnMenuInputChanged(val input: String) : Event
        data class OnModelSelected(val model: GeminiModel) : Event
        data object OnGenerateClick : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}