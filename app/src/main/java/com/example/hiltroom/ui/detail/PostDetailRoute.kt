package com.example.hiltroom.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PostDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    PostDetailScreen(
        state = viewModel.uiState,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                PostDetailEvent.BackClicked -> onBack()
                PostDetailEvent.RetryClicked -> viewModel.onEvent(event)
            }
        },
    )
}
