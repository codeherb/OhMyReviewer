package io.yogiyo.ohmyreviewer.ui.aireview

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.viewModelScope
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateTextReviewUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.InitializeModelUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.ParseReviewRequestUseCase
import io.yogiyo.ohmyreviewer.ui.base.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AiReviewViewModel(
    private val repository: MLRepository,
    private val initializeModelUseCase: InitializeModelUseCase,
    private val generateTextReviewUseCase: GenerateTextReviewUseCase,
    private val parseReviewRequestUseCase: ParseReviewRequestUseCase,
) : MviViewModel<AiReviewContract.State, AiReviewContract.Event, AiReviewContract.Effect>(
    initialState = AiReviewContract.State(),
) {

    val reviewTextFieldState = TextFieldState()

    init {
        initializeModel()
        collectDownloadProgress()
    }

    override fun handleEvent(event: AiReviewContract.Event) {
        when (event) {
            is AiReviewContract.Event.OnMenuInputChanged -> onMenuInputChanged(event.input)
            is AiReviewContract.Event.OnGenerateClick -> generateReview()
        }
    }

    private fun onMenuInputChanged(input: String) {
        updateState { copy(menuInput = input, parsedData = parseReviewRequestUseCase(input)) }
    }

    private fun initializeModel() {
        viewModelScope.launch {
            updateState { copy(isInitializingModel = true) }
            initializeModelUseCase.initializeAll()
                .onSuccess { status ->
                    updateState {
                        copy(
                            modelStatus = status,
                            isInitializingModel = false,
                            isCloudMode = repository.isCloudApiAvailable,
                        )
                    }
                }
                .onFailure {
                    updateState { copy(modelStatus = ModelStatus.UNAVAILABLE, isInitializingModel = false) }
                }
        }
    }

    private fun collectDownloadProgress() {
        viewModelScope.launch {
            repository.downloadProgress.collect { progress ->
                updateState { copy(downloadProgress = progress) }
            }
        }
    }

    private fun generateReview() {
        val input = state.value.menuInput.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            generateTextReviewUseCase(input)
                .flowOn(Dispatchers.IO)
                .onStart { updateState { copy(isGenerating = true, generatedReview = "") } }
                .catch { e ->
                    updateState { copy(isGenerating = false) }
                    sendEffect(AiReviewContract.Effect.ShowError("AI 리뷰 생성 실패: ${e.message}"))
                }
                .collect { review ->
                    updateState { copy(generatedReview = review, isGenerating = false) }
                    reviewTextFieldState.edit { replace(0, length, review) }
                }
        }
    }
}
