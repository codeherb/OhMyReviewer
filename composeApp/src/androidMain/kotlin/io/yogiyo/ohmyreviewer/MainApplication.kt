package io.yogiyo.ohmyreviewer

import android.app.Application
import io.yogiyo.ohmyreviewer.di.androidModule
import io.yogiyo.ohmyreviewer.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(commonModule, androidModule)
        }
    }
}
