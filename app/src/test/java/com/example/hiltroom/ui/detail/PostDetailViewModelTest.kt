package com.example.hiltroom.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.hiltroom.FakePostRepository
import com.example.hiltroom.MainDispatcherRule
import com.example.hiltroom.data.PostDataSource
import com.example.hiltroom.data.PostDetailResult
import com.example.hiltroom.data.model.PostDetail
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun blankPostId_showsErrorImmediately() {
        val repository = FakePostRepository()

        val viewModel = PostDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("postId" to "")),
            repository = repository,
        )

        assertTrue(viewModel.uiState.content is PostDetailContentState.Error)
        assertTrue(repository.detailIds.isEmpty())
    }

    @Test
    fun retryAfterError_requestsTheSamePostAgainAndRecovers() = runTest {
        val repository = FakePostRepository()
        var attempt = 0
        repository.detailHandler = { postId ->
            attempt += 1
            if (attempt == 1) {
                throw IOException("offline")
            }
            PostDetailResult(
                post = PostDetail(
                    id = postId.toInt(),
                    userId = 4,
                    title = "Recovered detail",
                    body = "Recovered body",
                ),
                source = PostDataSource.Network,
            )
        }

        val viewModel = PostDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("postId" to "4")),
            repository = repository,
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.content is PostDetailContentState.Error)

        viewModel.onEvent(PostDetailEvent.RetryClicked)
        advanceUntilIdle()

        val content = viewModel.uiState.content
        assertEquals(listOf("4", "4"), repository.detailIds)
        assertEquals("Post #4", viewModel.uiState.title)
        assertTrue(content is PostDetailContentState.Success)
        content as PostDetailContentState.Success
        assertEquals("Recovered detail", content.post.title)
    }
}
