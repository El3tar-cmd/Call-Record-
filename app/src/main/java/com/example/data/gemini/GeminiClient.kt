package com.example.data.gemini

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

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    var apiKey: String = ""

    /**
     * Checks if the Gemini API key is configured and not the placeholder.
     */
    fun isKeyConfigured(): Boolean {
        val key = apiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    /**
     * Generates a realistic transcript using Gemini based on call parameters (simulated transcription engine)
     */
    suspend fun generateTranscript(
        callerName: String,
        source: String,
        durationSec: Int,
        userNotes: String?
    ): String = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            return@withContext getOfflineMockTranscript(callerName, source, durationSec, userNotes)
        }

        val prompt = """
            اكتب نص حوار (ترجمة/تفريغ صوتي) مفصل واحترافي باللغة العربية لمكالمة هاتفية مسجلة بالكامل.
            المعلومات المتاحة عن المكالمة:
            - اسم الطرف الآخر: $callerName
            - منصة الاتصال: $source (مثال: WhatsApp, Messenger, Cellular)
            - مدة المكالمة بالثواني: $durationSec ثانية
            - ملاحظات المستخدم أو سياق المكالمة: ${userNotes ?: "لا توجد ملاحظات إضافية"}

            الشروط والتعليمات:
            1. يجب أن يبدأ الحوار بتحية وينتهي بختام منطقي ومناسب للمدة والسياق.
            2. استخدم تنسيق المتحدثين بوضوح مثل:
               "المتصل ($callerName): [نص الكلام]"
               "أنت: [نص الكلام]"
            3. اجعل الحوار يبدو واقعياً جداً واحترافياً يحتوي على تفاصيل دقيقة وتفاعلية تناسب مدة المكالمة ($durationSec ثانية).
            4. اكتب الحوار باللغة العربية الفصحى المبسطة أو اللهجة المصرية/الخليجية البيضاء المفهومة جداً.
            5. لا تذكر أي نصوص تمهيدية أو استهلالية خارج نص الحوار نفسه. ابدأ بكتابة تفريغ المكالمة مباشرة.
        """.trimIndent()

        try {
            return@withContext callGeminiApi(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating transcript via Gemini", e)
            return@withContext getOfflineMockTranscript(callerName, source, durationSec, userNotes)
        }
    }

    /**
     * Summarizes, gets sentiment, and extracts important points from a transcript.
     */
    suspend fun analyzeTranscript(transcript: String): AnalysisResult = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            return@withContext getOfflineMockAnalysis(transcript)
        }

        val prompt = """
            قم بتحليل تفريغ المكالمة الهاتفية التالي بدقة واستخرج النتائج باللغة العربية.
            
            نص تفريغ المكالمة:
            \"\"\"
            $transcript
            \"\"\"

            المطلوب هو إرجاع النتيجة بتنسيق JSON حصرياً وصالح للاستخدام البرمجي مباشرة (ولا تضع أي وسوم markdown مثل ```json أو أي نصوص قبل أو بعد الـ JSON).
            يجب أن يحتوي الـ JSON على المفاتيح التالية تماماً وباللغة العربية للقيم:
            {
               "summary": "ملخص شامل وذكي للمكالمة في سطرين أو ثلاثة",
               "sentiment": "اختر واحدة فقط من القيم التالية: 'إيجابي' أو 'متعادل' أو 'سلبي'",
               "importantPoints": "قائمة نقطية منسقة تفصل القرارات المتخذة أو النقاط الهامة والخطوات التالية"
            }
        """.trimIndent()

        try {
            val jsonResponse = callGeminiApi(prompt)
            // Parse response (clean markdown wrappers if the model ignored instructions)
            val cleanedJson = jsonResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanedJson)
            return@withContext AnalysisResult(
                summary = jsonObject.optString("summary", "لم يتم توليد الملخص بسب خطأ في الاستجابة."),
                sentiment = jsonObject.optString("sentiment", "متعادل"),
                importantPoints = jsonObject.optString("importantPoints", "• لم يتم استخراج نقاط هامة.")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing transcript via Gemini", e)
            return@withContext getOfflineMockAnalysis(transcript)
        }
    }

    private suspend fun callGeminiApi(prompt: String): String {
        val keyToUse = apiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
        val url = "$BASE_URL?key=$keyToUse"

        val requestPart = JSONObject().put("text", prompt)
        val partArray = JSONArray().put(requestPart)
        val contentObj = JSONObject().put("parts", partArray)
        val contentArray = JSONArray().put(contentObj)
        val requestBodyJson = JSONObject().put("contents", contentArray)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP Error: ${response.code} with message: ${response.message}")
            }
            val responseBodyString = response.body?.string() ?: throw Exception("Empty response body")
            
            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            
            return text
        }
    }

    // Fallbacks for Offline or API key missing
    private fun getOfflineMockTranscript(
        callerName: String,
        source: String,
        durationSec: Int,
        userNotes: String?
    ): String {
        val platformAr = when (source.uppercase()) {
            "WHATSAPP" -> "واتساب"
            "MESSENGER" -> "ماسينجر"
            "CELLULAR" -> "الهاتف العادي"
            else -> "تسجيل عام"
        }
        return """
            المتصل ($callerName): أهلاً بك، أتصل بك بخصوص موضوعنا المشترك عبر $platformAr.
            أنت: مرحباً بك يا $callerName. نعم، تفضل، أنا أستمع إليك.
            المتصل ($callerName): أردت التأكيد على مراجعة الملفات المرسلة والتأكد من إتمام المهمة المطلوبة. مدة مكالمتنا الآن $durationSec ثانية وهو وقت كافٍ لإنهاء هذا الاتفاق.
            أنت: ممتاز، سأقوم بمراجعة الملاحظات ${userNotes ?: "الخاصة بالعمل"} والرد عليك فوراً.
            المتصل ($callerName): رائع، أتمنى لك يوماً سعيداً. مع السلامة.
            أنت: مع السلامة، في أمان الله.
        """.trimIndent()
    }

    private fun getOfflineMockAnalysis(transcript: String): AnalysisResult {
        return AnalysisResult(
            summary = "مكالمة هاتفية للتأكيد على مراجعة الملفات والتنسيق لإنهاء المهام المشتركة بين الطرفين.",
            sentiment = "إيجابي",
            importantPoints = "• مراجعة الملفات المرسلة فوراً.\n• التأكيد على الاتفاق والتنسيق المشترك.\n• إنهاء العمل بحسب الجدول المتفق عليه."
        )
    }

    data class AnalysisResult(
        val summary: String,
        val sentiment: String,
        val importantPoints: String
    )
}
