package com.example.blood_donor.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.data.MatchedDonorDto
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.viewmodels.DashboardViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Phone

import com.example.blood_donor.ui.utils.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMatchingScreen(
    viewModel: DashboardViewModel,
    tokenManager: com.example.blood_donor.data.TokenManager,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Int, String) -> Unit
) {
    val preferredLanguage by tokenManager.preferredLanguageFlow.collectAsState(initial = "en")
    fun t(key: String): String {
        return LocalizedStrings.get(key, preferredLanguage)
    }

    val nearbyDonors by viewModel.nearbyDonors.collectAsState()
    val fallbackActivated by viewModel.fallbackActivated.collectAsState()
    val fallbackBloodBanks by viewModel.fallbackBloodBanks.collectAsState()
    var selectedFilter by remember { mutableStateOf("All Donors") }
    val context = LocalContext.current

    // Filter Logic using real database availability scores
    val filteredDonors = remember(nearbyDonors, selectedFilter) {
        when (selectedFilter) {
            "Available" -> nearbyDonors.filter { (it.availabilityScore ?: 0.0) > 0.0 }
            "Busy" -> nearbyDonors.filter { (it.availabilityScore ?: 0.0) == 0.0 }
            "Within 5km" -> nearbyDonors.filter { it.distance_km <= 5.0 }
            else -> nearbyDonors
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        t("ai_donor_matching"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = TextDark
                        )
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
        ) {
            // Scanning Status Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F1)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val count = nearbyDonors.size
                        Text(
                            text = t("scanning_donors").replace("%d", count.toString()),
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 16.sp
                        )
                        Text(
                            "Matching by blood group, health, distance & availability",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = null,
                        tint = TextGray
                    )
                }
            }

            // Filters Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                listOf("All Donors", "Available", "Busy", "Within 5km").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = if (isSelected) PrimaryRed else Color.White,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryRed else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (filter == "Available") {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .size(8.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                            } else if (filter == "Busy") {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .size(8.dp)
                                        .background(Color(0xFFFF9800), CircleShape)
                                )
                            }
                            val filterKey = when(filter) {
                                "All Donors" -> "all_donors"
                                "Available" -> "available"
                                "Busy" -> "busy"
                                "Within 5km" -> "within_5km"
                                else -> filter
                            }
                            Text(
                                text = t(filterKey),
                                color = if (isSelected) Color.White else TextDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // List of Matching Donors
            if (filteredDonors.isEmpty()) {
                if (fallbackActivated && fallbackBloodBanks.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "No compatible donors found nearby. Fallback Blood Banks:",
                            color = PrimaryRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            itemsIndexed(fallbackBloodBanks) { _, bank ->
                                BloodBankCard(bank)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching donors found.", color = TextGray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(filteredDonors) { index, donor ->
                        // Determine if Available or Busy or Unavailable based on score/mock
                        val status = if (donor.final_score >= 80) "Available" else "Busy"
                        val statusColor = if (status == "Available") Color(0xFF4CAF50) else Color(0xFFFF9800)
                        val lastDonatedMock = if (index % 2 == 0) "12 days ago" else "45 days ago"

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (index == 0 && donor.final_score >= 90) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = PrimaryRed.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.ElectricBolt,
                                                        contentDescription = null,
                                                        tint = PrimaryRed,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "AI BEST MATCH",
                                                        color = PrimaryRed,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            "${donor.final_score.toInt()}% match",
                                            color = PrimaryRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Exact vs Compatible Badge
                                    val badgeColor = if (donor.isExactMatch) Color(0xFFC62828) else Color(0xFFEF6C00)
                                    val badgeBg = if (donor.isExactMatch) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
                                    val badgeText = if (donor.isExactMatch) "Exact Match" else "Compatible Match"

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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier.size(52.dp),
                                        contentAlignment = Alignment.BottomEnd
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    color = PrimaryRed.copy(alpha = 0.08f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                donor.name.take(2).uppercase(),
                                                color = PrimaryRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        // Status dot
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(Color.White, CircleShape)
                                                .padding(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(statusColor, CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Mid content
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            donor.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextDark
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = TextGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                "${donor.distance_km} km",
                                                color = TextGray,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(TextGray, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                lastDonatedMock,
                                                color = TextGray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    // Blood Group Badge
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(PrimaryRed, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            donor.bloodGroup,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                // Compatibility indicator progress bar
                                LinearProgressIndicator(
                                    progress = { donor.final_score.toFloat() / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = PrimaryRed,
                                    trackColor = Color(0xFFF5F5F5)
                                )

                                if (!donor.matchExplanation.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text("✨", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = donor.matchExplanation,
                                                color = TextDark,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(statusColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = status,
                                            color = statusColor,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onNavigateToChat(donor.donorId, donor.name) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF0F1)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "Chat",
                                            color = PrimaryRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:${donor.phoneNumber}")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Cannot open dialer", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Call",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BloodBankCard(bank: com.example.blood_donor.data.BloodBankDto) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    bank.bloodBankName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = bank.availabilityStatus,
                        color = Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    bank.location,
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:${bank.contactNumber}")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Call Blood Bank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

