package io.yogiyo.ohmyreviewer.ui.review

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.viewModelScope
import io.yogiyo.ohmyreviewer.ui.base.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewViewModel(
    private val appContext: Context,
) : MviViewModel<ReviewContract.State, ReviewContract.Event, ReviewContract.Effect>(
    initialState = ReviewContract.State(),
) {

    val reviewTextFieldState = TextFieldState()

    override fun handleEvent(event: ReviewContract.Event) {
        when (event) {
            is ReviewContract.Event.OnImageSelected -> onImageSelected(event.uri)
        }
    }

    private fun onImageSelected(uri: Uri?) {
        if (uri == null) return

        updateState { copy(isLoading = true, selectedImageUri = uri) }

        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    appContext.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                updateState { copy(isLoading = false, selectedBitmap = bitmap) }
            } catch (e: Exception) {
                updateState { copy(isLoading = false) }
                sendEffect(ReviewContract.Effect.ShowError("이미지를 불러올 수 없습니다: ${e.message}"))
            }
        }
    }
}
