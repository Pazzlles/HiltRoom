package com.example.hiltroom.di

import android.content.Context
import androidx.room.Room
import com.example.hiltroom.data.PostRepository
import com.example.hiltroom.data.PostRepositoryImpl
import com.example.hiltroom.data.local.CachedPostDao
import com.example.hiltroom.data.local.PostDatabase
import com.example.hiltroom.data.local.SearchCacheMetadataDao
import com.example.hiltroom.data.remote.PostApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
private const val DATABASE_NAME = "posts_cache.db"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePostApiService(retrofit: Retrofit): PostApiService {
        return retrofit.create(PostApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePostDatabase(
        @ApplicationContext context: Context,
    ): PostDatabase {
        return Room.databaseBuilder(
            context,
            PostDatabase::class.java,
            DATABASE_NAME,
        )
            // Cache data can always be restored from the network.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCachedPostDao(database: PostDatabase): CachedPostDao {
        return database.cachedPostDao()
    }

    @Provides
    @Singleton
    fun provideSearchCacheMetadataDao(database: PostDatabase): SearchCacheMetadataDao {
        return database.searchCacheMetadataDao()
    }

    @Provides
    @Singleton
    fun providePostRepository(
        apiService: PostApiService,
        cachedPostDao: CachedPostDao,
        cacheMetadataDao: SearchCacheMetadataDao,
    ): PostRepository {
        return PostRepositoryImpl(
            apiService = apiService,
            cachedPostDao = cachedPostDao,
            cacheMetadataDao = cacheMetadataDao,
        )
    }
}
