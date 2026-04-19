package com.example.hiltroom.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class PostListItem(
    val id: Int,
    val userId: Int,
    val title: String,
    val preview: String,
    val body: String,
)
