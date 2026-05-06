package com.example.hiltroom.data

import com.example.hiltroom.data.remote.PostApiService
import com.example.hiltroom.data.remote.dto.PostResponseDto

class FakePostApiService : PostApiService {
    var listFailure: Throwable? = null
    var detailFailure: Throwable? = null
    var allPosts: List<PostResponseDto> = emptyList()
    val postsByUserId: MutableMap<Int, List<PostResponseDto>> = mutableMapOf()
    val detailsById: MutableMap<Int, PostResponseDto> = mutableMapOf()

    override suspend fun getAllPosts(): List<PostResponseDto> {
        listFailure?.let { throw it }
        return allPosts
    }

    override suspend fun getPostsByUserId(userId: Int): List<PostResponseDto> {
        listFailure?.let { throw it }
        return postsByUserId[userId].orEmpty()
    }

    override suspend fun getPostDetail(id: Int): PostResponseDto {
        detailFailure?.let { throw it }
        return detailsById[id] ?: error("No detail configured for id=$id")
    }
}
