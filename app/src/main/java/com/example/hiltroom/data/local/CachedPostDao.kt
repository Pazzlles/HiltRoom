package com.example.hiltroom.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CachedPostDao {
    @Query("SELECT * FROM cached_posts ORDER BY post_id ASC")
    suspend fun getAllPosts(): List<CachedPostEntity>

    @Query("SELECT * FROM cached_posts WHERE post_id = :postId LIMIT 1")
    suspend fun getPostById(postId: Int): CachedPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<CachedPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: CachedPostEntity)

    @Query("DELETE FROM cached_posts")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(posts: List<CachedPostEntity>) {
        clearAll()
        if (posts.isNotEmpty()) {
            insertAll(posts)
        }
    }
}
