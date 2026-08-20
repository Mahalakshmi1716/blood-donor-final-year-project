package com.example.blood_donor.ui.screens.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.core.content.ContextCompat
import com.example.blood_donor.ui.theme.BloodRed
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.viewmodels.DashboardState
import com.example.blood_donor.ui.viewmodels.DashboardViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencySosScreen(viewModel: DashboardViewModel, onNavigateBack: () -> Unit) {
    var selectedBloodGroup by remember { mutableStateOf("O+") }
    var hospitalName by remember { mutableStateOf("") }
    val dashboardState by viewModel.dashboardState.collectAsState()
    val context = LocalContext.current
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // For handling location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permission granted, trigger SOS
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.createBloodRequest(selectedBloodGroup, hospitalName, location.latitude, location.longitude)
                    } else {
                        Toast.makeText(context, "Location not found. Ensure GPS is on.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                // Handled
            }
        } else {
            Toast.makeText(context, "Location permission is required for SOS", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(dashboardState) {
        if (dashboardState is DashboardState.Success) {
            Toast.makeText(context, (dashboardState as DashboardState.Success).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
            onNavigateBack()
        } else if (dashboardState is DashboardState.Error) {
            Toast.makeText(context, (dashboardState as DashboardState.Error).error, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency SOS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryRed)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Warning Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "SOS alerts are sent to all available donors within 10 km. SMS works even without internet.",
                        color = Color(0xFFF57F17),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("BLOOD GROUP NEEDED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SosBloodGroupChip("A+", selectedBloodGroup == "A+") { selectedBloodGroup = "A+" }
                SosBloodGroupChip("A-", selectedBloodGroup == "A-") { selectedBloodGroup = "A-" }
                SosBloodGroupChip("B+", selectedBloodGroup == "B+") { selectedBloodGroup = "B+" }
                SosBloodGroupChip("B-", selectedBloodGroup == "B-") { selectedBloodGroup = "B-" }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SosBloodGroupChip("O+", selectedBloodGroup == "O+") { selectedBloodGroup = "O+" }
                SosBloodGroupChip("O-", selectedBloodGroup == "O-") { selectedBloodGroup = "O-" }
                SosBloodGroupChip("AB+", selectedBloodGroup == "AB+") { selectedBloodGroup = "AB+" }
                SosBloodGroupChip("AB-", selectedBloodGroup == "AB-") { selectedBloodGroup = "AB-" }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("ALERT VIA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
            Spacer(modifier = Modifier.height(16.dp))
            AlertChannelRow("App Notification", "Instant push to all donors", "🔔", true)
            Spacer(modifier = Modifier.height(12.dp))
            AlertChannelRow("WhatsApp Message", "Message via WhatsApp", "💬", true)
            Spacer(modifier = Modifier.height(12.dp))
            AlertChannelRow("SMS Alert", "Works without Internet", "📩", true)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                placeholder = { Text("Hospital / Location (e.g. AIIMS, Ward 7B)", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = PrimaryRed
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (hospitalName.isBlank()) {
                        Toast.makeText(context, "Please enter hospital name.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    viewModel.createBloodRequest(selectedBloodGroup, hospitalName, location.latitude, location.longitude)
                                } else {
                                    Toast.makeText(context, "Location not found. Ensure GPS is on.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: SecurityException) {}
                    } else {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = BloodRed),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(16.dp),
                enabled = dashboardState !is DashboardState.Loading
            ) {
                if (dashboardState is DashboardState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("🚨 SEND SOS ALERT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SosBloodGroupChip(group: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(if (selected) PrimaryRed else Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(group, color = if (selected) Color.White else TextGray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AlertChannelRow(title: String, subtitle: String, icon: String, selected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFFFF0F1), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                Text(subtitle, color = TextGray, fontSize = 12.sp)
            }
            if (selected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryRed)
            }
        }
    }
}
