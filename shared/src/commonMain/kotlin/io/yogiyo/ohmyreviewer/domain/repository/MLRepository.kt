package io.yogiyo.ohmyreviewer.domain.repository

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow

interface MLRepository {

    suspend fun generateCloudImageDescription(model: GeminiModel, image: PlatformImage): String

    fun generateImageDescription(image: PlatformImage): Flow<String>

    suspend fun generateReview(model: GeminiModel, image: PlatformImage, prompt: String): String

    suspend fun generateTextReview(model: GeminiModel, prompt: String): String
}
