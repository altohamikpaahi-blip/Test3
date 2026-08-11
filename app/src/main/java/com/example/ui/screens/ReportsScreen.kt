package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceMethod
import com.example.data.model.AttendanceStatus
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val lang = LocalAppLanguage.current
    val todayRecords by viewModel.todayRecords.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }

    val filteredRecords = remember(todayRecords, selectedStatusFilter) {
        if (selectedStatusFilter == null) todayRecords
        else todayRecords.filter { it.status == selectedStatusFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Export Action Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == Language.ARABIC) "مركز تقارير الحضور المتقدم" else "Attendance Reports Hub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportPdfReport(context) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.get("action_export_pdf", lang), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportExcelCsvReport(context) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_excel_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.get("action_export_excel", lang), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                label = { Text(if (lang == Language.ARABIC) "الكل (${todayRecords.size})" else "All") }
            )

            FilterChip(
                selected = selectedStatusFilter == AttendanceStatus.PRESENT,
                onClick = { selectedStatusFilter = AttendanceStatus.PRESENT },
                label = { Text(if (lang == Language.ARABIC) "حاضر" else "Present") }
            )

            FilterChip(
                selected = selectedStatusFilter == AttendanceStatus.LATE,
                onClick = { selectedStatusFilter = AttendanceStatus.LATE },
                label = { Text(if (lang == Language.ARABIC) "متأخر" else "Late") }
            )

            FilterChip(
                selected = selectedStatusFilter == AttendanceStatus.ABSENT,
                onClick = { selectedStatusFilter = AttendanceStatus.ABSENT },
                label = { Text(if (lang == Language.ARABIC) "غائب" else "Absent") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Records Table List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredRecords, key = { it.id }) { record ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when(record.method) {
                                        AttendanceMethod.FACE_RECOGNITION -> Icons.Default.Face
                                        AttendanceMethod.QR_SCAN -> Icons.Default.QrCode
                                        AttendanceMethod.MANUAL -> Icons.Default.EditNote
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when(record.method) {
                                        AttendanceMethod.FACE_RECOGNITION -> if (lang == Language.ARABIC) "بصمة الوجه" else "Face Scan"
                                        AttendanceMethod.QR_SCAN -> if (lang == Language.ARABIC) "كود QR" else "QR Scan"
                                        AttendanceMethod.MANUAL -> if (lang == Language.ARABIC) "إدخال يدوي" else "Manual"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            Text(
                                text = timeFmt.format(Date(record.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when (record.status) {
                                    AttendanceStatus.PRESENT -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    AttendanceStatus.LATE -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    AttendanceStatus.ABSENT -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = when (record.status) {
                                        AttendanceStatus.PRESENT -> if (lang == Language.ARABIC) "حاضر" else "Present"
                                        AttendanceStatus.LATE -> if (lang == Language.ARABIC) "متأخر" else "Late"
                                        AttendanceStatus.ABSENT -> if (lang == Language.ARABIC) "غائب" else "Absent"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (record.status) {
                                        AttendanceStatus.PRESENT -> Color(0xFF10B981)
                                        AttendanceStatus.LATE -> Color(0xFFF59E0B)
                                        AttendanceStatus.ABSENT -> Color(0xFFEF4444)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
