package com.example.hiltroom.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CachedPostDao {
    @Query(
        "SELECT * FROM cached_posts " +
            "WHERE search_query = :searchQuery " +
            "ORDER BY post_id ASC",
    )
    suspend fun getPostsByQuery(searchQuery: String): List<CachedPostEntity>

    @Query(
        "SELECT * FROM cached_posts " +
            "WHERE search_query = :searchQuery AND post_id = :postId " +
            "LIMIT 1",
    )
    suspend fun getPostByIdForQuery(
        searchQuery: String,
        postId: Int,
    ): CachedPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<CachedPostEntity>)

    @Query("DELETE FROM cached_posts WHERE search_query = :searchQuery")
    suspend fun deleteByQuery(searchQuery: String)

    @Transaction
    suspend fun replaceQuery(searchQuery: String, posts: List<CachedPostEntity>) {
        deleteByQuery(searchQuery)
        if (posts.isNotEmpty()) {
            insertAll(posts)
        }
    }
}
