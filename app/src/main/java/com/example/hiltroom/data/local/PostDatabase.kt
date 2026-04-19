package com.example.hiltroom.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedPostEntity::class,
        SearchCacheMetadataEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class PostDatabase : RoomDatabase() {
    abstract fun cachedPostDao(): CachedPostDao
    abstract fun searchCacheMetadataDao(): SearchCacheMetadataDao
}
