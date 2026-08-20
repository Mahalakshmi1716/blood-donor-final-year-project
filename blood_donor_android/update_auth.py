import os
import re

auth_vm_path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\viewmodels\AuthViewModel.kt"
reg_screen_path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\screens\auth\RegistrationScreen.kt"

# Update AuthViewModel.kt
with open(auth_vm_path, "r", encoding="utf-8") as f:
    auth_vm_content = f.read()

new_register = """    fun register(name: String, phoneNumber: String, pass: String, bloodGroup: String?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.register(RegisterRequest(name, phoneNumber, pass))
                if (response.isSuccessful) {
                    // Auto login after registration
                    val loginResponse = RetrofitClient.apiService.login(LoginRequest(phoneNumber, pass))
                    if (loginResponse.isSuccessful && loginResponse.body() != null) {
                        val token = loginResponse.body()?.token
                        if (token != null) {
                            tokenManager.saveToken(token)
                            if (bloodGroup != null) {
                                // Now create donor profile since we have a token
                                try {
                                    RetrofitClient.apiService.createDonorProfile(com.example.blood_donor.data.DonorProfileRequest(bloodGroup))
                                } catch (e: Exception) {
                                    // ignore donor profile creation error for now
                                }
                            }
                            _authState.value = AuthState.Success("Registration Successful")
                        } else {
                            _authState.value = AuthState.Error("Token missing in login response")
                        }
                    } else {
                        _authState.value = AuthState.Success("Registration Successful. Please Login.")
                    }
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }"""

auth_vm_content = re.sub(
    r'fun register\(name: String, phoneNumber: String, pass: String\)\s*\{.*?\n    \}',
    new_register,
    auth_vm_content,
    flags=re.DOTALL
)

with open(auth_vm_path, "w", encoding="utf-8") as f:
    f.write(auth_vm_content)


# Update RegistrationScreen.kt
with open(reg_screen_path, "r", encoding="utf-8") as f:
    reg_screen_content = f.read()

new_reg_vars = """    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf<String?>("O+") }
    var isDonor by remember { mutableStateOf(false) }"""

reg_screen_content = re.sub(
    r'    var name by remember \{ mutableStateOf\(""\) \}\n    var phone by remember \{ mutableStateOf\(""\) \}\n    var password by remember \{ mutableStateOf\(""\) \}',
    new_reg_vars,
    reg_screen_content
)

new_role_step = """                    0 -> RoleSelectionStep { roleIsDonor -> 
                        isDonor = roleIsDonor
                        scope.launch { pagerState.animateScrollToPage(1) } 
                    }"""

reg_screen_content = re.sub(
    r'0 -> RoleSelectionStep \{ scope\.launch \{ pagerState\.animateScrollToPage\(1\) \} \}',
    new_role_step,
    reg_screen_content
)

new_donor_step = """                    2 -> {
                        if (isDonor) {
                            DonorDetailsStep(
                                selectedBloodGroup = bloodGroup ?: "O+",
                                onBloodGroupSelected = { bloodGroup = it },
                                onNext = { scope.launch { pagerState.animateScrollToPage(3) } }
                            )
                        } else {
                            // Skip donor step
                            LaunchedEffect(Unit) { scope.launch { pagerState.animateScrollToPage(3) } }
                        }
                    }"""

reg_screen_content = re.sub(
    r'2 -> DonorDetailsStep \{ scope\.launch \{ pagerState\.animateScrollToPage\(3\) \} \}',
    new_donor_step,
    reg_screen_content
)

new_loc_step = """                    3 -> LocationPrivacyStep(isLoading = authState is AuthState.Loading) { 
                        viewModel.register(name, phone, password, if (isDonor) bloodGroup else null)
                    }"""
                    
reg_screen_content = re.sub(
    r'3 -> LocationPrivacyStep\(isLoading = authState is AuthState\.Loading\) \{ \n                        viewModel\.register\(name, phone, password\)\n                    \}',
    new_loc_step,
    reg_screen_content
)


# Modify RoleSelectionStep to pass Boolean
reg_screen_content = reg_screen_content.replace(
    "fun RoleSelectionStep(onNext: () -> Unit)",
    "fun RoleSelectionStep(onNext: (Boolean) -> Unit)"
)
reg_screen_content = reg_screen_content.replace(
    'RoleCard("Blood Donor", "Register as a donor and help save lives.", "🩸", Color(0xFFFFF0F1), PrimaryRed, onNext)',
    'RoleCard("Blood Donor", "Register as a donor and help save lives.", "🩸", Color(0xFFFFF0F1), PrimaryRed, { onNext(true) })'
)
reg_screen_content = reg_screen_content.replace(
    'RoleCard("Patient / Family", "Request blood urgently.", "🏥", Color(0xFFE8F4FD), Color(0xFF1976D2), onNext)',
    'RoleCard("Patient / Family", "Request blood urgently.", "🏥", Color(0xFFE8F4FD), Color(0xFF1976D2), { onNext(false) })'
)
reg_screen_content = reg_screen_content.replace(
    'RoleCard("Hospital", "Manage blood requests.", "🏢", Color(0xFFE8F5E9), Color(0xFF388E3C), onNext)',
    'RoleCard("Hospital", "Manage blood requests.", "🏢", Color(0xFFE8F5E9), Color(0xFF388E3C), { onNext(false) })'
)


# Modify DonorDetailsStep to accept selectedBloodGroup
new_donor_details_func = """@Composable
fun DonorDetailsStep(
    selectedBloodGroup: String,
    onBloodGroupSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Donor Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("BLOOD GROUP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            BloodGroupChip("A+", selectedBloodGroup == "A+") { onBloodGroupSelected("A+") }
            BloodGroupChip("O+", selectedBloodGroup == "O+") { onBloodGroupSelected("O+") }
            BloodGroupChip("B+", selectedBloodGroup == "B+") { onBloodGroupSelected("B+") }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        CustomTextField("Age")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("Gender")
        
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton("Continue", onClick = onNext)
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
        Text(group, color = if (selected) Color.White else TextDark, fontWeight = FontWeight.Bold)
    }
}
"""

reg_screen_content = re.sub(
    r'@Composable\nfun DonorDetailsStep\(onNext: \(\) -> Unit\) \{.*?\nfun BloodGroupChip\(group: String, selected: Boolean\) \{.*?\n\}\n',
    new_donor_details_func,
    reg_screen_content,
    flags=re.DOTALL
)

with open(reg_screen_path, "w", encoding="utf-8") as f:
    f.write(reg_screen_content)
