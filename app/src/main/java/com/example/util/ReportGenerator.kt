package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    /**
     * Generates a printable PDF report for attendance records
     */
    fun exportToPdf(context: Context, records: List<AttendanceRecord>, batchName: String = "جميع الدفعات"): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()

        // Background
        canvas.drawColor(Color.WHITE)

        // Header Background Banner
        paint.color = Color.parseColor("#0F172A") // Dark Navy
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Header Text
        titlePaint.color = Color.WHITE
        titlePaint.textSize = 22f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("تقرير الحضور والغياب الأكاديمي", 40f, 45f, titlePaint)

        titlePaint.textSize = 12f
        titlePaint.typeface = Typeface.DEFAULT
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        canvas.drawText("تاريخ التقرير: ${dateFormat.format(Date())} | الدفعة: $batchName", 40f, 75f, titlePaint)

        // Summary Statistics Box
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(40f, 120f, 555f, 180f, paint)

        val presentCount = records.count { it.status == AttendanceStatus.PRESENT }
        val lateCount = records.count { it.status == AttendanceStatus.LATE }
        val absentCount = records.count { it.status == AttendanceStatus.ABSENT }

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ملخص الحضور: إجمالي السجلات: ${records.size} | حاضر: $presentCount | متأخر: $lateCount | غائب: $absentCount", 55f, 155f, paint)

        // Table Headers
        var yPos = 220f
        paint.color = Color.parseColor("#334155")
        canvas.drawRect(40f, yPos - 20f, 555f, yPos + 10f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        canvas.drawText("الاسم", 50f, yPos, paint)
        canvas.drawText("الرقم الجامعي", 220f, yPos, paint)
        canvas.drawText("الدفعة", 340f, yPos, paint)
        canvas.drawText("الحالة", 480f, yPos, paint)

        yPos += 30f
        paint.color = Color.BLACK
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f

        for (record in records.take(25)) { // Max 25 rows per page for clean alignment
            val statusStr = when(record.status) {
                AttendanceStatus.PRESENT -> "حاضر"
                AttendanceStatus.LATE -> "متأخر"
                AttendanceStatus.ABSENT -> "غائب"
            }

            canvas.drawText(record.studentName, 50f, yPos, paint)
            canvas.drawText(record.universityId, 220f, yPos, paint)
            canvas.drawText(record.batch.take(18), 340f, yPos, paint)

            val statusColor = when(record.status) {
                AttendanceStatus.PRESENT -> Color.parseColor("#10B981")
                AttendanceStatus.LATE -> Color.parseColor("#F59E0B")
                AttendanceStatus.ABSENT -> Color.parseColor("#EF4444")
            }
            val statusPaint = Paint().apply {
                color = statusColor
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(statusStr, 480f, yPos, statusPaint)

            // Divider line
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawLine(40f, yPos + 8f, 555f, yPos + 8f, paint)
            paint.color = Color.BLACK

            yPos += 22f
        }

        // Footer & Signature
        canvas.drawText("توقيع مشرف النظام: .......................................", 40f, 800f, paint)

        pdfDocument.finishPage(page)

        // Save PDF file
        return try {
            val file = File(context.cacheDir, "Attendance_Report_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exports attendance records into UTF-8 CSV file compatible with Excel
     */
    fun exportToCsv(context: Context, records: List<AttendanceRecord>): File? {
        val file = File(context.cacheDir, "Attendance_Report_${System.currentTimeMillis()}.csv")
        return try {
            val outputStream = FileOutputStream(file)
            // Add UTF-8 BOM so Microsoft Excel correctly displays Arabic text
            outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val header = "اسم الطالب,الرقم الجامعي,الدفعة,تاريخ الحضور,وقت الحضور,طريقة التسجيل,الحالة\n"
            outputStream.write(header.toByteArray(Charsets.UTF_8))

            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            records.forEach { rec ->
                val methodStr = when(rec.method) {
                    com.example.data.model.AttendanceMethod.FACE_RECOGNITION -> "بصمة الوجه"
                    com.example.data.model.AttendanceMethod.QR_SCAN -> "رمز QR"
                    com.example.data.model.AttendanceMethod.MANUAL -> "إدخال يدوي"
                }
                val statusStr = when(rec.status) {
                    AttendanceStatus.PRESENT -> "حاضر"
                    AttendanceStatus.LATE -> "متأخر"
                    AttendanceStatus.ABSENT -> "غائب"
                }
                val timeStr = timeFormat.format(Date(rec.timestamp))

                val row = "\"${rec.studentName}\",\"${rec.universityId}\",\"${rec.batch}\",\"${rec.dateString}\",\"$timeStr\",\"$methodStr\",\"$statusStr\"\n"
                outputStream.write(row.toByteArray(Charsets.UTF_8))
            }

            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares file via Android Intent Chooser
     */
    fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة تقرير الحضور"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
