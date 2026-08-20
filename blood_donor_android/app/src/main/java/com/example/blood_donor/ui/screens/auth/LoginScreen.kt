package com.example.blood_donor.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.BloodRed
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.viewmodels.AuthState
import com.example.blood_donor.ui.viewmodels.AuthViewModel
import com.example.blood_donor.ui.utils.LocalizedStrings
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToOtpVerification: (String) -> Unit = {}
) {
    val preferredLanguage by viewModel.tokenManager.preferredLanguageFlow.collectAsState(initial = "en")
    fun t(key: String): String {
        return LocalizedStrings.get(key, preferredLanguage)
    }

    val coroutineScope = rememberCoroutineScope()
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordPhone by remember { mutableStateOf("") }
    
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetPasswordOtp by remember { mutableStateOf("") }
    var resetPasswordNew by remember { mutableStateOf("") }
    
    val rememberedPhone by viewModel.tokenManager.rememberedPhoneFlow.collectAsState(initial = "")
    var rememberMe by remember { mutableStateOf(false) }
    
    val savedPhone by viewModel.tokenManager.savedPhoneFlow.collectAsState(initial = "")
    val savedPasswordEncrypted by viewModel.tokenManager.savedPasswordEncryptedFlow.collectAsState(initial = "")
    
    var showSaveCredentialsDialog by remember { mutableStateOf(false) }
    var pendingPhoneToSave by remember { mutableStateOf("") }
    var pendingPasswordToSave by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(savedPhone, savedPasswordEncrypted, rememberedPhone) {
        if (!savedPhone.isNullOrEmpty()) {
            emailOrPhone = savedPhone!!
            if (!savedPasswordEncrypted.isNullOrEmpty()) {
                password = com.example.blood_donor.utils.CryptoManager.decrypt(savedPasswordEncrypted!!)
            }
        } else if (!rememberedPhone.isNullOrEmpty()) {
            emailOrPhone = rememberedPhone!!
            rememberMe = true
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, (authState as AuthState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                
                val decryptedSavedPass = com.example.blood_donor.utils.CryptoManager.decrypt(savedPasswordEncrypted ?: "")
                val alreadySaved = (savedPhone == emailOrPhone && decryptedSavedPass == password)
                if (!alreadySaved && emailOrPhone.isNotBlank() && password.isNotBlank()) {
                    pendingPhoneToSave = emailOrPhone
                    pendingPasswordToSave = password
                    showSaveCredentialsDialog = true
                } else {
                    onNavigateToDashboard()
                }
            }
            is AuthState.Unverified -> {
                val email = (authState as AuthState.Unverified).email
                viewModel.resetState()
                onNavigateToOtpVerification(email)
            }
            is AuthState.Error -> {
                val errKey = (authState as AuthState.Error).error
                val msg = if (errKey.startsWith("Registration failed") || errKey.startsWith("Verification failed")) {
                    errKey
                } else {
                    LocalizedStrings.get(errKey, preferredLanguage)
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryRed, shape = RoundedCornerShape(20.dp))
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🩸", fontSize = 40.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
              Text(
            text = t("welcome"),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Text(
            text = t("sign_in_save_lives"),
            fontSize = 16.sp,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Inputs
        OutlinedTextField(
            value = emailOrPhone,
            onValueChange = { emailOrPhone = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(t("email_or_phone"), color = Color.LightGray) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(t("password"), color = Color.LightGray) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = null, tint = TextGray)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryRed)
                )
                Text(
                    text = t("remember_me"),
                    color = TextDark,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                )
            }
            Text(
                text = t("forgot_password"),
                color = BloodRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { showForgotPasswordDialog = true }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                if (emailOrPhone.isBlank() || password.isBlank()) {
                    Toast.makeText(context, t("please_enter_phone_password"), Toast.LENGTH_SHORT).show()
                } else {
                    if (rememberMe) {
                        coroutineScope.launch {
                            viewModel.tokenManager.saveRememberedPhone(emailOrPhone)
                        }
                    } else {
                        coroutineScope.launch {
                            viewModel.tokenManager.clearRememberedPhone()
                        }
                    }
                    viewModel.login(emailOrPhone, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp), spotColor = PrimaryRed),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = RoundedCornerShape(16.dp),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(t("login"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row {
            Text(t("new_here"), color = TextGray, fontSize = 14.sp)
            Text(
                t("create_account"), 
                color = BloodRed, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text(t("reset_password_title"), fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Column {
                    Text(t("forgot_password_desc"), color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotPasswordPhone,
                        onValueChange = { forgotPasswordPhone = it },
                        placeholder = { Text(t("phone_number_placeholder")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordPhone.isBlank()) {
                            Toast.makeText(context, t("please_enter_phone"), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.forgotPassword(
                            forgotPasswordPhone,
                            onSuccess = {
                                Toast.makeText(context, t("reset_otp_sent"), Toast.LENGTH_LONG).show()
                                showForgotPasswordDialog = false
                                showResetPasswordDialog = true
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text(t("send_otp"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text(t("cancel"), color = TextGray)
                }
            }
        )
    }

    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = { Text(t("enter_new_password_title"), fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Column {
                    Text(t("enter_new_password_desc"), color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetPasswordOtp,
                        onValueChange = { resetPasswordOtp = it },
                        placeholder = { Text(t("otp_code_placeholder")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetPasswordNew,
                        onValueChange = { resetPasswordNew = it },
                        placeholder = { Text(t("new_password_placeholder")) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetPasswordOtp.isBlank() || resetPasswordNew.isBlank()) {
                            Toast.makeText(context, t("please_fill_all_fields"), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.resetPassword(
                            forgotPasswordPhone,
                            resetPasswordOtp,
                            resetPasswordNew,
                            onSuccess = {
                                Toast.makeText(context, t("password_reset_success"), Toast.LENGTH_LONG).show()
                                showResetPasswordDialog = false
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text(t("reset_password_title"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }) {
                    Text(t("cancel"), color = TextGray)
                }
            }
        )
    }

    if (showSaveCredentialsDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveCredentialsDialog = false
                onNavigateToDashboard()
            },
            title = { Text(t("save_login_title"), fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text(t("save_login_desc"), color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.tokenManager.saveCredentials(pendingPhoneToSave, pendingPasswordToSave)
                            if (rememberMe) {
                                viewModel.tokenManager.saveRememberedPhone(pendingPhoneToSave)
                            }
                            showSaveCredentialsDialog = false
                            onNavigateToDashboard()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text(t("save"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveCredentialsDialog = false
                        onNavigateToDashboard()
                    }
                ) {
                    Text(t("not_now"), color = TextGray)
                }
            }
        )
    }
}
