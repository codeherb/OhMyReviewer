package io.yogiyo.ohmyreviewer.ui.image

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.viewModelScope
import io.yogiyo.ohmyreviewer.ui.base.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageViewModel(
    private val appContext: Context,
) : MviViewModel<ImageContract.State, ImageContract.Event, ImageContract.Effect>(
    initialState = ImageContract.State(),
) {

    override fun handleEvent(event: ImageContract.Event) {
        when (event) {
            is ImageContract.Event.OnImageSelected -> onImageSelected(event.uri)
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
                sendEffect(ImageContract.Effect.ShowError("이미지를 불러올 수 없습니다: ${e.message}"))
            }
        }
    }
}
