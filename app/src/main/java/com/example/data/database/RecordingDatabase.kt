package com.example.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "recordings")
data class Recording(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val source: String,               // "CELLULAR", "WHATSAPP", "MESSENGER", "MIC"
    val direction: String,            // "INBOUND", "OUTBOUND", "MEMO"
    val durationSec: Int,
    val filePath: String,
    val timestamp: Long,
    val isTranscribed: Boolean = false,
    val transcript: String? = null,
    val summary: String? = null,
    val sentiment: String? = null,    // "إيجابي" (Positive), "متعادل" (Neutral), "سلبي" (Negative)
    val importantPoints: String? = null, // Bulleted key decisions/action points
    val notes: String? = null
)

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<Recording>>

    @Query("SELECT * FROM recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingById(id: Long): Recording?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording): Long

    @Update
    suspend fun updateRecording(recording: Recording)

    @Delete
    suspend fun deleteRecording(recording: Recording)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Long)

    @Query("SELECT * FROM recordings WHERE title LIKE '%' || :query || '%' OR transcript LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecordings(query: String): Flow<List<Recording>>
}

@Database(entities = [Recording::class], version = 1, exportSchema = false)
abstract class RecordingDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile
        private var INSTANCE: RecordingDatabase? = null

        fun getDatabase(context: Context): RecordingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordingDatabase::class.java,
                    "sajil_recordings_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
