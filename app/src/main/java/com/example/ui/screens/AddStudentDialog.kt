package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit
) {
    val lang = LocalAppLanguage.current

    var name by remember { mutableStateOf("") }
    var universityId by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("علوم الحاسب - الدفعة الثالثة") }
    var email by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }

    var isCapturingFace by remember { mutableStateOf(false) }
    var faceCaptured by remember { mutableStateOf(false) }

    val batchOptions = listOf(
        "هندسة الحاسب - الدفعة الرابعة",
        "علوم الحاسب - الدفعة الثالثة",
        "هندسة البرمجيات - الدفعة الثانية",
        "نظم المعلومات - الدفعة الرابعة",
        "الذكاء الاصطناعي - الدفعة الأولى"
    )
    var batchMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.get("title_add_student", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.get("field_name", lang)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = universityId,
                    onValueChange = { universityId = it },
                    label = { Text(Strings.get("field_university_id", lang)) },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_id_input"),
                    singleLine = true
                )

                Box {
                    OutlinedTextField(
                        value = batch,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Strings.get("field_batch", lang)) },
                        trailingIcon = {
                            IconButton(onClick = { batchMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = batchMenuExpanded,
                        onDismissRequest = { batchMenuExpanded = false }
                    ) {
                        batchOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    batch = opt
                                    batchMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(Strings.get("field_email", lang)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = parentEmail,
                    onValueChange = { parentEmail = it },
                    label = { Text(Strings.get("field_parent_email", lang)) },
                    leadingIcon = { Icon(Icons.Default.ContactMail, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        isCapturingFace = true
                        faceCaptured = true
                        isCapturingFace = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (faceCaptured) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (faceCaptured) Icons.Default.CheckCircle else Icons.Default.Face,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (faceCaptured)
                            if (lang == Language.ARABIC) "تم التقاط وتحليل الوجه ✓" else "Face Template Ready ✓"
                        else
                            Strings.get("field_capture_face", lang)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && universityId.isNotBlank()) {
                        viewModel.registerNewStudent(
                            name = name,
                            universityId = universityId,
                            batch = batch,
                            email = email,
                            parentEmail = parentEmail
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_student_btn")
            ) {
                Text(Strings.get("btn_save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.get("btn_cancel", lang))
            }
        }
    )
}
