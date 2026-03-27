package io.yogiyo.ohmyreviewer.data.repository

import io.yogiyo.ohmyreviewer.data.datasource.CloudMLDatasource
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class MLRepositoryImpl(
    private val datasource: MLDatasource,
    private val cloudDatasource: CloudMLDatasource
) : MLRepository {

    override val downloadProgress: StateFlow<Float>
        get() = datasource.downloadProgress

    override val isCloudApiAvailable: Boolean
        get() = datasource.isCloudApiAvailable

    override val isPromptApiAvailable: Boolean
        get() = datasource.isPromptApiAvailable

    override suspend fun initialize(): ModelStatus =
        datasource.initialize().await()

    override suspend fun initializeImageDescription(): ModelStatus =
        datasource.initializeImageDescription().await()

    override fun generateContent(prompt: String): Flow<String> =
        datasource.generateContent(prompt)

    override fun generateCloudImageDescription(image: PlatformImage): Flow<String> {
        return cloudDatasource.generateImageDescription(image)
    }

    override fun generateImageDescription(image: PlatformImage): Flow<String> =
        datasource.generateImageDescription(image)

    override fun generateReview(image: PlatformImage, prompt: String): Flow<String> =
        datasource.generateReview(image, prompt)

    override fun generateTextReview(prompt: String): Flow<String> =
        datasource.generateTextReview(prompt)

    override val currentCloudModel: GeminiModel
        get() = datasource.currentCloudModel

    override fun changeCloudModel(model: GeminiModel) =
        datasource.changeCloudModel(model)

    override suspend fun close() {
        datasource.close().await()
    }
}
