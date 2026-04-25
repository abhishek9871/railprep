package com.railprep.feature.tests.offline

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "local_attempt_meta")
data class LocalAttemptMetaEntity(
    @PrimaryKey @ColumnInfo(name = "attempt_id") val attemptId: String,
    @ColumnInfo(name = "test_id")             val testId: String,
    @ColumnInfo(name = "deadline_epoch_ms")   val deadlineEpochMs: Long,
    @ColumnInfo(name = "last_question_index") val lastQuestionIndex: Int = 0,
    @ColumnInfo(name = "bilingual_is_hi")     val bilingualIsHi: Boolean = false,
    @ColumnInfo(name = "last_sync_ms")        val lastSyncMs: Long = 0L,
)

@Entity(tableName = "local_attempt_answer", primaryKeys = ["attempt_id", "question_id"])
data class LocalAttemptAnswerEntity(
    @ColumnInfo(name = "attempt_id")         val attemptId: String,
    @ColumnInfo(name = "question_id")        val questionId: String,
    @ColumnInfo(name = "selected_option_id") val selectedOptionId: String?,
    @ColumnInfo(name = "flagged")            val flagged: Boolean = false,
    @ColumnInfo(name = "answered_at_epoch_ms") val answeredAtEpochMs: Long,
    @ColumnInfo(name = "synced")             val synced: Boolean = false,
)

@Dao
interface AttemptLocalDao {
    @Query("select * from local_attempt_meta where attempt_id = :attemptId")
    suspend fun getMeta(attemptId: String): LocalAttemptMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(row: LocalAttemptMetaEntity)

    @Query("update local_attempt_meta set last_question_index = :idx where attempt_id = :attemptId")
    suspend fun setIndex(attemptId: String, idx: Int)

    @Query("update local_attempt_meta set bilingual_is_hi = :isHi where attempt_id = :attemptId")
    suspend fun setBilingual(attemptId: String, isHi: Boolean)

    @Query("update local_attempt_meta set last_sync_ms = :ms where attempt_id = :attemptId")
    suspend fun setLastSync(attemptId: String, ms: Long)

    @Query("select * from local_attempt_answer where attempt_id = :attemptId")
    suspend fun listAnswers(attemptId: String): List<LocalAttemptAnswerEntity>

    @Query("select count(*) from local_attempt_answer where attempt_id = :attemptId and synced = 0")
    suspend fun countUnsynced(attemptId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnswer(row: LocalAttemptAnswerEntity)

    @Query("update local_attempt_answer set synced = 1 where attempt_id = :attemptId and question_id = :questionId")
    suspend fun markSynced(attemptId: String, questionId: String)

    @Query("delete from local_attempt_answer where attempt_id = :attemptId")
    suspend fun clearAnswers(attemptId: String)

    @Query("delete from local_attempt_meta where attempt_id = :attemptId")
    suspend fun clearMeta(attemptId: String)
}

@Database(
    entities = [LocalAttemptMetaEntity::class, LocalAttemptAnswerEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AttemptLocalDatabase : RoomDatabase() {
    abstract fun dao(): AttemptLocalDao
}
