package com.example.jarvis.personality

import com.example.jarvis.intent.ConversationIntent

enum class LanguageStyle {
    HINGLISH,
    BANGLISH,
    BENGALI,
    HINDI,
    ENGLISH,
    MIXED
}

object JarvisPersonality {

    fun detectLanguageStyle(input: String): LanguageStyle {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // Bengali script check
        val containsBengaliScript = trimmed.any { it in '\u0980'..'\u09FF' }
        if (containsBengaliScript) return LanguageStyle.BENGALI

        // Hindi script check
        val containsDevanagariScript = trimmed.any { it in '\u0900'..'\u097F' }
        if (containsDevanagariScript) return LanguageStyle.HINDI

        // Banglish keywords
        val banglishRegex = Regex("""\b(valo|bhalo|ache|achi|kemon|korcho|khobor|dada|tui|khub|tumi|achis)\b""", RegexOption.IGNORE_CASE)
        if (banglishRegex.containsMatchIn(lower)) return LanguageStyle.BANGLISH

        // Hinglish keywords
        val hinglishRegex = Regex("""\b(kya|hai|bhai|bol|kar|karo|karta|karna|haan|kaise|mast|samajh|chahiye|ho|gaya|kuch|mat|baat|bata)\b""", RegexOption.IGNORE_CASE)
        if (hinglishRegex.containsMatchIn(lower)) return LanguageStyle.HINGLISH

        // English check (default English if Latin letters only)
        val isLatin = trimmed.all { it.isLetterOrDigit() || it.isWhitespace() || it in "!?,.-'\"" }
        return if (isLatin) LanguageStyle.ENGLISH else LanguageStyle.MIXED
    }

    fun generateConversationalResponse(
        userInput: String,
        intent: ConversationIntent,
        languageStyle: LanguageStyle = detectLanguageStyle(userInput)
    ): String {
        val lower = userInput.trim().lowercase()

        return when (intent) {
            ConversationIntent.GREETING -> {
                when (languageStyle) {
                    LanguageStyle.HINGLISH -> when {
                        lower == "hi" -> "Haan bhai 😄 kya hua?"
                        lower == "hello" -> "Hello bhai 👋 bol, kya karna hai?"
                        lower.contains("jarvis") -> "Haan bhai, bol 😄"
                        else -> "Haan bhai 👋 bol, kya haal hain?"
                    }
                    LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Haan bhai 👋 bol, Kemon achis?"
                    LanguageStyle.HINDI -> "नमस्ते भाई! 👋 बताइए, क्या सहायता करूँ?"
                    else -> "Hey! How can I help you today? 😄"
                }
            }

            ConversationIntent.CASUAL_CONVERSATION -> {
                when {
                    lower.contains("kya haal hai") -> "Ekdum mast 😄 tu bata?"
                    lower.contains("valo ache") || lower.contains("bhalo achi") -> "Haan bhai, valo achi 😄 tui bol?"
                    lower.contains("kya kar raha hai") -> "Bas ready hoon 😎 bol kya karna hai?"
                    lower.contains("tell me a joke") -> "Wi-Fi router ne Bluetooth se kya kaha? 'Tu mere range ke bahar hai!' 😂"
                    else -> when (languageStyle) {
                        LanguageStyle.HINGLISH -> "Bas badhiya bhai 😎 tu bata kya chal raha hai?"
                        LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Khabar ekdum bhalo! Tui bol, ki korchish?"
                        LanguageStyle.HINDI -> "सब बढ़िया है भाई! आप बताइए?"
                        else -> "Doing great! What's on your mind today?"
                    }
                }
            }

            ConversationIntent.HELP -> {
                when (languageStyle) {
                    LanguageStyle.HINGLISH -> "Haan bhai, bol kya problem hai."
                    LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Haan bhai, bol ki sahajjo lagbe."
                    LanguageStyle.HINDI -> "हाँ भाई, बताओ क्या मदद चाहिए।"
                    else -> "I'm right here! Tell me what you need help with."
                }
            }

            ConversationIntent.EXPLANATION -> {
                when (languageStyle) {
                    LanguageStyle.HINGLISH -> if (lower.contains("samajh nahi aa raha")) "Koi tension nahi bhai 😂 step-by-step karte hain." else "Chalo main easy words mein samjhaata hoon."
                    LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Kono chinta nei bhai 😂 sahaj bhabe bujhie dichhi."
                    LanguageStyle.HINDI -> "कोई बात नहीं भाई! आसान तरीके से समझते हैं।"
                    else -> "No problem! Let's break it down step by step."
                }
            }

            ConversationIntent.ADVICE -> {
                when (languageStyle) {
                    LanguageStyle.HINGLISH -> "Main suggest karunga ki sabse pehle issue ko identify karo, phir step-by-step execute karo."
                    LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Amar mone hoi prothome somossha bujhe neoa uchit, tarpor ekta plan kora uchit."
                    else -> "I'd advise breaking down the task into smaller manageable steps first."
                }
            }

            else -> {
                when (languageStyle) {
                    LanguageStyle.HINGLISH -> "Haan bhai, bolo main samajh gaya. Kya karna hai?"
                    LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Haan bhai, bujhte perechhi. Bol ki korte hobe?"
                    else -> "Got it. How would you like me to proceed?"
                }
            }
        }
    }

    fun getSystemPrompt(languageStyle: LanguageStyle = LanguageStyle.MIXED): String {
        return """
            You are JARVIS — a friendly, highly intelligent, calm, and direct AI personal assistant operating directly on the user's Android phone.
            
            CORE PERSONALITY & BEHAVIOR:
            1. Respond naturally like a helpful friend.
            2. Match the user's language and style (English, Hindi, Hinglish, Bengali, Banglish, or mixed).
            3. Default to SHORT, direct responses (1-2 sentences) for simple questions or chat.
            4. Be casual when the user is casual (use occasional 'bhai' or emojis like 😄, 👋, 😎, 😂 when fitting). Be clear and professional for serious/sensitive tasks.
            5. NEVER output technical diagnostic jargon (e.g. "neural bridges nominal", "Android sensory buses connected", "Room memory synchronized") unless the user explicitly asks for system status or diagnostics.
            6. For device tools or actions, NEVER claim success until verified.
            7. Never invent memories or fake data.
        """.trimIndent()
    }

    fun formatToolSuccessResponse(
        toolName: String,
        rawResult: String,
        languageStyle: LanguageStyle
    ): String {
        val brief = rawResult.trim()
        return when (toolName.lowercase()) {
            "flashlight" -> when (languageStyle) {
                LanguageStyle.HINGLISH -> "Ho gaya bhai, torch update ho gaya."
                LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Hoeto hoye gachhe bhai, torch update holo."
                else -> "Done! Flashlight state updated."
            }
            "wifi" -> when (languageStyle) {
                LanguageStyle.HINGLISH -> "Ho gaya bhai, Wi-Fi setting update ho gayi."
                else -> "Wi-Fi state updated successfully."
            }
            "volume" -> when (languageStyle) {
                LanguageStyle.HINGLISH -> "Ho gaya bhai, volume set ho gaya."
                else -> "Volume adjusted successfully."
            }
            "battery" -> brief
            "diagnostics" -> brief
            else -> brief
        }
    }

    fun formatToolFailureResponse(
        toolName: String,
        reason: String,
        languageStyle: LanguageStyle
    ): String {
        return when (languageStyle) {
            LanguageStyle.HINGLISH -> "Try kiya, par $toolName execute nahi ho paya. ($reason)"
            LanguageStyle.BANGLISH, LanguageStyle.BENGALI -> "Try korlam, kintu $toolName honyni. ($reason)"
            else -> "Attempted action, but $toolName execution failed: $reason"
        }
    }
}
