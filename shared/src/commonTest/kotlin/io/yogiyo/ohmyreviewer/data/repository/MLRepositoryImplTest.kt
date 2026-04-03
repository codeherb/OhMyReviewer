package io.yogiyo.ohmyreviewer.data.repository

import io.yogiyo.ohmyreviewer.data.datasource.CloudMLDatasource
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.model.GeminiModel
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MLRepositoryImplTest {

    private class FakePlatformImage(private val bytes: ByteArray = byteArrayOf(1, 2, 3)) : PlatformImage {
        override val image: Any = bytes
        override fun toByteArray(): ByteArray = bytes
    }

    private class FakeMLDatasource(
        private val imageDescriptionResult: String = "image description"
    ) : MLDatasource {
        override val downloadProgress: kotlinx.coroutines.flow.StateFlow<Float> =
            kotlinx.coroutines.flow.MutableStateFlow(0f)

        override fun initializeImageDescription(): kotlinx.coroutines.Deferred<io.yogiyo.ohmyreviewer.data.model.ModelStatus> =
            kotlinx.coroutines.CompletableDeferred(io.yogiyo.ohmyreviewer.data.model.ModelStatus.SUCCESS)

        override fun generateImageDescription(image: PlatformImage): Flow<String> = flow {
            emit(imageDescriptionResult)
        }
    }

    private class FakeCloudMLDatasource(
        private val result: String? = null,
        private val error: Throwable? = null
    ) : CloudMLDatasource {
        override suspend fun generateImageDescription(model: GeminiModel, image: PlatformImage): String {
            if (error != null) throw error
            return result ?: "cloud image description"
        }

        override suspend fun generateTextReview(model: GeminiModel, prompt: String): String {
            if (error != null) throw error
            return result ?: "cloud text review"
        }

        override suspend fun generateImageReview(model: GeminiModel, image: PlatformImage, prompt: String): String {
            if (error != null) throw error
            return result ?: "cloud image review"
        }
    }

    @Test
    fun generateImageDescription_delegatesToMLDatasource() = runTest {
        val repository = MLRepositoryImpl(
            datasource = FakeMLDatasource(imageDescriptionResult = "local description"),
            cloudDatasource = FakeCloudMLDatasource()
        )

        val result = repository.generateImageDescription(FakePlatformImage()).toList()

        assertEquals(listOf("local description"), result)
    }

    @Test
    fun generateCloudImageDescription_delegatesToCloudDatasource() = runTest {
        val repository = MLRepositoryImpl(
            datasource = FakeMLDatasource(),
            cloudDatasource = FakeCloudMLDatasource(result = "cloud description")
        )

        val result = repository.generateCloudImageDescription(GeminiModel.DEFAULT, FakePlatformImage())

        assertEquals("cloud description", result)
    }

    @Test
    fun generateCloudImageDescription_propagatesError() = runTest {
        val repository = MLRepositoryImpl(
            datasource = FakeMLDatasource(),
            cloudDatasource = FakeCloudMLDatasource(error = RuntimeException("API error"))
        )

        assertFailsWith<RuntimeException> {
            repository.generateCloudImageDescription(GeminiModel.DEFAULT, FakePlatformImage())
        }
    }

    @Test
    fun generateTextReview_delegatesToCloudDatasource() = runTest {
        val repository = MLRepositoryImpl(
            datasource = FakeMLDatasource(),
            cloudDatasource = FakeCloudMLDatasource(result = "text review")
        )

        val result = repository.generateTextReview(GeminiModel.DEFAULT, "prompt")

        assertEquals("text review", result)
    }

    @Test
    fun generateReview_delegatesToCloudDatasource() = runTest {
        val repository = MLRepositoryImpl(
            datasource = FakeMLDatasource(),
            cloudDatasource = FakeCloudMLDatasource(result = "image review")
        )

        val result = repository.generateReview(GeminiModel.DEFAULT, FakePlatformImage(), "prompt")

        assertEquals("image review", result)
    }
}
