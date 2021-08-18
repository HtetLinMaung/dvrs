package com.dvc.utils;

import com.dvc.models.RecipentsData;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;

public interface PDFWriter {

	void writePDF(RecipentsData rdata, Document document, Font font, Font hfont, Font fontb, Font idFont, Font lblFont);

}
