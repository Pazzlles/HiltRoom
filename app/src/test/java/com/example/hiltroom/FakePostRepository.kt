package com.example.hiltroom

import com.example.hiltroom.data.PostDetailResult
import com.example.hiltroom.data.PostListResult
import com.example.hiltroom.data.PostRepository

class FakePostRepository : PostRepository {
    var restoreQueryHandler: suspend () -> String? = { null }
    var postsHandler: suspend (String) -> PostListResult = { query ->
        error("postsHandler is not configured for query=$query")
    }
    var detailHandler: suspend (String) -> PostDetailResult = { postId ->
        error("detailHandler is not configured for postId=$postId")
    }

    val postQueries = mutableListOf<String>()
    val detailIds = mutableListOf<String>()

    override suspend fun getLastSuccessfulSearchQuery(): String? {
        return restoreQueryHandler()
    }

    override suspend fun getPosts(query: String): PostListResult {
        postQueries += query
        return postsHandler(query)
    }

    override suspend fun getPostDetail(postId: String): PostDetailResult {
        detailIds += postId
        return detailHandler(postId)
    }
}
