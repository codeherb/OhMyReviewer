package io.yogiyo.ohmyreviewer.di

import io.yogiyo.ohmyreviewer.data.repository.MLRepositoryImpl
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateImageDescriptionUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateImageReviewUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.GenerateTextReviewUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.InitializeModelUseCase
import io.yogiyo.ohmyreviewer.domain.usecase.ParseReviewRequestUseCase
import org.koin.dsl.module

val commonModule = module {
    single<MLRepository> { MLRepositoryImpl(get()) }
    factory { InitializeModelUseCase(get()) }
    factory { GenerateTextReviewUseCase(get()) }
    factory { GenerateImageDescriptionUseCase(get()) }
    factory { GenerateImageReviewUseCase(get()) }
    factory { ParseReviewRequestUseCase() }
}
