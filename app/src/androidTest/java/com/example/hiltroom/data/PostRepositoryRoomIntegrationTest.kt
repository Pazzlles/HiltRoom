package com.example.hiltroom.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hiltroom.data.local.PostDatabase
import com.example.hiltroom.data.remote.dto.PostResponseDto
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.fail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostRepositoryRoomIntegrationTest {
    private lateinit var database: PostDatabase
    private lateinit var apiService: FakePostApiService
    private lateinit var repository: PostRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, PostDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        apiService = FakePostApiService()
        repository = PostRepositoryImpl(
            apiService = apiService,
            cachedPostDao = database.cachedPostDao(),
            cacheMetadataDao = database.searchCacheMetadataDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repositoryReturnsRoomCacheForLastSuccessfulQueryWhenNetworkFails() = runTest {
        apiService.postsByUserId[3] = listOf(
            postDto(id = 31, userId = 3, title = "Cached title"),
        )

        val firstResult = repository.getPosts("3")
        apiService.listFailure = IOException("offline")
        val secondResult = repository.getPosts("3")

        assertEquals(PostDataSource.Network, firstResult.source)
        assertEquals(PostDataSource.RoomCache, secondResult.source)
        assertEquals("3", repository.getLastSuccessfulSearchQuery())
        assertEquals(listOf("Cached title"), secondResult.posts.map { it.title })
    }

    @Test
    fun newSuccessfulSearchReplacesPreviousCacheInsteadOfKeepingAllQueries() = runTest {
        apiService.postsByUserId[1] = listOf(postDto(id = 11, userId = 1, title = "First query"))
        apiService.postsByUserId[2] = listOf(postDto(id = 21, userId = 2, title = "Second query"))

        repository.getPosts("1")
        repository.getPosts("2")
        apiService.listFailure = IOException("offline")

        val secondQueryFallback = repository.getPosts("2")

        assertEquals(PostDataSource.RoomCache, secondQueryFallback.source)
        assertEquals(listOf("Second query"), secondQueryFallback.posts.map { it.title })
        assertEquals("2", repository.getLastSuccessfulSearchQuery())

        try {
            repository.getPosts("1")
            fail("Expected the old query cache to be unavailable after a newer successful search.")
        } catch (exception: IOException) {
            assertTrue(exception.message?.contains("offline") == true)
        }
    }

    private fun postDto(
        id: Int,
        userId: Int,
        title: String,
    ): PostResponseDto {
        return PostResponseDto(
            userId = userId,
            id = id,
            title = title,
            body = "Body for $title",
        )
    }
}
