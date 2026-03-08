package io.yogiyo.ohmyreviewer

sealed interface ImageMeta {
    object None : ImageMeta
    data class ImageLabels(
        val labels: List<ImageLabel> = emptyList()
    ): ImageMeta
    data class ImageDescription(
        val text: String
    ): ImageMeta
}

/**
 * 이미지 분석 결과 라벨
 */
data class ImageLabel(
    val text: String,
    val confidence: Float
)

/**
 * 이미지 분석기 인터페이스
 * 플랫폼별로 actual 구현 필요
 */
expect class ImageAnalyzer() {
    /**
     * 이미지 바이트 배열을 분석하여 라벨 목록 반환
     * @param imageBytes 분석할 이미지의 바이트 배열
     * @param width 이미지 너비
     * @param height 이미지 높이
     * @return 감지된 라벨 목록
     */
    suspend fun analyzeImage(imageBytes: ByteArray, width: Int, height: Int): List<ImageLabel>
}
