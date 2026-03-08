package io.yogiyo.ohmyreviewer.data.model

import android.graphics.Bitmap

class AndroidPlatformImage private constructor(
    private val bitmap: Bitmap
): PlatformImage {
    override val image: Any = bitmap

    companion object {
        fun create(bitmap: Bitmap): AndroidPlatformImage {
            val bitmapConfig = bitmap.config ?: Bitmap.Config.ARGB_8888
            val copied = bitmap.copy(bitmapConfig, false)
            return AndroidPlatformImage(copied)
        }
    }
}