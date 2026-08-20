package com.example.blood_donor.ui.screens.splash
import androidx.compose.runtime.remember

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blood_donor.ui.theme.BloodRed
import com.example.blood_donor.ui.theme.BloodRedDark
import com.example.blood_donor.ui.theme.PrimaryRed



import kotlinx.coroutines.flow.first
import com.example.blood_donor.data.TokenManager
import com.example.blood_donor.network.RetrofitClient

@Composable
fun SplashScreen(
    tokenManager: TokenManager,
    onNavigateToLanguageSelection: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    
    LaunchedEffect(key1 = true) {
        alpha.animateTo(1f, animationSpec = tween(1500))
        kotlinx.coroutines.delay(1000)
        
        try {
            val isLanguageSelected = tokenManager.hasLanguageSelectedFlow.first()
            if (!isLanguageSelected) {
                onNavigateToLanguageSelection()
            } else {
                val token = tokenManager.tokenFlow.first()
                if (!token.isNullOrEmpty()) {
                    val response = RetrofitClient.apiService.getMe()
                    if (response.isSuccessful && response.body() != null) {
                        onNavigateToDashboard()
                    } else {
                        tokenManager.clearToken()
                        onNavigateToOnboarding()
                    }
                } else {
                    onNavigateToOnboarding()
                }
            }
        } catch (e: Exception) {
            onNavigateToDashboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BloodRedDark, BloodRed, PrimaryRed)
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Heart/Drop Icon with pulse
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)
                    .background(Color.White.copy(alpha = 0.2f), shape = MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                // Using an emoji or simple shape for now, replace with actual SVG painter later if needed
                Text(text = "🩸", fontSize = 50.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Smart Blood\nDonor Finder",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 40.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Saving Lives Faster Through AI\n& Instant Donor Connection",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Bottom text
        Text(
            text = "Powered by AI",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
