"""
Post-processes a DOCX file to apply RTL direction to all paragraphs and runs.
"""
import sys
from docx import Document
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

def set_rtl(doc_path, out_path):
    doc = Document(doc_path)

    for para in doc.paragraphs:
        # Set RTL on paragraph properties
        pPr = para._p.get_or_add_pPr()
        bidi = OxmlElement('w:bidi')
        bidi.set(qn('w:val'), '1')
        if pPr.find(qn('w:bidi')) is None:
            pPr.insert(0, bidi)

        # Right-align paragraph
        jc = pPr.find(qn('w:jc'))
        if jc is None:
            jc = OxmlElement('w:jc')
            pPr.append(jc)
        jc.set(qn('w:val'), 'right')

        # Set RTL on each run
        for run in para.runs:
            rPr = run._r.get_or_add_rPr()
            rtl = rPr.find(qn('w:rtl'))
            if rtl is None:
                rtl = OxmlElement('w:rtl')
                rtl.set(qn('w:val'), '1')
                rPr.append(rtl)

    # Also process tables
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                for para in cell.paragraphs:
                    pPr = para._p.get_or_add_pPr()
                    bidi = OxmlElement('w:bidi')
                    bidi.set(qn('w:val'), '1')
                    if pPr.find(qn('w:bidi')) is None:
                        pPr.insert(0, bidi)
                    jc = pPr.find(qn('w:jc'))
                    if jc is None:
                        jc = OxmlElement('w:jc')
                        pPr.append(jc)
                    jc.set(qn('w:val'), 'right')
                    for run in para.runs:
                        rPr = run._r.get_or_add_rPr()
                        if rPr.find(qn('w:rtl')) is None:
                            rtl = OxmlElement('w:rtl')
                            rtl.set(qn('w:val'), '1')
                            rPr.append(rtl)

    doc.save(out_path)
    print(f"Saved RTL document: {out_path}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: apply_rtl.py input.docx output.docx")
        sys.exit(1)
    set_rtl(sys.argv[1], sys.argv[2])
