package io.yogiyo.ohmyreviewer.domain.usecase

import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository

class InitializeModelUseCase(
    private val repository: MLRepository,
) {
    suspend fun initializeAll(): Result<ModelStatus> = runCatching {
        repository.initialize()
    }

    suspend fun initializeImageDescription(): Result<ModelStatus> = runCatching {
        repository.initializeImageDescription()
    }
}
