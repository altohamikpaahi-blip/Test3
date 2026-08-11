package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceMethod
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.NotificationLog
import com.example.data.model.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [Student::class, AttendanceRecord::class, NotificationLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_face_attendance_db"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDemoData(database)
                    }
                }
            }

            private suspend fun seedDemoData(database: AppDatabase) {
                val studentDao = database.studentDao()
                val attendanceDao = database.attendanceDao()
                val notificationDao = database.notificationDao()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val todayStr = dateFormat.format(Date())

                val demoStudents = listOf(
                    Student(
                        id = "std_001",
                        name = "أحمد محمد العتيبي",
                        universityId = "442010892",
                        batch = "هندسة الحاسب - الدفعة الرابعة",
                        email = "ahmed.otaibi@univ.edu.sa",
                        parentEmail = "parent.otaibi@gmail.com",
                        faceVector = "0.12,0.88,0.45,0.23,0.91,0.34,0.11,0.76",
                        qrCodeData = "QR_STD_442010892"
                    ),
                    Student(
                        id = "std_002",
                        name = "سارة عبد الله الشمري",
                        universityId = "442010915",
                        batch = "علوم الحاسب - الدفعة الثالثة",
                        email = "sara.shammari@univ.edu.sa",
                        parentEmail = "parent.shammari@gmail.com",
                        faceVector = "0.85,0.14,0.67,0.89,0.12,0.45,0.78,0.23",
                        qrCodeData = "QR_STD_442010915"
                    ),
                    Student(
                        id = "std_003",
                        name = "عمر خالد الدوسري",
                        universityId = "442010773",
                        batch = "هندسة البرمجيات - الدفعة الثانية",
                        email = "omar.dosari@univ.edu.sa",
                        parentEmail = "parent.dosari@gmail.com",
                        faceVector = "0.33,0.44,0.55,0.66,0.77,0.88,0.99,0.11",
                        qrCodeData = "QR_STD_442010773"
                    ),
                    Student(
                        id = "std_004",
                        name = "فاطمة إبراهيم الغامدي",
                        universityId = "442010640",
                        batch = "نظم المعلومات - الدفعة الرابعة",
                        email = "fatima.ghamdi@univ.edu.sa",
                        parentEmail = "parent.ghamdi@gmail.com",
                        faceVector = "0.91,0.82,0.73,0.64,0.55,0.46,0.37,0.28",
                        qrCodeData = "QR_STD_442010640"
                    ),
                    Student(
                        id = "std_005",
                        name = "يوسف حسن القحطاني",
                        universityId = "442010512",
                        batch = "الذكاء الاصطناعي - الدفعة الأولى",
                        email = "youssef.qahtani@univ.edu.sa",
                        parentEmail = "parent.qahtani@gmail.com",
                        faceVector = "0.22,0.33,0.44,0.55,0.66,0.77,0.88,0.99",
                        qrCodeData = "QR_STD_442010512"
                    ),
                    Student(
                        id = "std_006",
                        name = "مريم علي الزهراني",
                        universityId = "442010431",
                        batch = "علوم الحاسب - الدفعة الثالثة",
                        email = "maryam.zahrani@univ.edu.sa",
                        parentEmail = "parent.zahrani@gmail.com",
                        faceVector = "0.77,0.66,0.55,0.44,0.33,0.22,0.11,0.00",
                        qrCodeData = "QR_STD_442010431"
                    )
                )

                studentDao.insertStudents(demoStudents)

                // Seed today's attendance records for quick testing
                val demoAttendance = listOf(
                    AttendanceRecord(
                        studentId = "std_001",
                        studentName = "أحمد محمد العتيبي",
                        universityId = "442010892",
                        batch = "هندسة الحاسب - الدفعة الرابعة",
                        timestamp = System.currentTimeMillis() - (1000 * 60 * 45), // 45 mins ago
                        dateString = todayStr,
                        method = AttendanceMethod.FACE_RECOGNITION,
                        status = AttendanceStatus.PRESENT
                    ),
                    AttendanceRecord(
                        studentId = "std_002",
                        studentName = "سارة عبد الله الشمري",
                        universityId = "442010915",
                        batch = "علوم الحاسب - الدفعة الثالثة",
                        timestamp = System.currentTimeMillis() - (1000 * 60 * 20), // 20 mins ago
                        dateString = todayStr,
                        method = AttendanceMethod.FACE_RECOGNITION,
                        status = AttendanceStatus.PRESENT
                    ),
                    AttendanceRecord(
                        studentId = "std_003",
                        studentName = "عمر خالد الدوسري",
                        universityId = "442010773",
                        batch = "هندسة البرمجيات - الدفعة الثانية",
                        timestamp = System.currentTimeMillis() - (1000 * 60 * 10), // 10 mins ago
                        dateString = todayStr,
                        method = AttendanceMethod.QR_SCAN,
                        status = AttendanceStatus.LATE
                    )
                )

                attendanceDao.insertRecords(demoAttendance)

                // Seed initial notification logs
                val demoNotifications = listOf(
                    NotificationLog(
                        title = "تأكيد تسجيل حضور",
                        message = "تم تسجيل حضور الطالب أحمد محمد العتيبي بواسطة بصمة الوجه بنجاح.",
                        recipientEmail = "ahmed.otaibi@univ.edu.sa",
                        studentName = "أحمد محمد العتيبي",
                        type = "ATTENDANCE"
                    ),
                    NotificationLog(
                        title = "تنبيه الحضور المتأخر",
                        message = "سجل الطالب عمر خالد الدوسري حضوراً متأخراً عبر رمز QR.",
                        recipientEmail = "omar.dosari@univ.edu.sa",
                        studentName = "عمر خالد الدوسري",
                        type = "ATTENDANCE"
                    ),
                    NotificationLog(
                        title = "تنبيه غياب تلقائي",
                        message = "تنبيه: الطالب يوسف حسن القحطاني لم يسجل حضوره اليوم، تم تحرير إشعار لولي الأمر.",
                        recipientEmail = "parent.qahtani@gmail.com",
                        studentName = "يوسف حسن القحطاني",
                        type = "ABSENCE_ALERT"
                    )
                )

                demoNotifications.forEach { notificationDao.insertNotification(it) }
            }
        }
    }
}
