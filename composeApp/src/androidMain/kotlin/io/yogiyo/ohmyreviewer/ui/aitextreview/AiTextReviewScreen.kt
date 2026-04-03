package io.yogiyo.ohmyreviewer.ui.aitextreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.ReviewRequestData
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AiTextReviewScreen(
    viewModel: AiTextReviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AiTextReviewContract.Effect.ShowError -> {
                    // TODO: Snackbar로 에러 표시
                }
            }
        }
    }

    AiReviewContent(
        state = state,
        reviewTextFieldState = viewModel.reviewTextFieldState,
        onMenuInputChanged = {
            viewModel.onEvent(AiTextReviewContract.Event.OnMenuInputChanged(it))
        },
        onModelSelected = {
            viewModel.onEvent(AiTextReviewContract.Event.OnModelSelected(it))
        },
        onGenerateClick = {
            viewModel.onEvent(AiTextReviewContract.Event.OnGenerateClick)
        },
    )
}

@Composable
private fun AiReviewContent(
    state: AiTextReviewContract.State,
    reviewTextFieldState: TextFieldState,
    onMenuInputChanged: (String) -> Unit,
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
            text = "AI 텍스트 리뷰",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cloud - ${state.selectedModel.displayName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ModelSelector(
            selectedModel = state.selectedModel,
            onModelSelected = onModelSelected,
            enabled = !state.isGenerating,
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuInputSection(
            menuInput = state.menuInput,
            isJsonMode = state.isJsonMode,
            onMenuInputChanged = onMenuInputChanged,
            enabled = !state.isGenerating,
        )

        if (state.parsedData != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ParsedDataCard(data = state.parsedData)
        }

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
            Text(text = if (state.isGenerating) "생성 중..." else "AI 리뷰 생성")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.hasGeneratedReview) {
            GeneratedReviewCard(review = state.generatedReview)
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
private fun MenuInputSection(
    menuInput: String,
    isJsonMode: Boolean,
    onMenuInputChanged: (String) -> Unit,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = menuInput,
        onValueChange = onMenuInputChanged,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text("메뉴 정보") },
        placeholder = { Text("JSON 또는 텍스트로 메뉴 정보를 입력하세요") },
        minLines = 3,
        maxLines = 8,
        shape = RoundedCornerShape(12.dp),
        supportingText = if (menuInput.isNotBlank()) {
            {
                Text(
                    text = if (isJsonMode) "JSON 감지됨 - 구조화된 프롬프트 사용" else "텍스트 모드 - 입력 내용 그대로 사용",
                    color = if (isJsonMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            null
        },
    )
}

@Composable
private fun ParsedDataCard(data: ReviewRequestData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "추출된 정보",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "가게명: ${data.shopName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "메뉴: ${data.menuName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "최대 글자수: ${data.maxReviewLength}자",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
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
private fun GeneratedReviewCard(
    review: String,
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
                text = "AI 생성 리뷰",
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