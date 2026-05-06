package com.example.hiltroom.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SearchCacheMetadataDao {
    @Query("SELECT * FROM search_cache_metadata ORDER BY updated_at_millis DESC LIMIT 1")
    suspend fun getLatestMetadata(): SearchCacheMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SearchCacheMetadataEntity)

    @Query("DELETE FROM search_cache_metadata")
    suspend fun clearAll()

    @Transaction
    suspend fun replace(metadata: SearchCacheMetadataEntity) {
        clearAll()
        upsert(metadata)
    }
}
