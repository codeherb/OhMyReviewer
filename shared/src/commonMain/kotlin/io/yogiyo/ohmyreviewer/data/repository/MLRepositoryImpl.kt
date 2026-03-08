package io.yogiyo.ohmyreviewer.data.repository

import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import kotlinx.coroutines.flow.Flow

class MLRepositoryImpl(
    private val aiDatasource: MLDatasource
) : MLRepository {

    override fun generateContent(prompt: String): Flow<String> {
        return aiDatasource.generateContent(prompt)
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> {
        return aiDatasource.generateImageDescription(image)
    }

}
