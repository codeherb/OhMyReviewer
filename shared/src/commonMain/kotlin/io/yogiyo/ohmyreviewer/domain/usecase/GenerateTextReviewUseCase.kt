package io.yogiyo.ohmyreviewer.domain.usecase

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.ReviewRequestData
import io.yogiyo.ohmyreviewer.domain.model.PromptBuilder
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository

class GenerateTextReviewUseCase(
    private val repository: MLRepository,
) {
    suspend operator fun invoke(model: GeminiModel, input: String): String {
        val prompt = ReviewRequestData.fromJson(input)
            ?.let { PromptBuilder.buildStructuredReviewPrompt(it) }
            ?: PromptBuilder.buildFreeTextReviewPrompt(input)
        return repository.generateTextReview(model, prompt)
    }
}
