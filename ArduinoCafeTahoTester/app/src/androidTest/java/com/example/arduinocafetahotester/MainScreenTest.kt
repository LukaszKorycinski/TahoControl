package com.example.arduinocafetahotester

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arduinocafetahotester.ui.theme.ArduinoCafeTahoTesterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {


    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initScreenTest() {
        // Start the app
        composeTestRule.setContent {
            ArduinoCafeTahoTesterTheme {
                MainScreen(state = MainActivityState.INITIALIZING)
            }
        }

        composeTestRule.onNodeWithText("Connect Device").assertIsDisplayed()
    }

    @Test
    fun waitingForDeviceScreenTest() {
        // Start the app
        composeTestRule.setContent {
            ArduinoCafeTahoTesterTheme {
                MainScreen()
            }
        }

        composeTestRule.onNodeWithText("Connect Device").assertIsDisplayed()
    }

}