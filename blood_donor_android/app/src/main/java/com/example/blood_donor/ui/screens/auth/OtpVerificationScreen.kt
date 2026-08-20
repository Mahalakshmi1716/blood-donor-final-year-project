package com.example.blood_donor.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.viewmodels.AuthState
import com.example.blood_donor.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    email: String,
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    val preferredLanguage by viewModel.tokenManager.preferredLanguageFlow.collectAsState(initial = "en")
    fun t(key: String): String {
        return com.example.blood_donor.ui.utils.LocalizedStrings.get(key, preferredLanguage)
    }

    var otpCode by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Toast.makeText(context, (authState as AuthState.Success).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onVerificationSuccess()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).error, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("verify_phone_title"), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(t("enter_otp_code_title"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                String.format(t("otp_sent_desc"), email),
                color = TextGray,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                placeholder = { Text(t("enter_6digit_placeholder"), color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = PrimaryRed
                )
            )
            
            // Simulated OTP display banner removed
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (otpCode.length < 6) {
                        Toast.makeText(context, t("please_enter_valid_otp"), Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.verifyOtp(email, otpCode)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = PrimaryRed),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(16.dp),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(t("verify_activate_button"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
