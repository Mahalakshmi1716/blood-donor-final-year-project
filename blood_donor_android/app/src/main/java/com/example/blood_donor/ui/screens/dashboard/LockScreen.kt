package com.example.blood_donor.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    viewModel: DashboardViewModel,
    tokenManager: com.example.blood_donor.data.TokenManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var hospitalNameInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }

    // Date math logic
    val lastDonationStr = profile?.lastDonationDate
    var daysSince by remember { mutableStateOf(0L) }
    var daysRemaining by remember { mutableStateOf(0L) }
    var formattedNextEligible by remember { mutableStateOf("Now") }
    var isLocked by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!lastDonationStr.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
                val lastDate = sdf.parse(lastDonationStr)
                if (lastDate != null) {
                    val diff = System.currentTimeMillis() - lastDate.time
                    daysSince = maxOf(0L, diff / (1000 * 60 * 60 * 24))
                    daysRemaining = maxOf(0L, 90L - daysSince)
                    isLocked = daysRemaining > 0
                    
                    val calendar = Calendar.getInstance().apply {
                        time = lastDate
                        add(Calendar.DAY_OF_YEAR, 90)
                    }
                    formattedNextEligible = sdf.format(calendar.time)
                }
            } catch (e: Exception) {
                // Fallback parsing if backend returned the other format
                try {
                    val sdfFallback = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    val lastDate = sdfFallback.parse(lastDonationStr)
                    if (lastDate != null) {
                        val diff = System.currentTimeMillis() - lastDate.time
                        daysSince = maxOf(0L, diff / (1000 * 60 * 60 * 24))
                        daysRemaining = maxOf(0L, 90L - daysSince)
                        isLocked = daysRemaining > 0
                        
                        val calendar = Calendar.getInstance().apply {
                            time = lastDate
                            add(Calendar.DAY_OF_YEAR, 90)
                        }
                        formattedNextEligible = SimpleDateFormat("MMM d, yyyy", Locale.US).format(calendar.time)
                    }
                } catch (e2: Exception) {
                    isLocked = false
                    daysRemaining = 0L
                    daysSince = 90L
                    formattedNextEligible = "Now"
                }
            }
        } else {
            isLocked = false
            daysRemaining = 0L
            daysSince = 90L
            formattedNextEligible = "Now"
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Record Blood Donation", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Are you sure you want to record a donation today? This will lock your status for the next 90 days.",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = hospitalNameInput,
                        onValueChange = { hospitalNameInput = it },
                        label = { Text("Hospital Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hosp = hospitalNameInput.trim().ifEmpty { "City Hospital" }
                        val loc = locationInput.trim().ifEmpty { "New Delhi" }
                        showConfirmDialog = false
                        viewModel.recordDonation(hosp, loc) {
                            Toast.makeText(context, "Donation recorded successfully!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextDark)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("90-Day Recovery", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Circular Progress Ring (Canvas)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw outer track ring
                    drawCircle(
                        color = Color(0xFFFFEBEE).copy(alpha = 0.6f),
                        radius = size.minDimension / 2 - 12.dp.toPx(),
                        style = Stroke(width = 12.dp.toPx())
                    )
                    // Draw elapsed arc in Red
                    val percentage = if (isLocked) (daysSince.toFloat() / 90f) else 1.0f
                    val sweepAngle = percentage * 360f
                    drawArc(
                        color = PrimaryRed,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = 12.dp.toPx(),
                            cap = StrokeCap.Round
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - 24.dp.toPx(),
                            size.height - 24.dp.toPx()
                        ),
                        topLeft = androidx.compose.ui.geometry.Offset(12.dp.toPx(), 12.dp.toPx())
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val centerNum = if (isLocked) daysSince else 90
                    Text(
                        text = "$centerNum",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "of 90 days",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recovery Status details
            Text(
                text = if (isLocked) "Recovery Period Active" else "Ready to Donate!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isLocked) "$daysRemaining days until next eligible donation" else "You have completed your 90 days recovery period.",
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Yellow Warning Card (Temporarily hidden from search)
            if (isLocked) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your profile is temporarily hidden from donor searches. This protects your health. The 90-day rule ensures a safe recovery period between donations.",
                            color = Color(0xFF5D4037),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Donation Timeline Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Donation Timeline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val donationRecords = profile?.donations ?: emptyList()
                    donationRecords.forEachIndexed { i, record ->
                        val isLatest = i == donationRecords.size - 1
                        val titleText = if (isLatest) "Donation #${i + 1} — Latest" else "Donation #${i + 1}"
                        TimelineItem(
                            title = titleText,
                            date = record.donationDate,
                            hospital = record.hospitalName ?: "City Clinic",
                            location = record.location ?: "New Delhi",
                            isChecked = true,
                            isDotted = false
                        )
                    }

                    // Draw Next Eligible dotted item if locked
                    if (isLocked) {
                        val nextIndex = donationRecords.size + 1
                        TimelineItem(
                            title = "Donation #$nextIndex — Next eligible",
                            date = formattedNextEligible,
                            hospital = "Awaiting Cooldown",
                            location = "",
                            isChecked = false,
                            isDotted = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Last Donated & Next Eligible card row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Last Donated
                Card(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Last Donated", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(lastDonationStr ?: "Never", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Next Eligible
                Card(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isLocked) Color(0xFFFFF0F1) else Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Next Eligible", color = if (isLocked) PrimaryRed else Color(0xFF388E3C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formattedNextEligible,
                            color = if (isLocked) PrimaryRed else Color(0xFF388E3C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Record Donation Action Button
            if (!isLocked) {
                Button(
                    onClick = {
                        hospitalNameInput = ""
                        locationInput = ""
                        showConfirmDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "Record New Donation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF0F1), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryRed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "You will be unlocked automatically on $formattedNextEligible.",
                        color = PrimaryRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TimelineItem(
    title: String,
    date: String,
    hospital: String,
    location: String,
    isChecked: Boolean,
    isDotted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Dot indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isChecked) PrimaryRed else Color.White)
                    .border(
                        width = 1.5.dp,
                        color = if (isChecked) PrimaryRed else Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            // Vertical timeline line spacer could go here, keeping it clean with simple dots for mobile screens
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDotted) TextGray else TextDark
            )
            val hospDetail = if (location.isNotEmpty()) "$hospital, $location" else hospital
            if (hospDetail.isNotEmpty() && hospDetail != "Awaiting Cooldown") {
                Text(
                    text = hospDetail,
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = date,
                color = if (isDotted) PrimaryRed else TextGray,
                fontSize = 12.sp,
                fontWeight = if (isDotted) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
