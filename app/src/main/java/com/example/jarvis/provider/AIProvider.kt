package com.example.jarvis.provider

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.jarvis.model.AIProviderType
import com.example.jarvis.storage.JarvisRepository
import com.example.jarvis.vision.HeuristicVisionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ToolDecision(
    val useTool: Boolean,
    val toolName: String? = null,
    val toolInput: String? = null,
    val reasoning: String? = null
)

interface AIProvider {
    suspend fun generateResponse(
        prompt: String,
        systemInstruction: String,
        onChunkReceived: (String) -> Unit
    ): String

    suspend fun decideTool(
        userInput: String,
        availableTools: List<Pair<String, String>>,
        contextHistory: String
    ): ToolDecision

    suspend fun analyzeImage(
        prompt: String,
        bitmap: Bitmap
    ): String
}

class GeminiAIProvider(
    private val repository: JarvisRepository
) : AIProvider {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        prompt: String,
        systemInstruction: String,
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val settings = repository.settings.value
        val effectiveApiKey = settings.customApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }

        if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
            return@withContext LocalNeuralBrainProvider.generateLocalResponse(prompt, onChunkReceived)
        }

        val model = "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$effectiveApiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", prompt))
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        if (systemInstruction.isNotBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray().apply { put(JSONObject().put("text", systemInstruction)) }
            sysObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysObj)
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("Gemini API error (HTTP ${response.code}): $responseBody")
        }

        val parsed = JSONObject(responseBody)
        val fullText = parsed.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text") ?: "JARVIS: No output generated."

        simulateStream(fullText, onChunkReceived)
        return@withContext fullText
    }

    override suspend fun decideTool(
        userInput: String,
        availableTools: List<Pair<String, String>>,
        contextHistory: String
    ): ToolDecision = withContext(Dispatchers.IO) {
        val settings = repository.settings.value
        val effectiveApiKey = settings.customApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }

        if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
            return@withContext LocalNeuralBrainProvider.decideToolLocal(userInput, availableTools)
        }

        try {
            val toolsCatalog = availableTools.joinToString("\n") { "• ${it.first}: ${it.second}" }
            val planningPrompt = """
                You are the JARVIS Executive Tool Planner.
                Given the user's input and the registered tool catalogue, decide if a tool should be executed.
                
                REGISTERED TOOLS:
                $toolsCatalog
                
                USER INPUT: "$userInput"
                
                Respond ONLY in strict JSON format:
                {
                  "useTool": true | false,
                  "toolName": "<Exact tool name from list or null>",
                  "toolInput": "<Refined argument or query to pass into tool>",
                  "reasoning": "<Short explanation>"
                }
            """.trimIndent()

            val model = "gemini-3.5-flash"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$effectiveApiKey"

            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val partsArray = JSONArray().apply { put(JSONObject().put("text", planningPrompt)) }
            contentsArray.put(JSONObject().put("parts", partsArray))
            rootJson.put("contents", contentsArray)

            // Generation config requesting JSON
            val genConfig = JSONObject().put("responseMimeType", "application/json")
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext LocalNeuralBrainProvider.decideToolLocal(userInput, availableTools)
            }

            val parsed = JSONObject(body)
            val jsonText = parsed.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val decisionJson = JSONObject(jsonText)
            val useTool = decisionJson.optBoolean("useTool", false)
            val toolName = decisionJson.optString("toolName", "").ifBlank { null }
            val toolInput = decisionJson.optString("toolInput", userInput)
            val reasoning = decisionJson.optString("reasoning", "")

            ToolDecision(useTool = useTool, toolName = toolName, toolInput = toolInput, reasoning = reasoning)
        } catch (_: Exception) {
            LocalNeuralBrainProvider.decideToolLocal(userInput, availableTools)
        }
    }

    override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val settings = repository.settings.value
        val effectiveApiKey = settings.customApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
            return@withContext HeuristicVisionProvider.analyzeLocal(bitmap, prompt)
        }

        try {
            val model = "gemini-3.5-flash"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$effectiveApiKey"

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val base64Bytes = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            partsArray.put(JSONObject().put("text", prompt.ifBlank { "Describe this image for JARVIS logs." }))
            partsArray.put(JSONObject().put("inlineData", JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", base64Bytes)
            }))

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext HeuristicVisionProvider.analyzeLocal(bitmap, prompt)
            }

            val parsed = JSONObject(responseBody)
            parsed.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: HeuristicVisionProvider.analyzeLocal(bitmap, prompt)
        } catch (e: Exception) {
            HeuristicVisionProvider.analyzeLocal(bitmap, prompt)
        }
    }

    private suspend fun simulateStream(fullText: String, onChunkReceived: (String) -> Unit) {
        val words = fullText.split(" ")
        val sb = StringBuilder()
        for (w in words) {
            sb.append(w).append(" ")
            onChunkReceived(sb.toString().trimEnd())
            delay(15)
        }
    }
}

class OpenAiCompatibleAIProvider(
    private val repository: JarvisRepository
) : AIProvider {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        prompt: String,
        systemInstruction: String,
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val settings = repository.settings.value
        val endpoint = settings.customEndpoint.ifBlank { "https://api.openai.com/v1" }
        val cleanEndpoint = if (endpoint.endsWith("/")) endpoint.dropLast(1) else endpoint
        val url = "$cleanEndpoint/chat/completions"

        val rootJson = JSONObject()
        rootJson.put("model", settings.selectedModel.ifBlank { "gpt-4o-mini" })
        val messages = JSONArray()
        if (systemInstruction.isNotBlank()) {
            messages.put(JSONObject().apply {
                put("role", "system")
                put("content", systemInstruction)
            })
        }
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
        })
        rootJson.put("messages", messages)

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${settings.customApiKey}")
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val bodyString = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("OpenAI endpoint error (${response.code}): $bodyString")
        }

        val parsed = JSONObject(bodyString)
        val text = parsed.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content") ?: "JARVIS: No output."

        onChunkReceived(text)
        return@withContext text
    }

    override suspend fun decideTool(
        userInput: String,
        availableTools: List<Pair<String, String>>,
        contextHistory: String
    ): ToolDecision {
        // Fallback or use local planning for ultra-reliable latency
        return LocalNeuralBrainProvider.decideToolLocal(userInput, availableTools)
    }

    override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): String {
        return HeuristicVisionProvider.analyzeLocal(bitmap, prompt)
    }
}

object LocalNeuralBrainProvider {

    suspend fun generateLocalResponse(prompt: String, onChunkReceived: (String) -> Unit): String {
        val lower = prompt.lowercase().trim()
        val response = when {
            lower.contains("who are you") || lower.contains("what are you") ->
                "I am JARVIS — Just A Rather Very Intelligent System. Operating on your native Android layer, coordinating telemetry, hardware sensors, and secure tool executors."
            lower.contains("status") || lower.contains("diagnostic") ->
                "All JARVIS neural bridges, Room memory databases, and Android sensory buses are performing within nominal thresholds, Sir."
            lower.contains("hello") || lower.contains("hey") || lower.contains("hi") ->
                "Greetings, Sir. JARVIS core is listening and prepared for your command."
            lower.contains("help") ->
                "I can monitor battery, network, Wi-Fi, Bluetooth, adjust volume, toggle the flashlight, launch apps, browse URLs, inspect accessibility UI, summarize notifications, and manage your long-term memories and agenda."
            else ->
                "Understood, Sir. I have processed your input: \"$prompt\". Operating layer telemetry confirms all systems nominal."
        }
        // Stream text smoothly
        val words = response.split(" ")
        val sb = StringBuilder()
        for (w in words) {
            sb.append(w).append(" ")
            onChunkReceived(sb.toString().trimEnd())
            delay(20)
        }
        return response
    }

    fun decideToolLocal(userInput: String, availableTools: List<Pair<String, String>>): ToolDecision {
        val lower = userInput.lowercase().trim()

        return when {
            // Flashlight / Torch
            lower.contains("flashlight") || lower.contains("torch") || lower.contains("ফ্ল্যাশলাইট") || lower.contains("फ्लैशलाइट") || lower.contains("লাইট") ->
                ToolDecision(true, "Flashlight", userInput, "Flashlight keyword detected")

            // YouTube Search & Playback
            lower.contains("youtube") || lower.contains("ইউটিউব") || lower.contains("यूट्यूब") ->
                ToolDecision(true, "YouTubeSearch", userInput, "YouTube video search and launch")

            // Phone Call
            lower.startsWith("call ") || lower.startsWith("dial ") || lower.startsWith("phone ") ||
            lower.contains("call koro") || lower.contains("call karo") || lower.contains("কল করো") || lower.contains("ফোন করো") || lower.contains("कॉल करो") ->
                ToolDecision(true, "PhoneCall", userInput, "Voice phone call dispatch")

            // Battery
            lower.contains("battery") || lower.contains("power level") || lower.contains("charging") || lower.contains("ব্যাটারি") || lower.contains("बैटरी") ->
                ToolDecision(true, "Battery", userInput, "Battery inspection query")

            // Network / Internet
            lower.contains("network") || lower.contains("internet connection") || lower.contains("online status") ->
                ToolDecision(true, "NetworkStatus", userInput, "Network connectivity check")

            // Wi-Fi
            lower.contains("wifi") || lower.contains("wi-fi") ->
                ToolDecision(true, "Wifi", userInput, "Wi-Fi radio query")

            // Bluetooth
            lower.contains("bluetooth") ->
                ToolDecision(true, "Bluetooth", userInput, "Bluetooth radio status query")

            // Volume
            lower.contains("volume") || lower.contains("sound level") || lower.contains("louder") || lower.contains("quieter") ||
            lower.contains("ভলিউম") || lower.contains("আওয়াজ") || lower.contains("आवाज़") || lower.contains("aawaz") ->
                ToolDecision(true, "Volume", userInput, "Audio volume request")

            // Brightness
            lower.contains("brightness") || lower.contains("screen light") ->
                ToolDecision(true, "Brightness", userInput, "Display brightness query")

            // Media
            lower.contains("play music") || lower.contains("pause music") || lower.contains("next song") || lower.contains("skip track") ||
            lower.contains("play this") || lower.contains("play ") || lower.contains("pause") || lower.contains("গান চালাও") || lower.contains("गाना बजाओ") ||
            lower.contains("chalao") || lower.contains("bajao") ->
                ToolDecision(true, "MediaControl", userInput, "Media key event request")

            // App Launcher
            lower.startsWith("open app") || lower.startsWith("launch app") || lower.startsWith("open ") || lower.startsWith("launch ") ||
            lower.contains("kholo") || lower.contains("खोलो") || lower.contains("খোলো") ||
            lower.contains("facebook") || lower.contains("whatsapp") || lower.contains("ফেসবুক") || lower.contains("फेसबुक") ->
                ToolDecision(true, "AppLauncher", userInput, "Application launch request")

            // Open URL
            lower.startsWith("open url") || lower.startsWith("browse to") || lower.contains("http://") || lower.contains("https://") ->
                ToolDecision(true, "OpenUrl", userInput, "External web URL navigation")

            // Android Settings
            lower.contains("setting") ->
                ToolDecision(true, "AndroidSettings", userInput, "Android settings launch request")

            // Clipboard
            lower.contains("clipboard") || lower.startsWith("copy ") || lower.contains("paste") ->
                ToolDecision(true, "Clipboard", userInput, "Clipboard buffer interaction")

            // Device Info / Hardware
            lower.contains("device info") || lower.contains("hardware") || lower.contains("specifications") || lower.contains("system telemetry") ->
                ToolDecision(true, "DeviceInfo", userInput, "Hardware diagnostics query")

            // Memory
            lower.startsWith("remember") || lower.startsWith("forget") || lower.contains("my memories") ->
                ToolDecision(true, "Memory", userInput, "Room memory management")

            // Tasks / Reminders
            lower.startsWith("add task") || lower.startsWith("create task") || lower.startsWith("remind me") || lower.contains("my agenda") || lower.contains("my tasks") ->
                ToolDecision(true, "Tasks", userInput, "Room task scheduling")

            // Timer
            lower.startsWith("set timer") || lower.startsWith("timer for") || lower.contains("countdown") ->
                ToolDecision(true, "Timer", userInput, "Countdown timer armed")

            // Calculator
            lower.startsWith("calculate") || lower.matches(Regex(""".*[\d\s]+[\+\-\*\/][\d\s]+.*""")) || lower.contains("% of") ->
                ToolDecision(true, "Calculator", userInput, "Mathematical calculation request")

            // Date / Time
            lower.contains("what time") || lower.contains("current time") || lower.contains("what's the date") || lower.contains("today's date") ->
                ToolDecision(true, "DateTime", userInput, "Chrono telemetry request")

            // Accessibility Agent
            lower.startsWith("inspect screen") || lower.startsWith("tap on") || lower.contains("accessibility") || lower.startsWith("scroll") ->
                ToolDecision(true, "AccessibilityAgent", userInput, "Accessibility agent dispatch")

            // Notifications
            lower.contains("notification") || lower.contains("unread messages") ->
                ToolDecision(true, "Notifications", userInput, "Notification listener inspection")

            // Web Search
            lower.startsWith("search for") || lower.startsWith("google ") || lower.startsWith("search ") ->
                ToolDecision(true, "WebSearch", userInput, "External web search synthesis")

            // Weather
            lower.contains("weather") || lower.contains("forecast") ->
                ToolDecision(true, "Weather", userInput, "Meteorological inquiry")

            else ->
                ToolDecision(useTool = false, toolName = null, toolInput = null, reasoning = "General conversational or semantic reasoning query")
        }
    }
}

class JarvisUnifiedAIProvider(
    private val repository: JarvisRepository
) : AIProvider {

    private val gemini = GeminiAIProvider(repository)
    private val openAi = OpenAiCompatibleAIProvider(repository)

    private fun getActiveProvider(): AIProvider {
        val settings = repository.settings.value
        val effectiveKey = settings.customApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }

        return when {
            settings.providerType == AIProviderType.OPENAI_COMPATIBLE && settings.customApiKey.isNotBlank() -> openAi
            settings.providerType == AIProviderType.GEMINI && effectiveKey.isNotBlank() && effectiveKey != "MY_GEMINI_API_KEY" -> gemini
            else -> object : AIProvider {
                override suspend fun generateResponse(
                    prompt: String,
                    systemInstruction: String,
                    onChunkReceived: (String) -> Unit
                ): String = LocalNeuralBrainProvider.generateLocalResponse(prompt, onChunkReceived)

                override suspend fun decideTool(
                    userInput: String,
                    availableTools: List<Pair<String, String>>,
                    contextHistory: String
                ): ToolDecision = LocalNeuralBrainProvider.decideToolLocal(userInput, availableTools)

                override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): String =
                    HeuristicVisionProvider.analyzeLocal(bitmap, prompt)
            }
        }
    }

    override suspend fun generateResponse(
        prompt: String,
        systemInstruction: String,
        onChunkReceived: (String) -> Unit
    ): String {
        return try {
            getActiveProvider().generateResponse(prompt, systemInstruction, onChunkReceived)
        } catch (e: Exception) {
            val local = LocalNeuralBrainProvider.generateLocalResponse(prompt, onChunkReceived)
            "$local\n\n*(Note: Cloud link experienced latency: ${e.localizedMessage ?: "Using onboard neural engine"}.)*"
        }
    }

    override suspend fun decideTool(
        userInput: String,
        availableTools: List<Pair<String, String>>,
        contextHistory: String
    ): ToolDecision {
        return try {
            getActiveProvider().decideTool(userInput, availableTools, contextHistory)
        } catch (_: Exception) {
            LocalNeuralBrainProvider.decideToolLocal(userInput, availableTools)
        }
    }

    override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): String {
        return try {
            getActiveProvider().analyzeImage(prompt, bitmap)
        } catch (_: Exception) {
            HeuristicVisionProvider.analyzeLocal(bitmap, prompt)
        }
    }
}
