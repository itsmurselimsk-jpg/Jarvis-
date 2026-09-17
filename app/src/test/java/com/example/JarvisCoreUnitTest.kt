package com.example

import com.example.jarvis.model.RiskLevel
import com.example.jarvis.provider.LocalNeuralBrainProvider
import com.example.jarvis.safety.RiskEngine
import com.example.jarvis.security.EncryptedStorage
import com.example.jarvis.security.SensitiveDataFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JarvisCoreUnitTest {

    @Test
    fun testRiskEngineRejectsArbitraryShellCommands() {
        val dangerousInputs = listOf(
            "rm -rf /",
            "su -c reboot",
            "sh test.sh",
            "/bin/bash -i",
            "pm uninstall com.android.chrome",
            "setenforce 0",
            "dd if=/dev/zero of=/dev/block/boot"
        )

        for (cmd in dangerousInputs) {
            val assessment = RiskEngine.assessAction("ShellExec", cmd, RiskLevel.SAFE)
            assertEquals("Expected RESTRICTED for command: $cmd", RiskLevel.RESTRICTED, assessment.level)
        }
    }

    @Test
    fun testRiskEngineAllowsSafeCommands() {
        val safeActions = listOf(
            Pair("Battery", "query battery status"),
            Pair("NetworkStatus", "audit connection"),
            Pair("Volume", "adjust volume to 50"),
            Pair("Flashlight", "toggle torch on"),
            Pair("DateTime", "what time is it")
        )

        for (action in safeActions) {
            val assessment = RiskEngine.assessAction(action.first, action.second, RiskLevel.SAFE)
            assertEquals("Expected SAFE for action: ${action.first}", RiskLevel.SAFE, assessment.level)
        }
    }

    @Test
    fun testSensitiveDataFilterDetectsCredentials() {
        assertTrue(SensitiveDataFilter.containsSensitiveData("Here is my secret sk-proj-1234567890abcdefghijklmnop"))
        assertTrue(SensitiveDataFilter.containsSensitiveData("AIzaSyD-1234567890abcdefghijklmnopqr"))
        assertTrue(SensitiveDataFilter.containsSensitiveData("Credit card 4532 0150 1234 5678"))
        assertTrue(SensitiveDataFilter.containsSensitiveData("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xyz"))

        assertFalse(SensitiveDataFilter.containsSensitiveData("Schedule meeting with Dr. Banner at 3 PM"))
        assertFalse(SensitiveDataFilter.containsSensitiveData("What is the battery level?"))
    }

    @Test
    fun testEncryptedStorageFallbackRoundTrip() {
        val rawSecret = "sk-ant-api03-sample-test-key-12345"
        val encrypted = EncryptedStorage.encrypt(rawSecret)
        val decrypted = EncryptedStorage.decrypt(encrypted)
        assertEquals(rawSecret, decrypted)
    }

    @Test
    fun testLocalNeuralBrainToolDecisions() {
        val tools = listOf(
            Pair("Flashlight", "Toggles LED torch"),
            Pair("Battery", "Checks battery level"),
            Pair("Volume", "Sets audio volume")
        )

        val flashlightDecision = LocalNeuralBrainProvider.decideToolLocal("turn on flashlight", tools)
        assertTrue(flashlightDecision.useTool)
        assertEquals("Flashlight", flashlightDecision.toolName)

        val batteryDecision = LocalNeuralBrainProvider.decideToolLocal("what is the battery charge?", tools)
        assertTrue(batteryDecision.useTool)
        assertEquals("Battery", batteryDecision.toolName)

        val chatDecision = LocalNeuralBrainProvider.decideToolLocal("Tell me about the history of quantum computing", tools)
        assertFalse(chatDecision.useTool)
    }

    @Test
    fun testLanguageDetector() {
        // Bengali script
        val bengali = com.example.jarvis.voice.LanguageDetector.detectLanguage("জারভিস কেমন আছো?")
        assertEquals(com.example.jarvis.voice.LanguageDetector.DetectedLanguage.BENGALI, bengali)

        // Hindi script
        val hindi = com.example.jarvis.voice.LanguageDetector.detectLanguage("नमस्ते जार्विस आप कैसे हैं?")
        assertEquals(com.example.jarvis.voice.LanguageDetector.DetectedLanguage.HINDI, hindi)

        // English
        val english = com.example.jarvis.voice.LanguageDetector.detectLanguage("Hello JARVIS how are you?")
        assertEquals(com.example.jarvis.voice.LanguageDetector.DetectedLanguage.ENGLISH, english)

        // Hinglish phonetics
        val hinglish = com.example.jarvis.voice.LanguageDetector.detectLanguage("jarvis gaana bajao aur aawaz badhao")
        assertEquals(com.example.jarvis.voice.LanguageDetector.DetectedLanguage.HINDI, hinglish)

        // Banglish phonetics
        val banglish = com.example.jarvis.voice.LanguageDetector.detectLanguage("jarvis ki obostha kholo")
        assertEquals(com.example.jarvis.voice.LanguageDetector.DetectedLanguage.BENGALI, banglish)
    }

    @Test
    fun testWakeWordRecognition() {
        val check1 = com.example.jarvis.voice.LanguageDetector.inspectForWakeWord("Hey JARVIS open camera")
        assertTrue(check1.isWakeWordPresent)
        assertEquals("open camera", check1.commandAfterWake)

        val check2 = com.example.jarvis.voice.LanguageDetector.inspectForWakeWord("JARVIS what is the battery percentage?")
        assertTrue(check2.isWakeWordPresent)
        assertEquals("what is the battery percentage?", check2.commandAfterWake)

        val check3 = com.example.jarvis.voice.LanguageDetector.inspectForWakeWord("Good morning world")
        assertFalse(check3.isWakeWordPresent)
    }

    @Test
    fun testVoiceProfilesAndLanguages() {
        // Verify 5 distinct voice profiles exist
        val profiles = com.example.jarvis.voice.VoiceProfileType.values()
        assertTrue("Expected at least 5 voice profiles", profiles.size >= 5)

        // Verify supported languages include required dialects
        val languages = com.example.jarvis.voice.SupportedLanguage.values().map { it.code }
        assertTrue(languages.contains("en"))
        assertTrue(languages.contains("hi"))
        assertTrue(languages.contains("bn"))
        assertTrue(languages.contains("hi-Latn"))
        assertTrue(languages.contains("bn-Latn"))
    }
}
