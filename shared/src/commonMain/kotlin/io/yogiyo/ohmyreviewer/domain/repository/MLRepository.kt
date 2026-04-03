package io.yogiyo.ohmyreviewer.domain.repository

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow

interface MLRepository {

    fun generateCloudImageDescription(model: GeminiModel, image: PlatformImage): Flow<String>

    fun generateImageDescription(image: PlatformImage): Flow<String>

    fun generateReview(model: GeminiModel, image: PlatformImage, prompt: String): Flow<String>

    fun generateTextReview(model: GeminiModel, prompt: String): Flow<String>
}
