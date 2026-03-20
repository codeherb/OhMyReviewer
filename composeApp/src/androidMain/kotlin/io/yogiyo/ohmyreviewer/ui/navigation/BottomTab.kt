package io.yogiyo.ohmyreviewer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 하단 네비게이션 탭 정의
 */
enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    IMAGE(
        route = Route.IMAGE,
        label = "이미지",
        icon = Icons.Default.Image,
    ),
    REVIEW(
        route = Route.REVIEW,
        label = "리뷰",
        icon = Icons.Default.RateReview,
    ),
    AI_REVIEW(
        route = Route.AI_TEXT_REVIEW,
        label = "AI 텍스트 리뷰",
        icon = Icons.Default.AutoAwesome,
    ),
}
