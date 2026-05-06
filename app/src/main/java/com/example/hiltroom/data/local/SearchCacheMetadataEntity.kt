package com.example.hiltroom.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_cache_metadata")
data class SearchCacheMetadataEntity(
    @PrimaryKey
    val query: String,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
)
