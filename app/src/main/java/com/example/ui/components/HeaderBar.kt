package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeaderBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    currentLanguage: Language,
    onLanguageToggle: (Language) -> Unit,
    isOffline: Boolean,
    isSyncing: Boolean,
    onSyncClick: () -> Unit
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Column {
        if (isOffline) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline Mode",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("offline_status", currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(
                        onClick = onSyncClick,
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Text(Strings.get("btn_sync_now", currentLanguage), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = Strings.get("app_title", currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (currentRole) {
                                UserRole.SUPERVISOR -> Strings.get("role_supervisor", currentLanguage)
                                UserRole.PROFESSOR -> Strings.get("role_professor", currentLanguage)
                                UserRole.STUDENT -> Strings.get("role_student", currentLanguage)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            actions = {
                // Role Selector Dropdown Chip
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { roleMenuExpanded = true },
                        label = {
                            Text(
                                text = when (currentRole) {
                                    UserRole.SUPERVISOR -> if (currentLanguage == Language.ARABIC) "مشرف" else "Supervisor"
                                    UserRole.PROFESSOR -> if (currentLanguage == Language.ARABIC) "محاضر" else "Professor"
                                    UserRole.STUDENT -> if (currentLanguage == Language.ARABIC) "طالب" else "Student"
                                },
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Role",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("role_selector_chip")
                    )

                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (currentLanguage == Language.ARABIC) role.labelAr else role.labelEn,
                                        fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onRoleSelected(role)
                                    roleMenuExpanded = false
                                },
                                leadingIcon = {
                                    val icon = when(role) {
                                        UserRole.SUPERVISOR -> Icons.Default.AdminPanelSettings
                                        UserRole.PROFESSOR -> Icons.Default.School
                                        UserRole.STUDENT -> Icons.Default.Person
                                    }
                                    Icon(imageVector = icon, contentDescription = null)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Language Toggle Button
                IconButton(
                    onClick = {
                        val nextLang = if (currentLanguage == Language.ARABIC) Language.ENGLISH else Language.ARABIC
                        onLanguageToggle(nextLang)
                    },
                    modifier = Modifier.testTag("language_toggle_btn")
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (currentLanguage == Language.ARABIC) "EN" else "عربي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
