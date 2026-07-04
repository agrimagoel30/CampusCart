package com.agrima.campuscart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.agrima.campuscart.ui.auth.AuthViewModel
import com.agrima.campuscart.ui.navigation.AppNavHost
import com.agrima.campuscart.ui.navigation.BottomNavigationBar
import com.agrima.campuscart.ui.navigation.Screen
import com.agrima.campuscart.ui.theme.CampusCartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusCartTheme {
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Sell.route,
                    Screen.Favorites.route,
                    Screen.Dashboard.route,
                    Screen.Profile.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(
                                navController = navController,
                                currentRoute = currentRoute
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        paddingValues = innerPadding,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}