package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

enum class Language(val code: String, val titleAr: String, val titleEn: String, val direction: LayoutDirection) {
    ARABIC("ar", "العربية", "Arabic", LayoutDirection.Rtl),
    ENGLISH("en", "English", "English", LayoutDirection.Ltr)
}

object Strings {
    private val arMap = mapOf(
        "app_title" to "نظام الحضور الذكي بالوجه",
        "dashboard" to "لوحة التحكم",
        "face_scanner" to "مسح الوجه",
        "qr_scanner" to "مسح QR",
        "students" to "الطلاب",
        "reports" to "التقارير",
        "notifications" to "الإشعارات",
        "settings" to "الإعدادات",
        
        "role_supervisor" to "مشرف النظام",
        "role_professor" to "محاضر / أستاذ",
        "role_student" to "طالب",

        "stats_total_students" to "إجمالي الطلاب",
        "stats_present_today" to "حضور اليوم",
        "stats_late_today" to "المتأخرين",
        "stats_absent_today" to "الغائبين",
        "stats_rate" to "نسبة الحضور الإجمالية",

        "action_scan_face" to "بدء تسجيل الحضور بالوجه",
        "action_scan_qr" to "مسح كود QR",
        "action_add_student" to "إضافة طالب جديد",
        "action_export_pdf" to "تصدير تقرير PDF",
        "action_export_excel" to "تصدير تقرير Excel",
        "action_send_absent_alerts" to "إرسال تنبيهات الغياب لولي الأمر",

        "title_add_student" to "تسجيل طالب جديد في النظام",
        "field_name" to "اسم الطالب الرباعي",
        "field_university_id" to "الرقم الجامعي / رقم القيد",
        "field_batch" to "الدفعة / القسم الأكاديمي",
        "field_email" to "البريد الإلكتروني للطالب",
        "field_parent_email" to "بريد ولي الأمر",
        "field_capture_face" to "التقاط / تحديث بصمة الوجه",

        "btn_save" to "حفظ البيانات",
        "btn_cancel" to "إلغاء",
        "btn_confirm" to "تأكيد",
        "btn_close" to "إغلاق",
        "btn_sync_now" to "تزامن الآن",

        "offline_status" to "وضع العمل بدون اتصال بالإنترنت (سيتم التزامن تلقائياً)",
        "scan_instruction" to "وجه وجه الطالب داخل الإطار الدائري لتسجيل الحضور بلمحة",
        "verification_success" to "تم التحقق وتسجيل الحضور بنجاح!",
        "already_checked_in" to "تم تسجيل حضور هذا الطالب من قبل اليوم!",
        "unknown_face" to "لم يتم التعرف على الوجه، يرجى تسجيل الطالب أو إعادة المحاولة.",
        
        "batch_distribution" to "توزيع الحضور حسب الدفعات",
        "recent_activity" to "سجل الحضور اللحظي",
        "parent_notification_sent" to "تم إرسال تقارير الغياب لأولياء الأمور بنجاح."
    )

    private val enMap = mapOf(
        "app_title" to "Smart Face Attendance",
        "dashboard" to "Dashboard",
        "face_scanner" to "Face Scanner",
        "qr_scanner" to "QR Scanner",
        "students" to "Students",
        "reports" to "Reports",
        "notifications" to "Notifications",
        "settings" to "Settings",

        "role_supervisor" to "Supervisor",
        "role_professor" to "Professor",
        "role_student" to "Student",

        "stats_total_students" to "Total Students",
        "stats_present_today" to "Present Today",
        "stats_late_today" to "Late Today",
        "stats_absent_today" to "Absent Today",
        "stats_rate" to "Attendance Rate",

        "action_scan_face" to "Start Face Attendance",
        "action_scan_qr" to "Scan QR Code",
        "action_add_student" to "Add New Student",
        "action_export_pdf" to "Export PDF Report",
        "action_export_excel" to "Export Excel / CSV",
        "action_send_absent_alerts" to "Send Parent Absence Alerts",

        "title_add_student" to "Register New Student",
        "field_name" to "Student Full Name",
        "field_university_id" to "University ID",
        "field_batch" to "Batch / Academic Dept",
        "field_email" to "Student Email",
        "field_parent_email" to "Parent Email",
        "field_capture_face" to "Capture / Update Face Embedding",

        "btn_save" to "Save Profile",
        "btn_cancel" to "Cancel",
        "btn_confirm" to "Confirm",
        "btn_close" to "Close",
        "btn_sync_now" to "Sync Now",

        "offline_status" to "Offline Mode active (Auto-syncs upon connection)",
        "scan_instruction" to "Align face within the bounding ring for instant verification",
        "verification_success" to "Face verified & attendance marked successfully!",
        "already_checked_in" to "Student has already recorded attendance today!",
        "unknown_face" to "Face not recognized. Please register student first.",

        "batch_distribution" to "Batch Attendance Distribution",
        "recent_activity" to "Real-Time Activity Feed",
        "parent_notification_sent" to "Parent absence alerts sent successfully."
    )

    fun get(key: String, language: Language): String {
        return when (language) {
            Language.ARABIC -> arMap[key] ?: key
            Language.ENGLISH -> enMap[key] ?: key
        }
    }
}

val LocalAppLanguage = staticCompositionLocalOf { Language.ARABIC }
