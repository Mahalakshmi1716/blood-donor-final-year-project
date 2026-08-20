package com.example.blood_donor.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationComplete: () -> Unit,
    onNavigateToOtpVerification: (String) -> Unit = {}
) {
    val preferredLanguage by viewModel.tokenManager.preferredLanguageFlow.collectAsState(initial = "en")
    fun t(key: String): String {
        return com.example.blood_donor.ui.utils.LocalizedStrings.get(key, preferredLanguage)
    }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf<String?>("O+") }
    var userRole by remember { mutableStateOf("Donor") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableStateOf("") }
    
    var showSaveCredentialsDialog by remember { mutableStateOf(false) }
    var pendingPhoneToSave by remember { mutableStateOf("") }
    var pendingPasswordToSave by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Toast.makeText(context, (authState as AuthState.Success).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            if (phone.isNotBlank() && password.isNotBlank()) {
                pendingPhoneToSave = phone
                pendingPasswordToSave = password
                showSaveCredentialsDialog = true
            } else {
                onRegistrationComplete()
            }
        } else if (authState is AuthState.Unverified) {
            val unverifiedEmail = (authState as AuthState.Unverified).email
            viewModel.resetState()
            onNavigateToOtpVerification(unverifiedEmail)
        } else if (authState is AuthState.Error) {
            val errKey = (authState as AuthState.Error).error
            val msg = if (errKey.startsWith("Registration failed") || errKey.startsWith("Verification failed")) {
                errKey
            } else {
                com.example.blood_donor.ui.utils.LocalizedStrings.get(errKey, preferredLanguage)
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("create_account_title"), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        } else {
                            onNavigateBack()
                        }
                    }) {
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
        ) {
            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(4.dp)
            ) {
                repeat(if (userRole == "Hospital") 2 else 3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (index <= pagerState.currentPage) PrimaryRed else Color.LightGray)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> RoleSelectionStep(::t) { selectedRole -> 
                        userRole = selectedRole
                        scope.launch { pagerState.animateScrollToPage(1) } 
                    }
                    1 -> BasicInfoStep(
                        name = name,
                        email = email,
                        phone = phone,
                        password = password,
                        userRole = userRole,
                        isLoading = authState is AuthState.Loading,
                        onNameChange = { name = it },
                        onEmailChange = { email = it },
                        onPhoneChange = { phone = it },
                        onPasswordChange = { password = it },
                        t = ::t,
                        onNext = { 
                            if (name.isBlank() || email.isBlank() || phone.isBlank() || password.length < 6) {
                                Toast.makeText(context, t("please_fill_all_fields_pwd"), Toast.LENGTH_SHORT).show()
                            } else {
                                if (userRole == "Donor" || userRole == "Patient") {
                                    scope.launch { pagerState.animateScrollToPage(2) } 
                                } else {
                                    viewModel.register(name, email, phone, password, null, null, null, userRole)
                                }
                            }
                        }
                    )
                    2 -> {
                        if (userRole == "Donor" || userRole == "Patient") {
                            DonorDetailsStep(
                                userRole = userRole,
                                selectedBloodGroup = bloodGroup ?: "O+",
                                age = age,
                                gender = gender,
                                lastDonationDate = lastDonationDate,
                                isLoading = authState is AuthState.Loading,
                                onBloodGroupSelected = { bloodGroup = it },
                                onAgeChange = { age = it },
                                onGenderChange = { gender = it },
                                onLastDonationDateChange = { lastDonationDate = it },
                                t = ::t,
                                onNext = { 
                                    if (age.isBlank() || gender.isBlank()) {
                                        Toast.makeText(context, t("please_enter_age_gender"), Toast.LENGTH_SHORT).show()
                                    } else {
                                        val formattedDate = lastDonationDate.trim().ifEmpty { null }
                                        viewModel.register(name, email, phone, password, bloodGroup, age.toIntOrNull(), gender, userRole, formattedDate)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSaveCredentialsDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveCredentialsDialog = false
                onRegistrationComplete()
            },
            title = { Text(t("save_login_title"), fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text(t("save_login_desc"), color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.tokenManager.saveCredentials(pendingPhoneToSave, pendingPasswordToSave)
                            showSaveCredentialsDialog = false
                            onRegistrationComplete()
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
                        onRegistrationComplete()
                    }
                ) {
                    Text(t("not_now"), color = TextGray)
                }
            }
        )
    }
}

@Composable
fun RoleSelectionStep(t: (String) -> String, onNext: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Text(t("i_am_a"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text(t("choose_role_desc"), color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))
        
        RoleCard(t("blood_donor_role"), t("blood_donor_role_desc"), "🩸", Color(0xFFFFF0F1), PrimaryRed, { onNext("Donor") })
        Spacer(modifier = Modifier.height(16.dp))
        RoleCard(t("patient_family_role"), t("patient_family_role_desc"), "🏥", Color(0xFFE8F4FD), Color(0xFF1976D2), { onNext("Patient") })
        Spacer(modifier = Modifier.height(16.dp))
        RoleCard(t("hospital_role"), t("hospital_role_desc"), "🏢", Color(0xFFE8F5E9), Color(0xFF388E3C), { onNext("Hospital") })
    }
}

@Composable
fun RoleCard(title: String, desc: String, emoji: String, bgColor: Color, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                Text(desc, color = TextGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BasicInfoStep(
    name: String,
    email: String,
    phone: String,
    password: String,
    userRole: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    t: (String) -> String,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(t("basic_info_title"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(24.dp))
        
        val nameLabel = when (userRole) {
            "Hospital" -> t("institution_name_label")
            "Patient" -> t("patient_family_name_label")
            else -> t("full_name_label")
        }
        CustomTextField(nameLabel, value = name, onValueChange = onNameChange)
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(t("email_address_label"), value = email, onValueChange = onEmailChange)
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(t("phone_number_placeholder"), value = phone, onValueChange = onPhoneChange)
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(t("password"), value = password, onValueChange = onPasswordChange, isPassword = true)
        
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(if (userRole == "Donor") t("continue_button") else t("create_account_title"), isLoading = isLoading, onClick = onNext)
    }
}

@Composable
fun DonorDetailsStep(
    userRole: String,
    selectedBloodGroup: String,
    age: String,
    gender: String,
    lastDonationDate: String,
    isLoading: Boolean,
    onBloodGroupSelected: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onLastDonationDateChange: (String) -> Unit,
    t: (String) -> String,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(if (userRole == "Patient") t("patient_details_title") else t("donor_details_title"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(t("blood_group").uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(8.dp))
        
        val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                bloodGroups.take(4).forEach { bg ->
                    BloodGroupChip(bg, selectedBloodGroup == bg) { onBloodGroupSelected(bg) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                bloodGroups.drop(4).forEach { bg ->
                    BloodGroupChip(bg, selectedBloodGroup == bg) { onBloodGroupSelected(bg) }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        CustomTextField(t("age"), value = age, onValueChange = onAgeChange)
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(t("gender"), value = gender, onValueChange = onGenderChange)
        
        if (userRole == "Donor") {
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(t("last_donation_date_label"), value = lastDonationDate, onValueChange = onLastDonationDateChange)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(t("create_account_title"), isLoading = isLoading, onClick = onNext)
    }
}

@Composable
fun BloodGroupChip(group: String, selected: Boolean, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable { onClick() }
            .background(if (selected) PrimaryRed else Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(group, color = if (selected) Color.White else TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(placeholder: String, value: String = "", onValueChange: (String) -> Unit = {}, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedBorderColor = PrimaryRed
        )
    )
}

@Composable
fun PrimaryButton(text: String, isLoading: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = PrimaryRed),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
