package io.yogiyo.ohmyreviewer.di

import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasourceImpl
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.IOSPlatformImage
import io.yogiyo.ohmyreviewer.data.model.ReviewRequestData
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateImageDescriptionUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateImageReviewUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateTextReviewUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.ParseReviewRequestUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.UIKit.UIImage

val iosModule = module {
    single<MLDatasource> { MLDatasourceImpl() }
}

fun initKoin() {
    startKoin {
        modules(commonModule, iosModule)
    }
}

class KoinHelper : KoinComponent {
    private val generateImageReviewUseCase: GenerateImageReviewUseCase by inject()
    private val generateImageDescriptionUseCase: GenerateImageDescriptionUseCase by inject()
    private val generateTextReviewUseCase: GenerateTextReviewUseCase by inject()
    private val parseReviewRequestUseCase: ParseReviewRequestUseCase by inject()

    val availableModels: List<GeminiModel> get() = GeminiModel.entries

    val defaultModel: GeminiModel get() = GeminiModel.DEFAULT

    @Throws(Exception::class)
    suspend fun generateImageReview(image: UIImage, model: GeminiModel = GeminiModel.DEFAULT): String =
        withContext(Dispatchers.IO) {
            val platformImage = IOSPlatformImage(image)
            generateImageReviewUseCase(model, platformImage).first()
        }

    @Throws(Exception::class)
    suspend fun generateImageDescription(image: UIImage): String =
        withContext(Dispatchers.IO) {
            val platformImage = IOSPlatformImage(image)
            generateImageDescriptionUseCase(platformImage).first()
        }

    @Throws(Exception::class)
    suspend fun generateTextReview(input: String, model: GeminiModel = GeminiModel.DEFAULT): String =
        withContext(Dispatchers.IO) {
            generateTextReviewUseCase(model, input).first()
        }

    fun parseReviewRequest(input: String): ReviewRequestData? {
        return parseReviewRequestUseCase(input)
    }
}
