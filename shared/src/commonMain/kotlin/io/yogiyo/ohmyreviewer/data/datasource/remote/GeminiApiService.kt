package io.yogiyo.ohmyreviewer.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.yogiyo.ohmyreviewer.data.datasource.remote.model.GeminiRequest
import io.yogiyo.ohmyreviewer.data.datasource.remote.model.GeminiResponse

class GeminiApiService(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL
) {
    suspend fun generateContent(request: GeminiRequest): GeminiResponse {
        return httpClient.post("$BASE_URL/v1beta/models/$model:generateContent") {
            url {
                parameters.append("key", apiKey)
            }
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com"
        const val DEFAULT_MODEL = "gemini-2.0-flash"
    }
}
