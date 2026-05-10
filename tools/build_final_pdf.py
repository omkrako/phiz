"""Convert latest DOCX chapters to PDF and merge with appendix.pdf into docs/final_project.pdf."""
import sys
from pathlib import Path
import tempfile
import shutil

import win32com.client
from pypdf import PdfWriter, PdfReader

WD_FORMAT_PDF = 17


def docx_to_pdf(word, src: Path, dst: Path):
    doc = word.Documents.Open(str(src), ReadOnly=True, AddToRecentFiles=False)
    try:
        doc.ExportAsFixedFormat(
            OutputFileName=str(dst),
            ExportFormat=WD_FORMAT_PDF,
            OpenAfterExport=False,
            OptimizeFor=0,  # wdExportOptimizeForPrint
            CreateBookmarks=1,  # wdExportCreateHeadingBookmarks
        )
    finally:
        doc.Close(SaveChanges=False)

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
OUT = DOCS / "final_project.pdf"

# Logical order for the project book
CHAPTERS = [
    DOCS / "intro-updated.docx",
    DOCS / "screens-and-architecture-he.docx",
    DOCS / "class-documentation-he.docx",
    DOCS / "user-manual-he-v3.docx",
    DOCS / "reflecation.docx",
]
APPENDIX = DOCS / "appendix.pdf"


def main():
    for p in CHAPTERS + [APPENDIX]:
        if not p.exists():
            print(f"Missing: {p}", file=sys.stderr)
            sys.exit(1)

    tmp = Path(tempfile.mkdtemp(prefix="phiz_pdf_"))
    print(f"Working dir: {tmp}")
    pdfs = []
    # DispatchEx forces a NEW Word process so we don't share state with an already-open Word window
    word = win32com.client.DispatchEx("Word.Application")
    word.Visible = False
    word.DisplayAlerts = 0
    try:
        for i, docx in enumerate(CHAPTERS, 1):
            staged = tmp / f"{i:02d}_{docx.stem}.docx"
            shutil.copy2(docx, staged)
            target = tmp / f"{i:02d}_{docx.stem}.pdf"
            print(f"[{i}/{len(CHAPTERS)}] Converting {docx.name} ...")
            docx_to_pdf(word, staged, target)
            if not target.exists():
                print(f"  Conversion failed for {docx}", file=sys.stderr)
                sys.exit(1)
            pdfs.append(target)

        pdfs.append(APPENDIX)

        print("Merging ...")
        writer = PdfWriter()
        for pdf in pdfs:
            reader = PdfReader(str(pdf))
            start = len(writer.pages)
            for page in reader.pages:
                writer.add_page(page)
            label = pdf.stem
            if pdf == APPENDIX:
                label = "Appendix - Source Code"
            writer.add_outline_item(label, start)

        with open(OUT, "wb") as f:
            writer.write(f)
        print(f"\nWrote {OUT} ({OUT.stat().st_size/1024:.1f} KB, {len(writer.pages)} pages)")
    finally:
        try:
            word.Quit()
        except Exception:
            pass
        shutil.rmtree(tmp, ignore_errors=True)


if __name__ == "__main__":
    main()
