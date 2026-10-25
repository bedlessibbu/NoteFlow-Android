package com.example.notes.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notes.ui.screens.BackupRestoreScreen
import com.example.notes.ui.screens.HomeScreen
import com.example.notes.ui.screens.NotebookScreen
import com.example.notes.ui.screens.PageManagerScreen
import com.example.notes.ui.screens.PDFViewerScreen
import com.example.notes.ui.screens.SettingsScreen
import com.example.notes.ui.theme.NoteFlowTheme

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNotebookClick = { notebookId ->
                    navController.navigate("notebook/$notebookId")
                },
                onBack = { /* handled by top bar */ },
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onPageManagerClick = { notebookId ->
                    navController.navigate("pageManager/$notebookId")
                },
                onBackupRestoreClick = {
                    navController.navigate("backupRestore")
                }
            )
        }
        composable(
            route = "notebook/{notebookId}",
            arguments = listOf(androidx.navigation.navArgument("notebookId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val notebookId = backStackEntry.getString()!!
            NotebookScreen(
                notebookId = notebookId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "pdf/{filePath}",
            arguments = listOf(androidx.navigation.navArgument("filePath") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val filePath = backStackEntry.getString()!!
            PDFViewerScreen(
                filePath = filePath,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "pageManager/{notebookId}",
            arguments = listOf(androidx.navigation.navArgument("notebookId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val notebookId = backStackEntry.getString()!!
            PageManagerScreen(
                notebookId = notebookId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("backupRestore") {
            BackupRestoreScreen(onBack = { navController.popBackStack() })
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NavHostPreview() {
    NoteFlowTheme {
        AppNavHost()
    }
}
