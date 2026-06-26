package com.example.data.repository

import android.content.Context
import com.example.data.database.Recording
import com.example.data.database.RecordingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File

class RecordingRepository(
    private val context: Context,
    private val recordingDao: RecordingDao
) {
    val allRecordings: Flow<List<Recording>> = recordingDao.getAllRecordings()

    suspend fun getRecordingById(id: Long): Recording? = recordingDao.getRecordingById(id)

    suspend fun insert(recording: Recording): Long = recordingDao.insertRecording(recording)

    suspend fun update(recording: Recording) = recordingDao.updateRecording(recording)

    suspend fun delete(recording: Recording) {
        // Delete physical file if it exists
        try {
            val file = File(recording.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recordingDao.deleteRecording(recording)
    }

    suspend fun deleteById(id: Long) {
        val recording = getRecordingById(id)
        if (recording != null) {
            delete(recording)
        }
    }

    fun search(query: String): Flow<List<Recording>> = recordingDao.searchRecordings(query)

    // Prepopulate some realistic recordings for outstanding first-use experience
    suspend fun prepopulateIfEmpty() {
        val sharedPrefs = context.getSharedPreferences("call_recorder_prefs", Context.MODE_PRIVATE)
        val hasPrepopulated = sharedPrefs.getBoolean("prepopulated_v1", false)
        
        if (!hasPrepopulated) {
            val current = allRecordings.first()
            if (current.isEmpty()) {
                val systemTime = System.currentTimeMillis()
                
                // Seed 1: WhatsApp Call
                val file1 = File(context.filesDir, "seed_whatsapp_1.m4a")
                if (!file1.exists()) { file1.createNewFile() }
                recordingDao.insertRecording(
                    Recording(
                        title = "م. أحمد مصطفى",
                        source = "WHATSAPP",
                        direction = "INBOUND",
                        durationSec = 52,
                        filePath = file1.absolutePath,
                        timestamp = systemTime - 3600 * 1000 * 2, // 2 hours ago
                        isTranscribed = true,
                        transcript = "المتصل (أحمد): السلام عليكم يا هندسة، بخصوص تعديلات لوحة التحكم للتطبيق الجديد.\nأنت: وعليكم السلام يا أحمد، أهلاً بك. نعم، ما هي التعديلات المطلوبة بالضبط؟\nالمتصل (أحمد): نريد تغيير ألوان لوحة المفاتيح لتكون متناسقة مع الهوية البصرية الجديدة، وكذلك تفعيل ميزة تصدير التقارير بصيغة PDF مباشرة.\nأنت: تمام، هذا واضح جداً. سأقوم بإرسال نسخة تجريبية بالتعديلات الجديدة قبل نهاية اليوم.\nالمتصل (أحمد): ممتاز جداً، شكراً لك وجزاك الله خيراً.",
                        summary = "اتصال من المهندس أحمد بخصوص تعديلات لوحة تحكم التطبيق، والاتفاق على تغيير الألوان وتفعيل تصدير ملفات PDF بحلول نهاية اليوم.",
                        sentiment = "إيجابي",
                        importantPoints = "• تغيير ألوان لوحة التحكم لتناسب الهوية الجديدة.\n• إضافة ميزة تصدير التقارير كملفات PDF.\n• تسليم نسخة تجريبية بنهاية اليوم الحالي.",
                        notes = "تحديث هام للمشروع، أحمد يركز على سرعة التنفيذ."
                    )
                )

                // Seed 2: Cellular Call
                val file2 = File(context.filesDir, "seed_cellular_2.m4a")
                if (!file2.exists()) { file2.createNewFile() }
                recordingDao.insertRecording(
                    Recording(
                        title = "شركة الشحن والخدمات",
                        source = "CELLULAR",
                        direction = "OUTBOUND",
                        durationSec = 118,
                        filePath = file2.absolutePath,
                        timestamp = systemTime - 3600 * 1000 * 24, // 1 day ago
                        isTranscribed = true,
                        transcript = "أنت: مرحباً، كنت أستفسر عن موعد وصول الشحنة رقم 90812.\nالمتصل (الدعم): أهلاً بك يا فندم. لحظة واحدة للتحقق... نعم، الشحنة الآن في مركز التوزيع الرئيسي بالقاهرة.\nأنت: متى يتوقع خروجها مع المندوب؟\nالمتصل (الدعم): ستخرج غداً صباحاً إن شاء الله، وسيتواصل معك المندوب على هذا الرقم بين الساعة العاشرة صباحاً والثانية ظهراً.\nأنت: شكراً جزيلاً لك، سأكون بالانتظار.",
                        summary = "مكالمة صادرة إلى خدمة عملاء شركة الشحن للاستفسار عن الشحنة رقم 90812، وتم التأكيد على وصولها غداً صباحاً وتواصل المندوب.",
                        sentiment = "متعادل",
                        importantPoints = "• الشحنة متواجدة حالياً بمركز التوزيع بالقاهرة.\n• موعد التسليم المتوقع: غداً صباحاً.\n• فترة تواصل المندوب: بين 10:00 ص و 2:00 ظ.",
                        notes = "متابعة الاستلام غداً صباحاً."
                    )
                )

                // Seed 3: Messenger Call
                val file3 = File(context.filesDir, "seed_messenger_3.m4a")
                if (!file3.exists()) { file3.createNewFile() }
                recordingDao.insertRecording(
                    Recording(
                        title = "خالد العبدالله",
                        source = "MESSENGER",
                        direction = "INBOUND",
                        durationSec = 35,
                        filePath = file3.absolutePath,
                        timestamp = systemTime - 3600 * 1000 * 48, // 2 days ago
                        isTranscribed = false,
                        transcript = null,
                        summary = null,
                        sentiment = null,
                        importantPoints = null,
                        notes = "مكالمة لم يتم نسخها بعد. انقر فوق 'نسخ الصوت' لتجربة ميزة تحويل الصوت إلى نص بالذكاء الاصطناعي."
                    )
                )
            }
            sharedPrefs.edit().putBoolean("prepopulated_v1", true).apply()
        }
    }
}
