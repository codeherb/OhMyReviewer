package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MLDatasourceImpl() : MLDatasource {

    private val _downloadProgress = MutableStateFlow(0f)
    override val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    override fun initializeImageDescription(): Deferred<ModelStatus> {
        return CompletableDeferred(ModelStatus.UNAVAILABLE)
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> {
        TODO("Not yet implemented")
    }
}
