import os
import re

# Root directory of the repository for batch operations
PROJECT_ROOT = "."

def clean_file_content(content, ext):
    """Strip code comments while preserving string literals and URLs."""
    if ext in ['.kt', '.java', '.kts']:
        # Remove multi-line block comments
        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
        # Remove single-line comments while ignoring inline URLs (http:// or https://)
        content = re.sub(r'(?<!https:)(?<!http:)//.*', '', content)
    elif ext in ['.py', '.pro']:
        # Remove full-line and trailing hash comments
        content = re.sub(r'(?m)^\s*#.*$', '', content)
        content = re.sub(r' \s*#.*$', '', content)
    elif ext == '.xml':
        # Remove XML block comments
        content = re.sub(r'<!--.*?-->', '', content, flags=re.DOTALL)

    # Collapse consecutive blank lines
    content = re.sub(r'\n\s*\n', '\n\n', content)
    return content

def run_stripper():
    """Traverse project files and strip existing comments."""
    processed_count = 0
    target_extensions = ['.kt', '.java', '.kts', '.py', '.xml', '.pro']

    for root, dirs, files in os.walk(PROJECT_ROOT):
        # Exclude hidden directories and build outputs
        dirs[:] = [d for d in dirs if not d.startswith('.') and d != 'build']

        for file in files:
            ext = os.path.splitext(file)[1]
            if ext in target_extensions:
                filepath = os.path.join(root, file)

                with open(filepath, 'r', encoding='utf-8') as f:
                    original_content = f.read()

                cleaned_content = clean_file_content(original_content, ext)

                if original_content != cleaned_content:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(cleaned_content)
                    processed_count += 1
                    print(f"Cleaned: {filepath}")

    print(f"\nDone! Stripped comments from {processed_count} files.")

if __name__ == "__main__":
    run_stripper()