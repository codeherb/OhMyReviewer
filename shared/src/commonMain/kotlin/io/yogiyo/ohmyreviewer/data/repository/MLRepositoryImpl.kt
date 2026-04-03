package io.yogiyo.ohmyreviewer.data.repository

import io.yogiyo.ohmyreviewer.data.datasource.CloudMLDatasource
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import kotlinx.coroutines.flow.Flow

class MLRepositoryImpl(
    private val datasource: MLDatasource,
    private val cloudDatasource: CloudMLDatasource,
) : MLRepository {

    override fun generateCloudImageDescription(model: GeminiModel, image: PlatformImage): Flow<String> =
        cloudDatasource.generateImageDescription(model, image)

    override fun generateImageDescription(image: PlatformImage): Flow<String> =
        datasource.generateImageDescription(image)

    override fun generateReview(model: GeminiModel, image: PlatformImage, prompt: String): Flow<String> =
        cloudDatasource.generateImageReview(model, image, prompt)

    override fun generateTextReview(model: GeminiModel, prompt: String): Flow<String> =
        cloudDatasource.generateTextReview(model, prompt)
}
