package io.yogiyo.ohmyreviewer.data.model


interface PlatformImage {
    val image: Any
    fun toByteArray(): ByteArray
}