package io.yogiyo.ohmyreviewer.data.model

data class ReviewRequestData(
    val shopName: String,
    val menuName: String,
    val maxReviewLength: Int,
) {
    companion object {
        fun fromJson(json: String): ReviewRequestData {
            fun extractString(key: String, from: String): String {
                val pattern = """"$key"\s*:\s*"([^"]*)"""".toRegex()
                return pattern.find(from)?.groupValues?.get(1).orEmpty()
            }

            fun extractInt(key: String, from: String): Int {
                val pattern = """"$key"\s*:\s*(\d+)""".toRegex()
                return pattern.find(from)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            }

            return ReviewRequestData(
                shopName = extractString("shop_name", json),
                menuName = extractString("menu_name", json),
                maxReviewLength = extractInt("food_review_comment_maximum_length", json),
            )
        }
    }
}
