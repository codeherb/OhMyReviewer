package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MLDatasourceImpl() : MLDatasource {

    override val downloadProgress: StateFlow<Float> = MutableStateFlow(0f)

    override var isPromptApiAvailable: Boolean = false
        private set

    override var isCloudApiAvailable: Boolean = false
        private set

    override fun initialize(): Deferred<ModelStatus> {
        TODO("Not yet implemented")
    }

    override fun initializeImageDescription(): Deferred<ModelStatus> {
        TODO("Not yet implemented")
    }

    override fun generateContent(prompt: String): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun generateReview(image: PlatformImage, prompt: String): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun generateTextReview(prompt: String): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun close(): Deferred<Unit> {
        TODO("Not yet implemented")
    }

}
