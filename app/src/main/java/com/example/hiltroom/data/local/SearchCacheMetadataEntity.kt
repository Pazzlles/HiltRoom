package com.example.hiltroom.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_cache_metadata")
data class SearchCacheMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_id")
    val cacheId: Int = DEFAULT_CACHE_ID,
    @ColumnInfo(name = "query")
    val query: String,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
) {
    companion object {
        const val DEFAULT_CACHE_ID: Int = 1
    }
}
