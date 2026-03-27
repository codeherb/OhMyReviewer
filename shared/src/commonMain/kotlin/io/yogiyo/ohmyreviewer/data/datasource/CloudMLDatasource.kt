package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow

interface CloudMLDatasource {
    fun generateImageDescription(image: PlatformImage): Flow<String>
}
