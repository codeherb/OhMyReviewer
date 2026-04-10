package io.yogiyo.ohmyreviewer.domain.usecase

import io.yogiyo.ohmyreviewer.data.model.ReviewRequestData

class ParseReviewRequestUseCase {
    operator fun invoke(input: String): ReviewRequestData? =
        ReviewRequestData.fromJson(input)
}
