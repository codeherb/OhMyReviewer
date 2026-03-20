package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MLDatasource {

    val downloadProgress: StateFlow<Float>

    val isPromptApiAvailable: Boolean

    val isCloudApiAvailable: Boolean

    fun initialize(): Deferred<ModelStatus>

    fun initializeImageDescription(): Deferred<ModelStatus>

    fun generateContent(prompt: String): Flow<String>

    fun generateImageDescription(image: PlatformImage): Flow<String>

    fun generateReview(image: PlatformImage, prompt: String): Flow<String>

    fun generateTextReview(prompt: String): Flow<String>

    val currentCloudModel: GeminiModel

    fun changeCloudModel(model: GeminiModel)

    fun close(): Deferred<Unit>

}
