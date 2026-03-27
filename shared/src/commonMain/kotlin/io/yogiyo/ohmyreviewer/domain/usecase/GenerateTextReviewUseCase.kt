package io.yogiyo.ohmyreviewer.domain.usecase

import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.ReviewRequestData
import io.yogiyo.ohmyreviewer.domain.model.PromptBuilder
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import kotlinx.coroutines.flow.Flow

class GenerateTextReviewUseCase(
    private val repository: MLRepository,
) {
    operator fun invoke(model: GeminiModel, input: String): Flow<String> {
        val prompt = runCatching { ReviewRequestData.fromJson(input) }
            .map { PromptBuilder.buildStructuredReviewPrompt(it) }
            .getOrElse { PromptBuilder.buildFreeTextReviewPrompt(input) }
        return repository.generateTextReview(model, prompt)
    }
}
