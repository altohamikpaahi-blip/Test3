package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.VerificationBottomSheet
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.QrEngine
import com.example.util.Strings

@Composable
fun QrScannerScreen(
    viewModel: AttendanceViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lang = LocalAppLanguage.current
    val students by viewModel.allStudents.collectAsState()

    val result by viewModel.verificationResult.collectAsState()
    val record by viewModel.verifiedRecord.collectAsState()

    var selectedQrStudent by remember { mutableStateOf<Student?>(null) }
    var showQrPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = Strings.get("qr_scanner", lang),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { showQrPicker = !showQrPicker }) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = "Pick QR", tint = Color.White)
                }
            }

            // Scanner Frame / Selected Student QR Code Display
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(width = 3.dp, color = Color(0xFF38BDF8), shape = RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                val studentToScan = selectedQrStudent ?: students.firstOrNull()

                if (studentToScan != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        QrEngine.QrCodeView(
                            data = studentToScan.qrCodeData,
                            size = 180.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = studentToScan.name,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = studentToScan.universityId,
                            color = Color(0xFF38BDF8),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            // Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (lang == Language.ARABIC)
                        "وجّه رمز QR المطبوع على بطاقة الطالب لمستشعر الكاميرا للتسجيل الفوري"
                    else
                        "Scan the student ID card QR code for instant check-in",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val studentToScan = selectedQrStudent ?: students.randomOrNull()
                        if (studentToScan != null) {
                            viewModel.processQrScan(studentToScan.qrCodeData, context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("scan_qr_now_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (lang == Language.ARABIC) "تأكيد مسح الكود وتسجيل الحضور" else "Confirm QR Check-in",
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box {
                    OutlinedButton(
                        onClick = { showQrPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(
                            if (selectedQrStudent != null)
                                "${selectedQrStudent?.name} (${selectedQrStudent?.universityId})"
                            else
                                if (lang == Language.ARABIC) "اختر كود طالب آخر للتجربة" else "Select Student QR"
                        )
                    }

                    DropdownMenu(
                        expanded = showQrPicker,
                        onDismissRequest = { showQrPicker = false }
                    ) {
                        students.forEach { std ->
                            DropdownMenuItem(
                                text = { Text("${std.name} (${std.universityId})") },
                                onClick = {
                                    selectedQrStudent = std
                                    showQrPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }

        VerificationBottomSheet(
            result = result,
            record = record,
            onDismiss = { viewModel.clearVerificationResult() }
        )
    }
}
