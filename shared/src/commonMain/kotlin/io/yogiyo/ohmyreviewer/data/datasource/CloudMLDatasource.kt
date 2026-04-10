package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage

interface CloudMLDatasource {

    suspend fun generateImageDescription(model: GeminiModel, image: PlatformImage): String

    suspend fun generateTextReview(model: GeminiModel, prompt: String): String

    suspend fun generateImageReview(model: GeminiModel, image: PlatformImage, prompt: String): String
}
