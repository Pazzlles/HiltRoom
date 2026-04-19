package com.example.hiltroom.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PostDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PostDetailScreen(
        state = state,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                PostDetailEvent.BackClicked -> onBack()
                PostDetailEvent.RetryClicked -> viewModel.onEvent(event)
            }
        },
    )
}
