package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceMethod
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.BatchSummary
import com.example.data.model.NotificationLog
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val notificationDao: NotificationDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudentsFlow()
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecordsFlow()
    val allNotifications: Flow<List<NotificationLog>> = notificationDao.getAllNotificationsFlow()
    val studentCount: Flow<Int> = studentDao.getStudentCountFlow()

    fun getTodayString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(Date())
    }

    fun getTodayRecords(): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecordsByDateFlow(getTodayString())
    }

    fun searchStudents(query: String): Flow<List<Student>> {
        return studentDao.searchStudents(query)
    }

    suspend fun getStudentById(id: String): Student? {
        return studentDao.getStudentById(id)
    }

    suspend fun getStudentByUniversityId(universityId: String): Student? {
        return studentDao.getStudentByUniversityId(universityId)
    }

    suspend fun addStudent(student: Student) {
        studentDao.insertStudent(student)
        notificationDao.insertNotification(
            NotificationLog(
                title = "طالب جديد",
                message = "تم تسجيل الطالب ${student.name} (${student.universityId}) بنجاح ببيانات الوجه.",
                type = "SYSTEM"
            )
        )
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    suspend fun markAttendance(
        student: Student,
        method: AttendanceMethod,
        status: AttendanceStatus = AttendanceStatus.PRESENT
    ): AttendanceRecord {
        val today = getTodayString()
        
        // Check if student already checked in today
        val existing = attendanceDao.getTodayRecordForStudent(today, student.id)
        if (existing != null) {
            return existing
        }

        val record = AttendanceRecord(
            studentId = student.id,
            studentName = student.name,
            universityId = student.universityId,
            batch = student.batch,
            timestamp = System.currentTimeMillis(),
            dateString = today,
            method = method,
            status = status,
            isSynced = true
        )

        attendanceDao.insertRecord(record)

        // Log notification
        val statusTextStr = when(status) {
            AttendanceStatus.PRESENT -> "حضور"
            AttendanceStatus.LATE -> "حضور متأخر"
            AttendanceStatus.ABSENT -> "غياب"
        }

        val methodTextStr = when(method) {
            AttendanceMethod.FACE_RECOGNITION -> "بصمة الوجه"
            AttendanceMethod.QR_SCAN -> "كود QR"
            AttendanceMethod.MANUAL -> "الإدخال اليدوي"
        }

        notificationDao.insertNotification(
            NotificationLog(
                title = "تسجيل $statusTextStr",
                message = "تم تسجل $statusTextStr للطالب ${student.name} (${student.universityId}) عبر $methodTextStr بنجاح.",
                recipientEmail = student.email,
                studentName = student.name,
                type = "ATTENDANCE"
            )
        )

        return record
    }

    suspend fun triggerAbsentAutoAlerts(): Int {
        val today = getTodayString()
        val students = studentDao.getAllStudentsFlow().first()
        val todayRecords = attendanceDao.getRecordsByDateFlow(today).first()

        val checkedInStudentIds = todayRecords.map { it.studentId }.toSet()
        val absentStudents = students.filter { !checkedInStudentIds.contains(it.id) }

        var countAlerts = 0
        absentStudents.forEach { student ->
            // Record absent status
            val absentRecord = AttendanceRecord(
                studentId = student.id,
                studentName = student.name,
                universityId = student.universityId,
                batch = student.batch,
                timestamp = System.currentTimeMillis(),
                dateString = today,
                method = AttendanceMethod.MANUAL,
                status = AttendanceStatus.ABSENT,
                isSynced = true
            )
            attendanceDao.insertRecord(absentRecord)

            // Send parent alert notification
            notificationDao.insertNotification(
                NotificationLog(
                    title = "إشعار غياب ولي الأمر",
                    message = "عزيزي ولي الأمر، نود إحاطتكم بغياب الطالب/ـة ${student.name} (${student.universityId}) عن المحاضرات لهذا اليوم $today.",
                    recipientEmail = student.parentEmail,
                    studentName = student.name,
                    type = "ABSENCE_ALERT"
                )
            )
            countAlerts++
        }

        return countAlerts
    }

    suspend fun getBatchSummaries(): List<BatchSummary> {
        val students = studentDao.getAllStudentsFlow().first()
        val todayRecords = attendanceDao.getRecordsByDateFlow(getTodayString()).first()

        val batches = students.map { it.batch }.distinct()
        val recordMap = todayRecords.associateBy { it.studentId }

        return batches.map { batchName ->
            val batchStudents = students.filter { it.batch == batchName }
            var present = 0
            var late = 0
            var absent = 0

            batchStudents.forEach { student ->
                val rec = recordMap[student.id]
                when (rec?.status) {
                    AttendanceStatus.PRESENT -> present++
                    AttendanceStatus.LATE -> late++
                    AttendanceStatus.ABSENT -> absent++
                    null -> absent++ // Unchecked considered absent in daily total
                }
            }

            BatchSummary(
                batchName = batchName,
                totalStudents = batchStudents.size,
                presentCount = present,
                lateCount = late,
                absentCount = absent
            )
        }
    }

    suspend fun deleteAttendanceRecord(recordId: Long) {
        attendanceDao.deleteRecord(recordId)
    }

    suspend fun clearNotifications() {
        notificationDao.clearAllNotifications()
    }
}
