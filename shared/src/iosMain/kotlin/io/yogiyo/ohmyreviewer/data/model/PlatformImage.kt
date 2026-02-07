package io.yogiyo.ohmyreviewer.data.model

import platform.UIKit.UIImage

class IOSPlatformImage(
    private val uiImage: UIImage
): PlatformImage {
    override val image: Any = uiImage
}