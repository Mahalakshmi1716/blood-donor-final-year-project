import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Revert the bad global replacement of containerColor
    content = content.replace(
        "focusedContainerColor = Color.White,\n                    unfocusedContainerColor = Color.White",
        "containerColor = Color.White"
    )
    content = content.replace(
        "focusedContainerColor = Color.White,\n unfocusedContainerColor = Color.White",
        "containerColor = Color.White"
    )

    # Now correctly fix OutlinedTextFieldDefaults.colors
    # Find all instances of OutlinedTextFieldDefaults.colors(...)
    # and replace containerColor with focusedContainerColor and unfocusedContainerColor
    def replacer(match):
        inner = match.group(1)
        inner = inner.replace("containerColor = Color.White", "focusedContainerColor = Color.White, unfocusedContainerColor = Color.White")
        return f"OutlinedTextFieldDefaults.colors({inner})"
        
    content = re.sub(r'OutlinedTextFieldDefaults\.colors\((.*?)\)', replacer, content, flags=re.DOTALL)

    # Fix SplashScreen Animatable
    if "SplashScreen.kt" in filepath:
        content = content.replace("scale(scale)", "scale(scale.value)")
        
    # Fix DashboardScreen conflicting imports
    if "DashboardScreen.kt" in filepath:
        # Just deduplicate the whole imports block
        lines = content.split('\n')
        new_lines = []
        seen_imports = set()
        for line in lines:
            if line.startswith('import '):
                if line in seen_imports:
                    continue
                seen_imports.add(line)
            new_lines.append(line)
        content = '\n'.join(new_lines)

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
