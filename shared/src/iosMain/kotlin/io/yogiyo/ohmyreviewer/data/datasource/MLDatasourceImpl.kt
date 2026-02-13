package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

class MLDatasourceImpl() : MLDatasource {

    override fun initialize(): Deferred<ModelStatus> {
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
