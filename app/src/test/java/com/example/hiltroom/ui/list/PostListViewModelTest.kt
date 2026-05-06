package com.example.hiltroom.ui.list

import com.example.hiltroom.FakePostRepository
import com.example.hiltroom.MainDispatcherRule
import com.example.hiltroom.data.PostDataSource
import com.example.hiltroom.data.PostListResult
import com.example.hiltroom.data.model.PostListItem
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_isLoadingWithEmptyQueryBeforeAsyncRestoreCompletes() {
        val repository = FakePostRepository().apply {
            postsHandler = { PostListResult(posts = emptyList(), source = PostDataSource.Network) }
        }

        val viewModel = PostListViewModel(repository)

        assertEquals("", viewModel.uiState.searchQuery)
        assertTrue(viewModel.uiState.content is PostListContentState.Loading)
    }

    @Test
    fun init_restoresLastSuccessfulQueryAndLoadsPosts() = runTest {
        val repository = FakePostRepository().apply {
            restoreQueryHandler = { "3" }
            postsHandler = { query ->
                PostListResult(
                    posts = listOf(samplePost(id = 31, userId = query.toInt(), title = "Restored")),
                    source = PostDataSource.Network,
                )
            }
        }

        val viewModel = PostListViewModel(repository)
        advanceUntilIdle()

        assertEquals("3", viewModel.uiState.searchQuery)
        assertEquals(listOf("3"), repository.postQueries)
        assertTrue(viewModel.uiState.content is PostListContentState.Success)
    }

    @Test
    fun searchSubmitted_loadsSuccessState() = runTest {
        val repository = FakePostRepository().apply {
            postsHandler = { query ->
                PostListResult(
                    posts = listOf(samplePost(id = 2, userId = query.toInt(), title = "Loaded")),
                    source = PostDataSource.Network,
                )
            }
        }

        val viewModel = PostListViewModel(repository)
        advanceUntilIdle()
        repository.postQueries.clear()

        viewModel.onEvent(PostListEvent.QueryChanged("2"))
        viewModel.onEvent(PostListEvent.SearchSubmitted)
        advanceUntilIdle()

        val content = viewModel.uiState.content
        assertEquals(listOf("2"), repository.postQueries)
        assertTrue(content is PostListContentState.Success)
        content as PostListContentState.Success
        assertEquals("Loaded", content.items.single().title)
    }

    @Test
    fun searchError_showsErrorState() = runTest {
        val repository = FakePostRepository().apply {
            postsHandler = { throw IOException("offline") }
        }

        val viewModel = PostListViewModel(repository)
        advanceUntilIdle()
        repository.postQueries.clear()

        viewModel.onEvent(PostListEvent.QueryChanged("7"))
        viewModel.onEvent(PostListEvent.SearchSubmitted)
        advanceUntilIdle()

        assertEquals(listOf("7"), repository.postQueries)
        assertTrue(viewModel.uiState.content is PostListContentState.Error)
    }

    @Test
    fun retryAfterError_restartsTheSameRequestAndRecovers() = runTest {
        val repository = FakePostRepository()
        var attempt = 0
        repository.postsHandler = { query ->
            attempt += 1
            if (attempt == 1) {
                throw IOException("offline")
            }
            PostListResult(
                posts = listOf(samplePost(id = 5, userId = query.toInt(), title = "Recovered")),
                source = PostDataSource.Network,
            )
        }

        val viewModel = PostListViewModel(repository)
        advanceUntilIdle()
        repository.postQueries.clear()
        attempt = 0

        viewModel.onEvent(PostListEvent.QueryChanged("5"))
        viewModel.onEvent(PostListEvent.SearchSubmitted)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.content is PostListContentState.Error)

        viewModel.onEvent(PostListEvent.RetryClicked)
        advanceUntilIdle()

        val content = viewModel.uiState.content
        assertEquals(listOf("5", "5"), repository.postQueries)
        assertTrue(content is PostListContentState.Success)
        content as PostListContentState.Success
        assertEquals("Recovered", content.items.single().title)
    }

    @Test
    fun emptySearchResult_becomesEmptyStateInsteadOfSuccess() = runTest {
        val repository = FakePostRepository().apply {
            postsHandler = {
                PostListResult(
                    posts = emptyList(),
                    source = PostDataSource.Network,
                )
            }
        }

        val viewModel = PostListViewModel(repository)
        advanceUntilIdle()
        repository.postQueries.clear()

        viewModel.onEvent(PostListEvent.QueryChanged("9"))
        viewModel.onEvent(PostListEvent.SearchSubmitted)
        advanceUntilIdle()

        val content = viewModel.uiState.content
        assertEquals(listOf("9"), repository.postQueries)
        assertTrue(content is PostListContentState.Empty)
    }

    private fun samplePost(
        id: Int,
        userId: Int,
        title: String,
    ): PostListItem {
        return PostListItem(
            id = id,
            userId = userId,
            title = title,
            preview = "Preview for $title",
            body = "Body for $title",
        )
    }
}
