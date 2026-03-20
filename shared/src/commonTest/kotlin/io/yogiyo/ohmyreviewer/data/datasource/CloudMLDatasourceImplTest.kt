package io.yogiyo.ohmyreviewer.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.yogiyo.ohmyreviewer.data.datasource.remote.GeminiApiService
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CloudMLDatasourceImplTest {

    private class FakePlatformImage : PlatformImage {
        override val image: Any = byteArrayOf(1, 2, 3)
        override fun toByteArray(): ByteArray = byteArrayOf(1, 2, 3)
    }

    private fun createMockHttpClient(responseBody: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = responseBody,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun generateImageDescription_returnsDescriptionOnSuccess() = runTest {
        val responseJson = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "이 이미지는 맛있는 비빔밥입니다."
                        }]
                    }
                }]
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(responseJson)
        val apiService = GeminiApiService(httpClient, apiKey = "test-key")
        val datasource = CloudMLDatasourceImpl(apiService)

        val result = datasource.generateImageDescription(FakePlatformImage()).toList()

        assertEquals(listOf("이 이미지는 맛있는 비빔밥입니다."), result)
    }

    @Test
    fun generateImageDescription_throwsOnEmptyResponse() = runTest {
        val responseJson = """
            {
                "candidates": []
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(responseJson)
        val apiService = GeminiApiService(httpClient, apiKey = "test-key")
        val datasource = CloudMLDatasourceImpl(apiService)

        assertFailsWith<GeminiApiException> {
            datasource.generateImageDescription(FakePlatformImage()).toList()
        }
    }

    @Test
    fun generateImageDescription_throwsOnErrorResponse() = runTest {
        val responseJson = """
            {
                "error": {
                    "code": 400,
                    "message": "Invalid API key"
                }
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(responseJson)
        val apiService = GeminiApiService(httpClient, apiKey = "invalid-key")
        val datasource = CloudMLDatasourceImpl(apiService)

        val exception = assertFailsWith<GeminiApiException> {
            datasource.generateImageDescription(FakePlatformImage()).toList()
        }
        assertEquals(400, exception.code)
        assertEquals("Invalid API key", exception.message)
    }

    @Test
    fun generateImageDescription_throwsOnNullCandidates() = runTest {
        val responseJson = """
            {
                "candidates": null
            }
        """.trimIndent()

        val httpClient = createMockHttpClient(responseJson)
        val apiService = GeminiApiService(httpClient, apiKey = "test-key")
        val datasource = CloudMLDatasourceImpl(apiService)

        assertFailsWith<GeminiApiException> {
            datasource.generateImageDescription(FakePlatformImage()).toList()
        }
    }

    @Test
    fun generateImageDescription_requestContainsBase64EncodedImage() = runTest {
        var capturedBody: String? = null
        val mockEngine = MockEngine { request ->
            capturedBody = request.body.toString()
            respond(
                content = """{"candidates":[{"content":{"parts":[{"text":"ok"}]}}]}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val apiService = GeminiApiService(httpClient, apiKey = "test-key")
        val datasource = CloudMLDatasourceImpl(apiService)

        datasource.generateImageDescription(FakePlatformImage()).toList()

        assertTrue(capturedBody != null)
    }
}
