package com.example.hiltroom.ui.list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiltroom.data.PostDataSource
import com.example.hiltroom.data.PostListResult
import com.example.hiltroom.data.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {
    var uiState by mutableStateOf(PostListUiState())
        private set

    private var lastSubmittedQuery: String = ""
    private var activeLoadJob: Job? = null
    private var latestRequestId: Long = 0

    init {
        restoreLastSuccessfulQuery()
    }

    fun onEvent(event: PostListEvent) {
        when (event) {
            is PostListEvent.QueryChanged -> {
                uiState = uiState.copy(searchQuery = event.value)
            }

            PostListEvent.SearchSubmitted -> {
                val query = uiState.searchQuery.trim()
                lastSubmittedQuery = query
                loadPosts(query = query)
            }

            PostListEvent.ClearSearchClicked -> {
                lastSubmittedQuery = ""
                uiState = uiState.copy(searchQuery = "")
                loadPosts(query = "")
            }

            PostListEvent.RetryClicked -> {
                loadPosts(query = lastSubmittedQuery)
            }

            is PostListEvent.PostClicked -> Unit
        }
    }

    private fun restoreLastSuccessfulQuery() {
        viewModelScope.launch {
            val restoredQuery = try {
                repository.getLastSuccessfulSearchQuery().orEmpty()
            } catch (throwable: Throwable) {
                Log.w(TAG, "Failed to restore last successful query", throwable)
                ""
            }

            lastSubmittedQuery = restoredQuery
            uiState = uiState.copy(searchQuery = restoredQuery)
            loadPosts(query = restoredQuery)
        }
    }

    private fun loadPosts(query: String) {
        activeLoadJob?.cancel()
        val requestId = ++latestRequestId

        activeLoadJob = viewModelScope.launch {
            uiState = uiState.copy(content = PostListContentState.Loading)

            try {
                val result = repository.getPosts(query)
                if (requestId != latestRequestId) return@launch

                uiState = uiState.copy(content = result.toContentState(query = query))
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                if (requestId != latestRequestId) return@launch

                Log.e(TAG, "Failed to load posts for query=$query", throwable)
                uiState = uiState.copy(
                    content = PostListContentState.Error(
                        message = throwable.toUserMessage(),
                    ),
                )
            }
        }
    }

    companion object {
        private const val TAG = "PostListViewModel"
    }
}

private fun PostListResult.toContentState(query: String): PostListContentState {
    val roomNote = source.toRoomNote(query)

    if (posts.isEmpty()) {
        return PostListContentState.Empty(
            title = "Ничего не найдено",
            message = if (query.isBlank()) {
                "Для общего запроса список оказался пустым."
            } else {
                "По userId=$query ничего не найдено."
            },
            roomNote = roomNote,
        )
    }

    val summary = if (query.isBlank()) {
        "Найдено постов: ${posts.size}"
    } else {
        "Найдено постов для userId=$query: ${posts.size}"
    }

    return PostListContentState.Success(
        summary = summary,
        roomNote = roomNote,
        items = posts.map { post ->
            PostCardUiModel(
                id = post.id.toString(),
                title = post.title,
                subtitle = "Post #${post.id} · User #${post.userId}",
                bodyPreview = post.preview,
            )
        },
    )
}

private fun Throwable.toUserMessage(): String {
    return when (this) {
        is IOException -> "Проверьте подключение к интернету и попробуйте снова."
        is IllegalArgumentException -> message ?: "Введите userId числом."
        is HttpException -> "Сервер вернул ошибку ${code()}."
        else -> message ?: "Не удалось загрузить список постов."
    }
}

private fun PostDataSource.toRoomNote(query: String): String {
    val queryLabel = if (query.isBlank()) {
        "для общего списка"
    } else {
        "для userId=$query"
    }

    return when (this) {
        PostDataSource.Network -> {
            "Данные пришли из сети. Room сохранил кэш последнего успешного поиска $queryLabel, чтобы его можно было открыть офлайн после перезапуска."
        }

        PostDataSource.RoomCache -> {
            "Сеть сейчас недоступна, поэтому показан Room-кэш последнего успешного поиска $queryLabel."
        }
    }
}
