package io.yogiyo.ohmyreviewer.ui.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        viewModel.onEvent(ReviewContract.Event.OnImageSelected(uri))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReviewContract.Effect.ShowError -> {
                    // TODO: Snackbar로 에러 표시
                }
            }
        }
    }

    ReviewContent(
        state = state,
        reviewTextFieldState = viewModel.reviewTextFieldState,
        onPickImageClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onDescribeClick = {
            viewModel.onEvent(ReviewContract.Event.OnDescribeClick)
        },
    )
}

@Composable
private fun ReviewContent(
    state: ReviewContract.State,
    reviewTextFieldState: TextFieldState,
    onPickImageClick: () -> Unit,
    onDescribeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "리뷰",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 모델 상태 표시
        ModelStatusSection(state = state)

        Spacer(modifier = Modifier.height(8.dp))

        // 이미지 프리뷰
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.selectedBitmap != null -> {
                    Image(
                        bitmap = state.selectedBitmap.asImageBitmap(),
                        contentDescription = "선택된 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                else -> {
                    Text(
                        text = "이미지를 선택해주세요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 액션 버튼
        ActionButtonsSection(
            hasSelectedImage = state.hasSelectedImage,
            isDescribing = state.isDescribing,
            isModelReady = state.isModelReady,
            onPickImageClick = onPickImageClick,
            onDescribeClick = onDescribeClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 이미지 설명 결과
        if (state.hasDescription) {
            DescriptionResultCard(description = state.description)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 설명 안내 메시지
        if (state.shouldShowDescriptionGuide) {
            Text(
                text = "이미지를 선택한 후 '이미지 설명하기' 버튼을 눌러주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            state = reviewTextFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("리뷰 내용을 입력하세요") },
            lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.MultiLine(
                minHeightInLines = 4,
                maxHeightInLines = 8,
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun ModelStatusSection(state: ReviewContract.State) {
    when {
        state.isInitializingModel -> {
            Text(
                text = "AI 모델 준비 중...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        state.isModelReady -> {
            Text(
                text = "AI 모델 준비 완료",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        else -> {
            Text(
                text = "AI 모델을 사용할 수 없습니다",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    hasSelectedImage: Boolean,
    isDescribing: Boolean,
    isModelReady: Boolean,
    onPickImageClick: () -> Unit,
    onDescribeClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onPickImageClick,
            enabled = !isDescribing,
        ) {
            Text(text = if (hasSelectedImage) "다른 이미지 선택" else "갤러리에서 선택")
        }

        if (hasSelectedImage) {
            Button(
                onClick = onDescribeClick,
                enabled = !isDescribing && isModelReady,
            ) {
                if (isDescribing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("설명 생성 중...")
                } else {
                    Text("이미지 설명하기")
                }
            }
        }
    }
}

@Composable
private fun DescriptionResultCard(
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "이미지 설명",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
