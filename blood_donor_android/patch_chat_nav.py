import os
import re

dashboard_path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\screens\dashboard\DashboardScreen.kt"
appnav_path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\navigation\AppNavigation.kt"

# Patch DashboardScreen.kt
with open(dashboard_path, "r", encoding="utf-8") as f:
    dashboard_content = f.read()

# Add onNavigateToChat to DashboardScreen
dashboard_content = dashboard_content.replace(
    "onLogout: () -> Unit = {}",
    "onLogout: () -> Unit = {},\n    onNavigateToChat: (Int, String) -> Unit = { _, _ -> }"
)

# Pass onNavigateToChat to ChatTab
dashboard_content = dashboard_content.replace(
    "DashboardTab.CHAT -> ChatTab()",
    "DashboardTab.CHAT -> ChatTab(onNavigateToChat = onNavigateToChat)"
)

# Add onNavigateToChat to ChatTab definition
dashboard_content = dashboard_content.replace(
    "fun ChatTab(chatViewModel: com.example.blood_donor.ui.viewmodels.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {",
    "fun ChatTab(chatViewModel: com.example.blood_donor.ui.viewmodels.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), onNavigateToChat: (Int, String) -> Unit = { _, _ -> }) {"
)

# Add navigation on click
dashboard_content = dashboard_content.replace(
    "// TODO: Navigate to individual ChatScreen",
    "onNavigateToChat(convo.userId, convo.name)"
)

with open(dashboard_path, "w", encoding="utf-8") as f:
    f.write(dashboard_content)


# Patch AppNavigation.kt
with open(appnav_path, "r", encoding="utf-8") as f:
    appnav_content = f.read()

# Add Route
appnav_content = appnav_content.replace(
    'const val DASHBOARD = "dashboard"',
    'const val DASHBOARD = "dashboard"\n    const val CHAT_SCREEN = "chat/{receiverId}/{receiverName}"'
)

# Add navigation imports
if "import androidx.navigation.NavType" not in appnav_content:
    appnav_content = appnav_content.replace(
        "import androidx.navigation.compose.rememberNavController",
        "import androidx.navigation.compose.rememberNavController\nimport androidx.navigation.NavType\nimport androidx.navigation.navArgument\nimport com.example.blood_donor.ui.screens.dashboard.ChatScreen"
    )

# Add onNavigateToChat in DashboardScreen composable
appnav_content = appnav_content.replace(
    "popUpTo(Routes.DASHBOARD) { inclusive = true }",
    "popUpTo(Routes.DASHBOARD) { inclusive = true }\n                    }\n                },\n                onNavigateToChat = { receiverId, receiverName ->\n                    navController.navigate(\"chat/$receiverId/$receiverName\")"
)

# Add ChatScreen composable
chat_composable = """        composable(
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}"""

appnav_content = re.sub(r'        }\s*\}\s*\}', chat_composable, appnav_content)

with open(appnav_path, "w", encoding="utf-8") as f:
    f.write(appnav_content)
