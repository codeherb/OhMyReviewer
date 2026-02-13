package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MLDatasourceImpl() : MLDatasource {

    override val downloadProgress: StateFlow<Float> = MutableStateFlow(0f)

    override fun initialize(): Deferred<Unit> {
        TODO("Not yet implemented")
    }

    override fun initializeImageDescription(): Deferred<Unit> {
        TODO("Not yet implemented")
    }

    override fun generateContent(prompt: String): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun close(): Deferred<Unit> {
        TODO("Not yet implemented")
    }

}
