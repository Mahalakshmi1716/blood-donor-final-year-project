package com.example.blood_donor.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.BloodRed
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.utils.LocalizedStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    tokenManager: com.example.blood_donor.data.TokenManager,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val preferredLanguage by tokenManager.preferredLanguageFlow.collectAsState(initial = "en")

    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var sosAlertsEnabled by remember { mutableStateOf(true) }

    var showServerUrlDialog by remember { mutableStateOf(false) }
    val savedServerUrl by tokenManager.serverUrlFlow.collectAsState(initial = "")
    var newServerUrl by remember { mutableStateOf("") }

    LaunchedEffect(savedServerUrl) {
        newServerUrl = savedServerUrl ?: ""
    }

    fun t(key: String): String {
        return LocalizedStrings.get(key, preferredLanguage)
    }

    if (showEditProfile) {
        EditProfileDialog(onDismiss = { showEditProfile = false })
        return
    }
    if (showChangePassword) {
        ChangePasswordDialog(onDismiss = { showChangePassword = false })
        return
    }
    if (showPrivacyPolicy) {
        PrivacyPolicyScreen(onBack = { showPrivacyPolicy = false })
        return
    }
    if (showAbout) {
    AboutScreen(onBack = { showAbout = false })
    return
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(t("select_language"), fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    tokenManager.savePreferredLanguage("en")
                                    showLanguageDialog = false
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = preferredLanguage == "en", onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(t("english"), color = TextDark)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    tokenManager.savePreferredLanguage("ta")
                                    showLanguageDialog = false
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = preferredLanguage == "ta", onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(t("tamil"), color = TextDark)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    tokenManager.savePreferredLanguage("hi")
                                    showLanguageDialog = false
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = preferredLanguage == "hi", onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(t("hindi"), color = TextDark)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(t("ok_button"), color = PrimaryRed)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("settings"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Account Section
            SettingsSectionHeader(t("account"))
            SettingsCard {
                SettingsRow(Icons.Default.Person, t("edit_profile"), t("edit_profile_desc")) {
                    showEditProfile = true
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 56.dp))
                SettingsRow(Icons.Default.Lock, t("change_password"), t("change_password_desc")) {
                    showChangePassword = true
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notifications Section
            SettingsSectionHeader(t("notifications"))
            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = t("push_notifications"),
                    subtitle = t("notifications_desc"),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 56.dp))
                SettingsToggleRow(
                    icon = Icons.Default.Warning,
                    title = t("sos_alerts"),
                    subtitle = t("sos_desc"),
                    checked = sosAlertsEnabled,
                    onCheckedChange = { sosAlertsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preferences Section
            SettingsSectionHeader(t("preferences_header"))
            SettingsCard {
                SettingsRow(Icons.Default.Language, t("language"), t("change_language_desc")) {
                    showLanguageDialog = true
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 56.dp))
                SettingsRow(Icons.Default.Wifi, t("server_url_label"), t("server_url_desc")) {
                    showServerUrlDialog = true
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 56.dp))
                SettingsRow(Icons.Default.Shield, t("privacy_policy"), t("privacy_desc")) {
                    showPrivacyPolicy = true
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 56.dp))
                SettingsRow(Icons.Default.Info, t("about"), t("about_desc")) {
                    showAbout = true
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("logout"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showServerUrlDialog) {
        AlertDialog(
            onDismissRequest = { showServerUrlDialog = false },
            title = { Text(t("server_config_title"), fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Column {
                    Text(t("enter_server_url_desc"), color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newServerUrl,
                        onValueChange = { newServerUrl = it },
                        placeholder = { Text("http://10.143.142.1:5000/") },
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
                        coroutineScope.launch {
                            tokenManager.saveServerUrl(newServerUrl.trim())
                            com.example.blood_donor.network.RetrofitClient.resetClient()
                            showServerUrlDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text(t("save_button"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerUrlDialog = false }) {
                    Text(t("cancel"), color = TextGray)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextGray,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFFFF0F1), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun SettingsToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFFFF0F1), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryRed)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(pv).padding(24.dp)
        ) {
            Text("Update your profile information below.", color = TextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(pv).padding(24.dp)
        ) {
            OutlinedTextField(value = current, onValueChange = { current = it }, label = { Text("Current Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm New Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(24.dp))
            val isValid = newPass == confirm && newPass.length >= 6
            Button(
                onClick = { if (isValid) onDismiss() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isValid) PrimaryRed else Color.Gray),
                shape = RoundedCornerShape(12.dp),
                enabled = isValid
            ) {
                Text("Update Password", fontWeight = FontWeight.Bold)
            }
            if (newPass.isNotEmpty() && newPass != confirm) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Passwords do not match", color = BloodRed, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(pv).padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Last updated: May 2026", color = TextGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("1. Data Collection\nWe collect your name, phone number, blood type and location to match you with compatible blood donors in your area.", color = TextDark, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("2. Data Usage\nYour data is used solely to power blood matching. We never sell or share your information with third parties.", color = TextDark, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("3. Location Data\nLocation is only used at the time of a search to find nearby donors. We do not continuously track you.", color = TextDark, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("4. Data Deletion\nYou can request deletion of all your data by contacting us through the app.", color = TextDark, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(pv).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Box(modifier = Modifier.size(80.dp).background(PrimaryRed, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Text("🩸", fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Smart Blood Donor Finder", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextDark)
            Text("Version 1.0.0", color = TextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Connecting blood donors with patients in real-time using AI-powered matching to save lives.", color = TextGray, fontSize = 14.sp)
        }
    }
}
