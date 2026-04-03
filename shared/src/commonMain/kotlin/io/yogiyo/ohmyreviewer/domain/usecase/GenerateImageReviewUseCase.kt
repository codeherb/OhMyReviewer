package io.yogiyo.ohmyreviewer.domain.usecase

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import io.yogiyo.ohmyreviewer.domain.model.PromptBuilder
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import kotlinx.coroutines.flow.Flow

class GenerateImageReviewUseCase(
    private val repository: MLRepository,
) {
    operator fun invoke(model: GeminiModel, image: PlatformImage): Flow<String> =
        repository.generateReview(model, image, PromptBuilder.buildImageReviewPrompt())
}
