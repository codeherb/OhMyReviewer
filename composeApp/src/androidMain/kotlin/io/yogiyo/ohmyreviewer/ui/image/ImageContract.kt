package io.yogiyo.ohmyreviewer.ui.image

import android.graphics.Bitmap
import android.net.Uri
import io.yogiyo.ohmyreviewer.ImageMeta
import io.yogiyo.ohmyreviewer.ui.base.UiEffect
import io.yogiyo.ohmyreviewer.ui.base.UiEvent
import io.yogiyo.ohmyreviewer.ui.base.UiState

object ImageContract {

    data class State(
        val isLoading: Boolean = false,
        val selectedImageUri: Uri? = null,
        val selectedBitmap: Bitmap? = null,
        val isAnalyzing: Boolean = false,
        val isDescribing: Boolean = false,
        val analysisResult: ImageMeta = ImageMeta.None,
        val errorMessage: String? = null,
    ) : UiState {
        val hasSelectedImage: Boolean get() = selectedBitmap != null
        val hasAnalysisResult: Boolean get() = analysisResult != ImageMeta.None
        val shouldShowAnalysisGuide: Boolean
            get() = hasSelectedImage && !isAnalyzing && !isDescribing && errorMessage == null && !hasAnalysisResult
    }

    sealed interface Event : UiEvent {
        data class OnImageSelected(val uri: Uri?) : Event
        data object OnAnalyzeClick : Event
        data object OnDescriptionClick : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}
