package com.example.primitivedevicestoic.presentation.welcome

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.ui.theme.White
import org.koin.androidx.compose.koinViewModel

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val isDefault by viewModel.isDefaultLauncher.collectAsState()

    LaunchedEffect(isDefault) {
        if (isDefault) {
            onContinue()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Kontrola statusu po návratu z nastavení
        viewModel.updateDefaultLauncherStatus()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.welcome_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Black
            )



            Spacer(modifier = Modifier.height(32.dp))

            if (!isDefault) {
                Button(
                    onClick = {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
                            val roleManager = context.getSystemService(RoleManager::class.java)
                            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                                roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                            } else {
                                Intent(Settings.ACTION_HOME_SETTINGS)
                            }
                        } else {
                            Intent(Settings.ACTION_HOME_SETTINGS)
                        }
                        launcher.launch(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.set_as_default))
                }
            } else {
                Text(stringResource(R.string.already_set_as_default), color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.continue_label))
                }
            }
        }
    }
}

// Helper pro přístup k theme
@Composable
fun Material3Theme(content: @Composable () -> Unit) {
    content()
}

@Composable
fun BenefitItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}
