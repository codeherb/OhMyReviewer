package io.yogiyo.ohmyreviewer.data.model

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

class IOSPlatformImage(
    private val uiImage: UIImage
): PlatformImage {
    override val image: Any = uiImage

    @OptIn(ExperimentalForeignApi::class)
    override fun toByteArray(): ByteArray {
        val data = UIImageJPEGRepresentation(uiImage, 0.9)
            ?: error("Failed to convert UIImage to JPEG data")
        return data.bytes!!.readBytes(data.length.toInt())
    }
}