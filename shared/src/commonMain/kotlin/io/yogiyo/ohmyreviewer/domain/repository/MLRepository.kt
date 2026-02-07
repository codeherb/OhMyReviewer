package io.yogiyo.ohmyreviewer.domain.repository

import kotlinx.coroutines.flow.Flow

interface MLRepository {

    fun generateContent(prompt: String): Flow<String>

    fun generateImageDescription(bitmap: ByteArray): Flow<String>

}
