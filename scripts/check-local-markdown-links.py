#!/usr/bin/env python3
"""Check local Markdown links in public repository documentation."""

from __future__ import annotations

import re
import sys
from pathlib import Path
from urllib.parse import unquote


INLINE_LINK = re.compile(r"(?<!!)\[[^\]]+\]\(([^)]+)\)")
IMAGE_LINK = re.compile(r"!\[[^\]]*\]\(([^)]+)\)")
SKIPPED_PREFIXES = ("http://", "https://", "mailto:", "#")
PUBLIC_PATHS = (
    "README.md",
    "CONTRIBUTING.md",
    "CODE_OF_CONDUCT.md",
    "docs",
    "reports",
    ".github",
)


def iter_markdown_files(root: Path):
    for public_path in PUBLIC_PATHS:
        path = root / public_path
        if path.is_file():
            yield path
        elif path.exists():
            yield from sorted(path.rglob("*.md"))


def iter_links(markdown_file: Path):
    in_fence = False
    for line_number, line in enumerate(
        markdown_file.read_text(encoding="utf-8").splitlines(), start=1
    ):
        if line.strip().startswith("```"):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        for regex in (INLINE_LINK, IMAGE_LINK):
            for match in regex.finditer(line):
                yield line_number, match.group(1).strip()


def normalize_target(target: str) -> str:
    if target.startswith("<") and target.endswith(">"):
        target = target[1:-1]
    return unquote(target.split()[0].split("#", 1)[0])


def main() -> int:
    root = Path.cwd().resolve()
    problems: list[str] = []
    checked = 0

    for markdown_file in iter_markdown_files(root):
        for line_number, raw_target in iter_links(markdown_file):
            if not raw_target or raw_target.startswith(SKIPPED_PREFIXES):
                continue

            target = normalize_target(raw_target)
            if not target:
                continue

            checked += 1
            resolved = (markdown_file.parent / target).resolve()

            try:
                resolved.relative_to(root)
            except ValueError:
                problems.append(
                    f"{markdown_file.relative_to(root)}:{line_number}: "
                    f"{raw_target} escapes repository"
                )
                continue

            if not resolved.exists():
                problems.append(
                    f"{markdown_file.relative_to(root)}:{line_number}: "
                    f"{raw_target} does not exist"
                )

    if problems:
        print("Broken local Markdown links:")
        for problem in problems:
            print(f"- {problem}")
        return 1

    print(f"Checked {checked} local Markdown links.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
