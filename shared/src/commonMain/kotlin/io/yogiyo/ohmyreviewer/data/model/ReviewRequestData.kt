package io.yogiyo.ohmyreviewer.data.model

data class ReviewRequestData(
    val shopName: String,
    val menuName: String,
    val maxReviewLength: Int,
) {
    companion object {
        fun fromJson(json: String): ReviewRequestData? {
            val trimmed = json.trim()
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null

            fun extractString(key: String, from: String): String {
                val pattern = """"$key"\s*:\s*"([^"]*)"""".toRegex()
                return pattern.find(from)?.groupValues?.get(1).orEmpty()
            }

            fun extractInt(key: String, from: String): Int {
                val pattern = """"$key"\s*:\s*(\d+)""".toRegex()
                return pattern.find(from)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            }

            val data = ReviewRequestData(
                shopName = extractString("shop_name", json),
                menuName = extractString("menu_name", json),
                maxReviewLength = extractInt("food_review_comment_maximum_length", json),
            )

            // 필수 필드가 비어있으면 null 반환 → 텍스트 그대로 사용
            if (data.shopName.isBlank() && data.menuName.isBlank()) return null

            return data
        }
    }
}
