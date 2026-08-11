package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole(val labelAr: String, val labelEn: String) {
    SUPERVISOR("مشرف النظام", "Supervisor"),
    PROFESSOR("أستاذ / محاضر", "Professor"),
    STUDENT("طالب", "Student")
}

enum class AttendanceStatus(val labelAr: String, val labelEn: String, val colorHex: Long) {
    PRESENT("حاضر", "Present", 0xFF10B981),
    LATE("متأخر", "Late", 0xFFF59E0B),
    ABSENT("غائب", "Absent", 0xFFEF4444)
}

enum class AttendanceMethod(val labelAr: String, val labelEn: String) {
    FACE_RECOGNITION("بصمة الوجه", "Face Recognition"),
    QR_SCAN("رمز QR", "QR Code"),
    MANUAL("إدخال يدوي", "Manual Entry")
}

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val universityId: String, // الرقم الجامعي
    val batch: String,        // الدفعة / الفرقة الدراسية
    val email: String,
    val parentEmail: String,
    val photoUri: String? = null,
    val faceVector: String = "", // Comma-separated float embeddings
    val qrCodeData: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val universityId: String,
    val batch: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // YYYY-MM-DD
    val method: AttendanceMethod = AttendanceMethod.FACE_RECOGNITION,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val isSynced: Boolean = true
)

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val recipientEmail: String? = null,
    val studentName: String? = null,
    val type: String = "INFO", // ATTENDANCE, ABSENCE_ALERT, SYSTEM
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class BatchSummary(
    val batchName: String,
    val totalStudents: Int,
    val presentCount: Int,
    val lateCount: Int,
    val absentCount: Int
) {
    val attendancePercentage: Float
        get() = if (totalStudents > 0) ((presentCount + lateCount).toFloat() / totalStudents) * 100f else 0f
}
