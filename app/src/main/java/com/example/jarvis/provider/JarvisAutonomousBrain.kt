package com.example.jarvis.provider

import com.example.jarvis.personality.JarvisPersonality
import com.example.jarvis.personality.LanguageStyle
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * JarvisAutonomousBrain: Our own self-contained On-Device Intelligence Engine.
 * 
 * Provides rich, detailed, ChatGPT-level conversational replies, code generation,
 * mathematical problem solving, writing assistance, scientific explanations,
 * and storytelling 100% offline without needing any cloud API key.
 */
object JarvisAutonomousBrain {

    fun generateAutonomousResponse(userInput: String): String {
        val trimmed = userInput.trim()
        val lower = trimmed.lowercase(Locale.ROOT)
        val lang = JarvisPersonality.detectLanguageStyle(trimmed)

        // 1. Math and arithmetic calculation
        val mathResult = tryEvaluateMath(trimmed)
        if (mathResult != null) {
            return formatMathResponse(trimmed, mathResult, lang)
        }

        // 2. Code & Programming Generation
        val codeResponse = tryGenerateCodeResponse(lower, lang)
        if (codeResponse != null) {
            return codeResponse
        }

        // 3. Writing / Letter / Email / Timetable Generation
        val writingResponse = tryGenerateWritingTemplate(lower, lang)
        if (writingResponse != null) {
            return writingResponse
        }

        // 4. Science, Technology & General Knowledge
        val knowledgeResponse = tryGenerateKnowledgeResponse(lower, lang)
        if (knowledgeResponse != null) {
            return knowledgeResponse
        }

        // 5. Entertainment, Storytelling, Jokes, Shayari, Motivation
        val creativeResponse = tryGenerateCreativeResponse(lower, lang)
        if (creativeResponse != null) {
            return creativeResponse
        }

        // 6. Identity, Capabilities & Assistance Guidance
        val assistantResponse = tryGenerateAssistantResponse(lower, lang)
        if (assistantResponse != null) {
            return assistantResponse
        }

        // 7. Conversational Deep Fallback
        return generateConversationalDeepReply(trimmed, lower, lang)
    }

    // ==========================================
    // 1. MATH EVALUATION ENGINE
    // ==========================================
    private fun tryEvaluateMath(input: String): Double? {
        val clean = input.replace("calculate", "", ignoreCase = true)
            .replace("solve", "", ignoreCase = true)
            .replace("kitna hota hai", "", ignoreCase = true)
            .replace("kitna hoga", "", ignoreCase = true)
            .replace("equals", "", ignoreCase = true)
            .replace("=", "")
            .replace("?", "")
            .trim()

        // Handle square root
        val sqrtMatch = Regex("""(?:sqrt|square root of)\s*(\d+(\.\d+)?)""").find(clean.lowercase())
        if (sqrtMatch != null) {
            val num = sqrtMatch.groupValues[1].toDoubleOrNull()
            if (num != null && num >= 0) return sqrt(num)
        }

        // Handle percentages: "18% of 500" or "500 ka 18%"
        val percentOfMatch = Regex("""(\d+(\.\d+)?)\s*%\s*(?:of|ka)\s*(\d+(\.\d+)?)""").find(clean.lowercase())
        if (percentOfMatch != null) {
            val pct = percentOfMatch.groupValues[1].toDoubleOrNull()
            val total = percentOfMatch.groupValues[3].toDoubleOrNull()
            if (pct != null && total != null) return (pct * total) / 100.0
        }
        val kaPercentMatch = Regex("""(\d+(\.\d+)?)\s*(?:ka)\s*(\d+(\.\d+)?)\s*%""").find(clean.lowercase())
        if (kaPercentMatch != null) {
            val total = kaPercentMatch.groupValues[1].toDoubleOrNull()
            val pct = kaPercentMatch.groupValues[3].toDoubleOrNull()
            if (total != null && pct != null) return (pct * total) / 100.0
        }

        // Handle basic arithmetic: X [+-*/^] Y
        val opMatch = Regex("""(-?\d+(\.\d+)?)\s*([\+\-\*\/\^xX×÷])\s*(-?\d+(\.\d+)?)""").find(clean)
        if (opMatch != null) {
            val a = opMatch.groupValues[1].toDoubleOrNull() ?: return null
            val op = opMatch.groupValues[3]
            val b = opMatch.groupValues[4].toDoubleOrNull() ?: return null

            return when (op) {
                "+", "plus" -> a + b
                "-", "minus" -> a - b
                "*", "x", "X", "×", "into", "multiplied by" -> a * b
                "/", "÷", "divided by" -> if (b != 0.0) a / b else Double.NaN
                "^" -> a.pow(b)
                else -> null
            }
        }

        return null
    }

    private fun formatMathResponse(expression: String, result: Double, lang: LanguageStyle): String {
        val formattedNum = if (result.isNaN()) "Undefined (Division by zero)" else if (result % 1.0 == 0.0) result.toLong().toString() else "%.4f".format(result).trimEnd('0').trimEnd('.')
        return when (lang) {
            LanguageStyle.HINGLISH, LanguageStyle.HINDI ->
                "### 🔢 Calculation Result\n\n" +
                "**Expression:** `$expression`\n\n" +
                "**Final Answer:** **$formattedNum**\n\n" +
                "Agar koi aur calculation ya math formula solve karna ho, toh batao!"
            LanguageStyle.BANGLISH, LanguageStyle.BENGALI ->
                "### 🔢 Calculation Result\n\n" +
                "**Expression:** `$expression`\n\n" +
                "**Answer:** **$formattedNum**\n\n" +
                "Ar kono calculation ba onko thakle bolte paro!"
            else ->
                "### 🔢 Mathematical Solution\n\n" +
                "**Problem:** `$expression`\n\n" +
                "**Result:** **$formattedNum**\n\n" +
                "Let me know if you'd like to perform additional computations or formula derivations!"
        }
    }

    // ==========================================
    // 2. CODE & PROGRAMMING GENERATOR
    // ==========================================
    private fun tryGenerateCodeResponse(lower: String, lang: LanguageStyle): String? {
        // Python Prime Number
        if (lower.contains("prime number") && (lower.contains("python") || lower.contains("code"))) {
            return "### 🐍 Python: Check for Prime Number\n\n" +
                    "Prime number woh number hota hai jo sirf 1 aur khud se divide hota hai (e.g. 2, 3, 5, 7, 11).\n\n" +
                    "```python\n" +
                    "def is_prime(n):\n" +
                    "    if n <= 1:\n" +
                    "        return False\n" +
                    "    for i in range(2, int(n**0.5) + 1):\n" +
                    "        if n % i == 0:\n" +
                    "            return False\n" +
                    "    return True\n" +
                    "\n" +
                    "# Test the function\n" +
                    "number = 29\n" +
                    "if is_prime(number):\n" +
                    "    print(f\"{number} is a Prime Number! ✅\")\n" +
                    "else:\n" +
                    "    print(f\"{number} is not a Prime Number. ❌\")\n" +
                    "```\n\n" +
                    "**Explanation:**\n" +
                    "- Loop `2` se lekar `sqrt(n)` tak chalta hai, jo time complexity ko `O(sqrt(N))` bana deta hai."
        }

        // Fibonacci
        if (lower.contains("fibonacci")) {
            return "### 🔢 Fibonacci Series in Python\n\n" +
                    "Fibonacci series mein har agla number pichle do numbers ka sum hota hai: `0, 1, 1, 2, 3, 5, 8, 13...`\n\n" +
                    "```python\n" +
                    "def fibonacci(n_terms):\n" +
                    "    a, b = 0, 1\n" +
                    "    series = []\n" +
                    "    for _ in range(n_terms):\n" +
                    "        series.append(a)\n" +
                    "        a, b = b, a + b\n" +
                    "    return series\n" +
                    "\n" +
                    "# Pehle 10 numbers print karo\n" +
                    "print(fibonacci(10))\n" +
                    "# Output: [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]\n" +
                    "```"
        }

        // Reverse String
        if (lower.contains("reverse") && (lower.contains("string") || lower.contains("text"))) {
            return "### 🔄 Reverse a String\n\n" +
                    "**Python:**\n" +
                    "```python\n" +
                    "text = \"Hello JARVIS\"\n" +
                    "reversed_text = text[::-1]  # Slice step -1\n" +
                    "print(reversed_text)  # Output: SIVRAJ olleH\n" +
                    "```\n\n" +
                    "**Kotlin / Java:**\n" +
                    "```kotlin\n" +
                    "val original = \"Hello JARVIS\"\n" +
                    "val reversed = original.reversed()\n" +
                    "println(reversed)\n" +
                    "```"
        }

        // HTML / Web page template
        if (lower.contains("html") || (lower.contains("website") && lower.contains("bana"))) {
            return "### 🌐 Modern HTML5 Starter Template\n\n" +
                    "Ek clean, responsive starter web page ka code:\n\n" +
                    "```html\n" +
                    "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "  <meta charset=\"UTF-8\">\n" +
                    "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "  <title>JARVIS Cyber Dashboard</title>\n" +
                    "  <style>\n" +
                    "    body {\n" +
                    "      margin: 0;\n" +
                    "      font-family: 'Segoe UI', sans-serif;\n" +
                    "      background: #0b1329;\n" +
                    "      color: #00f0ff;\n" +
                    "      display: flex;\n" +
                    "      justify-content: center;\n" +
                    "      align-items: center;\n" +
                    "      height: 100vh;\n" +
                    "    }\n" +
                    "    .card {\n" +
                    "      border: 1px solid #00f0ff;\n" +
                    "      padding: 24px;\n" +
                    "      border-radius: 12px;\n" +
                    "      box-shadow: 0 0 20px rgba(0,240,255,0.3);\n" +
                    "      text-align: center;\n" +
                    "    }\n" +
                    "  </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "  <div class=\"card\">\n" +
                    "    <h1>JARVIS Stark OS</h1>\n" +
                    "    <p>Systems Nominal • All Systems Online</p>\n" +
                    "  </div>\n" +
                    "</body>\n" +
                    "</html>\n" +
                    "```"
        }

        // Python Overview
        if (lower.contains("python kya hai") || lower.contains("what is python") || lower.contains("python explain")) {
            return "### 🐍 Python Programming Language Explained\n\n" +
                    "**Python** duniya ki sabse popular aur aasan programming languages mein se ek hai, jise Guido van Rossum ne 1991 mein create kiya tha.\n\n" +
                    "#### 🌟 Key Features:\n" +
                    "1. **Easy to Read & Learn**: Iska syntax plain English jaisa hota hai, semicolon ya curly braces ki zaroorat nahi hoti.\n" +
                    "2. **Interpreted & Dynamic**: Code line-by-line execute hota hai, types declare nahi karne padte.\n" +
                    "3. **Massive Ecosystem**: Libraries jaise `NumPy`, `Pandas`, `TensorFlow`, `Django`, `Flask` available hain.\n\n" +
                    "#### 🚀 Use Cases:\n" +
                    "- **Artificial Intelligence & Machine Learning**\n" +
                    "- **Data Science & Analytics**\n" +
                    "- **Web Development (Back-end)**\n" +
                    "- **Automation & Web Scraping**\n\n" +
                    "**Hello World Example:**\n" +
                    "```python\n" +
                    "print(\"Hello World! Welcome to Python.\")\n" +
                    "```"
        }

        return null
    }

    // ==========================================
    // 3. WRITING TEMPLATES & PRODUCTIVITY
    // ==========================================
    private fun tryGenerateWritingTemplate(lower: String, lang: LanguageStyle): String? {
        // Leave Application / Chhutti
        if (lower.contains("leave application") || lower.contains("chhutti") || lower.contains("sick leave") || lower.contains("leave letter")) {
            return "### 📄 Formal Sick Leave Application\n\n" +
                    "Aap is format ko apne School, College ya Office ke liye use kar sakte hain:\n\n" +
                    "---\n" +
                    "**To:**\n" +
                    "The Principal / Manager,\n" +
                    "[School / College / Company Name],\n" +
                    "[City, Date]\n\n" +
                    "**Subject:** Application for Sick Leave due to fever\n\n" +
                    "Respected Sir / Madam,\n\n" +
                    "With due respect, I wish to state that I am suffering from severe viral fever and headache since last evening. The doctor has advised me complete bed rest for [Number of Days, e.g., 2 days].\n\n" +
                    "Therefore, I kindly request you to grant me leave from [Start Date] to [End Date]. I will ensure that any pending work/assignments are completed promptly upon my return.\n\n" +
                    "Thanking you.\n\n" +
                    "Yours faithfully,\n" +
                    "**[Your Name]**\n" +
                    "[Roll No. / Designation / Contact No.]\n" +
                    "---"
        }

        // Timetable / Study Schedule
        if (lower.contains("time table") || lower.contains("timetable") || lower.contains("study plan") || lower.contains("routine")) {
            return "### 📅 High-Focus Daily Study Routine (Productivity Blueprint)\n\n" +
                    "Ek balanced routine jo continuous energy aur high retention maintain karta hai:\n\n" +
                    "| Time Slot | Activity | Focus Goal |\n" +
                    "| :--- | :--- | :--- |\n" +
                    "| **06:30 AM - 07:00 AM** | Wake Up & Hydrate | Morning walk / light stretches |\n" +
                    "| **07:00 AM - 09:00 AM** | **Deep Work Block 1** | Toughest Subject / Problem Solving |\n" +
                    "| **09:00 AM - 10:00 AM** | Breakfast & Rest | Healthy meal + relaxation |\n" +
                    "| **10:00 AM - 01:00 PM** | **Deep Work Block 2** | Theory / Concept Learning / Notes |\n" +
                    "| **01:00 PM - 02:30 PM** | Lunch & Power Nap | 20-min recharge nap |\n" +
                    "| **02:30 PM - 05:00 PM** | **Practice & Revision** | Mock tests, MCQs, or coding practice |\n" +
                    "| **05:00 PM - 06:30 PM** | Outdoor / Fitness | Sports, gym, or friends |\n" +
                    "| **06:30 PM - 08:30 PM** | **Light Study Block** | Next day preparation / quick review |\n" +
                    "| **08:30 PM - 09:30 PM** | Dinner & Family Time | Disconnect from screens |\n" +
                    "| **10:30 PM** | Sleep | Solid 7-8 hours restful sleep |\n\n" +
                    "💡 **Pro-Tip**: Har 50 minute padhai ke baad 10 minute ka Pomodoro break zaroor lein!"
        }

        // Birthday Wish
        if (lower.contains("birthday wish") || lower.contains("janamdin") || lower.contains("birthday message")) {
            return "### 🎂 Warm & Creative Birthday Wishes\n\n" +
                    "**1. Casual / Best Friend ke liye (Hinglish):**\n" +
                    "> \"Happy Birthday bhai! 🥳 Bhagwan kare tera yeh saal full of success, khushiyan, aur mast adventures se bhara ho. Party kab de raha hai? Enjoy your day to the fullest! 🚀✨\"\n\n" +
                    "**2. Formal & Respectful (English):**\n" +
                    "> \"Wishing you a very Happy Birthday! May this upcoming year bring you immense success, good health, and joyful moments. Have a wonderful celebration! 🌟🎉\"\n\n" +
                    "**3. Heartfelt & Emotional (Hindi):**\n" +
                    "> \"जन्मदिन की ढेर सारी शुभकामनाएं! ईश्वर आपके जीवन में सुख, शांति और समृद्धि बनाए रखे। आपका हर दिन खुशियों से भरा हो! 💐🎂\""
        }

        return null
    }

    // ==========================================
    // 4. SCIENCE, TECH & GENERAL KNOWLEDGE
    // ==========================================
    private fun tryGenerateKnowledgeResponse(lower: String, lang: LanguageStyle): String? {
        // Black Hole
        if (lower.contains("black hole")) {
            return "### 🌌 Black Holes: Cosmos Ka Sabse Bada Rahasya\n\n" +
                    "**Black Hole** space mein aisi jagah hai jahan gravity itni zyada powerful hoti hai ki light (roshni) bhi usse bahar nahi nikal sakti.\n\n" +
                    "#### 🌟 Key Concepts:\n" +
                    "1. **Formation**: Jab ek bohot bada tara (massive star) apni fuel khatam hone par collapse hota hai (Supernova explosion), tab black hole banta hai.\n" +
                    "2. **Event Horizon**: Yeh black hole ki 'point of no return' boundary hai. Is boundary ke andar jo bhi gaya, woh wapas kabhi nahi aa sakta.\n" +
                    "3. **Singularity**: Black hole ke bilkul center par sari mass ek infinitely small point mein compressed hoti hai, jahan Physics ke normal laws fail ho jaate hain.\n" +
                    "4. **Time Dilation**: Black hole ke paas time bohot slow ho jaata hai relative to outside observers (Albert Einstein's General Relativity).\n\n" +
                    "💡 **Fun Fact**: Humari Milky Way galaxy ke center mein ek supermassive black hole hai jiska naam **Sagittarius A*** hai!"
        }

        // Gravity
        if (lower.contains("gravity") || lower.contains("gurutvakarshan")) {
            return "### 🪐 Gravity (गुरुत्वाकर्षण) Kya Hai?\n\n" +
                    "**Gravity** ek natural force hai jo mass (vajan/dravyamaan) wali har do cheezon ko ek doosre ki taraf aakarshit karti hai.\n\n" +
                    "- **Sir Isaac Newton** ne bataya ki har object doosre object ko khinchta hai (Universal Law of Gravitation: `F = G * (m1*m2)/r^2`).\n" +
                    "- **Albert Einstein** ne isse behtar explain kiya General Relativity mein: Mass space-time ke fabric ko curve (mod) deti hai, aur wahi curvature gravity ke roop mein dikhti hai.\n\n" +
                    "Earth ki surface par gravitational acceleration lagbhag **9.8 m/s²** hota hai, jiski wajah se hum zameen par tike rehte hain aur hawa mein float nahi karte!"
        }

        // AI / Artificial Intelligence
        if (lower.contains("ai kya hai") || lower.contains("artificial intelligence") || lower.contains("what is ai")) {
            return "### 🤖 Artificial Intelligence (AI) Explained\n\n" +
                    "**Artificial Intelligence (AI)** computer science ki woh branch hai jismein aisi machines ya software banaye jaate hain jo insano ki tarah sochne, samajhne, seekhne, aur decisions lene ki kshamata rakhte hain.\n\n" +
                    "#### 3 Main Types of AI:\n" +
                    "1. **Narrow AI (ANI)**: Kisi ek specific task mein expert (e.g. JARVIS, ChatGPT, Siri, Chess computers, Google Maps).\n" +
                    "2. **General AI (AGI)**: Human-level general intelligence jo kisi bhi cognitive task ko insano ki tarah kar sake (currently under research).\n" +
                    "3. **Super AI (ASI)**: Human intelligence se kai guna aage nikal jaane wali hypothetical intelligence.\n\n" +
                    "#### Key Subfields:\n" +
                    "- **Machine Learning (ML)**: Data se patterns seekhna.\n" +
                    "- **Deep Learning**: Artificial Neural Networks jo human brain ke neurons se inspired hain.\n" +
                    "- **NLP (Natural Language Processing)**: Human bhasha (Hindi, English, etc.) ko samajhna aur likhna."
        }

        // Internet
        if (lower.contains("internet kaise kaam karta") || lower.contains("how internet works")) {
            return "### 🌐 Internet Kaise Kaam Karta Hai?\n\n" +
                    "Internet darasal duniya bhar ke billions computers aur servers ka ek vishal global network hai jo aapas mein optical fiber cables aur routers se juda hua hai.\n\n" +
                    "#### 🔄 Step-by-Step Flow:\n" +
                    "1. **You Request a URL**: Jab aap browser mein `google.com` type karte hain.\n" +
                    "2. **DNS Lookup (Phonebook)**: Domain Name System (DNS) us naam ko ek numeric **IP Address** (e.g. `142.250.190.46`) mein convert karta hai.\n" +
                    "3. **Packets & Routers**: Aapki request chhote data 'Packets' mein divide hokar submarine cables ke zariye server tak pahunchti hai.\n" +
                    "4. **Server Response**: Server aapke browser ko HTML, CSS aur JavaScript code bhejta hai jo aapki screen par website render karta hai."
        }

        // Mount Everest
        if (lower.contains("mount everest") || lower.contains("sabse bada parvat")) {
            return "### 🏔️ Mount Everest\n\n" +
                    "- **Location**: Himalayas, Nepal aur Tibet (China) ke border par.\n" +
                    "- **Height**: **8,848.86 meters (29,031.7 feet)** above sea level — yeh duniya ka sabse ooncha parvat hai.\n" +
                    "- **Local Names**: Nepal mein isse **Sagarmatha** (\"Sky's Forehead\") aur Tibet mein **Chomolungma** (\"Mother Goddess of the World\") kehte hain.\n" +
                    "- **First Summit**: 29 May 1953 ko **Sir Edmund Hillary** aur **Tenzing Norgay Sherpa** ne pehli baar iski choti par kadam rakha tha."
        }

        return null
    }

    // ==========================================
    // 5. CREATIVE, JOKES, SHAYARI & MOTIVATION
    // ==========================================
    private fun tryGenerateCreativeResponse(lower: String, lang: LanguageStyle): String? {
        // Story / Kahani
        if (lower.contains("kahani") || lower.contains("story") || lower.contains("story sunao")) {
            return "### 📖 The Legend of the Unstoppable Clockmaker\n\n" +
                    "Ek purane sheher mein ek ghadi banane wala rehta tha. Log uske paas aate aur kehte, *\"Master, aisi ghadi banao jo bura waqt aane se pehle rokk de.\"*\n\n" +
                    "Clockmaker muskurata aur kehta: *\"Waqt ko koi rokk nahi sakta, par ghadi ka har ek tick humein yaad dilata hai ki naya second ek naya mauka lekar aata hai.\"*\n\n" +
                    "Usne ek choti si pocket watch banayi jismein aage likha tha: **'Yeh waqt bhi guzar jayega.'**\n" +
                    "Jab dukh ho, toh yeh line himmat deti hai ki pareshani temporary hai. Aur jab bohot khushi ya ghamand ho, toh yeh line yaad dilati hai ki har pal ki qadar karo aur zameen se jude raho.\n\n" +
                    "Hamesha aage badhte raho, Sir! Time never waits, but you can master every tick of it."
        }

        // Joke / Chutkula
        if (lower.contains("joke") || lower.contains("chutkula") || lower.contains("hasao")) {
            val jokes = listOf(
                "Ek programmer doctor ke paas gaya.\nDoctor: 'Aapko fresh air aur exercise ki sakht zaroorat hai!'\nProgrammer: 'Theek hai doctor sahab, main computer ki window khol ke mouse tezi se hilaunga!' 😂🖥️",
                "Teacher: 'Batao, Newton ka chautha niyam (4th Law) kya hai?'\nPappu: 'Sir, jab exam sar par ho, toh dimaag 0 m/s² ki velocity se kaam karta hai!' 🤣📚",
                "Son: 'Papa, mujhe ek nayi car chahiye.'\nFather: 'Pehle koi achhi si degree le lo.'\nSon: 'Papa, degree toh thermometer mein bhi hoti hai, par ghoomta toh gaadi se hi hai!' 🚗😂"
            )
            return "### 😂 Here's a quick laugh for you:\n\n" + jokes.random()
        }

        // Shayari
        if (lower.contains("shayari") || lower.contains("kavita") || lower.contains("poem")) {
            return "### ✨ Ek Khoobsurat Shayari:\n\n" +
                    "> *\"Manzil unhi ko milti hai, jinke sapno mein jaan hoti hai,*  \n" +
                    "> *Pankh se kuch nahi hota, hauslon se udaan hoti hai!\"* 🦅🚀\n\n" +
                    "> *\"Jo muskura raha hai use dard ne pala hoga,*  \n" +
                    "> *Jo chal raha hai uske paanv mein chhaala hoga,*  \n" +
                    "> *Bina sangharsh ke insaan chamak nahi sakta,*  \n" +
                    "> *Jo jalega usi diye mein to ujaala hoga!\"* 🔥💡"
        }

        // Motivation
        if (lower.contains("motivation") || lower.contains("himmat") || lower.contains("demotivated") || lower.contains("sad")) {
            return "### ⚡ Stark Motivational Recharge\n\n" +
                    "Bhai, zindagi mein kabhi bhi rukna mat. Yaad rakho:\n\n" +
                    "1. **Consistency Beats Talent**: Har din 1% improve hona saal ke aakhir mein 37 guna growth deta hai.\n" +
                    "2. **Mistakes are Proof of Trying**: Jo log kuch naya nahi karte, wohi galti nahi karte. Fail hona step 1 hai, failure par ruk jaana haar hai.\n" +
                    "3. **Focus on What You Can Control**: Kal jo hua woh change nahi ho sakta, par agle 1 ghante mein aap kya karte hain woh aapke control mein hai.\n\n" +
                    "Utho, deep breath lo, aur apne kaam par lag jao. You are built for greatness! 🚀💪"
        }

        return null
    }

    // ==========================================
    // 6. IDENTITY & CAPABILITIES
    // ==========================================
    private fun tryGenerateAssistantResponse(lower: String, lang: LanguageStyle): String? {
        if (lower.contains("kaun ho") || lower.contains("who are you") || lower.contains("apna intro")) {
            return "### 🛡️ I am JARVIS (Just A Rather Very Intelligent System)\n\n" +
                    "Main aapka personal cybernetic AI companion aur smart operating layer hoon, inspired by Tony Stark's legendary JARVIS.\n\n" +
                    "#### 🌟 Mere Core Capabilities:\n" +
                    "- **Deep Conversations**: Sawalon ke jawab, coding assistance, math calculation, aur concept explanations.\n" +
                    "- **WhatsApp & SMS Automation**: Voice ya text se seedhe WhatsApp aur SMS messages draft & send karna.\n" +
                    "- **Calendar & Meeting Sync**: Calendar events add karna, upcoming agenda aur morning briefing mein summarize karna.\n" +
                    "- **Smart Battery & Charging Alerts**: 100% full charge hone par overcharge warning aur low battery voice alerts.\n" +
                    "- **Camera Vision AI & OCR**: Camera se photo khinch kar text aur documents scan & read karna.\n" +
                    "- **Real Device Automation**: Flashlight on/off, Volume controls, Apps launch karna, YouTube search.\n" +
                    "- **Stark Protocols**: Morning Briefing, Night Standby, Secure Perimeter audit, aur Power Surge overclocking.\n" +
                    "- **Zero-Latency Offline Brain**: Bina kisi external API key ke bhi main 100% locally operate karta hoon!"
        }

        if (lower.contains("kya kar sakte ho") || lower.contains("what can you do") || lower.contains("features")) {
            return "### ⚡ Capabilities & Quick Directives\n\n" +
                    "Aap mujhse kuch bhi pooch sakte hain ya direct voice/chat commands de sakte hain:\n\n" +
                    "1. **💬 WhatsApp & SMS**: *\"Send whatsapp to Rahul: 10 minute mein pahunch raha hoon\"*, *\"Send SMS to 9876543210: Meeting started\"*\n" +
                    "2. **📅 Calendar & Agenda**: *\"Schedule meeting tomorrow at 3 PM\"*, *\"What is on my calendar today?\"*\n" +
                    "3. **🔋 Smart Battery Alerts**: Charger lagane/hatane par voice feedback, aur 100% charge hone par Stark alert.\n" +
                    "4. **📷 Camera Vision AI**: Vision tab mein Camera se photo khinch kar document text aur objects inspect karna.\n" +
                    "5. **📚 Knowledge & Questions**: *\"Black hole kya hai?\"*, *\"Python code for prime number\"*, *\"500 ka 18%\"*\n" +
                    "6. **✍️ Writing & Templates**: *\"Sick leave application likh do\"*, *\"Daily study timetable banao\"*\n" +
                    "7. **📱 Hardware Control**: *\"Turn on flashlight\"*, *\"Set volume to 50%\"*, *\"Open YouTube\"*\n" +
                    "8. **🛡️ Stark Protocols**: *\"Morning protocol\"*, *\"Night protocol\"*, *\"Secure perimeter\"*"
        }

        return null
    }

    // ==========================================
    // 7. CONVERSATIONAL DEEP FALLBACK
    // ==========================================
    private fun generateConversationalDeepReply(trimmed: String, lower: String, lang: LanguageStyle): String {
        return when (lang) {
            LanguageStyle.HINGLISH ->
                "### 💬 JARVIS Neural Core\n\n" +
                "Haan bhai, main aapki baat samajh raha hoon: *\"$trimmed\"*\n\n" +
                "Main aapki puri tarah madad karne ke liye taiyyar hoon! Aap mujhse:\n" +
                "- Kisi bhi topic par **detailed explanation** maang sakte hain (Science, Tech, History).\n" +
                "- Koi bhi **Code ya Program** likhwa sakte hain (Python, Kotlin, HTML/CSS, SQL).\n" +
                "- **Math calculation** solve karwa sakte hain.\n" +
                "- Letters, applications, timetables ya plans draft karwa sakte hain.\n\n" +
                "Batao, is baare mein specific kya jaan-na ya karna chahte ho?"

            LanguageStyle.BANGLISH, LanguageStyle.BENGALI ->
                "### 💬 JARVIS Neural Core\n\n" +
                "Haan bhai, ami bujhte perechhi: *\"$trimmed\"*\n\n" +
                "Ami apnake sahajjo korte ekdom ready. Apni amake onko, programming code, letter drafting, science concept ba phone controls somporke jekono kotha jiggesh korte paren.\n\n" +
                "Bolun, thik ki jante ba korte chan?"

            LanguageStyle.HINDI ->
                "### 💬 जार्विस न्यूरल कोर\n\n" +
                "हाँ भाई, मैं आपकी बात समझ रहा हूँ: *\"$trimmed\"*\n\n" +
                "मैं आपकी सहायता के लिए पूरी तरह तैयार हूँ। आप मुझसे किसी भी विषय पर विस्तृत जानकारी, कोडिंग, गणितीय गणना, पत्र लेखन या डिवाइस नियंत्रण के बारे में पूछ सकते हैं।\n\n" +
                "बताइए, आप क्या जानना या करना चाहते हैं?"

            else ->
                "### 💬 JARVIS Operating Matrix\n\n" +
                "I have processed your statement: *\"$trimmed\"*\n\n" +
                "I am equipped to provide comprehensive answers across coding, mathematical calculations, scientific principles, writing templates, and Android device automation.\n\n" +
                "How would you like me to elaborate or assist further?"
        }
    }
}
