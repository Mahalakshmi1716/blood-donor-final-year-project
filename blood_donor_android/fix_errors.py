import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    package_line = None
    imports = []
    other_lines = []

    for line in lines:
        if line.startswith('package '):
            package_line = line
        elif line.startswith('import '):
            imports.append(line)
        else:
            other_lines.append(line)

    # Sort and deduplicate imports
    imports = sorted(list(set(imports)))

    # Fix specific errors in other_lines
    for i in range(len(other_lines)):
        line = other_lines[i]
        
        # TextFieldDefaults.outlinedTextFieldColors -> OutlinedTextFieldDefaults.colors
        if "TextFieldDefaults.outlinedTextFieldColors" in line:
            line = line.replace("TextFieldDefaults.outlinedTextFieldColors", "OutlinedTextFieldDefaults.colors")
            if "import androidx.compose.material3.OutlinedTextFieldDefaults\n" not in imports:
                imports.append("import androidx.compose.material3.OutlinedTextFieldDefaults\n")

        # Icons.Default.ArrowBack -> Icons.AutoMirrored.Filled.ArrowBack
        if "Icons.Default.ArrowBack" in line:
            line = line.replace("Icons.Default.ArrowBack", "Icons.AutoMirrored.Filled.ArrowBack")
            
        # systemBarsPadding
        if ".systemBarsPadding()" in line and "import androidx.compose.foundation.layout.systemBarsPadding\n" not in imports:
            imports.append("import androidx.compose.foundation.layout.systemBarsPadding\n")

        # Animatable
        if "Animatable(" in line and "import androidx.compose.animation.core.Animatable\n" not in imports:
            imports.append("import androidx.compose.animation.core.Animatable\n")
            
        # Arrangement
        if "Arrangement." in line and "import androidx.compose.foundation.layout.Arrangement\n" not in imports:
            imports.append("import androidx.compose.foundation.layout.Arrangement\n")
            
        # clickable
        if ".clickable " in line or ".clickable {" in line:
            if "import androidx.compose.foundation.clickable\n" not in imports:
                imports.append("import androidx.compose.foundation.clickable\n")
                
        other_lines[i] = line

    # Reconstruct file
    with open(filepath, 'w', encoding='utf-8') as f:
        if package_line:
            f.write(package_line)
            f.write("\n")
        
        for imp in sorted(list(set(imports))):
            f.write(imp)
            
        f.write("\n")
        
        for line in other_lines:
            f.write(line)

def main():
    directory = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor"
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                fix_file(os.path.join(root, file))
                
    # Specific fix for Theme.kt: Add Color import
    theme_kt = os.path.join(directory, "ui", "theme", "Theme.kt")
    with open(theme_kt, 'r', encoding='utf-8') as f:
        content = f.read()
    if "import androidx.compose.ui.graphics.Color" not in content:
        content = content.replace("package com.example.blood_donor.ui.theme\n", "package com.example.blood_donor.ui.theme\nimport androidx.compose.ui.graphics.Color\n")
        with open(theme_kt, 'w', encoding='utf-8') as f:
            f.write(content)

if __name__ == "__main__":
    main()
