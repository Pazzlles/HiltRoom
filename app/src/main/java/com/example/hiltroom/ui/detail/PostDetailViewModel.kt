package com.example.hiltroom.ui.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiltroom.data.PostDataSource
import com.example.hiltroom.data.PostRepository
import com.example.hiltroom.data.model.PostDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PostRepository,
) : ViewModel() {
    private val postId: String = savedStateHandle.get<String>("postId").orEmpty()

    var uiState by mutableStateOf(PostDetailUiState())
        private set

    private var activeLoadJob: Job? = null
    private var latestRequestId: Long = 0

    init {
        if (postId.isBlank()) {
            uiState = uiState.copy(
                content = PostDetailContentState.Error(
                    message = "Не удалось открыть экран: отсутствует id поста в маршруте.",
                ),
            )
        } else {
            loadPost()
        }
    }

    fun onEvent(event: PostDetailEvent) {
        if (event is PostDetailEvent.RetryClicked) {
            if (postId.isBlank()) {
                uiState = uiState.copy(
                    content = PostDetailContentState.Error(
                        message = "Не удалось повторить запрос: отсутствует id поста.",
                    ),
                )
            } else {
                loadPost()
            }
        }
    }

    private fun loadPost() {
        activeLoadJob?.cancel()
        val requestId = ++latestRequestId

        activeLoadJob = viewModelScope.launch {
            uiState = uiState.copy(content = PostDetailContentState.Loading)

            try {
                val result = repository.getPostDetail(postId)
                if (requestId != latestRequestId) return@launch

                uiState = uiState.copy(
                    title = "Post #${result.post.id}",
                    content = PostDetailContentState.Success(
                        post = result.post.toUiModel(),
                        sourceNote = result.source.toSourceNote(),
                    ),
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                if (requestId != latestRequestId) return@launch

                Log.e(TAG, "Failed to load post detail for id=$postId", throwable)
                uiState = uiState.copy(
                    content = PostDetailContentState.Error(
                        message = throwable.toUserMessage(),
                    ),
                )
            }
        }
    }

    private companion object {
        private const val TAG = "PostDetailViewModel"
    }
}

private fun PostDetail.toUiModel(): PostDetailBodyUiModel {
    val properties = buildList {
        add(PostPropertyUiModel(label = "ID поста", value = id.toString()))
        add(PostPropertyUiModel(label = "ID автора", value = userId.toString()))
    }

    return PostDetailBodyUiModel(
        title = title,
        body = body,
        properties = properties,
    )
}

private fun Throwable.toUserMessage(): String {
    return when (this) {
        is IOException -> "Проверьте подключение к интернету и попробуйте снова."
        is HttpException -> "Сервер вернул ошибку ${code()}."
        else -> message ?: "Не удалось загрузить детали поста."
    }
}

private fun PostDataSource.toSourceNote(): String {
    return when (this) {
        PostDataSource.Network -> {
            "Детали загружены из сети. Этот пост уже есть в Room-кэше последнего успешного поиска, поэтому его можно открыть офлайн."
        }

        PostDataSource.RoomCache -> {
            "Сеть недоступна, поэтому детали открыты из Room-кэша последнего успешного поиска."
        }
    }
}
