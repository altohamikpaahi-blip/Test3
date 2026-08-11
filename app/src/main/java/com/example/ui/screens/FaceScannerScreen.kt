package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.VerificationBottomSheet
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.FaceMatchingEngine
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.Strings
import kotlinx.coroutines.delay

@Composable
fun FaceScannerScreen(
    viewModel: AttendanceViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lang = LocalAppLanguage.current
    val students by viewModel.allStudents.collectAsState()

    val result by viewModel.verificationResult.collectAsState()
    val record by viewModel.verifiedRecord.collectAsState()

    var isScanning by remember { mutableStateOf(false) }
    var selectedTestStudent by remember { mutableStateOf<Student?>(null) }
    var showStudentPicker by remember { mutableStateOf(false) }

    // Pulsing scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

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
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = Strings.get("face_scanner", lang),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { showStudentPicker = !showStudentPicker }) {
                    Icon(
                        imageVector = Icons.Default.SwitchAccount,
                        contentDescription = "Simulate Face",
                        tint = Color.White
                    )
                }
            }

            // Scanner Ring & Face Frame
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = if (isScanning) Color(0xFF10B981) else Color(0xFF38BDF8),
                        shape = CircleShape
                    )
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                // Simulated Mesh grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.3f),
                        style = Stroke(width = 2f, pathEffect = pathEffect)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Face Target",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(160.dp * scalePulse)
                )

                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(220.dp),
                        color = Color(0xFF10B981),
                        strokeWidth = 4.dp
                    )
                }
            }

            // Instructions & Action Triggers
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.get("scan_instruction", lang),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Scan Trigger
                Button(
                    onClick = {
                        isScanning = true
                        val targetStudent = selectedTestStudent ?: students.randomOrNull()
                        if (targetStudent != null) {
                            val mockVector = FaceMatchingEngine.getMockScanVectorForStudent(targetStudent)
                            viewModel.processFaceScan(mockVector, context)
                        } else {
                            viewModel.processFaceScan(FloatArray(8) { 0f }, context)
                        }
                        isScanning = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("capture_face_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = null, tint = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (lang == Language.ARABIC) "التقاط الوجه والتسجيل الحيي" else "Scan & Verify Face",
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Student Selector Dropdown
                Box {
                    OutlinedButton(
                        onClick = { showStudentPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.PersonSearch, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedTestStudent != null)
                                "${selectedTestStudent?.name} (${selectedTestStudent?.universityId})"
                            else
                                if (lang == Language.ARABIC) "اختر طالباً للتجربة الحية" else "Select Student for Demo Scan"
                        )
                    }

                    DropdownMenu(
                        expanded = showStudentPicker,
                        onDismissRequest = { showStudentPicker = false }
                    ) {
                        students.forEach { student ->
                            DropdownMenuItem(
                                text = { Text("${student.name} - ${student.universityId}") },
                                onClick = {
                                    selectedTestStudent = student
                                    showStudentPicker = false
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null) }
                            )
                        }
                    }
                }
            }
        }

        // Verification Bottom Sheet
        VerificationBottomSheet(
            result = result,
            record = record,
            onDismiss = { viewModel.clearVerificationResult() }
        )
    }
}
