package io.yogiyo.ohmyreviewer.di

import io.yogiyo.ohmyreviewer.ImageAnalyzer
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasourceImpl
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.ui.aiimagereview.AiImageReviewViewModel
import io.yogiyo.ohmyreviewer.ui.aitextreview.AiTextReviewViewModel
import io.yogiyo.ohmyreviewer.ui.image.ImageViewModel
import io.yogiyo.ohmyreviewer.ui.review.ReviewViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    single<MLDatasource> { MLDatasourceImpl(androidContext(), CoroutineScope(SupervisorJob() + Dispatchers.IO)) }
    single { ImageAnalyzer() }

    // ViewModels
    viewModel { ImageViewModel(androidContext(), get(), get()) }
    viewModel { ReviewViewModel(androidContext(), get()) }
    viewModel { AiTextReviewViewModel(get(), get()) }
    viewModel { AiImageReviewViewModel(androidContext().contentResolver, get()) }
}
