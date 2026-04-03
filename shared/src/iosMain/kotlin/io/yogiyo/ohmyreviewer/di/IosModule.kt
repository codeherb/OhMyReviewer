package io.yogiyo.ohmyreviewer.di

import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasourceImpl
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateImageReviewUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateTextReviewUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module

val iosModule = module {
    single<MLDatasource> { MLDatasourceImpl() }
}

fun initKoin() {
    startKoin {
        modules(commonModule, iosModule)
    }
}

class KoinHelper : KoinComponent {
    val aiRepository: MLRepository by inject()
    val generateTextReviewUseCase: GenerateTextReviewUseCase by inject()
    val generateImageReviewUseCase: GenerateImageReviewUseCase by inject()
}
