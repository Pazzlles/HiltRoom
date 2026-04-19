package com.example.hiltroom.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SearchCacheMetadataDao {
    @Query("SELECT * FROM search_cache_metadata WHERE query = :query LIMIT 1")
    suspend fun getMetadata(query: String): SearchCacheMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SearchCacheMetadataEntity)
}
