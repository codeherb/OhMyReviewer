package io.yogiyo.ohmyreviewer

/**
 * iOS 이미지 분석기 구현 (추후 구현 예정)
 * 현재는 빈 결과 반환
 */
actual class ImageAnalyzer {

    /**
     * iOS에서의 이미지 분석 (TODO: Vision Framework 연동)
     */
    actual suspend fun analyzeImage(
        imageBytes: ByteArray,
        width: Int,
        height: Int
    ): List<ImageLabel> {
        // TODO: iOS Vision Framework를 사용한 이미지 라벨링 구현
        return emptyList()
    }
}
