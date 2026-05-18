import os
import re

C_STYLE_EXTENSIONS = {'.kt', '.kts', '.ts', '.tsx', '.js', '.java'}
PYTHON_EXTENSIONS = {'.py'}

def remove_c_style_comments(text):

    pattern = re.compile(
        r'("(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|`(?:\\.|[^`\\])*`)|//.*?$|/\*[\s\S]*?\*/', 
        re.MULTILINE
    )
    return pattern.sub(lambda m: m.group(1) if m.group(1) else '', text)

def remove_python_comments(text):

    pattern = re.compile(
        r'(\"\"\"[\s\S]*?\"\"\"|\'\'\'[\s\S]*?\'\'\'|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\')|#.*?$', 
        re.MULTILINE
    )
    return pattern.sub(lambda m: m.group(1) if m.group(1) else '', text)

def clean_empty_lines(text):

    return re.sub(r'\n\s*\n', '\n\n', text)

def process_directory(root_dir):
    for dirpath, _, filenames in os.walk(root_dir):

        if any(ignored in dirpath for ignored in ['.idea', 'build', 'node_modules', '.git', 'venv', '__pycache__']):
            continue

        for filename in filenames:
            ext = os.path.splitext(filename)[1]
            filepath = os.path.join(dirpath, filename)

            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()

                original_content = content

                if ext in C_STYLE_EXTENSIONS:
                    content = remove_c_style_comments(content)
                elif ext in PYTHON_EXTENSIONS:
                    content = remove_python_comments(content)
                else:
                    continue

                if content != original_content:
                    content = clean_empty_lines(content)
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(content)
                    print(f"Purged: {filepath}")

            except Exception as e:
                print(f"Skipped {filepath} due to error: {e}")

if __name__ == "__main__":
    print("Starting comment purge...")
    process_directory(".")
    print("\nPurge complete!")
