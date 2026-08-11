package com.example.data.dao

import androidx.room.*
import com.example.data.model.AttendanceRecord
import com.example.data.model.NotificationLog
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: String): Student?

    @Query("SELECT * FROM students WHERE universityId = :universityId LIMIT 1")
    suspend fun getStudentByUniversityId(universityId: String): Student?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR universityId LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: String)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCountFlow(): Flow<Int>
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :date ORDER BY timestamp DESC")
    fun getRecordsByDateFlow(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getStudentRecordsFlow(studentId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :date AND studentId = :studentId LIMIT 1")
    suspend fun getTodayRecordForStudent(date: String, studentId: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE dateString = :date AND status = 'PRESENT'")
    fun getTodayPresentCountFlow(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE dateString = :date AND status = 'LATE'")
    fun getTodayLateCountFlow(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE dateString = :date AND status = 'ABSENT'")
    fun getTodayAbsentCountFlow(date: String): Flow<Int>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLog)

    @Query("UPDATE notification_logs SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAllNotifications()
}
