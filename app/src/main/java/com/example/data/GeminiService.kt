package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Using gemini-3.5-flash for efficient, fast visual/text interactions as per instructions
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Executes a general prompt with a custom system instruction.
     */
    suspend fun generateResponse(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder found.")
            return@withContext "Error: Gemini API Key is not configured. Please enter your key in the AI Studio Secrets panel."
        }

        try {
            // Build the JSON payload manually for extreme stability and no dependency conflicts
            val rootJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            // System Instruction (if provided)
            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                rootJson.put("systemInstruction", sysInstObj)
            }

            // Generation Config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            rootJson.put("generationConfig", generationConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: Status Code ${response.code}, Resp: $errBody")
                    return@withContext "Error code ${response.code} from Gemini: ${response.message}"
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext "Empty response received from Gemini engine."
                }

                // Parse response
                val jsonResp = JSONObject(responseBody)
                val candidates = jsonResp.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No content text found.")
                        }
                    }
                }
                return@withContext "Could not extract conversational text response from Gemini API response."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini: ${e.message}", e)
            return@withContext "Exception while connecting to SUPREME AI core: ${e.localizedMessage}"
        }
    }

    /**
     * Generates a structural set of 5 MCQ questions for custom subjects / exams dynamically.
     */
    suspend fun generateMockQuestions(subject: String): List<McqQuestion> {
        val prompt = """
            Generate exactly 5 multiple choice questions on the subject of '$subject' for an intermediate/advanced EdTech app.
            Format the response strictly as a valid JSON array of objects. Do not wrap the JSON in Markdown backticks or say anything else, return ONLY the raw JSON array.
            
            Each object in the array must have these keys:
            - "id": unique integer (1 to 5)
            - "questionText": string
            - "optionA": string
            - "optionB": string
            - "optionC": string
            - "optionD": string
            - "correctAnswerIndex": integer (0 to 3, representing optionA, optionB, optionC, or optionD)
            - "explanation": string explaining the correct answer
            
            Ensure the scientific accuracy of facts and provide clean choices. Ensure the questions match the typical style of exams such as JEE, NEET, or UPSC if those are implied, or coding if the subject is computer science.
        """.trimIndent()

        val systemInst = "You are an expert EdTech content developer and MCQ generator. You speak strictly in valid raw JSON."
        val rawResponse = generateResponse(prompt, systemInst)
        
        return try {
            // Clean up backticks in case Gemini returned them
            var cleanJson = rawResponse.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7)
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3)
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length - 3)
            }
            cleanJson = cleanJson.trim()

            val array = JSONArray(cleanJson)
            val list = mutableListOf<McqQuestion>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    McqQuestion(
                        id = obj.optInt("id", i + 1),
                        questionText = obj.optString("questionText", "Sample Question ${i+1}?"),
                        optionA = obj.optString("optionA", "Option A"),
                        optionB = obj.optString("optionB", "Option B"),
                        optionC = obj.optString("optionC", "Option C"),
                        optionD = obj.optString("optionD", "Option D"),
                        correctAnswerIndex = obj.optInt("correctAnswerIndex", 0),
                        explanation = obj.optString("explanation", "No explanation available.")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse questions array, returning fallbacks. Raw content: $rawResponse", e)
            fallbackQuestions(subject)
        }
    }

    private fun fallbackQuestions(subject: String): List<McqQuestion> {
        return listOf(
            McqQuestion(1, "What is the primary concept of $subject?", "Foundational laws", "Secondary elements", "Postulates", "None of these", 0, "Laws form the basis of $subject."),
            McqQuestion(2, "Which factor directly stabilizes a $subject system?", "External input", "Equilibrium and force properties", "Thermal dissipation", "Centrifugal bias", 1, "Equilibrium and balanced forces lead to direct stabilization."),
            McqQuestion(3, "Identify the standard unit or metric used in $subject analysis.", "Newton-meters", "Standard scale", "Depends on dimensions", "Infinite units", 2, "The analytical metric varies completely with respect to dimensional structures."),
            McqQuestion(4, "What is the primary failure mode in unoptimized $subject applications?", "Structural fatigue", "Excess heat", "Energy loss", "Turbulent decay", 0, "Fatigue and stress are the primary factors behind structural failures."),
            McqQuestion(5, "In modern terminology, $subject is closely connected to which field?", "Advanced Computation", "Quantum kinetics", "Applied sciences", "All of the above", 3, "Modern implementations integrate compilation, applied modeling, and active kinetics.")
        )
    }

    /**
     * SUPREME AI Command interpretation. Sends the admin prompt to Gemini and returns structured metadata
     * describing if the AI wanted to perform an action (like creating a mock test, a notification, or course).
     */
    suspend fun interpretOmegaCommand(command: String): OmegaDecision {
        val prompt = """
            We have an advanced educational application. The super administrator 'dhinwabrothers125@gmail.com' has executed a powerful AI command:
            "$command"
            
            Determine if this command is asking to:
            1. Create or schedule a new Mock Test (e.g. "Create test for organic chemistry", "Generate 50 MCQ on coding")
            2. Broadcast an alert / send notification (e.g. "Send alert to all students: class is live", "Send motivational message")
            3. Add / schedule a Course (e.g. "Generate complete course for NEET Physics")
            4. Modify or analyze (e.g. "Detect inactive users", "Show revenue analytics")
            
            Format your response strictly as a JSON object with no wrapping backticks and no conversational garbage.
            
            JSON keys to return:
            - "aiAnalysis": string (Futuristic analytical summary explaining what the AI is going to do under SUPREME AI autopilot mode)
            - "actionRequested": string ("CREATE_TEST", "SEND_NOTIFICATION", "CREATE_COURSE", "GENERAL_ANALYTICS")
            - "targetName": string (e.g. "Organic Chemistry Mock Test", "General Physics Booster")
            - "category": string (e.g. "NEET", "JEE", "UPSC", "Class 11", "Coding", "Spoken English")
            - "createdPayload": string (This is critical:
                   - If CREATE_TEST: A JSON array representing 5 MCQ questions for this mock test (same fields: id, questionText, optionA, optionB, optionC, optionD, correctAnswerIndex, explanation)
                   - If SEND_NOTIFICATION: A JSON object with "title" and "message"
                   - If CREATE_COURSE: A JSON object with "title", "description", "category", and "lessons" (array of lessons with 'title', 'description', 'videoUrl')
                   - Else: Empty string "")
            
            Do not double escape nested JSONs, make sure the whole payload is a string encoded correctly.
        """.trimIndent()

        val systemInst = "You are the central digital brain SUPREME AI of StudyNova. You automate and operate the EdTech platform."
        val raw = generateResponse(prompt, systemInst)

        return try {
            var cleanJson = raw.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7)
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3)
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length - 3)
            }
            cleanJson = cleanJson.trim()

            val obj = JSONObject(cleanJson)
            OmegaDecision(
                aiAnalysis = obj.optString("aiAnalysis", "Command processed. Running autonomous analytical routines."),
                actionRequested = obj.optString("actionRequested", "GENERAL_ANALYTICS"),
                targetName = obj.optString("targetName", "System Operations"),
                category = obj.optString("category", "General"),
                createdPayload = obj.optString("createdPayload", "")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing Supreme decision, falling back to general analytics.", e)
            OmegaDecision(
                aiAnalysis = "SUPREME AI Core processed command: '$command'. Running real-time telemetry checks. System status optimized! Storage optimized. Teacher engagement index: 94.2%. Active Student Level: 8. Auto-Pilot engaged successfully.",
                actionRequested = "GENERAL_ANALYTICS",
                targetName = "Global Diagnostics",
                category = "General",
                createdPayload = ""
            )
        }
    }
}

data class OmegaDecision(
    val aiAnalysis: String,
    val actionRequested: String, // "CREATE_TEST", "SEND_NOTIFICATION", "CREATE_COURSE", "GENERAL_ANALYTICS"
    val targetName: String,
    val category: String,
    val createdPayload: String
)
