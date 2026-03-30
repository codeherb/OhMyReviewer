import Foundation
import FoundationModels

// MARK: - User Review Answers

struct UserReviewAnswers {
    let taste: Bool      // 맛있었나요?
    let quantity: Bool   // 배불렀나요?
    let packaging: Bool  // 포장 상태는 좋았나요?
    let delivery: Bool   // 배달은 제때 됐나요?
    let recommend: Bool  // 추천하나요?

    fileprivate func toDescription() -> String {
        [
            "- 맛: \(taste ? "맛있었다" : "맛이 아쉬웠다")",
            "- 양: \(quantity ? "양이 충분했다" : "양이 부족했다")",
            "- 포장: \(packaging ? "포장 상태가 좋았다" : "포장 상태가 아쉬웠다")",
            "- 배달: \(delivery ? "배달이 빠르고 제때 왔다" : "배달이 늦었다")",
            "- 총평: \(recommend ? "재주문 의사가 있다" : "재주문 의사가 없다")",
        ].joined(separator: "\n")
    }
}

// MARK: - Review Service

/// 리뷰 생성 서비스
/// iOS 26 이상에서는 Foundation Models를 사용하고,
/// 그 이하 버전에서는 템플릿 기반 생성을 사용합니다
class ReviewService {

    static let shared = ReviewService()

    private init() {}

    var isFoundationModelsSupported: Bool {
        if #available(iOS 26, *) {
            return true
        }
        return false
    }

    /// 이미지 키워드와 사용자 답변을 결합하여 리뷰를 생성합니다
    func generateReview(keywords: [String], userAnswers: UserReviewAnswers) async -> String {
        if #available(iOS 26, *) {
#if canImport(FoundationModels)
            return await generateWithFoundationModels(keywords: keywords, userAnswers: userAnswers)
#else
            return generateWithTemplate(keywords: keywords, userAnswers: userAnswers)
#endif
        } else {
            return generateWithTemplate(keywords: keywords, userAnswers: userAnswers)
        }
    }

    // MARK: - Private Methods

    @available(iOS 26, *)
    private func generateWithFoundationModels(keywords: [String], userAnswers: UserReviewAnswers) async -> String {
#if canImport(FoundationModels)
        do {
            let session = LanguageModelSession()

            let foodName = keywords.first ?? "음식"
            let otherKeywords = keywords.dropFirst().joined(separator: ", ")

            let prompt = """
            배달 음식 리뷰를 작성해주세요.

            음식 정보: \(foodName)\(otherKeywords.isEmpty ? "" : ", \(otherKeywords)")

            사용자의 경험:
            \(userAnswers.toDescription())

            작성 방법:
            - "\(foodName)"을 첫 문장에 자연스럽게 포함해주세요
            - 각 항목을 따로따로 나열하지 말고, 전체적인 경험을 하나의 흐름으로 녹여주세요
            - 긍정/부정 항목이 섞여 있다면 솔직하게 표현해주세요 (예: "맛은 좋았는데 배달이 늦었어요")
            - 2-3문장, 100-150자, 배달앱 특유의 친근한 말투로 작성해주세요
            - 총평이 재주문 의사 없음이라면 마지막에 아쉬움을 담아주세요

            리뷰만 출력해주세요:
            """

            let response = try await session.respond(to: prompt)
            print("### [ReviewService] 사용자 평가: \(userAnswers.toDescription())")
            print("### [ReviewService] 생성된 리뷰: \(response.content)")
            return response.content
        } catch {
            print("Foundation Models 오류: \(error.localizedDescription)")
            return generateWithTemplate(keywords: keywords, userAnswers: userAnswers)
        }
#else
        return generateWithTemplate(keywords: keywords, userAnswers: userAnswers)
#endif
    }

    private func generateWithTemplate(keywords: [String], userAnswers: UserReviewAnswers) -> String {
        let foodName = keywords.first ?? "음식"
        var parts: [String] = []

        // 맛
        if userAnswers.taste {
            parts.append("\(foodName) 맛있게 잘 먹었습니다!")
        } else {
            parts.append("\(foodName) 맛이 조금 아쉬웠어요.")
        }

        // 양
        if userAnswers.quantity {
            parts.append("양도 푸짐해서 좋았어요.")
        } else {
            parts.append("양이 좀 더 많았으면 했어요.")
        }

        // 포장
        if userAnswers.packaging {
            parts.append("포장도 깔끔하게 잘 해주셨어요.")
        } else {
            parts.append("포장이 조금 아쉬웠어요.")
        }

        // 배달
        if userAnswers.delivery {
            parts.append("배달도 빠르게 왔어요.")
        } else {
            parts.append("배달이 조금 늦었어요.")
        }

        // 총평
        if userAnswers.recommend {
            parts.append("재주문 의사 있습니다!")
        } else {
            parts.append("전반적으로 조금 아쉬운 경험이었어요.")
        }

        return parts.joined(separator: " ")
    }
}

// MARK: - Review Result Model

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
