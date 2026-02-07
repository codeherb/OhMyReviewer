package io.yogiyo.ohmyreviewer.data.model

import android.graphics.Bitmap

class AndroidPlatformImage(
    private val bitmap: Bitmap
): PlatformImage {
    override val image: Any = bitmap
}