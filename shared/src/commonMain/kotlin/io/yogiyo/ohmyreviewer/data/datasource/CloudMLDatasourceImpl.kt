package io.yogiyo.ohmyreviewer.data.datasource

import io.yogiyo.ohmyreviewer.data.datasource.remote.GeminiApiService
import io.yogiyo.ohmyreviewer.data.datasource.remote.model.Content
import io.yogiyo.ohmyreviewer.data.datasource.remote.model.GeminiRequest
import io.yogiyo.ohmyreviewer.data.datasource.remote.model.InlineData
import io.yogiyo.ohmyreviewer.data.datasource.remote.model.Part
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CloudMLDatasourceImpl(
    private val geminiApiService: GeminiApiService
) : CloudMLDatasource {

    @OptIn(ExperimentalEncodingApi::class)
    override fun generateImageDescription(model: GeminiModel, image: PlatformImage): Flow<String> = flow {
        val request = buildImageRequest(image, IMAGE_DESCRIPTION_PROMPT)
        emit(executeRequest(model, request))
    }

    override fun generateTextReview(model: GeminiModel, prompt: String): Flow<String> = flow {
        val request = buildTextRequest(prompt)
        emit(executeRequest(model, request))
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun generateImageReview(model: GeminiModel, image: PlatformImage, prompt: String): Flow<String> = flow {
        val request = buildImageRequest(image, prompt)
        emit(executeRequest(model, request))
    }

    private fun buildTextRequest(prompt: String): GeminiRequest =
        GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            )
        )

    @OptIn(ExperimentalEncodingApi::class)
    private fun buildImageRequest(image: PlatformImage, prompt: String): GeminiRequest {
        val base64Image = Base64.encode(image.toByteArray())
        return GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = base64Image
                            )
                        ),
                        Part(text = prompt)
                    )
                )
            )
        )
    }

    private suspend fun executeRequest(model: GeminiModel, request: GeminiRequest): String {
        val response = geminiApiService.generateContent(model.modelId, request)

        val error = response.error
        if (error != null) {
            throw GeminiApiException(
                code = error.code ?: -1,
                message = error.message ?: "Unknown error"
            )
        }

        return response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: throw GeminiApiException(code = -1, message = "Empty response from Gemini API")
    }

    companion object {
        const val IMAGE_DESCRIPTION_PROMPT = "이 이미지를 자세히 설명해주세요. 음식 사진이라면 음식의 종류, 외관, 플레이팅을 중심으로 설명해주세요."
    }
}

class GeminiApiException(val code: Int, override val message: String) : Exception(message)
