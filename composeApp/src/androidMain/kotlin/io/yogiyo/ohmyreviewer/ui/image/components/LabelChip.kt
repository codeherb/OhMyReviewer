package io.yogiyo.ohmyreviewer.ui.image.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.yogiyo.ohmyreviewer.ImageLabel

/**
 * 라벨 결과를 표시하는 Chip 컴포넌트
 *
 * @param label 표시할 라벨 정보
 * @param modifier Modifier
 */
@Composable
fun LabelChip(
    label: ImageLabel,
    modifier: Modifier = Modifier,
) {
    val confidencePercent = (label.confidence * 100).toInt()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = "${label.text} ($confidencePercent%)",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
