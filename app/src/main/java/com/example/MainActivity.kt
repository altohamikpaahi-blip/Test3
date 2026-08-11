package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AppHeaderBar
import com.example.ui.screens.AddStudentDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FaceScannerScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.Language
import com.example.util.LocalAppLanguage
import com.example.util.Strings

sealed class NavRoute(val route: String, val titleKey: String, val icon: @Composable () -> Unit) {
    object Dashboard : NavRoute("dashboard", "dashboard", { Icon(Icons.Default.Dashboard, contentDescription = null) })
    object FaceScan : NavRoute("face_scan", "face_scanner", { Icon(Icons.Default.CenterFocusWeak, contentDescription = null) })
    object QrScan : NavRoute("qr_scan", "qr_scanner", { Icon(Icons.Default.QrCodeScanner, contentDescription = null) })
    object Students : NavRoute("students", "students", { Icon(Icons.Default.People, contentDescription = null) })
    object Reports : NavRoute("reports", "reports", { Icon(Icons.Default.Assessment, contentDescription = null) })
    object Notifications : NavRoute("notifications", "notifications", { Icon(Icons.Default.Notifications, contentDescription = null) })
    object Settings : NavRoute("settings", "settings", { Icon(Icons.Default.Settings, contentDescription = null) })
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: AttendanceViewModel = viewModel()
                val currentLang by viewModel.currentLanguage.collectAsState()
                val currentRole by viewModel.currentRole.collectAsState()
                val isOffline by viewModel.isOfflineMode.collectAsState()
                val isSyncing by viewModel.isSyncing.collectAsState()
                val snackbarMsg by viewModel.snackbarMessage.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                var showAddStudentDialog by remember { mutableStateOf(false) }

                LaunchedEffect(snackbarMsg) {
                    snackbarMsg?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbar()
                    }
                }

                CompositionLocalProvider(
                    LocalAppLanguage provides currentLang,
                    LocalLayoutDirection provides currentLang.direction
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination?.route

                    val bottomNavRoutes = listOf(
                        NavRoute.Dashboard,
                        NavRoute.FaceScan,
                        NavRoute.QrScan,
                        NavRoute.Students,
                        NavRoute.Reports,
                        NavRoute.Notifications,
                        NavRoute.Settings
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            AppHeaderBar(
                                currentRole = currentRole,
                                onRoleSelected = { viewModel.setRole(it) },
                                currentLanguage = currentLang,
                                onLanguageToggle = { viewModel.setLanguage(it) },
                                isOffline = isOffline,
                                isSyncing = isSyncing,
                                onSyncClick = { viewModel.syncDataNow() }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                bottomNavRoutes.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentDestination == item.route,
                                        onClick = {
                                            if (currentDestination != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(NavRoute.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = item.icon,
                                        label = {
                                            Text(
                                                text = Strings.get(item.titleKey, currentLang),
                                                fontSize = 10.sp
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_${item.route}")
                                    )
                                }
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = NavRoute.Dashboard.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(NavRoute.Dashboard.route) {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToFaceScan = { navController.navigate(NavRoute.FaceScan.route) },
                                    onNavigateToQrScan = { navController.navigate(NavRoute.QrScan.route) },
                                    onNavigateToReports = { navController.navigate(NavRoute.Reports.route) },
                                    onNavigateToStudents = { navController.navigate(NavRoute.Students.route) }
                                )
                            }

                            composable(NavRoute.FaceScan.route) {
                                FaceScannerScreen(
                                    viewModel = viewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable(NavRoute.QrScan.route) {
                                QrScannerScreen(
                                    viewModel = viewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable(NavRoute.Students.route) {
                                StudentListScreen(
                                    viewModel = viewModel,
                                    onAddStudentClick = { showAddStudentDialog = true }
                                )
                            }

                            composable(NavRoute.Reports.route) {
                                ReportsScreen(viewModel = viewModel)
                            }

                            composable(NavRoute.Notifications.route) {
                                NotificationsScreen(viewModel = viewModel)
                            }

                            composable(NavRoute.Settings.route) {
                                SettingsScreen(viewModel = viewModel)
                            }
                        }

                        if (showAddStudentDialog) {
                            AddStudentDialog(
                                viewModel = viewModel,
                                onDismiss = { showAddStudentDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
