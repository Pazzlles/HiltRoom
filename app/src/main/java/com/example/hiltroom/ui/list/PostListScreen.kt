package com.example.hiltroom.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Immutable
data class PostListUiState(
    val title: String = "Posts Explorer",
    val searchQuery: String = "",
    val content: PostListContentState = PostListContentState.Loading,
)

@Immutable
sealed interface PostListContentState {
    data object Loading : PostListContentState

    data class Error(
        val message: String,
    ) : PostListContentState

    data class Empty(
        val title: String,
        val message: String,
        val roomNote: String,
    ) : PostListContentState

    data class Success(
        val summary: String,
        val roomNote: String,
        val items: List<PostCardUiModel>,
    ) : PostListContentState
}

@Immutable
data class PostCardUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val bodyPreview: String,
)

sealed interface PostListEvent {
    data class QueryChanged(val value: String) : PostListEvent
    data object SearchSubmitted : PostListEvent
    data object ClearSearchClicked : PostListEvent
    data object RetryClicked : PostListEvent
    data class PostClicked(val postId: String) : PostListEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    state: PostListUiState,
    onEvent: (PostListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(PostListEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Фильтр по userId") },
                placeholder = { Text("Введите userId числом, например 1") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onEvent(PostListEvent.SearchSubmitted)
                    },
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onEvent(PostListEvent.SearchSubmitted) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Искать")
                }

                TextButton(
                    onClick = { onEvent(PostListEvent.ClearSearchClicked) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Сбросить")
                }
            }

            when (val content = state.content) {
                PostListContentState.Loading -> {
                    PostListMessageState(
                        title = "Загрузка списка...",
                        message = "Получаем данные из JSONPlaceholder API.",
                        modifier = Modifier.weight(1f),
                    )
                }

                is PostListContentState.Error -> {
                    PostListErrorState(
                        message = content.message,
                        onRetry = { onEvent(PostListEvent.RetryClicked) },
                        modifier = Modifier.weight(1f),
                    )
                }

                is PostListContentState.Empty -> {
                    PostListMessageState(
                        title = content.title,
                        message = content.message,
                        roomNote = content.roomNote,
                        modifier = Modifier.weight(1f),
                    )
                }

                is PostListContentState.Success -> {
                    PostListContent(
                        summary = content.summary,
                        roomNote = content.roomNote,
                        items = content.items,
                        onPostClick = { postId ->
                            onEvent(PostListEvent.PostClicked(postId))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PostListContent(
    summary: String,
    roomNote: String,
    items: List<PostCardUiModel>,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RoomUsageCard(text = roomNote)
            }
        }

        items(
            items = items,
            key = { item -> item.id },
        ) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPostClick(item.id) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = item.bodyPreview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PostListMessageState(
    title: String,
    message: String,
    roomNote: String = "",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (roomNote.isNotBlank()) {
                RoomUsageCard(text = roomNote)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PostListErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun RoomUsageCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
