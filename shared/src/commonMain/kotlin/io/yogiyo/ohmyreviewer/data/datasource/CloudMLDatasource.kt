package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow

interface CloudMLDatasource {

    fun generateImageDescription(model: GeminiModel, image: PlatformImage): Flow<String>

    fun generateTextReview(model: GeminiModel, prompt: String): Flow<String>

    fun generateImageReview(model: GeminiModel, image: PlatformImage, prompt: String): Flow<String>
}
