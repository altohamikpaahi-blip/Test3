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
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.Strings

@Composable
fun SettingsScreen(
    viewModel: AttendanceViewModel
) {
    val lang = LocalAppLanguage.current
    val currentRole by viewModel.currentRole.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = Strings.get("settings", lang),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Language Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == Language.ARABIC) "لغة التطبيق" else "App Language",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = lang == Language.ARABIC,
                        onClick = { viewModel.setLanguage(Language.ARABIC) },
                        label = { Text("العربية (RTL)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lang_ar_chip")
                    )

                    FilterChip(
                        selected = lang == Language.ENGLISH,
                        onClick = { viewModel.setLanguage(Language.ENGLISH) },
                        label = { Text("English (LTR)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lang_en_chip")
                    )
                }
            }
        }

        // Role Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == Language.ARABIC) "صلاحيات المستخدم والتطبيق" else "User Role Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                UserRole.values().forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (lang == Language.ARABIC) role.labelAr else role.labelEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal
                        )
                        RadioButton(
                            selected = role == currentRole,
                            onClick = { viewModel.setRole(role) }
                        )
                    }
                }
            }
        }

        // Offline Mode Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == Language.ARABIC) "وضع العمل بدون اتصال" else "Offline Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (lang == Language.ARABIC) "حفظ الحضور في قاعدة بيانات Room والمزامنة لاحقاً" else "Store records in Room DB for later cloud sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isOffline,
                    onCheckedChange = { viewModel.setOfflineMode(it) }
                )
            }
        }

        // System Version & Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Smart Face Attendance v1.0",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (lang == Language.ARABIC) "تطبيق أندرويد متكامل بلغة Kotlin ومكتبة Jetpack Compose" else "Integrated Android Kotlin Jetpack Compose Application",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
