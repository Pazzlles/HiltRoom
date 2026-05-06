package com.example.hiltroom.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hiltroom.ui.detail.POST_DETAIL_BACK_BUTTON_TAG
import com.example.hiltroom.ui.detail.POST_DETAIL_BODY_TAG
import com.example.hiltroom.ui.detail.POST_DETAIL_TITLE_TAG
import com.example.hiltroom.ui.theme.HiltRoomTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostNavigationUiIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clickingPost_opensDetailForTheSameId() {
        composeRule.setContent {
            HiltRoomTheme(darkTheme = false) {
                ListDetailStateHost()
            }
        }

        composeRule.onNodeWithTag(postCardTag(TEST_POST_ID)).assertIsDisplayed().performClick()

        composeRule.onNodeWithTag(POST_DETAIL_TITLE_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(POST_DETAIL_BODY_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(POST_DETAIL_BACK_BUTTON_TAG).assertIsDisplayed()
    }
}
