package com.example.hiltroom.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "cached_posts",
    primaryKeys = ["search_query", "post_id"],
)
data class CachedPostEntity(
    @ColumnInfo(name = "search_query")
    val searchQuery: String,
    @ColumnInfo(name = "post_id")
    val postId: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
)
