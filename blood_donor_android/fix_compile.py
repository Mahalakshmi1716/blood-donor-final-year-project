import os
import re

appnav_path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\navigation\AppNavigation.kt"
dash_path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\screens\dashboard\DashboardScreen.kt"

# Fix AppNavigation.kt
with open(appnav_path, "r", encoding="utf-8") as f:
    appnav = f.read()

appnav = appnav.replace(
"""                onNavigateToChat = { receiverId, receiverName ->
                    navController.navigate("chat/$receiverId/$receiverName")
                    }
                }
            )
        composable(""",
"""                onNavigateToChat = { receiverId, receiverName ->
                    navController.navigate("chat/$receiverId/$receiverName")
                }
            )
        }
        composable("""
)

with open(appnav_path, "w", encoding="utf-8") as f:
    f.write(appnav)


# Fix DashboardScreen.kt
with open(dash_path, "r", encoding="utf-8") as f:
    dash = f.read()

dash = dash.replace(
    "val conversations by chatViewModel.conversations.androidx.compose.runtime.collectAsState()",
    "val conversations by chatViewModel.conversations.collectAsState()"
)
dash = dash.replace(
    "val isLoading by chatViewModel.isLoading.androidx.compose.runtime.collectAsState()",
    "val isLoading by chatViewModel.isLoading.collectAsState()"
)
dash = dash.replace(
    "androidx.compose.runtime.LaunchedEffect(Unit)",
    "LaunchedEffect(Unit)"
)

with open(dash_path, "w", encoding="utf-8") as f:
    f.write(dash)
