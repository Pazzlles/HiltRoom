package com.example.hiltroom.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.hiltroom.ui.detail.POST_DETAIL_BODY_TAG
import com.example.hiltroom.ui.detail.POST_DETAIL_TITLE_TAG
import com.example.hiltroom.ui.detail.PostDetailBodyUiModel
import com.example.hiltroom.ui.detail.PostDetailContentState
import com.example.hiltroom.ui.detail.PostDetailEvent
import com.example.hiltroom.ui.detail.PostDetailScreen
import com.example.hiltroom.ui.detail.PostDetailUiState
import com.example.hiltroom.ui.detail.PostPropertyUiModel
import com.example.hiltroom.ui.list.POST_LIST_CARD_TAG_PREFIX
import com.example.hiltroom.ui.list.PostCardUiModel
import com.example.hiltroom.ui.list.PostListContentState
import com.example.hiltroom.ui.list.PostListEvent
import com.example.hiltroom.ui.list.PostListScreen
import com.example.hiltroom.ui.list.PostListUiState

internal const val TEST_POST_ID = "8"
internal const val TEST_SUMMARY = "Recovered summary"
internal const val TEST_ROOM_NOTE = "Room note"
internal const val TEST_RECOVERED_TITLE = "Recovered post"
internal const val TEST_DETAIL_TITLE = "Post #8"
internal const val TEST_DETAIL_BODY = "Detail body"

@Composable
internal fun RetryStateHost() {
    var state by remember {
        mutableStateOf(
            PostListUiState(
                content = PostListContentState.Error(
                    message = "Retry error message",
                ),
            ),
        )
    }

    PostListScreen(
        state = state,
        onEvent = { event ->
            if (event == PostListEvent.RetryClicked) {
                state = PostListUiState(
                    content = PostListContentState.Success(
                        summary = TEST_SUMMARY,
                        roomNote = TEST_ROOM_NOTE,
                        items = listOf(
                            PostCardUiModel(
                                id = "5",
                                title = TEST_RECOVERED_TITLE,
                                subtitle = "Post #5 · User #5",
                                bodyPreview = "Recovered preview",
                            ),
                        ),
                    ),
                )
            }
        },
    )
}

@Composable
internal fun ListDetailStateHost() {
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    if (selectedPostId == null) {
        PostListScreen(
            state = PostListUiState(
                content = PostListContentState.Success(
                    summary = TEST_SUMMARY,
                    roomNote = TEST_ROOM_NOTE,
                    items = listOf(
                        PostCardUiModel(
                            id = TEST_POST_ID,
                            title = "List item",
                            subtitle = "Post #8 · User #3",
                            bodyPreview = "Preview body",
                        ),
                    ),
                ),
            ),
            onEvent = { event ->
                if (event is PostListEvent.PostClicked) {
                    selectedPostId = event.postId
                }
            },
        )
    } else {
        PostDetailScreen(
            state = PostDetailUiState(
                title = TEST_DETAIL_TITLE,
                content = PostDetailContentState.Success(
                    post = PostDetailBodyUiModel(
                        title = "Detail title",
                        body = TEST_DETAIL_BODY,
                        properties = listOf(
                            PostPropertyUiModel("ID поста", selectedPostId.orEmpty()),
                            PostPropertyUiModel("ID автора", "3"),
                        ),
                    ),
                    sourceNote = "Details from network",
                ),
            ),
            onEvent = { event ->
                if (event == PostDetailEvent.BackClicked) {
                    selectedPostId = null
                }
            },
        )
    }
}

internal fun postCardTag(postId: String): String = "$POST_LIST_CARD_TAG_PREFIX$postId"

