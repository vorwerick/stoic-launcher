package com.example.primitivedevicestoic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.primitivedevicestoic.presentation.welcome.WelcomeScreen
import com.example.primitivedevicestoic.presentation.welcome.WelcomeViewModel
import com.example.primitivedevicestoic.presentation.home.HomeScreen
import com.example.primitivedevicestoic.ui.theme.PrimitiveDeviceStoicTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainContent(viewModel: WelcomeViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()
    val startDestination = if (isDefaultLauncher) "home" else "welcome"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.primitivedevicestoic.ui.theme.White
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onContinue = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen()
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                com.example.primitivedevicestoic.ui.theme.White.toArgb(),
                com.example.primitivedevicestoic.ui.theme.White.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                com.example.primitivedevicestoic.ui.theme.White.toArgb(),
                com.example.primitivedevicestoic.ui.theme.White.toArgb()
            )
        )
        setContent {
            PrimitiveDeviceStoicTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrimitiveDeviceStoicTheme {
        Greeting("Android")
    }
}