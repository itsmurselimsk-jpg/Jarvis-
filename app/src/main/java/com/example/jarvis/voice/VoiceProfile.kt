package com.example.jarvis.voice

import java.util.Locale

/**
 * JARVIS Selectable Voice Profiles.
 * Provides distinct tonal calibrations for Text-To-Speech synthesis.
 */
enum class VoiceProfileType(
    val profileName: String,
    val tagline: String,
    val description: String,
    val defaultPitch: Float,
    val defaultSpeed: Float,
    val previewPhrase: String
) {
    CALM(
        profileName = "JARVIS Calm",
        tagline = "Composed & Tranquil",
        description = "Steady, composed, relaxed acoustic cadence with low resonance",
        defaultPitch = 0.85f,
        defaultSpeed = 0.95f,
        previewPhrase = "All telemetry streams are tranquil and within nominal thresholds, Sir."
    ),
    DEEP(
        profileName = "JARVIS Deep",
        tagline = "Authoritative Baritone",
        description = "Authoritative baritone with deep resonance and measured pace",
        defaultPitch = 0.70f,
        defaultSpeed = 0.90f,
        previewPhrase = "Security protocols active. Executive override confirmed, Sir."
    ),
    NATURAL(
        profileName = "JARVIS Natural",
        tagline = "Balanced & Conversational",
        description = "Balanced pitch and standard conversational cadence",
        defaultPitch = 1.00f,
        defaultSpeed = 1.00f,
        previewPhrase = "JARVIS online and standing by. How may I be of assistance today, Sir?"
    ),
    WARM(
        profileName = "JARVIS Warm",
        tagline = "Gentle & Cordial",
        description = "Gentle, cordial tone with smooth acoustic modulation",
        defaultPitch = 0.95f,
        defaultSpeed = 0.92f,
        previewPhrase = "Good day, Sir. I hope your agenda proceeds smoothly today."
    ),
    CRISP(
        profileName = "JARVIS Crisp",
        tagline = "Brisk & High Clarity",
        description = "High clarity, crisp articulation, and brisk operational tempo",
        defaultPitch = 1.15f,
        defaultSpeed = 1.10f,
        previewPhrase = "Telemetry updated. All subsystems responding at optimal velocity."
    );

    companion object {
        fun fromName(name: String): VoiceProfileType {
            return entries.find { it.profileName.equals(name, ignoreCase = true) } ?: NATURAL
        }
    }
}

/**
 * Multi-Language support for Bengali (বাংলা), Hindi (हिंदी), and English.
 */
enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val locale: Locale,
    val greetingPhrase: String
) {
    AUTO(
        code = "auto",
        displayName = "Auto Detect",
        nativeName = "English / বাংলা / हिंदी",
        locale = Locale.US,
        greetingPhrase = "Standing by for your directive."
    ),
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        locale = Locale.US,
        greetingPhrase = "Yes Sir, I am listening."
    ),
    BENGALI(
        code = "bn",
        displayName = "Bengali",
        nativeName = "বাংলা",
        locale = Locale("bn", "BD"),
        greetingPhrase = "হ্যাঁ স্যার, আমি শুনছি। কীভাবে সাহায্য করতে পারি?"
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिंदी",
        locale = Locale("hi", "IN"),
        greetingPhrase = "हाँ सर, मैं सुन रहा हूँ। मैं आपकी क्या मदद कर सकता हूँ?"
    ),
    HINGLISH(
        code = "hi-Latn",
        displayName = "Hinglish",
        nativeName = "Hinglish (Roman Script)",
        locale = Locale("hi", "IN"),
        greetingPhrase = "Haan Sir, main sun raha hoon. How can I help you?"
    ),
    BANGLISH(
        code = "bn-Latn",
        displayName = "Banglish",
        nativeName = "Banglish (Roman Script)",
        locale = Locale("bn", "BD"),
        greetingPhrase = "Hae Sir, ami shunchi. Kibaabe help korte pari?"
    );

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: AUTO
        }
    }
}
