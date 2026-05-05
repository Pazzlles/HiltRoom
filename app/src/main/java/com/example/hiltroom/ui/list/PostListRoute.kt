package com.example.hiltroom.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PostListRoute(
    onOpenPost: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostListViewModel = hiltViewModel(),
) {
    PostListScreen(
        state = viewModel.uiState,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                is PostListEvent.PostClicked -> onOpenPost(event.postId)
                else -> viewModel.onEvent(event)
            }
        },
    )
}
