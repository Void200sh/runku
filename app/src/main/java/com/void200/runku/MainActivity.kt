package com.void200.runku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.void200.runku.ui.RunkuViewModel
import com.void200.runku.ui.screens.ActivityDetailScreen
import com.void200.runku.ui.screens.HomeScreen
import com.void200.runku.ui.screens.LiveRunScreen
import com.void200.runku.ui.screens.SettingsScreen
import com.void200.runku.ui.screens.TargetScreen
import com.void200.runku.ui.theme.RunkuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: RunkuViewModel = viewModel()
            val settings by vm.settingsFlow.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val dark = when (settings.darkMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            RunkuTheme(theme = settings.theme, dark = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "home") {
                        composable("home") { HomeScreen(vm, nav) }
                        composable("live") { LiveRunScreen(vm, nav) }
                        composable("target") { TargetScreen(vm, nav) }
                        composable("settings") { SettingsScreen(vm, nav) }
                        composable("detail/{id}") { back ->
                            val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            ActivityDetailScreen(vm, id, nav)
                        }
                    }
                }
            }
        }
    }
}
