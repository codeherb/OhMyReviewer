package io.yogiyo.ohmyreviewer.data.model

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class AndroidPlatformImage private constructor(
    private val bitmap: Bitmap
): PlatformImage {
    override val image: Any = bitmap

    override fun toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    companion object {
        fun create(bitmap: Bitmap): AndroidPlatformImage {
            val bitmapConfig = bitmap.config ?: Bitmap.Config.ARGB_8888
            val copied = bitmap.copy(bitmapConfig, false)
            return AndroidPlatformImage(copied)
        }
    }
}