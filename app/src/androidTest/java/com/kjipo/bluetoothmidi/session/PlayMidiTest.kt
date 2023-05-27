package com.kjipo.bluetoothmidi.session

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjipo.bluetoothmidi.AppTheme
import com.kjipo.bluetoothmidi.ui.midiplay.PlayMidi
import com.kjipo.bluetoothmidi.ui.midiplay.PlayState
import com.kjipo.bluetoothmidi.ui.midiplay.PlayViewUiState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PlayMidiTest {

    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun modelStatesUpdated() {
        val uiState = PlayViewUiState(PlayState.WAITING, 0)
        var playClicked = false
        val onClickPlay = {
            playClicked = true
        }

        composeTestRule.setContent {
            AppTheme {
                PlayMidi(uiState,
                    onClickPlay,
                    {
                        // Do nothing
                    }, {
                        // Do nothing
                    })
            }
        }

        composeTestRule.onNodeWithTag("play").performClick()

        assertThat(playClicked, equalTo(true))
    }


}