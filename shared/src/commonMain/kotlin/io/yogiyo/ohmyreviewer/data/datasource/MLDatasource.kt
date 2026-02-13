package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MLDatasource {

    val downloadProgress: StateFlow<Float>

    fun initialize(): Deferred<ModelStatus>

    fun initializeImageDescription(): Deferred<ModelStatus>

    fun generateContent(prompt: String): Flow<String>

    fun generateImageDescription(image: PlatformImage): Flow<String>

    fun close(): Deferred<Unit>

}
