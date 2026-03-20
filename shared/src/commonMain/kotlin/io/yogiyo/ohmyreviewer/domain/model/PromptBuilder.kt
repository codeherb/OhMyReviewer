package io.yogiyo.ohmyreviewer.domain.model

import io.yogiyo.ohmyreviewer.data.model.ReviewRequestData

object PromptBuilder {

    private const val SYSTEM_INSTRUCTION =
        "반드시 리뷰 본문만 출력하세요. " +
            "여러 옵션을 제시하지 말고 하나의 완성된 리뷰만 작성하세요. " +
            "제목, 번호, 설명, 부가 문구 없이 리뷰 텍스트만 출력하세요."

    fun buildStructuredReviewPrompt(data: ReviewRequestData): String =
        "다음 주문 정보를 바탕으로 배달 앱에 올릴 한국어 리뷰를 작성해주세요.\n" +
            "자연스럽고 솔직한 톤으로, 음식에 대한 만족감을 포함해주세요.\n" +
            "3~5문장, ${data.maxReviewLength}자 이내로 작성해주세요.\n" +
            "$SYSTEM_INSTRUCTION\n\n" +
            "가게명: ${data.shopName}\n" +
            "주문 메뉴: ${data.menuName}"

    fun buildFreeTextReviewPrompt(text: String): String =
        "다음 메뉴 정보를 바탕으로 배달 앱에 올릴 한국어 리뷰를 작성해주세요.\n" +
            "자연스럽고 솔직한 톤으로, 음식에 대한 만족감을 포함해주세요.\n" +
            "3~5문장으로 작성해주세요.\n" +
            "$SYSTEM_INSTRUCTION\n\n" +
            "메뉴 정보: $text"
}
