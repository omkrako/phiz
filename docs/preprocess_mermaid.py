"""
Preprocesses a markdown file: extracts mermaid code blocks,
renders them to PNG with mmdc, and replaces them with image references.
"""
import re
import subprocess
import sys
import os

def main():
    if len(sys.argv) < 3:
        print("Usage: preprocess_mermaid.py input.md output.md")
        sys.exit(1)

    input_path = sys.argv[1]
    output_path = sys.argv[2]
    img_dir = os.path.join(os.path.dirname(input_path), "images", "diagrams")
    os.makedirs(img_dir, exist_ok=True)

    with open(input_path, encoding="utf-8") as f:
        content = f.read()

    counter = [0]

    def replace_mermaid(match):
        diagram_src = match.group(1)
        counter[0] += 1
        name = f"diagram-{counter[0]:02d}"
        mmd_path = os.path.join(img_dir, f"{name}.mmd")
        png_path = os.path.join(img_dir, f"{name}.png")

        with open(mmd_path, "w", encoding="utf-8") as f:
            f.write(diagram_src.strip())

        mmdc_cmd = r"C:\Users\omer\AppData\Roaming\npm\mmdc.cmd"
        result = subprocess.run(
            [mmdc_cmd, "-i", mmd_path, "-o", png_path,
             "-b", "white", "-w", "900", "-H", "600"],
            capture_output=True, text=True, shell=True
        )
        if result.returncode != 0:
            print(f"Warning: mmdc failed for {name}: {result.stderr}", file=sys.stderr)
            return match.group(0)  # keep original block on failure

        # Use a relative path from the md file location
        rel_path = os.path.relpath(png_path, os.path.dirname(input_path))
        rel_path = rel_path.replace("\\", "/")
        return f"![]({rel_path})\n"

    pattern = re.compile(r"```mermaid\n(.*?)```", re.DOTALL)
    processed = pattern.sub(replace_mermaid, content)

    with open(output_path, "w", encoding="utf-8") as f:
        f.write(processed)

    print(f"Done. {counter[0]} diagram(s) rendered.")

if __name__ == "__main__":
    main()
