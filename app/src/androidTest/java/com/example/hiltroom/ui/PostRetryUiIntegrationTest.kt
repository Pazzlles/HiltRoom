package com.example.hiltroom.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hiltroom.ui.list.POST_LIST_RETRY_BUTTON_TAG
import com.example.hiltroom.ui.list.POST_LIST_SUMMARY_TAG
import com.example.hiltroom.ui.theme.HiltRoomTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostRetryUiIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun retryAfterError_showsSuccessfulListOnScreen() {
        composeRule.setContent {
            HiltRoomTheme(darkTheme = false) {
                RetryStateHost()
            }
        }

        composeRule.onNodeWithTag(POST_LIST_RETRY_BUTTON_TAG).assertIsDisplayed().performClick()

        composeRule.onNodeWithTag(POST_LIST_SUMMARY_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(TEST_SUMMARY).assertIsDisplayed()
        composeRule.onNodeWithText(TEST_RECOVERED_TITLE).assertIsDisplayed()
    }
}
