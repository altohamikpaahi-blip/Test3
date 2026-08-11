package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceMethod
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.BatchSummary
import com.example.data.model.UserRole
import com.example.ui.components.MetricCard
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AttendanceViewModel,
    onNavigateToFaceScan: () -> Unit,
    onNavigateToQrScan: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToStudents: () -> Unit
) {
    val context = LocalContext.current
    val lang = LocalAppLanguage.current
    val role by viewModel.currentRole.collectAsState()

    val totalCount by viewModel.totalStudentCount.collectAsState()
    val todayRecords by viewModel.todayRecords.collectAsState()
    val batchSummaries by viewModel.batchSummaries.collectAsState()

    val presentTodayCount = todayRecords.count { it.status == AttendanceStatus.PRESENT }
    val lateTodayCount = todayRecords.count { it.status == AttendanceStatus.LATE }
    val absentTodayCount = (totalCount - (presentTodayCount + lateTodayCount)).coerceAtLeast(0)
    val attendancePercent = if (totalCount > 0) ((presentTodayCount + lateTodayCount).toFloat() / totalCount) * 100f else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Try loading generated hero image
                    val heroDrawableId = context.resources.getIdentifier(
                        "face_hero_banner_1786435253082",
                        "drawable",
                        context.packageName
                    )

                    if (heroDrawableId != 0) {
                        Image(
                            painter = painterResource(id = heroDrawableId),
                            contentDescription = "Face Recognition Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == Language.ARABIC) "نظام الذكاء الاصطناعي مفعّل" else "AI Scanner Active",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = if (lang == Language.ARABIC) "تسجيل الحضور بلمحة الوجه" else "Instant Face Verification",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Strings.get("scan_instruction", lang),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToFaceScan,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("start_face_scan_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.CenterFocusWeak, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.get("action_scan_face", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToQrScan,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("start_qr_scan_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.get("action_scan_qr", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Metric Statistics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = Strings.get("stats_total_students", lang),
                        value = totalCount.toString(),
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = Strings.get("stats_present_today", lang),
                        value = presentTodayCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        subText = "${String.format(Locale.US, "%.0f", attendancePercent)}%"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = Strings.get("stats_late_today", lang),
                        value = lateTodayCount.toString(),
                        icon = Icons.Default.AccessTime,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = Strings.get("stats_absent_today", lang),
                        value = absentTodayCount.toString(),
                        icon = Icons.Default.Cancel,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Supervisor Control Actions (Export PDF/Excel, Parent Alerts)
        if (role == UserRole.SUPERVISOR) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (lang == Language.ARABIC) "إداريات المشرف الإشرافية" else "Supervisor Quick Tools",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.exportPdfReport(context) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PDF", fontSize = 12.sp)
                            }

                            FilledTonalButton(
                                onClick = { viewModel.exportExcelCsvReport(context) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Excel", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.triggerAbsentAutoAlerts(context) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == Language.ARABIC) "تنبيه الغياب" else "Parent Alert", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Batch Distribution Progress Bars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Strings.get("batch_distribution", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    batchSummaries.forEach { summary ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = summary.batchName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${summary.presentCount + summary.lateCount} / ${summary.totalStudents} (${String.format(Locale.US, "%.0f", summary.attendancePercentage)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (summary.attendancePercentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = if (summary.attendancePercentage > 75) Color(0xFF10B981) else Color(0xFFF59E0B),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Activity Log Ticker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("recent_activity", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onNavigateToReports) {
                    Text(if (lang == Language.ARABIC) "عرض الكل" else "View All")
                }
            }
        }

        items(todayRecords.take(5)) { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                when (record.status) {
                                    AttendanceStatus.PRESENT -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    AttendanceStatus.LATE -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    AttendanceStatus.ABSENT -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (record.method) {
                                AttendanceMethod.FACE_RECOGNITION -> Icons.Default.Face
                                AttendanceMethod.QR_SCAN -> Icons.Default.QrCode
                                AttendanceMethod.MANUAL -> Icons.Default.EditNote
                            },
                            contentDescription = null,
                            tint = when (record.status) {
                                AttendanceStatus.PRESENT -> Color(0xFF10B981)
                                AttendanceStatus.LATE -> Color(0xFFF59E0B)
                                AttendanceStatus.ABSENT -> Color(0xFFEF4444)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.studentName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${record.universityId} • ${record.batch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(
                            text = timeFormat.format(Date(record.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (record.status) {
                                AttendanceStatus.PRESENT -> if (lang == Language.ARABIC) "حاضر" else "Present"
                                AttendanceStatus.LATE -> if (lang == Language.ARABIC) "متأخر" else "Late"
                                AttendanceStatus.ABSENT -> if (lang == Language.ARABIC) "غائب" else "Absent"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (record.status) {
                                AttendanceStatus.PRESENT -> Color(0xFF10B981)
                                AttendanceStatus.LATE -> Color(0xFFF59E0B)
                                AttendanceStatus.ABSENT -> Color(0xFFEF4444)
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
