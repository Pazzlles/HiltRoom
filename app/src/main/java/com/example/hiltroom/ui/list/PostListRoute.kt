package com.example.hiltroom.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PostListRoute(
    onOpenPost: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PostListScreen(
        state = state,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                is PostListEvent.PostClicked -> onOpenPost(event.postId)
                else -> viewModel.onEvent(event)
            }
        },
    )
}
