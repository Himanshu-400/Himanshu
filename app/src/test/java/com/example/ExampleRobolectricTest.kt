package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
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
    assertEquals("StudyNova", appName)
  }

  @Test
  fun `launch MainActivity`() {
    try {
      ActivityScenario.launch(MainActivity::class.java).use { scenario ->
        scenario.onActivity { activity ->
            println("Activity launched successfully")
        }
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
      }
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }
  }
}
