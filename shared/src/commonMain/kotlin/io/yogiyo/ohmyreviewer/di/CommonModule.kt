package io.yogiyo.ohmyreviewer.di

import io.yogiyo.ohmyreviewer.data.repository.MLRepositoryImpl
import io.yogiyo.ohmyreviewer.domain.repository.MLRepository
import org.koin.dsl.module

val commonModule = module {
    single<MLRepository> { MLRepositoryImpl(get()) }
}




