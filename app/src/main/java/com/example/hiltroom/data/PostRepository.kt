package com.example.hiltroom.data

import androidx.compose.runtime.Immutable
import com.example.hiltroom.data.model.PostDetail
import com.example.hiltroom.data.model.PostListItem

interface PostRepository {
    suspend fun getLastSuccessfulSearchQuery(): String?
    suspend fun getPosts(query: String): PostListResult
    suspend fun getPostDetail(postId: String): PostDetailResult
}

@Immutable
data class PostListResult(
    val posts: List<PostListItem>,
    val source: PostDataSource,
)

@Immutable
data class PostDetailResult(
    val post: PostDetail,
    val source: PostDataSource,
)

enum class PostDataSource {
    Network,
    RoomCache,
}
