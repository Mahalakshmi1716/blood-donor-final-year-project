package com.example.blood_donor.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.blood_donor.ui.screens.dashboard.ChatScreen
import com.example.blood_donor.data.TokenManager
import com.example.blood_donor.ui.screens.auth.LoginScreen
import com.example.blood_donor.ui.screens.auth.RegistrationScreen
import com.example.blood_donor.ui.screens.dashboard.DashboardScreen
import com.example.blood_donor.ui.screens.onboarding.OnboardingScreen
import com.example.blood_donor.ui.screens.splash.SplashScreen
import com.example.blood_donor.ui.viewmodels.AuthViewModel
import com.example.blood_donor.ui.viewmodels.DashboardViewModel
import com.example.blood_donor.ui.screens.dashboard.SettingsScreen
import com.example.blood_donor.ui.screens.dashboard.AiMatchingScreen
import com.example.blood_donor.ui.screens.dashboard.AvailabilityScreen
import com.example.blood_donor.ui.screens.dashboard.LockScreen

object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE_SELECTION = "language_selection"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTRATION = "registration"
    const val DASHBOARD = "dashboard"
    const val CHAT_SCREEN = "chat/{receiverId}/{receiverName}"
    const val SETTINGS = "settings"
    const val AI_MATCHING = "ai_matching"
    const val AVAILABILITY = "availability"
    const val LOCK_SCREEN = "lock_screen"
    const val OTP_VERIFICATION = "otp_verification/{email}"
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    
    val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(tokenManager) as T
        }
    })

    val dashboardViewModel: DashboardViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                tokenManager = tokenManager,
                onNavigateToLanguageSelection = {
                    navController.navigate(Routes.LANGUAGE_SELECTION) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LANGUAGE_SELECTION) {
            com.example.blood_donor.ui.screens.onboarding.LanguageSelectionScreen(
                tokenManager = tokenManager,
                onNavigateNext = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.LANGUAGE_SELECTION) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                tokenManager = tokenManager,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTRATION) },
                onNavigateToDashboard = {
                    dashboardViewModel.clearState()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToOtpVerification = { phone ->
                    navController.navigate("otp_verification/$phone")
                }
            )
        }
        composable(Routes.REGISTRATION) {
            RegistrationScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegistrationComplete = {
                    dashboardViewModel.clearState()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.REGISTRATION) { inclusive = true }
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToOtpVerification = { phone ->
                    navController.navigate("otp_verification/$phone")
                }
            )
        }
        composable(
            route = Routes.OTP_VERIFICATION,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            com.example.blood_donor.ui.screens.auth.OtpVerificationScreen(
                email = email,
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onVerificationSuccess = {
                    dashboardViewModel.clearState()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.OTP_VERIFICATION) { inclusive = true }
                        popUpTo(Routes.REGISTRATION) { inclusive = true }
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                tokenManager = tokenManager,
                viewModel = dashboardViewModel,
                onLogout = {
                    authViewModel.logout()
                    dashboardViewModel.clearState()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onNavigateToChat = { receiverId, receiverName ->
                    navController.navigate("chat/$receiverId/$receiverName")
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToAiMatching = {
                    navController.navigate(Routes.AI_MATCHING)
                },
                onNavigateToAvailability = {
                    navController.navigate(Routes.AVAILABILITY)
                },
                onNavigateToLockScreen = {
                    navController.navigate(Routes.LOCK_SCREEN)
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    dashboardViewModel.clearState()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SETTINGS) { inclusive = true }
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.AI_MATCHING) {
            AiMatchingScreen(
                viewModel = dashboardViewModel,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { receiverId, receiverName ->
                    navController.navigate("chat/$receiverId/$receiverName")
                }
            )
        }
        composable(Routes.AVAILABILITY) {
            AvailabilityScreen(
                viewModel = dashboardViewModel,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.LOCK_SCREEN) {
            LockScreen(
                viewModel = dashboardViewModel,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.CHAT_SCREEN,
            arguments = listOf(
                navArgument("receiverId") { type = NavType.IntType },
                navArgument("receiverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getInt("receiverId") ?: 0
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: "Unknown"
            ChatScreen(
                receiverId = receiverId,
                receiverName = receiverName,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
