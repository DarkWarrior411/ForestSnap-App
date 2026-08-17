package com.example.forestsnap.features.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.forestsnap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/** Instrument UI test suite verifying main activity launch and initial dashboard composable rendering. */
@HiltAndroidTest
class DashboardUITest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndShowsDashboard() {
        composeTestRule.onNodeWithText("Camera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("Risk Level").assertIsDisplayed()
    }
}
