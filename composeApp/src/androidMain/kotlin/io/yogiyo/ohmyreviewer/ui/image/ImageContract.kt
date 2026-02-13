package io.yogiyo.ohmyreviewer.ui.image

import android.graphics.Bitmap
import android.net.Uri
import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object ImageContract {

    data class State(
        val isLoading: Boolean = false,
        val selectedImageUri: Uri? = null,
        val selectedBitmap: Bitmap? = null,
    ) : UiState {
        val hasSelectedImage: Boolean get() = selectedBitmap != null
    }

    sealed interface Event : UiEvent {
        data class OnImageSelected(val uri: Uri?) : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}
