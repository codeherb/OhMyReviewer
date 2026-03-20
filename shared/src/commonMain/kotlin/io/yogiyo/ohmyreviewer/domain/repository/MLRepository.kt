package io.yogiyo.ohmyreviewer.domain.repository

import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MLRepository {

    val downloadProgress: StateFlow<Float>

    val isCloudApiAvailable: Boolean

    val isPromptApiAvailable: Boolean

    suspend fun initialize(): ModelStatus

    suspend fun initializeImageDescription(): ModelStatus

    fun generateContent(prompt: String): Flow<String>

    fun generateImageDescription(image: PlatformImage): Flow<String>

    fun generateCloudImageDescription(image: PlatformImage): Flow<String>

    fun generateReview(image: PlatformImage, prompt: String): Flow<String>

    fun generateTextReview(prompt: String): Flow<String>

    suspend fun close()
}
