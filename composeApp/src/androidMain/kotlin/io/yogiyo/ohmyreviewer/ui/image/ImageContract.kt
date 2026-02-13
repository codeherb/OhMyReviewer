package io.yogiyo.ohmyreviewer.ui.image

import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object ImageContract {

    data class State(
        val isLoading: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent

    sealed interface Effect : UiEffect
}
