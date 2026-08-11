package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.model.Student

object NotificationHelper {

    private const val CHANNEL_ATTENDANCE = "channel_attendance"
    private const val CHANNEL_ABSENCE = "channel_absence"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE,
                "إشعارات الحضور",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات تأكيد تسجيل الحضور اللحظية للطلاب"
            }

            val absenceChannel = NotificationChannel(
                CHANNEL_ABSENCE,
                "تنبيهات الغياب",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات الغياب الأكاديمي التلقائية للمشرفين وأولياء الأمور"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(attendanceChannel)
            manager.createNotificationChannel(absenceChannel)
        }
    }

    fun sendAttendanceNotification(context: Context, studentName: String, universityId: String, statusText: String) {
        createNotificationChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ATTENDANCE)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("تأكيد تسجيل $statusText")
            .setContentText("تم تسجيل $statusText بنجاح للطالب $studentName ($universityId)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    fun sendAbsenceAlertNotification(context: Context, absentCount: Int) {
        createNotificationChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ABSENCE)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("تنبيه غياب تلقائي")
            .setContentText("تم رصد $absentCount طالب متغيب اليوم، وتم تحرير تقارير وإرسال إشعارات لولي الأمر.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    fun composeParentEmailIntent(context: Context, student: Student, todayDate: String) {
        try {
            val subject = "تنبيه هام: غياب الطالب/ـة ${student.name} - $todayDate"
            val body = """
                السلام عليكم ورحمة الله وبركاته،
                
                نود إحاطتكم علماً بأن الطالب/ـة: ${student.name}
                الرقم الجامعي: ${student.universityId}
                الدفعة الأكاديمية: ${student.batch}
                
                قد يتغيب عن حضور المحاضرات المقررة ليوم $todayDate.
                يرجى التواصل مع إدارة الشؤون الأكاديمية للتوضيح في حال كان هناك عذر رسمي.
                
                مع خالص التحية والتقدير،
                إدارة نظام الحضور الذكي
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${student.parentEmail}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "إرسال تقرير عبر البريد"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
