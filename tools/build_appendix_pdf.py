"""Build docs/appendix.pdf with all project source code, syntax-highlighted, one file per page."""
import os
import sys
from pathlib import Path
from xml.sax.saxutils import escape as xml_escape

from pygments import lex
from pygments.lexers import get_lexer_for_filename, guess_lexer
from pygments.styles import get_style_by_name
from pygments.token import Token
from pygments.util import ClassNotFound

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import mm
from reportlab.pdfgen import canvas
from reportlab.platypus import (
    BaseDocTemplate, Frame, PageTemplate, Paragraph, XPreformatted,
    PageBreak, Spacer,
)

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "docs" / "appendix.pdf"

# File patterns to include
INCLUDE_GLOBS = [
    "app/src/**/*.java",
    "app/src/**/*.kt",
    "app/src/**/*.xml",
    "app/build.gradle.kts",
    "app/proguard-rules.pro",
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
    "firebase.json",
    "firestore.rules",
    "firestore.indexes.json",
    "functions/index.js",
    "functions/package.json",
]

EXCLUDE_PARTS = {"node_modules", "build", ".gradle", ".idea"}


def collect_files():
    files = []
    seen = set()
    for pat in INCLUDE_GLOBS:
        for p in ROOT.glob(pat):
            if not p.is_file():
                continue
            if any(part in EXCLUDE_PARTS for part in p.parts):
                continue
            if p in seen:
                continue
            seen.add(p)
            files.append(p)
    files.sort(key=lambda p: str(p.relative_to(ROOT)).lower())
    return files


def token_color(style, ttype):
    while ttype is not None:
        s = style.styles.get(ttype)
        if s:
            for part in s.split():
                if part.startswith("#") and len(part) in (4, 7):
                    return part
            # noinherit means stop
            if "noinherit" in s:
                return None
        ttype = ttype.parent
    return None


def token_bold_italic(style, ttype):
    bold = italic = False
    cur = ttype
    while cur is not None:
        s = style.styles.get(cur, "")
        if "bold" in s:
            bold = True
        if "italic" in s:
            italic = True
        cur = cur.parent
    return bold, italic


def highlight_to_xml(code, lexer, style):
    """Tokenize and return an XML string suitable for reportlab XPreformatted."""
    out = []
    for ttype, value in lex(code, lexer):
        if not value:
            continue
        color = token_color(style, ttype)
        bold, italic = token_bold_italic(style, ttype)
        text = xml_escape(value).replace("\t", "    ")
        if color or bold or italic:
            attrs = ""
            if color:
                attrs = f' color="{color}"'
            open_tags = ""
            close_tags = ""
            if attrs:
                open_tags += f"<font{attrs}>"
                close_tags = "</font>" + close_tags
            if bold:
                open_tags += "<b>"
                close_tags = "</b>" + close_tags
            if italic:
                open_tags += "<i>"
                close_tags = "</i>" + close_tags
            out.append(open_tags + text + close_tags)
        else:
            out.append(text)
    return "".join(out)


def get_lexer(path: Path, source: str):
    try:
        return get_lexer_for_filename(path.name, stripall=False)
    except ClassNotFound:
        pass
    # Fallback by extension
    ext = path.suffix.lower()
    if ext in (".gradle", ".kts"):
        from pygments.lexers.jvm import KotlinLexer
        return KotlinLexer()
    if ext == ".pro":
        from pygments.lexers.special import TextLexer
        return TextLexer()
    try:
        return guess_lexer(source)
    except ClassNotFound:
        from pygments.lexers.special import TextLexer
        return TextLexer()


def build():
    files = collect_files()
    if not files:
        print("No files found", file=sys.stderr)
        sys.exit(1)
    print(f"Including {len(files)} files")

    style = get_style_by_name("friendly")  # light theme
    bg = style.background_color or "#ffffff"

    OUT.parent.mkdir(parents=True, exist_ok=True)

    page_w, page_h = A4
    margin = 14 * mm
    frame = Frame(margin, margin, page_w - 2 * margin, page_h - 2 * margin,
                  leftPadding=0, rightPadding=0, topPadding=0, bottomPadding=0,
                  showBoundary=0)

    def on_page(canv, doc):
        canv.saveState()
        canv.setFillColorRGB(1, 1, 1)
        canv.rect(0, 0, page_w, page_h, fill=1, stroke=0)
        # footer page number
        canv.setFont("Helvetica", 8)
        canv.setFillColorRGB(0.4, 0.4, 0.4)
        canv.drawCentredString(page_w / 2, 8 * mm, f"Page {doc.page}")
        canv.restoreState()

    template = PageTemplate(id="code", frames=[frame], onPage=on_page)
    doc = BaseDocTemplate(str(OUT), pagesize=A4, pageTemplates=[template],
                          leftMargin=margin, rightMargin=margin,
                          topMargin=margin, bottomMargin=margin,
                          title="Phiz Source Code Appendix")

    title_style = ParagraphStyle(
        "title", fontName="Helvetica-Bold", fontSize=12, leading=15,
        textColor="#222222", spaceAfter=4,
    )
    path_style = ParagraphStyle(
        "path", fontName="Helvetica", fontSize=9, leading=11,
        textColor="#666666", spaceAfter=8,
    )
    code_style = ParagraphStyle(
        "code", fontName="Courier", fontSize=7.2, leading=9,
        textColor="#000000",
    )

    story = []
    cover_title = ParagraphStyle("cover", fontName="Helvetica-Bold", fontSize=22,
                                 leading=28, alignment=1, spaceAfter=20)
    cover_sub = ParagraphStyle("coversub", fontName="Helvetica", fontSize=12,
                               leading=16, alignment=1, textColor="#555555")
    story.append(Spacer(1, 60 * mm))
    story.append(Paragraph("Source Code Appendix", cover_title))
    story.append(Paragraph("Phiz — Physics Simulator Android App", cover_sub))
    story.append(Spacer(1, 12))
    story.append(Paragraph(f"{len(files)} source files", cover_sub))
    story.append(PageBreak())

    for i, path in enumerate(files):
        rel = path.relative_to(ROOT).as_posix()
        try:
            source = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            source = path.read_text(encoding="latin-1")
        lexer = get_lexer(path, source)
        xml_body = highlight_to_xml(source, lexer, style)

        story.append(Paragraph(xml_escape(path.name), title_style))
        story.append(Paragraph(xml_escape(rel), path_style))
        # XPreformatted preserves whitespace and accepts inline tags
        story.append(XPreformatted(xml_body, code_style))
        if i != len(files) - 1:
            story.append(PageBreak())
        print(f"  [{i+1}/{len(files)}] {rel}")

    doc.build(story)
    print(f"\nWrote {OUT} ({OUT.stat().st_size/1024:.1f} KB)")


if __name__ == "__main__":
    build()
