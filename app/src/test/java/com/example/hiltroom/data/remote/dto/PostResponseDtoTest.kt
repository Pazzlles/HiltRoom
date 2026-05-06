package com.example.hiltroom.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PostResponseDtoTest {
    @Test
    fun toPostListItemOrNull_returnsNullWhenIdIsMissing() {
        val dto = PostResponseDto(
            userId = 1,
            id = null,
            title = "Title",
            body = "Body",
        )

        assertNull(dto.toPostListItemOrNull())
    }

    @Test
    fun toPostListItemOrNull_usesFallbacksForBlankStrings() {
        val dto = PostResponseDto(
            userId = 2,
            id = 10,
            title = " ",
            body = "",
        )

        val item = dto.toPostListItemOrNull()

        requireNotNull(item)
        assertEquals("Без названия", item.title)
        assertEquals("Без описания", item.preview)
        assertEquals("Без описания", item.body)
    }

    @Test
    fun toPostDetail_throwsWhenUserIdIsMissing() {
        val dto = PostResponseDto(
            userId = null,
            id = 5,
            title = "Title",
            body = "Body",
        )

        assertThrows(IllegalStateException::class.java) {
            dto.toPostDetail()
        }
    }
}
