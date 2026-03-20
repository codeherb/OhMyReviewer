package io.yogiyo.ohmyreviewer.ui.aiimagereview

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AiImageReviewScreen(
    viewModel: AiImageReviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.onEvent(AiImageReviewContract.Event.OnImageSelected(uri))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AiImageReviewContract.Effect.ShowError -> {
                    // TODO: Snackbar로 에러 표시
                }
            }
        }
    }

    AiImageReviewContent(
        state = state,
        onPickImage = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onClearImage = { viewModel.onEvent(AiImageReviewContract.Event.OnClearImage) },
        onModelSelected = { viewModel.onEvent(AiImageReviewContract.Event.OnModelSelected(it)) },
        onGenerateClick = { viewModel.onEvent(AiImageReviewContract.Event.OnGenerateClick) },
    )
}

@Composable
private fun AiImageReviewContent(
    state: AiImageReviewContract.State,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onModelSelected: (GeminiModel) -> Unit,
    onGenerateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "AI 이미지 리뷰",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ModelStatusSection(state = state)

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isCloudMode) {
            ModelSelector(
                selectedModel = state.selectedModel,
                onModelSelected = onModelSelected,
                enabled = !state.isGenerating,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        ImageSection(
            selectedImage = state.selectedImage,
            onPickImage = onPickImage,
            onClearImage = onClearImage,
            enabled = !state.isGenerating,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onGenerateClick,
            enabled = state.canGenerate,
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = if (state.isGenerating) "분석 중..." else "이미지 분석 & 리뷰 생성")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.hasGeneratedReview) {
            GeneratedReviewCard(review = state.generatedReview)
        }
    }
}

@Composable
private fun ImageSection(
    selectedImage: Bitmap?,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    enabled: Boolean,
) {
    if (selectedImage != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Image(
                bitmap = selectedImage.asImageBitmap(),
                contentDescription = "선택된 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onClearImage, enabled = enabled) {
            Text("이미지 변경")
        }
    } else {
        OutlinedButton(onClick = onPickImage, enabled = enabled) {
            Text("음식 사진 선택")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelSelector(
    selectedModel: GeminiModel,
    onModelSelected: (GeminiModel) -> Unit,
    enabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "모델 선택",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GeminiModel.entries.forEach { model ->
                FilterChip(
                    selected = model == selectedModel,
                    onClick = { onModelSelected(model) },
                    enabled = enabled,
                    label = { Text(model.displayName, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ModelStatusSection(state: AiImageReviewContract.State) {
    when {
        state.isInitializingModel -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (state.isDownloading) {
                        "AI 모델 다운로드 중... ${(state.downloadProgress * 100).toInt()}%"
                    } else {
                        "AI 모델 준비 중..."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (state.isDownloading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                    )
                }
            }
        }

        state.isModelReady -> {
            val modeLabel = if (state.isCloudMode) "Cloud - ${state.selectedModel.displayName}" else "On-device"
            Text(
                text = "AI 모델 준비 완료 ($modeLabel)",
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
private fun GeneratedReviewCard(review: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI 이미지 분석 리뷰",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = review,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
