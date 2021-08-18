package com.dvc.utils;

import com.dvc.models.RecipentsData;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;

public class CustomPDFWriter {
    private PDFWriter pdfWriter;

    public CustomPDFWriter(PDFWriter pdfWriter, RecipentsData rdata, Document document, Font font, Font hfont,
            Font fontb, Font idFont, Font lblFont) {
        super();
        this.pdfWriter = pdfWriter;
        this.pdfWriter.writePDF(rdata, document, font, hfont, fontb, idFont, lblFont);
    }

    public PDFWriter getCustomPdfWriter() {
        return pdfWriter;
    }

    public void setPdfWriter(PDFWriter pdfWriter) {
        this.pdfWriter = pdfWriter;
    }
}