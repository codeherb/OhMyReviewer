package io.yogiyo.ohmyreviewer.domain.usecase

import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import kotlinx.coroutines.flow.Flow

class GenerateImageDescriptionUseCase(
    private val repository: MLRepository,
) {
    operator fun invoke(image: PlatformImage): Flow<String> =
        repository.generateImageDescription(image)
}
