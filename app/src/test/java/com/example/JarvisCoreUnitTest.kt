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
}
