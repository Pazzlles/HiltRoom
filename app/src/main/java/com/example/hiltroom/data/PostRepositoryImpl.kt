package com.example.hiltroom.data

import com.example.hiltroom.data.local.CachedPostDao
import com.example.hiltroom.data.local.CachedPostEntity
import com.example.hiltroom.data.local.SearchCacheMetadataDao
import com.example.hiltroom.data.local.SearchCacheMetadataEntity
import com.example.hiltroom.data.model.PostDetail
import com.example.hiltroom.data.model.PostListItem
import com.example.hiltroom.data.remote.PostApiService
import com.example.hiltroom.data.remote.dto.toPostDetail
import com.example.hiltroom.data.remote.dto.toPostListItemOrNull
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: PostApiService,
    private val cachedPostDao: CachedPostDao,
    private val cacheMetadataDao: SearchCacheMetadataDao,
) : PostRepository {

    override suspend fun getPosts(query: String): PostListResult {
        val normalizedQuery = query.trim()

        return try {
            val remotePosts = loadRemotePosts(normalizedQuery)
            cacheSearchResult(
                query = normalizedQuery,
                posts = remotePosts,
            )
            PostListResult(
                posts = remotePosts,
                source = PostDataSource.Network,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            val cachedPosts = loadCachedPostsForQuery(normalizedQuery)

            if (throwable.canFallbackToCache() && cachedPosts != null) {
                PostListResult(
                    posts = cachedPosts,
                    source = PostDataSource.RoomCache,
                )
            } else {
                throw throwable
            }
        }
    }

    override suspend fun getPostDetail(postId: String): PostDetailResult {
        val normalizedId = postId.trim()
        val id = normalizedId.toIntOrNull()
            ?: throw IllegalStateException("Некорректный идентификатор поста: $postId")

        return try {
            val post = apiService.getPostDetail(id).toPostDetail()
            val updatedAtMillis = System.currentTimeMillis()

            cachedPostDao.replaceQuery(
                searchQuery = DETAIL_CACHE_QUERY,
                posts = listOf(
                    post.toCachedEntity(
                        searchQuery = DETAIL_CACHE_QUERY,
                        updatedAtMillis = updatedAtMillis,
                    ),
                ),
            )

            PostDetailResult(
                post = post,
                source = PostDataSource.Network,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            val cachedPost = cachedPostDao.getPostById(id)

            if (throwable.canFallbackToCache() && cachedPost != null) {
                PostDetailResult(
                    post = cachedPost.toPostDetail(),
                    source = PostDataSource.RoomCache,
                )
            } else {
                throw throwable
            }
        }
    }

    private suspend fun loadRemotePosts(query: String): List<PostListItem> {
        val posts = if (query.isBlank()) {
            apiService.getAllPosts()
        } else {
            val userId = query.toIntOrNull()
                ?: throw IllegalArgumentException("Введите userId числом, например 1.")

            apiService.getPostsByUserId(userId = userId)
        }

        return posts
            .mapNotNull { it.toPostListItemOrNull() }
            .sortedBy { it.id }
    }

    private suspend fun cacheSearchResult(
        query: String,
        posts: List<PostListItem>,
    ) {
        val updatedAtMillis = System.currentTimeMillis()

        cachedPostDao.replaceQuery(
            searchQuery = query,
            posts = posts.map { post ->
                post.toCachedEntity(
                    searchQuery = query,
                    updatedAtMillis = updatedAtMillis,
                )
            },
        )
        cacheMetadataDao.upsert(
            SearchCacheMetadataEntity(
                query = query,
                updatedAtMillis = updatedAtMillis,
            ),
        )
    }

    private suspend fun loadCachedPostsForQuery(query: String): List<PostListItem>? {
        val metadata = cacheMetadataDao.getMetadata(query)
            ?: return null

        return cachedPostDao.getPostsByQuery(metadata.query).map { entity ->
            entity.toPostListItem()
        }
    }

    private companion object {
        private const val DETAIL_CACHE_QUERY = "__detail__"
    }
}

private fun Throwable.canFallbackToCache(): Boolean {
    return this is IOException || this is HttpException
}

private fun PostListItem.toCachedEntity(
    searchQuery: String,
    updatedAtMillis: Long,
): CachedPostEntity {
    return CachedPostEntity(
        searchQuery = searchQuery,
        postId = id,
        userId = userId,
        title = title,
        body = body,
        updatedAtMillis = updatedAtMillis,
    )
}

private fun PostDetail.toCachedEntity(
    searchQuery: String,
    updatedAtMillis: Long,
): CachedPostEntity {
    return CachedPostEntity(
        searchQuery = searchQuery,
        postId = id,
        userId = userId,
        title = title,
        body = body,
        updatedAtMillis = updatedAtMillis,
    )
}

private fun CachedPostEntity.toPostListItem(): PostListItem {
    return PostListItem(
        id = postId,
        userId = userId,
        title = title,
        preview = body
            .replace("\n", " ")
            .trim(),
        body = body,
    )
}

private fun CachedPostEntity.toPostDetail(): PostDetail {
    return PostDetail(
        id = postId,
        userId = userId,
        title = title,
        body = body,
    )
}
