import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Fix OutlinedTextField colors
    content = content.replace(
        "containerColor = Color.White",
        "focusedContainerColor = Color.White,\n                    unfocusedContainerColor = Color.White"
    )

    # 2. Fix DashboardScreen.kt imports
    if "DashboardScreen.kt" in filepath:
        if "import androidx.compose.material.icons.automirrored.filled.ArrowBack" not in content:
            content = content.replace(
                "package com.example.blood_donor.ui.screens.dashboard\n",
                "package com.example.blood_donor.ui.screens.dashboard\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack\nimport androidx.compose.material.icons.Icons\n"
            )

    # 3. Fix SplashScreen.kt imports
    if "SplashScreen.kt" in filepath:
        needed_imports = [
            "import androidx.compose.runtime.remember",
            "import androidx.compose.animation.core.tween",
            "import androidx.compose.animation.core.infiniteRepeatable",
            "import androidx.compose.animation.core.RepeatMode",
            "import androidx.compose.animation.core.FastOutSlowInEasing"
        ]
        for imp in needed_imports:
            if imp not in content:
                content = content.replace(
                    "package com.example.blood_donor.ui.screens.splash\n",
                    f"package com.example.blood_donor.ui.screens.splash\n{imp}\n"
                )

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

def main():
    directory = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor"
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                fix_file(os.path.join(root, file))

if __name__ == "__main__":
    main()
