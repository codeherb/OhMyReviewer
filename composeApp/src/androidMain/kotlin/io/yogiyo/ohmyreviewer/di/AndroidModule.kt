package io.yogiyo.ohmyreviewer.di

import android.util.Log
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasourceImpl
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<MLDatasource> { MLDatasourceImpl(androidContext(), get()) }

    factory {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("ExternalScope", null,throwable)
        }
        CoroutineScope(SupervisorJob() + Dispatchers.Default + errorHandler)
    }
}
