package io.yogiyo.ohmyreviewer.ui.review

import android.graphics.Bitmap
import android.net.Uri
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object ReviewContract {

    data class State(
        val isLoading: Boolean = false,
        val selectedImageUri: Uri? = null,
        val selectedBitmap: Bitmap? = null,
        val isDescribing: Boolean = false,
        val description: String = "",
        val modelStatus: ModelStatus = ModelStatus.UNAVAILABLE,
        val isInitializingModel: Boolean = false,
        val downloadProgress: Float = 0f,
    ) : UiState {
        val hasSelectedImage: Boolean get() = selectedBitmap != null
        val hasDescription: Boolean get() = description.isNotEmpty()
        val isModelReady: Boolean get() = modelStatus == ModelStatus.SUCCESS
        val isDownloading: Boolean get() = downloadProgress in 0.01f..0.99f
        val shouldShowDescriptionGuide: Boolean
            get() = hasSelectedImage && !isDescribing && !hasDescription
    }

    sealed interface Event : UiEvent {
        data class OnImageSelected(val uri: Uri?) : Event
        data object OnDescribeClick : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}
