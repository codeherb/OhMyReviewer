package io.yogiyo.ohmyreviewer.domain.repository

import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow

interface MLRepository {

    fun generateContent(prompt: String): Flow<String>

    fun generateImageDescription(image: PlatformImage): Flow<String>

}
