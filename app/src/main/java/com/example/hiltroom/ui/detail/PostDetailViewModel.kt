package com.example.hiltroom.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiltroom.data.PostDataSource
import com.example.hiltroom.data.PostRepository
import com.example.hiltroom.data.model.PostDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val _uiState = MutableStateFlow(PostDetailUiState(postId = postId))
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    private var activeLoadJob: Job? = null
    private var latestRequestId: Long = 0

    init {
        if (postId.isBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    content = PostDetailContentState.Error(
                        message = "Не удалось открыть экран: отсутствует id поста в маршруте.",
                    ),
                )
            }
        } else {
            loadPost()
        }
    }

    fun onEvent(event: PostDetailEvent) {
        if (event is PostDetailEvent.RetryClicked) {
            if (postId.isBlank()) {
                _uiState.update { currentState ->
                    currentState.copy(
                        content = PostDetailContentState.Error(
                            message = "Не удалось повторить запрос: отсутствует id поста.",
                        ),
                    )
                }
            } else {
                loadPost()
            }
        }
    }

    private fun loadPost() {
        activeLoadJob?.cancel()
        val requestId = ++latestRequestId

        activeLoadJob = viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(content = PostDetailContentState.Loading)
            }

            try {
                val result = repository.getPostDetail(postId)
                if (requestId != latestRequestId) return@launch

                _uiState.update { currentState ->
                    currentState.copy(
                        title = "Post #${result.post.id}",
                        content = PostDetailContentState.Success(
                            post = result.post.toUiModel(),
                            sourceNote = result.source.toSourceNote(),
                        ),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                if (requestId != latestRequestId) return@launch

                Log.e(TAG, "Failed to load post detail for id=$postId", throwable)
                _uiState.update { currentState ->
                    currentState.copy(
                        content = PostDetailContentState.Error(
                            message = throwable.toUserMessage(),
                        ),
                    )
                }
            }
        }
    }

    companion object {
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
            "Детали загружены из сети. Room хранит данные последнего успешного поиска и может подстраховать при сбое сети."
        }

        PostDataSource.RoomCache -> {
            "Сеть недоступна, поэтому детали открыты из Room-кэша последнего успешного поиска."
        }
    }
}
