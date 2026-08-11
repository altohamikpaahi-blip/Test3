package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceMethod
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.BatchSummary
import com.example.data.model.NotificationLog
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.data.repository.AttendanceRepository
import com.example.util.FaceMatchResult
import com.example.util.FaceMatchingEngine
import com.example.util.Language
import com.example.util.NotificationHelper
import com.example.util.ReportGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AttendanceRepository(
        db.studentDao(),
        db.attendanceDao(),
        db.notificationDao()
    )

    // Language & Role State
    val currentLanguage = MutableStateFlow(Language.ARABIC)
    val currentRole = MutableStateFlow(UserRole.SUPERVISOR)

    // Offline Sync State
    val isOfflineMode = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedBatchFilter = MutableStateFlow("ALL")

    // UI Feedback Message
    val snackbarMessage = MutableStateFlow<String?>(null)

    // Verification Result Bottom Sheet State
    val verificationResult = MutableStateFlow<FaceMatchResult?>(null)
    val verifiedRecord = MutableStateFlow<AttendanceRecord?>(null)

    // Active Student Details Card
    val activeStudent = MutableStateFlow<Student?>(null)

    val allStudents: StateFlow<List<Student>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allStudents
            else repository.searchStudents(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayRecords: StateFlow<List<AttendanceRecord>> = repository.getTodayRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationLog>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudentCount: StateFlow<Int> = repository.studentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val batchSummaries = MutableStateFlow<List<BatchSummary>>(emptyList())

    init {
        refreshSummaries()
    }

    fun setLanguage(language: Language) {
        currentLanguage.value = language
    }

    fun setRole(role: UserRole) {
        currentRole.value = role
    }

    fun setOfflineMode(enabled: Boolean) {
        isOfflineMode.value = enabled
    }

    fun syncDataNow() {
        viewModelScope.launch {
            isSyncing.value = true
            delay(1200) // Simulate sync payload to cloud
            isSyncing.value = false
            snackbarMessage.value = "تمت مزامنة البيانات والملاحظات بنجاح!"
        }
    }

    fun refreshSummaries() {
        viewModelScope.launch {
            batchSummaries.value = repository.getBatchSummaries()
        }
    }

    /**
     * Executes facial recognition verification for a scanned vector
     */
    fun processFaceScan(scannedVector: FloatArray, context: Context) {
        viewModelScope.launch {
            val students = allStudents.value
            val match = FaceMatchingEngine.matchFace(scannedVector, students)
            verificationResult.value = match

            if (match is FaceMatchResult.MatchFound) {
                val student = match.student
                val record = repository.markAttendance(
                    student = student,
                    method = AttendanceMethod.FACE_RECOGNITION,
                    status = AttendanceStatus.PRESENT
                )
                verifiedRecord.value = record
                refreshSummaries()

                NotificationHelper.sendAttendanceNotification(
                    context,
                    student.name,
                    student.universityId,
                    "حضور بلمحة الوجه"
                )
            } else if (match is FaceMatchResult.NoMatch) {
                verifiedRecord.value = null
            }
        }
    }

    /**
     * Executes QR code attendance check-in
     */
    fun processQrScan(qrData: String, context: Context) {
        viewModelScope.launch {
            val student = repository.getStudentByUniversityId(qrData.removePrefix("QR_STD_"))
                ?: allStudents.value.find { it.qrCodeData == qrData || it.universityId == qrData }

            if (student != null) {
                val match = FaceMatchResult.MatchFound(student, 100f)
                verificationResult.value = match
                val record = repository.markAttendance(
                    student = student,
                    method = AttendanceMethod.QR_SCAN,
                    status = AttendanceStatus.PRESENT
                )
                verifiedRecord.value = record
                refreshSummaries()

                NotificationHelper.sendAttendanceNotification(
                    context,
                    student.name,
                    student.universityId,
                    "حضور بكود QR"
                )
            } else {
                verificationResult.value = FaceMatchResult.NoMatch("رمز QR غير مسجل في قاعدة البيانات")
            }
        }
    }

    fun clearVerificationResult() {
        verificationResult.value = null
        verifiedRecord.value = null
    }

    fun registerNewStudent(
        name: String,
        universityId: String,
        batch: String,
        email: String,
        parentEmail: String,
        customFaceVector: String? = null
    ) {
        viewModelScope.launch {
            val vector = customFaceVector ?: FaceMatchingEngine.generateVectorForNewStudent(name, universityId)
            val newStudent = Student(
                name = name,
                universityId = universityId,
                batch = batch,
                email = email.ifBlank { "$universityId@univ.edu.sa" },
                parentEmail = parentEmail.ifBlank { "parent_$universityId@gmail.com" },
                faceVector = vector,
                qrCodeData = "QR_STD_$universityId"
            )
            repository.addStudent(newStudent)
            refreshSummaries()
            snackbarMessage.value = "تمت إضافة الطالب ${newStudent.name} وتوليد بصمة الوجه بامتياز!"
        }
    }

    fun triggerAbsentAutoAlerts(context: Context) {
        viewModelScope.launch {
            val alertsCount = repository.triggerAbsentAutoAlerts()
            refreshSummaries()
            NotificationHelper.sendAbsenceAlertNotification(context, alertsCount)
            snackbarMessage.value = "تم رصد $alertsCount متغيبين وإرسال تنبيهات تلقائية لأولياء الأمور!"
        }
    }

    fun exportPdfReport(context: Context) {
        val file = ReportGenerator.exportToPdf(context, todayRecords.value)
        if (file != null) {
            ReportGenerator.shareFile(context, file, "application/pdf")
        } else {
            snackbarMessage.value = "تعذر إنتاج ملف PDF"
        }
    }

    fun exportExcelCsvReport(context: Context) {
        val file = ReportGenerator.exportToCsv(context, todayRecords.value)
        if (file != null) {
            ReportGenerator.shareFile(context, file, "text/csv")
        } else {
            snackbarMessage.value = "تعذر إنتاج ملف Excel/CSV"
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }
}
