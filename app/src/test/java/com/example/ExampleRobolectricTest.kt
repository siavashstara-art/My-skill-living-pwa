package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VoiceProviderConfig
import com.example.summarizer.BookSummarizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("استودیوی آفرینا", appName)
    }

    @Test
    fun `test book summarizer generates compact standard and detailed summaries`() {
        val sample = BookSummarizer.sampleBooks.first()
        val result = BookSummarizer.summarize(sample.text, sample.title)

        assertEquals("شازده کوچولو", result.bookTitle)
        assertTrue("Chapters should be detected", result.chapters.isNotEmpty())
        assertTrue("Compact summary should not be empty", result.fullCompact.isNotBlank())
        assertTrue("Standard summary should not be empty", result.fullStandard.isNotBlank())
        assertTrue("Detailed summary should not be empty", result.fullDetailed.isNotBlank())
        assertTrue("Narration text should contain greeting", result.fullNarrationReady.contains("استودیوی آفرینا"))
    }

    @Test
    fun `test voice provider configuration state`() {
        val unconfigured = VoiceProviderConfig(providerType = "SYSTEM_TTS")
        assertFalse(unconfigured.isConfigured)

        val elevenLabsConfigured = VoiceProviderConfig(
            providerType = "ELEVENLABS",
            apiKey = "test_key",
            voiceId = "test_voice"
        )
        assertTrue(elevenLabsConfigured.isConfigured)
    }
}
