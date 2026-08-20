package com.example.blood_donor.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.data.TokenManager
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionScreen(
    tokenManager: TokenManager,
    onNavigateNext: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedLanguage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = "🌐",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Choose Your Language",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "மொழியைத் தேர்ந்தெடுக்கவும் / भाषा चुनें",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Options List
            val languages = listOf(
                Triple("en", "English", "English"),
                Triple("ta", "தமிழ்", "Tamil"),
                Triple("hi", "हिन्दी", "Hindi")
            )

            languages.forEach { (code, nativeName, englishName) ->
                val isSelected = selectedLanguage == code
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { selectedLanguage = code }
                        .border(
                            width = 2.dp,
                            color = if (isSelected) PrimaryRed else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFF0F1) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedLanguage = code },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryRed
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = nativeName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = englishName,
                                fontSize = 14.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Button
        Button(
            onClick = {
                val lang = selectedLanguage
                if (lang != null) {
                    coroutineScope.launch {
                        tokenManager.savePreferredLanguage(lang)
                        onNavigateNext()
                    }
                }
            },
            enabled = selectedLanguage != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
