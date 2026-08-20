package com.example.blood_donor.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: Int,
    receiverName: String,
    tokenManager: com.example.blood_donor.data.TokenManager,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(receiverId) {
        viewModel.fetchChatHistory(receiverId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receiverName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        // Shared approximate location for privacy
                        viewModel.sendMessage(receiverId, "📍 Shared Location: Lat=28.6139, Lon=77.2090 (approximate location for privacy)")
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFF0F1), RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Share Location", tint = PrimaryRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(receiverId, inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryRed, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                val isMine = msg.receiverId == receiverId
                MessageBubble(text = msg.content, isMine = isMine)
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isMine: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isMine) PrimaryRed else Color.White,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 0.dp,
                        bottomEnd = if (isMine) 0.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = if (isMine) Color.White else TextDark,
                fontSize = 16.sp
            )
        }
    }
}
