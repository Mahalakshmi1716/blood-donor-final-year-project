package com.example.blood_donor.ui.screens.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.PrimaryRed
import com.example.blood_donor.ui.theme.TextDark
import com.example.blood_donor.ui.theme.TextGray
import com.example.blood_donor.ui.utils.LocalizedStrings
import kotlinx.coroutines.launch

val onboardingEmojis = listOf("⚡", "⚠️", "🧭")

@Composable
fun OnboardingScreen(
    tokenManager: com.example.blood_donor.data.TokenManager,
    onNavigateToLogin: () -> Unit
) {
    val preferredLanguage by tokenManager.preferredLanguageFlow.collectAsState(initial = "en")
    fun t(key: String): String {
        return LocalizedStrings.get(key, preferredLanguage)
    }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8)) // Subtle premium off-white background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { position ->
                OnboardingCard(
                    pageIndex = position,
                    iconEmoji = onboardingEmojis[position],
                    t = ::t
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Pager Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(3) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PrimaryRed else Color.LightGray
                    val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = width, height = 8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onNavigateToLogin()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == 2) "${t("get_started")} →" else "Next →",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (pagerState.currentPage < 2) {
                    TextButton(onClick = onNavigateToLogin) {
                        Text(t("skip"), color = TextGray, fontSize = 16.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp)) // To keep button height balanced
                }
            }
        }
    }
}

@Composable
fun OnboardingCard(pageIndex: Int, iconEmoji: String, t: (String) -> String) {
    val title = t("onboarding_title_${pageIndex + 1}")
    val description = t("onboarding_desc_${pageIndex + 1}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFFFF0F1), shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 48.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = description,
                fontSize = 16.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}
