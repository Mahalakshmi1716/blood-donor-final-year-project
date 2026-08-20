package com.example.blood_donor.ui.screens.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.viewmodels.DashboardViewModel
import com.example.blood_donor.ui.utils.IndiaGeoData
import com.example.blood_donor.ui.utils.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    viewModel: DashboardViewModel,
    tokenManager: com.example.blood_donor.data.TokenManager,
    onNavigateBack: () -> Unit
) {
    val preferredLanguage by tokenManager.preferredLanguageFlow.collectAsState(initial = "en")
    fun t(key: String): String {
        return LocalizedStrings.get(key, preferredLanguage)
    }

    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val prefs = remember { context.getSharedPreferences("availability_prefs", Context.MODE_PRIVATE) }

    // Read initial states
    var morningSelected by remember { mutableStateOf(prefs.getBoolean("morning", true)) }
    var afternoonSelected by remember { mutableStateOf(prefs.getBoolean("afternoon", false)) }
    var eveningSelected by remember { mutableStateOf(prefs.getBoolean("evening", true)) }
    var locationName by remember { mutableStateOf(prefs.getString("location", "Lajpat Nagar, New Delhi") ?: "Lajpat Nagar, New Delhi") }
    var lastUpdatedMinutesAgo by remember { mutableStateOf(prefs.getInt("last_updated_min", 5)) }

    // Map profile availability to current status
    var selectedStatus by remember {
        mutableStateOf(
            if (profile?.isAvailableToday == true) "Available"
            else prefs.getString("status", "Busy") ?: "Busy"
        )
    }

    var showLocationDialog by remember { mutableStateOf(false) }
    var selectedState by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("") }
    var enteredCity by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            selectedStatus = if (it.isAvailableToday) "Available" else prefs.getString("status", "Busy") ?: "Busy"
            if (it.state != null) {
                locationName = "${it.city ?: ""}, ${it.district ?: ""}, ${it.state ?: ""}"
                selectedState = it.state
                selectedDistrict = it.district ?: ""
                enteredCity = it.city ?: ""
            }
        }
    }

    // State capital coordinates for Indian States
    val stateCoordinates = remember {
        mapOf(
            "Andhra Pradesh" to Pair(16.5062, 80.6480),
            "Arunachal Pradesh" to Pair(27.0844, 93.6053),
            "Assam" to Pair(26.1445, 91.7362),
            "Bihar" to Pair(25.5941, 85.1376),
            "Chhattisgarh" to Pair(21.2787, 81.6496),
            "Goa" to Pair(15.4909, 73.8278),
            "Gujarat" to Pair(23.2156, 72.6369),
            "Haryana" to Pair(30.7333, 76.7794),
            "Himachal Pradesh" to Pair(31.1048, 77.1734),
            "Jharkhand" to Pair(23.3441, 85.3096),
            "Karnataka" to Pair(12.9716, 77.5946),
            "Kerala" to Pair(8.5241, 76.9366),
            "Madhya Pradesh" to Pair(23.2599, 77.4126),
            "Maharashtra" to Pair(18.9220, 72.8347),
            "Manipur" to Pair(24.8170, 93.9368),
            "Meghalaya" to Pair(25.5788, 91.8833),
            "Mizoram" to Pair(23.7307, 92.7173),
            "Nagaland" to Pair(25.6751, 94.1086),
            "Odisha" to Pair(20.2961, 85.8245),
            "Punjab" to Pair(30.7333, 76.7794),
            "Rajasthan" to Pair(26.9124, 75.7873),
            "Sikkim" to Pair(27.3314, 88.6138),
            "Tamil Nadu" to Pair(13.0827, 80.2707),
            "Telangana" to Pair(17.3850, 78.4867),
            "Tripura" to Pair(23.8315, 91.2868),
            "Uttar Pradesh" to Pair(26.8467, 80.9462),
            "Uttarakhand" to Pair(30.3165, 78.0322),
            "West Bengal" to Pair(22.5726, 88.3639),
            "Andaman and Nicobar Islands" to Pair(11.6234, 92.7265),
            "Chandigarh" to Pair(30.7333, 76.7794),
            "Dadra and Nagar Haveli and Daman and Diu" to Pair(20.3974, 72.8328),
            "Delhi" to Pair(28.6139, 77.2090),
            "Jammu and Kashmir" to Pair(34.0837, 74.7973),
            "Ladakh" to Pair(34.1526, 77.5771),
            "Lakshadweep" to Pair(10.5667, 72.6417),
            "Puducherry" to Pair(11.9416, 79.8083)
        )
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text(t("update_location"), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(t("select_state"), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    DropdownSelector(
                        label = t("select_state"),
                        options = IndiaGeoData.states,
                        selectedOption = selectedState,
                        onOptionSelected = {
                            selectedState = it
                            selectedDistrict = ""
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val districts = IndiaGeoData.districtsMap[selectedState] ?: emptyList()
                    val isDistrictEnabled = selectedState.isNotEmpty()
                    Text(t("select_district"), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = if (isDistrictEnabled) TextDark else TextGray)
                    DropdownSelector(
                        label = t("select_district"),
                        options = districts,
                        selectedOption = selectedDistrict,
                        onOptionSelected = { selectedDistrict = it },
                        enabled = isDistrictEnabled
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val isCityEnabled = selectedDistrict.isNotEmpty()
                    Text(t("enter_city"), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = if (isCityEnabled) TextDark else TextGray)
                    OutlinedTextField(
                        value = enteredCity,
                        onValueChange = { enteredCity = it },
                        label = { Text(t("enter_city")) },
                        enabled = isCityEnabled,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                val isFormValid = selectedState.isNotEmpty() && selectedDistrict.isNotEmpty() && enteredCity.isNotEmpty()
                Button(
                    onClick = {
                        if (isFormValid) {
                            val coords = stateCoordinates[selectedState] ?: Pair(28.6139, 77.2090)
                            locationName = "$enteredCity, $selectedDistrict, $selectedState"
                            lastUpdatedMinutesAgo = 0
                            
                            prefs.edit()
                                .putString("location", locationName)
                                .putInt("last_updated_min", 0)
                                .apply()
                                
                            viewModel.updateAvailability(
                                isAvailableToday = (selectedStatus == "Available"),
                                lat = coords.first,
                                lon = coords.second,
                                state = selectedState,
                                district = selectedDistrict,
                                city = enteredCity,
                                onComplete = {
                                    showLocationDialog = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isFormValid) PrimaryRed else Color.Gray),
                    enabled = isFormValid
                ) {
                    Text(t("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text(t("cancel"), color = TextDark)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("available_hours"), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark) },
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
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Today's Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(t("today_status"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Status - Available
                    StatusOptionCard(
                        title = t("available"),
                        subtitle = t("ready_to_donate"),
                        isSelected = selectedStatus == "Available",
                        accentColor = Color(0xFF4CAF50),
                        lightColor = Color(0xFFE8F5E9),
                        onClick = {
                            selectedStatus = "Available"
                            prefs.edit().putString("status", "Available").apply()
                            val coords = stateCoordinates[selectedState] ?: Pair(28.6139, 77.2090)
                            viewModel.updateAvailability(true, lat = coords.first, lon = coords.second, state = selectedState, district = selectedDistrict, city = enteredCity)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status - Busy
                    StatusOptionCard(
                        title = t("busy"),
                        subtitle = t("currently_unavailable"),
                        isSelected = selectedStatus == "Busy",
                        accentColor = Color(0xFFFF9800),
                        lightColor = Color(0xFFFFF3E0),
                        onClick = {
                            selectedStatus = "Busy"
                            prefs.edit().putString("status", "Busy").apply()
                            val coords = stateCoordinates[selectedState] ?: Pair(28.6139, 77.2090)
                            viewModel.updateAvailability(false, lat = coords.first, lon = coords.second, state = selectedState, district = selectedDistrict, city = enteredCity)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status - Unavailable
                    StatusOptionCard(
                        title = t("busy"),
                        subtitle = t("not_available_to_donate"),
                        isSelected = selectedStatus == "Unavailable",
                        accentColor = Color(0xFF9E9E9E),
                        lightColor = Color(0xFFF5F5F5),
                        onClick = {
                            selectedStatus = "Unavailable"
                            prefs.edit().putString("status", "Unavailable").apply()
                            val coords = stateCoordinates[selectedState] ?: Pair(28.6139, 77.2090)
                            viewModel.updateAvailability(false, lat = coords.first, lon = coords.second, state = selectedState, district = selectedDistrict, city = enteredCity)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Hours Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(t("available_hours"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Morning Toggle
                    HourToggleRow(
                        title = t("morning"),
                        subtitle = t("morning_hours"),
                        checked = morningSelected,
                        onCheckedChange = { checked ->
                            morningSelected = checked
                            prefs.edit().putBoolean("morning", checked).apply()
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 12.dp))

                    // Afternoon Toggle
                    HourToggleRow(
                        title = t("afternoon"),
                        subtitle = t("afternoon_hours"),
                        checked = afternoonSelected,
                        onCheckedChange = { checked ->
                            afternoonSelected = checked
                            prefs.edit().putBoolean("afternoon", checked).apply()
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 12.dp))

                    // Evening Toggle
                    HourToggleRow(
                        title = t("evening"),
                        subtitle = t("evening_hours"),
                        checked = eveningSelected,
                        onCheckedChange = { checked ->
                            eveningSelected = checked
                            prefs.edit().putBoolean("evening", checked).apply()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(t("location"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF5F5), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryRed)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(locationName, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                            val timeText = if (lastUpdatedMinutesAgo == 0) t("updated_just_now") else String.format(t("updated_minutes_ago"), lastUpdatedMinutesAgo)
                            Text(timeText, color = TextGray, fontSize = 12.sp)
                        }
                        Text(
                            t("update_location").split(" ").first(),
                            color = PrimaryRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                showLocationDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    lightColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) lightColor else Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFFECEFF1),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else TextDark,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = accentColor
                )
            }
        }
    }
}

@Composable
fun HourToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryRed,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            enabled = enabled,
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().clickable { if (enabled) expanded = !expanded },
            shape = RoundedCornerShape(12.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
