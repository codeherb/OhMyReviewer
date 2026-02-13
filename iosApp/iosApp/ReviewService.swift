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
            다음 키워드를 참고하여 배달 음식 리뷰를 작성해주세요.
            리뷰는 2-3문장으로 작성하고, 실제 배달앱 리뷰처럼 친근하고 솔직한 느낌으로 작성해주세요.
            음식 맛, 양, 포장 상태, 배달 속도 등 배달 음식 리뷰에 적합한 내용을 포함해주세요.
            
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
            return "맛있게 잘 먹었습니다. 다음에 또 시켜먹을게요!"
        }

        // 배달 음식 리뷰 키워드 카테고리
        let tasteWords = ["맛", "맛있", "달콤", "매콤", "짭짤", "고소", "담백", "신선", "바삭"]
        let quantityWords = ["양", "푸짐", "넉넉", "가성비", "가격"]
        let packagingWords = ["포장", "용기", "깔끔", "깨끗", "정성"]
        let deliveryWords = ["배달", "빠른", "따뜻", "뜨거운", "식지"]

        var reviewParts: [String] = []

        // 맛 관련
        if keywords.contains(where: { keyword in
            tasteWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("음식 맛이 정말 좋았어요.")
        }

        // 양/가성비 관련
        if keywords.contains(where: { keyword in
            quantityWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("양도 푸짐해서 가성비 좋습니다.")
        }
        
        // 포장 상태 관련
        if keywords.contains(where: { keyword in
            packagingWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("포장도 깔끔하게 잘 해주셨어요.")
        }
        
        // 배달 관련
        if keywords.contains(where: { keyword in
            deliveryWords.contains(where: { keyword.contains($0) })
        }) {
            reviewParts.append("배달도 빠르고 음식이 따뜻하게 왔어요.")
        }
        
        // 기본 리뷰 생성
        if reviewParts.isEmpty {
            let keywordText = keywords.prefix(3).joined(separator: ", ")
            return "\(keywordText) - 맛있게 잘 먹었습니다. 또 주문할게요!"
        }
        
        reviewParts.append("다음에도 또 주문하겠습니다!")
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
