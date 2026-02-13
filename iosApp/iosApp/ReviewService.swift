import Foundation
import FoundationModels

/// 리뷰 생성 서비스
/// iOS 26 이상에서는 Foundation Models를 사용하고,
/// 그 이하 버전에서는 템플릿 기반 생성을 사용합니다
class ReviewService {

    /// 싱글톤 인스턴스
    static let shared = ReviewService()

    private init() {}

    /// Foundation Models 지원 여부 확인
    var isFoundationModelsSupported: Bool {
        if #available(iOS 26, *) {
            return true
        }
        return false
    }

    /// 키워드를 기반으로 리뷰를 생성합니다
    /// - Parameter keywords: 리뷰에 포함할 키워드 배열
    /// - Returns: 생성된 리뷰 텍스트
    func generateReview(keywords: [String]) async -> String {
        if #available(iOS 26, *) {
#if canImport(FoundationModels)
            return await generateWithFoundationModels(keywords: keywords)
#else
            return generateWithTemplate(keywords: keywords)
#endif
        } else {
            return generateWithTemplate(keywords: keywords)
        }
    }

    /// 이미지 분석 결과와 사용자 키워드를 조합하여 리뷰를 생성합니다
    /// - Parameters:
    ///   - imageLabels: 이미지 분석에서 추출된 라벨
    ///   - userKeywords: 사용자가 입력한 키워드
    /// - Returns: 생성된 리뷰 텍스트
    func generateReview(imageLabels: [String], userKeywords: [String]) async -> String {
        let combinedKeywords = Array(Set(imageLabels + userKeywords))
        return await generateReview(keywords: combinedKeywords)
    }

    // MARK: - Private Methods

    /// Foundation Models를 사용한 리뷰 생성 (iOS 26+)
    @available(iOS 26, *)
    private func generateWithFoundationModels(keywords: [String]) async -> String {
#if canImport(FoundationModels)
        do {
            let session = LanguageModelSession()

            let keywordText = keywords.joined(separator: ", ")
            let prompt = """
            다음 키워드를 참고하여 자연스러운 한국어 리뷰를 작성해주세요.
            리뷰는 2-3문장으로 작성하고, 친근하고 솔직한 느낌으로 작성해주세요.
            
            키워드: \(keywordText)
            
            리뷰:
            """

            let response = try await session.respond(to: prompt)
            return response.content
        } catch {
            print("Foundation Models 오류: \(error.localizedDescription)")
            return generateWithTemplate(keywords: keywords)
        }
#else
        return generateWithTemplate(keywords: keywords)
#endif
    }

    /// 템플릿 기반 리뷰 생성 (Fallback)
    private func generateWithTemplate(keywords: [String]) -> String {
        guard !keywords.isEmpty else {
            return "좋은 경험이었습니다. 다음에 또 방문하고 싶어요!"
        }

        // 키워드 카테고리 분류
        let positiveWords = ["맛있다", "좋다", "훌륭하다", "추천", "최고", "만족", "친절", "깔끔"]
        let foodWords = ["음식", "맛", "식사", "요리", "메뉴"]
        let serviceWords = ["서비스", "직원", "친절", "응대"]
        let atmosphereWords = ["분위기", "인테리어", "깨끗", "청결"]

        var reviewParts: [String] = []

        // 음식 관련 키워드가 있으면
        if keywords.contains(where: { keyword in
            foodWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("음식이 정말 맛있었어요.")
        }

        // 서비스 관련 키워드가 있으면
        if keywords.contains(where: { keyword in
            serviceWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("직원분들도 친절하셨습니다.")
        }
        
        // 분위기 관련 키워드가 있으면
        if keywords.contains(where: { keyword in
            atmosphereWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("분위기도 좋았어요.")
        }
        
        // 긍정적 키워드가 있으면
        if keywords.contains(where: { keyword in
            positiveWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("다음에 또 방문하고 싶습니다!")
        }
        
        // 기본 리뷰 생성
        if reviewParts.isEmpty {
            let keywordText = keywords.prefix(3).joined(separator: ", ")
            return "\(keywordText) - 전반적으로 좋은 경험이었습니다. 추천드려요!"
        }
        
        return reviewParts.joined(separator: " ")
    }
}

// MARK: - Review Result Model

/// 리뷰 생성 결과
struct ReviewResult {
    let text: String
    let keywords: [String]
    let generatedAt: Date
    let usedFoundationModels: Bool
    
    init(text: String, keywords: [String], usedFoundationModels: Bool = false) {
        self.text = text
        self.keywords = keywords
        self.generatedAt = Date()
        self.usedFoundationModels = usedFoundationModels
    }
}
