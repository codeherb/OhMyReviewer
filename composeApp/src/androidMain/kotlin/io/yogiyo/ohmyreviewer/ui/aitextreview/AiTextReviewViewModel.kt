package io.yogiyo.ohmyreviewer.ui.aitextreview

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.viewModelScope
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateTextReviewUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.ParseReviewRequestUseCase
import io.yogiyo.ohmyreviewer.ui.base.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AiTextReviewViewModel(
    private val generateTextReviewUseCase: GenerateTextReviewUseCase,
    private val parseReviewRequestUseCase: ParseReviewRequestUseCase,
) : MviViewModel<AiTextReviewContract.State, AiTextReviewContract.Event, AiTextReviewContract.Effect>(
    initialState = AiTextReviewContract.State(),
) {

    val reviewTextFieldState = TextFieldState()

    override fun handleEvent(event: AiTextReviewContract.Event) {
        when (event) {
            is AiTextReviewContract.Event.OnMenuInputChanged -> onMenuInputChanged(event.input)
            is AiTextReviewContract.Event.OnModelSelected -> onModelSelected(event.model)
            is AiTextReviewContract.Event.OnGenerateClick -> generateReview()
        }
    }

    private fun onModelSelected(model: GeminiModel) {
        updateState { copy(selectedModel = model) }
    }

    private fun onMenuInputChanged(input: String) {
        updateState { copy(menuInput = input, parsedData = parseReviewRequestUseCase(input)) }
    }

    private fun generateReview() {
        val input = state.value.menuInput.trim()
        if (input.isBlank()) return
        val model = state.value.selectedModel

        viewModelScope.launch {
            generateTextReviewUseCase(model, input)
                .flowOn(Dispatchers.IO)
                .onStart { updateState { copy(isGenerating = true, generatedReview = "") } }
                .catch { e ->
                    updateState { copy(isGenerating = false) }
                    sendEffect(AiTextReviewContract.Effect.ShowError("AI 리뷰 생성 실패: ${e.message}"))
                }
                .collect { review ->
                    updateState { copy(generatedReview = review, isGenerating = false) }
                    reviewTextFieldState.edit { replace(0, length, review) }
                }
        }
    }
}
