import UIKit
import Shared

#if canImport(FoundationModels)
import FoundationModels
#endif

/// Cloud ML(Gemini)로 이미지에서 설명을 추출하고, Foundation Models로 키워드를 정제하는 이미지 분석 서비스
class ImageAnalyzer {

    /// 싱글톤 인스턴스
    static let shared = ImageAnalyzer()

    private let koinHelper = KoinHelper()

    private init() {}

    /// Cloud ML을 통해 이미지를 분석하여 키워드 목록을 반환합니다
    /// - Parameter image: 분석할 UIImage
    /// - Returns: 추출된 키워드 배열
    func analyzeImage(_ image: UIImage) async -> [String] {
        let platformImage = IOSPlatformImage(uiImage: image)
        do {
let description = try await koinHelper.aiRepository.generateCloudImageDescription(
    model: GeminiModel.companion.DEFAULT,
    image: platformImage
)
            return [description]
        } catch {
            print("### [ImageAnalyzer] Cloud ML 오류: \(error)")
            return []
        }
    }

    /// Vision 라벨 + OCR 텍스트를 FoundationModels로 한국어 키워드로 변환합니다
    /// - Parameters:
    ///   - labels: Vision 분류 라벨
    ///   - texts: OCR 인식 텍스트
    /// - Returns: 한국어 키워드 배열
    #if canImport(FoundationModels)
    @available(iOS 26, *)
    func enrichKeywords(labels: [String]) async -> [String] {
        let description = labels.joined(separator: " ")
        guard !description.isEmpty else { return [] }

        do {
            let session = LanguageModelSession()
            let prompt = """
            다음은 배달 음식 사진을 AI가 분석한 설명입니다:
            \(description)

            위 설명에서 음식 리뷰 작성에 유용한 핵심 키워드를 추출해주세요.

            규칙:
            1. 음식명, 재료, 외관 특징을 한글로 추출
            2. 총 5-8개, 쉼표로 구분
            3. 한글 키워드만 출력

            예시 출력: 비빔밥, 신선한 채소, 고소한 참기름, 푸짐한, 색감이 좋은
            """
            let response = try await session.respond(to: prompt)
            let keywords = response.content
                .split(separator: ",")
                .map { $0.trimmingCharacters(in: .whitespaces) }
                .filter { !$0.isEmpty }

            return keywords
        } catch {
            return labels
        }
    }
    #endif


}


