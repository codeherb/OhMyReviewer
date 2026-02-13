package io.yogiyo.ohmyreviewer.ui.review

import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object ReviewContract {

    data class State(
        val isLoading: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent

    sealed interface Effect : UiEffect
}
