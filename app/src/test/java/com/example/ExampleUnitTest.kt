package com.example

import com.example.data.youtube.YouTubeService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testYouTubeUrlExtraction_standardUrl() {
    val id = YouTubeService.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    assertEquals("dQw4w9WgXcQ", id)
  }

  @Test
  fun testYouTubeUrlExtraction_shortenedUrl() {
    val id = YouTubeService.extractVideoId("https://youtu.be/jNQXAC9IVRw")
    assertEquals("jNQXAC9IVRw", id)
  }

  @Test
  fun testYouTubeUrlExtraction_shortsUrl() {
    val id = YouTubeService.extractVideoId("https://www.youtube.com/shorts/9bZkp7q19f0")
    assertEquals("9bZkp7q19f0", id)
  }

  @Test
  fun testYouTubeUrlExtraction_rawId() {
    val id = YouTubeService.extractVideoId("kJQP7kiw5Fk")
    assertEquals("kJQP7kiw5Fk", id)
  }
}

