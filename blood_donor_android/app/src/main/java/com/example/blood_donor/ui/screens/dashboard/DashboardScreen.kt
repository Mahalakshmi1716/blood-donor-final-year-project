package com.example.blood_donor.ui.screens.dashboard
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.data.DonorProfileDto
import com.example.blood_donor.data.MatchedDonorDto
import com.example.blood_donor.ui.theme.BloodRed
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.viewmodels.DashboardViewModel
import com.example.blood_donor.ui.utils.LocalizedStrings


enum class DashboardTab(val translationKey: String, val icon: ImageVector) {
    HOME("home", Icons.Default.Home),
    MAP("map", Icons.Default.Map),
    CHAT("chat", Icons.Default.Chat),
    ALERTS("alerts", Icons.Default.Notifications),
    PROFILE("profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tokenManager: com.example.blood_donor.data.TokenManager,
    viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLogout: () -> Unit = {},
    onNavigateToChat: (Int, String) -> Unit = { _, _ -> },
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAiMatching: () -> Unit = {},
    onNavigateToAvailability: () -> Unit = {},
    onNavigateToLockScreen: () -> Unit = {}
) {
    val preferredLanguage by tokenManager.preferredLanguageFlow.collectAsState(initial = "en")
    fun t(key: String): String {
        return com.example.blood_donor.ui.utils.LocalizedStrings.get(key, preferredLanguage)
    }

    var selectedTab by remember { mutableStateOf(DashboardTab.HOME) }
    val profile by viewModel.profile.collectAsState()
    val user by viewModel.user.collectAsState()
    val nearbyDonors by viewModel.nearbyDonors.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val tipOfTheDay by viewModel.tipOfTheDay.collectAsState()
    var showRequestDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchMe()
        viewModel.fetchProfile()
        viewModel.fetchAlerts()
        viewModel.fetchTipOfTheDay()
        
        // Start polling for new emergency alerts
        viewModel.startAlertPolling(context)
    }

    LaunchedEffect(user, profile) {
        // Use user?.bloodGroup first for Patients, fallback to profile for Donors, then "O+"
        val lat = profile?.latitude ?: 28.6139
        val lon = profile?.longitude ?: 77.2090
        val bloodGroup = user?.bloodGroup ?: profile?.bloodGroup ?: "O+"
        viewModel.fetchNearbyDonors(bloodGroup, lat, lon)
    }
    val dashboardState by viewModel.dashboardState.collectAsState()

    LaunchedEffect(dashboardState) {
        if (dashboardState is com.example.blood_donor.ui.viewmodels.DashboardState.Success) {
            android.widget.Toast.makeText(context, t("sos_broadcast_success"), android.widget.Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    if (showRequestDialog) {
        CreateBloodRequestDialog(
            defaultBloodGroup = user?.bloodGroup ?: profile?.bloodGroup ?: "O+",
            onDismiss = { showRequestDialog = false },
            t = ::t,
            onSubmit = { reqBloodGroup, hospital, units, urgency ->
                showRequestDialog = false
                val lat = profile?.latitude ?: 28.6139
                val lon = profile?.longitude ?: 77.2090
                viewModel.createBloodRequest(reqBloodGroup, hospital, lat, lon, urgency, units)
            }
        )
    }

    if (user != null && user?.userType != "Hospital" && user?.bloodGroup == null) {
        MissingInfoDialog(
            onDismiss = {},
            t = ::t,
            onSubmit = { bloodGroup, age, gender ->
                viewModel.updateProfile(bloodGroup, age.toIntOrNull(), gender)
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                DashboardTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = t(tab.translationKey)) },
                        label = { Text(t(tab.translationKey)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryRed,
                            selectedTextColor = PrimaryRed,
                            indicatorColor = Color(0xFFFFF0F1),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            when (selectedTab) {
                DashboardTab.HOME -> HomeTab(
                    profile = profile, 
                    user = user,
                    nearbyDonors = nearbyDonors,
                    alerts = alerts,
                    tipOfTheDay = tipOfTheDay,
                    lang = preferredLanguage,
                    onRefreshTip = { viewModel.fetchTipOfTheDay() },
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToProfile = { selectedTab = DashboardTab.PROFILE },
                    onRequestBloodClicked = { showRequestDialog = true },
                    onViewAll = onNavigateToAiMatching,
                    onNavigateToAvailability = onNavigateToAvailability
                )
                DashboardTab.MAP -> MapTab(profile = profile, nearbyDonors = nearbyDonors, lang = preferredLanguage)
                DashboardTab.CHAT -> ChatTab(lang = preferredLanguage, onNavigateToChat = onNavigateToChat)
                DashboardTab.ALERTS -> AlertsTab(
                    alerts = alerts,
                    lang = preferredLanguage,
                    onAccept = { id -> viewModel.acceptAlert(id) },
                    onDecline = { id -> viewModel.declineAlert(id) }
                )
                DashboardTab.PROFILE -> ProfileTab(
                    profile = profile, 
                    user = user,
                    lang = preferredLanguage,
                    onLogout = onLogout,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToAvailability = onNavigateToAvailability,
                    onNavigateToLockScreen = onNavigateToLockScreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBloodRequestDialog(
    defaultBloodGroup: String,
    onDismiss: () -> Unit,
    t: (String) -> String,
    onSubmit: (bloodGroup: String, hospital: String, units: Int, urgency: String) -> Unit
) {
    var selectedGroup by remember { mutableStateOf(defaultBloodGroup) }
    var hospital by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("1") }
    var urgency by remember { mutableStateOf("High") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text(t("create_blood_request"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(t("blood_group_needed"), fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach { bg ->
                    val isSelected = selectedGroup == bg
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(if (isSelected) PrimaryRed else Color.LightGray.copy(0.2f), RoundedCornerShape(8.dp))
                            .clickable { selectedGroup = bg }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(bg, color = if (isSelected) Color.White else TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = hospital,
                onValueChange = { hospital = it },
                label = { Text(t("hospital_name_location")) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = units,
                    onValueChange = { units = it },
                    label = { Text(t("units_pints")) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = urgency,
                    onValueChange = { urgency = it },
                    label = { Text(t("urgency_level")) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSubmit(selectedGroup, if (hospital.isBlank()) "Nearest Hospital" else hospital, units.toIntOrNull() ?: 1, urgency) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(t("broadcast_request"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ActiveRequestTimelineCard(alert: com.example.blood_donor.data.AlertDto) {
    val steps = listOf("Created", "Alert Sent", "Accepted", "Traveling", "In Progress", "Closed")
    val currentStep = when (alert.status) {
        "CREATED" -> 0
        "ALERT_SENT" -> 1
        "DONOR_ACCEPTED" -> 2
        "TRAVELING" -> 3
        "IN_PROGRESS" -> 4
        "CLOSED", "COMPLETED" -> 5
        else -> 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Active Request Progress", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Emergency ${alert.bloodGroup} at ${alert.hospitalName}", color = TextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(20.dp))

            // Draw steps timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val isActive = index <= currentStep
                    val isCurrent = index == currentStep
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (isActive) PrimaryRed else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isCurrent) 2.dp else 0.dp,
                                    color = if (isCurrent) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = step,
                            fontSize = 8.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) TextDark else TextGray,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTab(
    profile: DonorProfileDto?, 
    user: com.example.blood_donor.data.UserDto?,
    nearbyDonors: List<MatchedDonorDto>,
    alerts: List<com.example.blood_donor.data.AlertDto>,
    tipOfTheDay: String,
    lang: String,
    onRefreshTip: () -> Unit,
    onNavigateToChat: (Int, String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onRequestBloodClicked: () -> Unit,
    onViewAll: () -> Unit,
    onNavigateToAvailability: () -> Unit = {}
) {
    fun t(key: String): String {
        return com.example.blood_donor.ui.utils.LocalizedStrings.get(key, lang)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (user?.userType == "Hospital") {
                    Text(t("hospital_dashboard"), color = TextGray, fontSize = 16.sp)
                } else {
                    Text(t("good_morning"), color = TextGray, fontSize = 16.sp)
                }
                Text(profile?.name ?: user?.name ?: "Guest", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier.size(48.dp).background(Color.LightGray, CircleShape).clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Text((profile?.name ?: user?.name)?.take(2)?.uppercase() ?: "G") // Profile Pic
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // SOS Banner (Hide for Donors)
        if (user?.userType != "Donor") {
            val activeAlert = alerts.find { it.patientId == user?.id && it.status != "CLOSED" && it.status != "COMPLETED" && it.status != "CANCELLED" && it.status != "EXPIRED" }
            if (activeAlert != null) {
                ActiveRequestTimelineCard(activeAlert)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth().clickable { onRequestBloodClicked() },
                colors = CardDefaults.cardColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚨", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(t("request_blood").uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(t("sos_request_desc"), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        if (user?.userType != "Donor") {
            if (nearbyDonors.isNotEmpty()) {
                val bestMatch = nearbyDonors.first()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t("best_match"), color = TextDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(t("see_all"), color = PrimaryRed, fontSize = 14.sp, modifier = Modifier.clickable { onViewAll() })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✨ " + String.format(t("compatible_percent"), bestMatch.final_score.toInt().toString()), color = PrimaryRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            
                            val badgeColor = if (bestMatch.isExactMatch) Color(0xFFC62828) else Color(0xFFEF6C00)
                            val badgeBg = if (bestMatch.isExactMatch) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
                            val badgeText = if (bestMatch.isExactMatch) t("exact_match") else t("compatible_match")
                            
                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(PrimaryRed.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(bestMatch.name.take(1).uppercase(), color = PrimaryRed, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bestMatch.name, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("\uD83D\uDCCD " + String.format(t("km_away"), bestMatch.distance_km.toString()), color = TextGray, fontSize = 12.sp)
                            }
                            Box(modifier = Modifier.background(PrimaryRed, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text(bestMatch.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { onNavigateToChat(bestMatch.donorId, bestMatch.name) },
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF0F1), RoundedCornerShape(8.dp))
                        ) {
                            Text(t("contact_donor"), color = PrimaryRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("other_compatible"), color = TextDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(t("view_all"), color = PrimaryRed, fontSize = 14.sp, modifier = Modifier.clickable { onViewAll() })
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (nearbyDonors.size > 1) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    nearbyDonors.drop(1).forEach { donor ->
                        NearbyDonorCard(donor) {
                            onNavigateToChat(donor.donorId, donor.name)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            } else {
                Text(t("no_donors_found"), color = TextGray, fontSize = 14.sp)
            }

        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📅", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            t("update_avail_today"),
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            t("let_patients_know"),
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        t("update"),
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onNavigateToAvailability() }
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Text(t("donor_summary"), color = TextDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F1)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(t("donor_registered"), fontWeight = FontWeight.Bold, color = PrimaryRed, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(t("donor_registered_desc"), color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌏", fontSize = 24.sp)
                            Text(t("available_now"), color = TextGray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("90", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryRed)
                            Text(t("day_lock_rule"), color = TextGray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📡", fontSize = 24.sp)
                            Text(t("alerts_active"), color = TextGray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TipOfTheDayCard(tip = tipOfTheDay, onRefresh = onRefreshTip)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TipOfTheDayCard(tip: String, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(" Tip of the Day", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Tip",
                        tint = PrimaryRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tip,
                color = TextDark,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun NearbyDonorCard(donor: MatchedDonorDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFFFF0F1), CircleShape)
                    .border(2.dp, PrimaryRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(donor.bloodGroup, color = PrimaryRed, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(donor.name.split(" ").first(), fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${donor.distance_km} km", color = TextGray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            val badgeColor = if (donor.isExactMatch) Color(0xFFC62828) else Color(0xFFEF6C00)
            val badgeBg = if (donor.isExactMatch) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
            val badgeText = if (donor.isExactMatch) "Exact" else "Compatible"
            
            Box(
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MapTab(profile: DonorProfileDto?, nearbyDonors: List<MatchedDonorDto>, lang: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val centerLat = profile?.latitude ?: 28.6139
    val centerLon = profile?.longitude ?: 77.2090

    

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                org.osmdroid.views.MapView(ctx).apply {
                    setTileSource(
                        org.osmdroid.tileprovider.tilesource.XYTileSource(
                            "OpenStreetMap",
                            0,
                            19,
                            256,
                            ".png",
                            arrayOf("https://tile.openstreetmap.org/")
                        )
                    )
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    controller.setCenter(org.osmdroid.util.GeoPoint(centerLat, centerLon))
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                
                // Add a "You are here" marker
                val startMarker = org.osmdroid.views.overlay.Marker(mapView)
                startMarker.position = org.osmdroid.util.GeoPoint(centerLat, centerLon)
                startMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                startMarker.title = LocalizedStrings.get("you_are_here", lang)
                startMarker.icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
                mapView.overlays.add(startMarker)
                
                // Add markers for nearby donors
                nearbyDonors.forEach { donor ->
                    val dLat = donor.latitude ?: (centerLat + (Math.random() - 0.5) * 0.05)
                    val dLon = donor.longitude ?: (centerLon + (Math.random() - 0.5) * 0.05)
                    
                    val donorMarker = org.osmdroid.views.overlay.Marker(mapView)
                    donorMarker.position = org.osmdroid.util.GeoPoint(dLat, dLon)
                    donorMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    donorMarker.title = "${donor.name} (${donor.bloodGroup})"
                    donorMarker.snippet = LocalizedStrings.get("km_away", lang).format(donor.distance_km.toString())
                    mapView.overlays.add(donorMarker)
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ChatTab(lang: String, chatViewModel: com.example.blood_donor.ui.viewmodels.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), onNavigateToChat: (Int, String) -> Unit = { _, _ -> }) {
    val conversations by chatViewModel.conversations.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()

    fun t(key: String): String {
        return LocalizedStrings.get(key, lang)
    }

    LaunchedEffect(Unit) {
        chatViewModel.fetchConversations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(20.dp)
    ) {
        Text(t("messages_title"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = PrimaryRed)
            }
        } else if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(t("no_recent_conversations"), color = TextGray)
            }
        } else {
            LazyColumn {
                items(conversations) { convo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onNavigateToChat(convo.userId, convo.name)
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(PrimaryRed.copy(alpha=0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(convo.name.take(1).uppercase(), color = PrimaryRed, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(convo.name, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(convo.lastMessage, color = TextGray, fontSize = 14.sp, maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AlertsTab(
    alerts: List<com.example.blood_donor.data.AlertDto>,
    lang: String,
    onAccept: (Int) -> Unit,
    onDecline: (Int) -> Unit
) {
    fun t(key: String): String {
        return LocalizedStrings.get(key, lang)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(t("notifications_title"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(t("mark_all_read"), fontSize = 14.sp, color = PrimaryRed, modifier = Modifier.clickable {})
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(t("no_recent_alerts"), color = TextGray)
            }
        } else {
            LazyColumn {
                items(alerts) { alert ->
                    AlertCard(alert, lang = lang, onAccept = onAccept, onDecline = onDecline)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: com.example.blood_donor.data.AlertDto,
    lang: String,
    onAccept: (Int) -> Unit,
    onDecline: (Int) -> Unit
) {
    fun t(key: String): String {
        return LocalizedStrings.get(key, lang)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(BloodRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = BloodRed)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val alertType = if (alert.urgency.isNotEmpty()) alert.urgency.uppercase() else "URGENT"
                    val desc = String.format(t("alert_needed_template"), alertType, alert.unitsRequired, alert.bloodGroup, alert.hospitalName)
                    Text(desc, fontWeight = FontWeight.Medium, color = TextDark, fontSize = 14.sp)
                    Text(alert.timestamp.take(16).replace("T", " "), color = TextGray, fontSize = 12.sp)
                }
                // Unread dot
                if (alert.status == "ALERT_SENT" || alert.status == "CREATED") {
                    Box(modifier = Modifier.size(8.dp).background(PrimaryRed, CircleShape))
                }
            }
            
            // Show Accept/Decline buttons if status is pending
            if (alert.status == "ALERT_SENT" || alert.status == "CREATED") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onDecline(alert.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(t("decline"))
                    }
                    Button(
                        onClick = { onAccept(alert.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Green accept
                    ) {
                        Text(t("accept"))
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status: " + alert.status,
                    color = if (alert.status == "DONOR_ACCEPTED") Color(0xFF2E7D32) else TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileTab(
    profile: DonorProfileDto?, 
    user: com.example.blood_donor.data.UserDto?,
    lang: String,
    onLogout: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAvailability: () -> Unit = {},
    onNavigateToLockScreen: () -> Unit = {}
) {
    fun t(key: String): String {
        return LocalizedStrings.get(key, lang)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // Ideally shouldn't be here in a tab, but keeping for fidelity
            Text(t("my_profile_title"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.clickable { onNavigateToSettings() })
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Pic
        Box(
            modifier = Modifier.size(80.dp).background(Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text((profile?.name ?: user?.name)?.take(2)?.uppercase() ?: "G", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(profile?.name ?: user?.name ?: "Guest", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text(user?.phoneNumber ?: "No phone number", color = TextGray)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (user?.userType == "Donor") {
            Box(modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("● Available to Donate", color = Color(0xFF388E3C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            val typeStr = user?.userType ?: "Patient"
            Text("$typeStr Account", color = PrimaryRed, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (user?.userType == "Donor") {
            // Stats - now using REAL data from backend
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard("${profile?.donationCount ?: 0}", "Donations", "❤️")
                StatCard("${profile?.trustScore ?: 3.0}", "Trust Score", "⭐")
                StatCard("${profile?.healthScore ?: 0}%", "Health Score", "📈")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val bloodGroup = user?.bloodGroup ?: profile?.bloodGroup ?: "Not specified"
                val age = user?.age ?: profile?.age
                if (user?.userType == "Donor" || user?.userType == "Patient") {
                    DetailRow("Blood Group", bloodGroup)
                    DetailRow("Age", age?.let { "$it years" } ?: "--")
                }
                
                if (user?.userType == "Donor") {
                    DetailRow("Last Donated", profile?.lastDonationDate ?: "Never")
                    // Calculate next eligible date (90 days after last donation)
                    val nextEligible = if (profile?.lastDonationDate != null) "90 days from last donation" else "Now"
                    DetailRow("Next Eligible", nextEligible)
                } else if (user?.userType == "Hospital") {
                    DetailRow("Type", "Blood Bank / Hospital")
                }
                DetailRow("Phone", user?.phoneNumber ?: "Not specified")
                user?.email?.let { DetailRow("Email", it) }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (user?.userType == "Donor") {
            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                ProfileActionButton(
                    icon = Icons.Default.AccessTime,
                    title = "Availability",
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    onClick = onNavigateToAvailability
                )
                ProfileActionButton(
                    icon = Icons.Default.HealthAndSafety,
                    title = "90-Day Lock",
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    onClick = onNavigateToLockScreen
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToSettings() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TextGray)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Settings", fontWeight = FontWeight.Bold, color = TextDark)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BloodRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(value: String, label: String, icon: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            Text(label, fontSize = 10.sp, color = TextGray)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Text(value, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ProfileActionButton(icon: ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F1)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingInfoDialog(
    onDismiss: () -> Unit,
    t: (String) -> String,
    onSubmit: (bloodGroup: String, age: String, gender: String) -> Unit
) {
    var selectedGroup by remember { mutableStateOf("O+") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text(t("complete_profile_title"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(t("complete_profile_desc"), color = TextGray)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(t("blood_group"), fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach { bg ->
                    val isSelected = selectedGroup == bg
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(if (isSelected) PrimaryRed else Color.LightGray.copy(0.2f), RoundedCornerShape(8.dp))
                            .clickable { selectedGroup = bg }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(bg, color = if (isSelected) Color.White else TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text(t("age")) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text(t("gender")) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSubmit(selectedGroup, age, gender) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(t("save_continue_button"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

