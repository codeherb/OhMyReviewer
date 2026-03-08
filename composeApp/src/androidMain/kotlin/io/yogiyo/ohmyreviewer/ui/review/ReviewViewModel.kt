package io.yogiyo.ohmyreviewer.ui.review

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.viewModelScope
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.model.AndroidPlatformImage
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.ui.base.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.yogiyo.ohmyreviewer.util.decodeBitmapWithOrientation

class ReviewViewModel(
    private val appContext: Context,
    private val mlDatasource: MLDatasource,
) : MviViewModel<ReviewContract.State, ReviewContract.Event, ReviewContract.Effect>(
    initialState = ReviewContract.State(),
) {

    val reviewTextFieldState = TextFieldState()

    init {
        initializeModel()
        collectDownloadProgress()
    }

    override fun handleEvent(event: ReviewContract.Event) {
        when (event) {
            is ReviewContract.Event.OnImageSelected -> onImageSelected(event.uri)
            is ReviewContract.Event.OnDescribeClick -> describeImage()
        }
    }

    private fun initializeModel() {
        viewModelScope.launch {
            updateState { copy(isInitializingModel = true) }
            try {
                val status = mlDatasource.initializeImageDescription().await()
                updateState { copy(modelStatus = status, isInitializingModel = false) }
            } catch (e: Exception) {
                Log.e(TAG, "이미지 설명 모델 초기화 실패", e)
                updateState { copy(modelStatus = ModelStatus.UNAVAILABLE, isInitializingModel = false) }
            }
        }
    }

    private fun collectDownloadProgress() {
        viewModelScope.launch {
            mlDatasource.downloadProgress.collect { progress ->
                updateState { copy(downloadProgress = progress) }
            }
        }
    }

    private fun onImageSelected(uri: Uri?) {
        if (uri == null) return

        updateState {
            copy(
                isLoading = true,
                selectedImageUri = uri,
                description = "",
            )
        }

        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    appContext.decodeBitmapWithOrientation(uri)
                }
                updateState { copy(isLoading = false, selectedBitmap = bitmap) }
            } catch (e: Exception) {
                updateState { copy(isLoading = false) }
                sendEffect(ReviewContract.Effect.ShowError("이미지를 불러올 수 없습니다: ${e.message}"))
            }
        }
    }

    private fun describeImage() {
        val currentBitmap = state.value.selectedBitmap ?: return

        viewModelScope.launch {
            updateState { copy(isDescribing = true, description = "") }

            try {
                // ML Kit runInference가 비트맵을 recycle할 수 있으므로 복사본을 전달
                val platformImage = AndroidPlatformImage.create(currentBitmap)

                mlDatasource.generateImageDescription(platformImage)
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        Log.e(TAG, "이미지 설명 생성 실패", e)
                        updateState { copy(isDescribing = false) }
                        sendEffect(ReviewContract.Effect.ShowError("이미지 설명 생성 실패: ${e.message}"))
                    }
                    .collect { description ->
                        updateState { copy(description = description, isDescribing = false) }
                        reviewTextFieldState.edit {
                            replace(0, length, description)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "이미지 설명 생성 실패", e)
                updateState { copy(isDescribing = false) }
                sendEffect(ReviewContract.Effect.ShowError("이미지 설명 생성 실패: ${e.message}"))
            }
        }
    }

    companion object {
        private const val TAG = "ReviewViewModel"
    }
}
