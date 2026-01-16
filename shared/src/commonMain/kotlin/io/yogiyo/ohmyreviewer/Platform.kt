package io.yogiyo.ohmyreviewer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform