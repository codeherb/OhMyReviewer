package io.yogiyo.ohmyreviewer.data.repository

import io.yogiyo.ohmyreviewer.data.datasource.CloudMLDatasource
import io.yogiyo.ohmyreviewer.data.datasource.MLDatasource
import io.yogiyo.ohmyreviewer.data.model.ModelStatus
import io.yogiyo.ohmyreviewer.data.model.PlatformImage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        private val contentResult: String = "generated content",
        private val imageDescriptionResult: String = "image description"
    ) : MLDatasource {
        override val downloadProgress: StateFlow<Float> = MutableStateFlow(1f)

        override fun initialize(): Deferred<ModelStatus> =
            CompletableDeferred(ModelStatus.READY)

        override fun initializeImageDescription(): Deferred<ModelStatus> =
            CompletableDeferred(ModelStatus.READY)

        override fun generateContent(prompt: String): Flow<String> = flow {
            emit(contentResult)
        }

        override fun generateImageDescription(image: PlatformImage): Flow<String> = flow {
            emit(imageDescriptionResult)
        }

        override fun close(): Deferred<Unit> = CompletableDeferred(Unit)
    }

    private class FakeCloudMLDatasource(
        private val result: String? = null,
        private val error: Throwable? = null
    ) : CloudMLDatasource {
        override fun generateImageDescription(image: PlatformImage): Flow<String> = flow {
            if (error != null) throw error
            emit(result ?: "cloud image description")
        }
    }

    @Test
    fun generateContent_delegatesToAiDatasource() = runTest {
        val repository = MLRepositoryImpl(
            aiDatasource = FakeMLDatasource(contentResult = "test content"),
            cloudDatasource = FakeCloudMLDatasource()
        )

        val result = repository.generateContent("prompt").toList()

        assertEquals(listOf("test content"), result)
    }

    @Test
    fun generateImageDescription_delegatesToAiDatasource() = runTest {
        val repository = MLRepositoryImpl(
            aiDatasource = FakeMLDatasource(imageDescriptionResult = "local description"),
            cloudDatasource = FakeCloudMLDatasource()
        )

        val result = repository.generateImageDescription(FakePlatformImage()).toList()

        assertEquals(listOf("local description"), result)
    }

    @Test
    fun generateCloudImageDescription_delegatesToCloudDatasource() = runTest {
        val repository = MLRepositoryImpl(
            aiDatasource = FakeMLDatasource(),
            cloudDatasource = FakeCloudMLDatasource(result = "cloud description")
        )

        val result = repository.generateCloudImageDescription(FakePlatformImage()).toList()

        assertEquals(listOf("cloud description"), result)
    }

    @Test
    fun generateCloudImageDescription_propagatesError() = runTest {
        val repository = MLRepositoryImpl(
            aiDatasource = FakeMLDatasource(),
            cloudDatasource = FakeCloudMLDatasource(error = RuntimeException("API error"))
        )

        assertFailsWith<RuntimeException> {
            repository.generateCloudImageDescription(FakePlatformImage()).toList()
        }
    }
}
