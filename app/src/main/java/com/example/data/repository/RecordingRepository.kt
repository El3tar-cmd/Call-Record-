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

    // Remove seed prepopulation and clear any existing seeds from first installation
    suspend fun prepopulateIfEmpty() {
        val sharedPrefs = context.getSharedPreferences("call_recorder_prefs", Context.MODE_PRIVATE)
        val hasCleanedSeeds = sharedPrefs.getBoolean("cleaned_seeds_v2", false)
        if (!hasCleanedSeeds) {
            try {
                val current = allRecordings.first()
                for (rec in current) {
                    if (rec.filePath.contains("seed_")) {
                        delete(rec)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            sharedPrefs.edit()
                .putBoolean("cleaned_seeds_v2", true)
                .putBoolean("prepopulated_v1", true)
                .apply()
        }
    }
}
