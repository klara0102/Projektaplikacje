package com.example.projektaplikacje

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Test instrumentalny, który zostanie wykonany na urządzeniu z Androidem.
 *
 * Zobacz [dokumentację testowania](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Kontekst aplikacji poddanej testowi.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.projektaplikacje", appContext.packageName)
    }
}
