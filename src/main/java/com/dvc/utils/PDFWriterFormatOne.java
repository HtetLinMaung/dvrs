package com.dvc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import com.dvc.models.RecipentsData;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class PDFWriterFormatOne implements PDFWriter {

        @Override
        public void writePDF(RecipentsData rdata, Document document, Font font, Font hfont, Font fontb, Font idFont,
                        Font lblFont) {
                try {
                        PdfPTable table = new PdfPTable(5);
                        table.setTotalWidth(new float[] { 100, 100, 100, 100, 100 });
                        table.setLockedWidth(true);
                        PdfPTable headertable = new PdfPTable(3);
                        PdfPTable firstbodytable = new PdfPTable(6);
                        PdfPTable secondbodytable = new PdfPTable(4);
                        PdfPTable thirdbodytable = new PdfPTable(4);
                        headertable.setWidths(new int[] { 1, 5, 2 });
                        headertable.setTotalWidth(new float[] { 60, 310, 130 });
                        headertable.setLockedWidth(true);

                        firstbodytable.setWidths(new int[] { 1, 5, 1, 3, 1, 3 });
                        firstbodytable.setTotalWidth(new float[] { 30, 200, 40, 90, 40, 100 });
                        firstbodytable.setLockedWidth(true);

                        secondbodytable.setWidths(new int[] { 3, 3, 2, 3 });
                        secondbodytable.setTotalWidth(new float[] { 115, 165, 50, 170 });
                        secondbodytable.setLockedWidth(true);

                        PdfPCell cell;
                        cell = new PdfPCell();
                        cell.setBorder(0);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        headertable.addCell(cell);

                        headertable.addCell(new PDFUtil().writePDFCell("COVID-19 ကာကြယ္ေဆးထိုးမွတ္တမ္းကတ္ျပား", hfont,
                                        Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, 0, -1f, 0, -1f));

                        ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) new PDFUtil()
                                        .generateQR(rdata.getQrToken(), rdata.getSyskey());
                        com.itextpdf.text.Image image = null;
                        if (byteArrayOutputStream != null) {
                                image = com.itextpdf.text.Image.getInstance(byteArrayOutputStream.toByteArray());
                                float imageWidth1 = 40;
                                float imageHeight1 = 30;
                                image.scaleAbsolute(imageWidth1, imageHeight1);
                                image.setWidthPercentage(90);
                                image.setAlignment(com.itextpdf.text.Image.ALIGN_LEFT);
                        }
                        PdfPCell cell1 = new PdfPCell();
                        Paragraph p = new Paragraph("ID: " + rdata.getCertificateID(), idFont);
                        p.setAlignment(Element.ALIGN_LEFT);
                        cell1.addElement(p);
                        cell1.addElement(image);
                        cell1.setBorder(0);
                        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        headertable.addCell(cell1);

                        firstbodytable.addCell(new PDFUtil().writePDFCell("အမည္", fontb, -1, -1, 0, -1f, 0, -1f));
                        firstbodytable.addCell(
                                        new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(rdata.getRecipientsName(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, -1, -1, 0, 0.5f, 0, -1f));
                        firstbodytable.addCell(new PDFUtil().writePDFCell("က်ား/မ", fontb, -1, Element.ALIGN_RIGHT, 0,
                                        -1f, 0, -1f));
                        firstbodytable.addCell(new PDFUtil().writePDFCell(
                                        new PDFUtil().convertString(rdata.getGender(), "Myanmar3", "Zawgyi_One"), fontb,
                                        -1, -1, 0, 0.5f, 0, -1f));
                        firstbodytable.addCell(new PDFUtil().writePDFCell("အသက္", fontb, -1, Element.ALIGN_RIGHT, 0,
                                        -1f, 0, -1f));
                        firstbodytable.addCell(new PDFUtil().writePDFCell(new PDFUtil()
                                        .convertString(rdata.getAge() + "", "Myanmar3", "Zawgyi_One")
                                        + new PDFUtil().convertString("      နှစ်", "Myanmar3", "Zawgyi_One"), fontb,
                                        -1, -1, 0, 0.5f, 0, -1f));

                        new PDFUtil().newRow(secondbodytable, fontb);

                        secondbodytable.addCell(new PDFUtil().writePDFCell("ႏိုင္ငံသားစိစစ္ေရးကတ္ျပားအမွတ္", fontb, -1,
                                        -1, 0, -1f, 0, -1f));
                        secondbodytable.addCell(new PDFUtil().writePDFCell(
                                        new PDFUtil().convertString(rdata.getNRIC(), "Myanmar3", "Zawgyi_One"), fontb,
                                        -1, -1, 0, 0.5f, 0, -1f));
                        secondbodytable.addCell(new PDFUtil().writePDFCell("အလုပ္အကိုင္", fontb, -1,
                                        Element.ALIGN_RIGHT, 0, -1f, 0, -1f));
                        secondbodytable.addCell(new PDFUtil().writePDFCell(
                                        new PDFUtil().convertString(rdata.getOccupation(), "Myanmar3", "Zawgyi_One"),
                                        fontb, -1, -1, 0, 0.5f, 0, -1f));

                        new PDFUtil().newRow(thirdbodytable, fontb);
                        thirdbodytable.setWidths(new int[] { 1, 2, 1, 5 });
                        thirdbodytable.setTotalWidth(new float[] { 50, 90, 50, 310 });
                        thirdbodytable.setLockedWidth(true);

                        thirdbodytable.addCell(
                                        new PDFUtil().writePDFCell("ဖုန္းနံပါတ္", fontb, -1, -1, 0, -1f, 0, -1f));
                        thirdbodytable.addCell(new PDFUtil().writePDFCell(
                                        new PDFUtil().convertString(rdata.getMobilePhone(), "Myanmar3", "Zawgyi_One"),
                                        fontb, -1, -1, 0, 0.5f, 0, -1f));
                        thirdbodytable.addCell(new PDFUtil().writePDFCell("ေနရပ္လိပ္စာ", fontb, -1, Element.ALIGN_RIGHT,
                                        0, -1f, 0, -1f));
                        thirdbodytable.addCell(new PDFUtil().writePDFCell(
                                        new PDFUtil().convertString(rdata.getAddress1(), "Myanmar3", "Zawgyi_One"),
                                        fontb, -1, -1, 0, 0.5f, 0, -1f));

                        new PDFUtil().newRow(thirdbodytable, fontb);
                        new PDFUtil().newRow(thirdbodytable, fontb);

                        table.addCell(new PDFUtil().writePDFCell("ကာကြယ္ေဆးထိုးႏွံမႈ", font, Element.ALIGN_MIDDLE,
                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                        table.addCell(new PDFUtil().writePDFCell("ေဆးအမည္/ ထုတ္လုပ္သူ/ \n\nLot အမွတ္", font,
                                        Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                        table.addCell(new PDFUtil().writePDFCell("ေဆးထိုးရန္ရက္ခ်ိန္း  \n\n(ရက္/ လ/ ႏွစ္)", font,
                                        Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f));

                        cell = new PdfPCell(new Phrase(20, "ထိုးႏွံၿပီးစီးသည့္ရက္  \n\n(ရက္/ လ/ ႏွစ္)", font));
                        cell.setRowspan(2);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);

                        table.addCell(new PDFUtil().writePDFCell("ကာကြယ္ေဆးထိုးႏွံသူ", font, Element.ALIGN_MIDDLE,
                                        Element.ALIGN_CENTER, -1, -1f, 0, 17f));
                        table.addCell(new PDFUtil().writePDFCell("ကာကြယ္ေဆးထိုးစုရပ္", font, Element.ALIGN_MIDDLE,
                                        Element.ALIGN_CENTER, -1, -1f, 0, 17f));

                        if (rdata.getvArrayList().size() == 0) {
                                for (int i = 0; i < 3; i++) {
                                        String cellText = "";
                                        if (i == 0)
                                                cellText = "ပထမအႀကိမ္";
                                        else if (i == 1)
                                                cellText = "ဒုတိယအႀကိမ္";
                                        else if (i == 2)
                                                cellText = "အျခား";
                                        table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        if (i < 2) {
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                        } else {
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                        }
                                }
                        } else if (rdata.getvArrayList().size() == 1) {
                                for (int i = 0; i < rdata.getvArrayList().size(); i++) {
                                        String cellText = "";
                                        if (i == 0)
                                                cellText = "ပထမအႀကိမ္";
                                        else if (i == 1)
                                                cellText = "ဒုတိယအႀကိမ္";
                                        else if (i == 2)
                                                cellText = "အျခား";
                                        table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i).getVaccineLotNo(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2,
                                                        -1f)));
                                        table.addCell(table.addCell(table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i)
                                                                                        .getNextVaccinationDate(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2,
                                                        -1f))));
                                        table.addCell(table.addCell(table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i)
                                                                                        .getVaccinationDate(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2,
                                                        -1f))));
                                        table.addCell(table
                                                        .addCell(table.addCell(new PDFUtil().writePDFCell(
                                                                        new PDFUtil().convertString(
                                                                                        rdata.getvArrayList().get(i)
                                                                                                        .getT1(),
                                                                                        "Myanmar3", "Zawgyi_One"),
                                                                        fontb, Element.ALIGN_MIDDLE,
                                                                        Element.ALIGN_CENTER, -1, -1f, 0, 20f))));
                                        table.addCell(table.addCell(table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i).getCenterName(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                        20f))));

                                }

                                for (int i = 1; i < 3; i++) {
                                        String cellText = "";
                                        if (i == 0)
                                                cellText = "ပထမအႀကိမ္";
                                        else if (i == 1)
                                                cellText = "ဒုတိယအႀကိမ္";
                                        else if (i == 2)
                                                cellText = "အျခား";
                                        table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        if (i < 2) {
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                        } else {
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                        }
                                }
                        } else {
                                for (int i = 0; i < rdata.getvArrayList().size(); i++) {
                                        String cellText = "";
                                        if (i == 0)
                                                cellText = "ပထမအႀကိမ္";
                                        else if (i == 1)
                                                cellText = "ဒုတိယအႀကိမ္";
                                        else if (i == 2)
                                                cellText = "အျခား";
                                        table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));

                                        table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i).getVaccineLotNo(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2,
                                                        -1f));
                                        table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i)
                                                                                        .getNextVaccinationDate(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2,
                                                        -1f));
                                        table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i)
                                                                                        .getVaccinationDate(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2,
                                                        -1f));
                                        table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i).getT1(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                        25f));
                                        table.addCell(new PDFUtil().writePDFCell(
                                                        new PDFUtil().convertString(
                                                                        rdata.getvArrayList().get(i).getCenterName(),
                                                                        "Myanmar3", "Zawgyi_One"),
                                                        fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                        25f));
                                }

                                for (int i = 2; i < 3; i++) {
                                        String cellText = "";
                                        if (i == 0)
                                                cellText = "ပထမအႀကိမ္";
                                        else if (i == 1)
                                                cellText = "ဒုတိယအႀကိမ္";
                                        else if (i == 2)
                                                cellText = "အျခား";
                                        table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE,
                                                        Element.ALIGN_CENTER, -1, -1f, 2, -1f));
                                        if (i < 2) {
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                        } else {
                                                table.addCell(new PDFUtil().writePDFCell("", fontb,
                                                                Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0,
                                                                25f));
                                        }
                                }
                        }

                        PdfPTable ftable = new PdfPTable(1);
                        ftable.setTotalWidth(new float[] { 500 });
                        ftable.setLockedWidth(true);
                        // ftable = new PdfPTable(1);
                        cell = new PdfPCell(new Phrase("", fontb));
                        cell.setBorder(0);
                        ftable.addCell(cell);
                        cell = new PdfPCell(new Phrase("", fontb));
                        cell.setBorder(0);
                        ftable.addCell(cell);

                        // cell = new PdfPCell(new Phrase(convertString("ကိုဗစ် ၁၉
                        // ကာကွယ်ဆေးနှင့် ပတ်သက်၍ သိရှိလိုပါက သို့ဆက်သွယ်ပါ။", "Myanmar3",
                        // "Zawgyi_One"),fontb));
                        ftable.addCell(new PDFUtil().writePDFCell(
                                        "ကိုဗစ္ ၁၉ ကာကြယ္ေဆးႏွင့္ ပတ္သက္၍ သိ႐ွိလိုပါက သို႔ဆက္သြယ္ပါ။", fontb, -1, -1, 0,
                                        -1f, 0, -1f));

                        // cell = new PdfPCell(new Phrase(convertString("ကာကွယ်ဆေးထိုးနှံပြီး
                        // ကျန်းမာရေး ပြဿနာ တစ်စုံတစ်ရာဖြစ်ပွားပါကအနီးဆုံးကျန်းမာရေးဌာန/ဆေးရုံသို့
                        // သွားပါ။", "Myanmar3", "Zawgyi_One"),fontb));
                        ftable.addCell(new PDFUtil().writePDFCell(
                                        "ကာကြယ္ေဆးထိုးႏွံၿပီး က်န္းမာ​ေရး ျပႆနာ တစ္စံုတစ္ရာျဖစ္ပြားပါကအနီးဆံုးက်န္းမာေရးဌာန/ေဆးရံုသို႔ သြားပါ။",
                                        fontb, -1, -1, 0, -1f, 0, -1f));
                        document.add(headertable);
                        document.add(firstbodytable);
                        document.add(secondbodytable);
                        document.add(thirdbodytable);
                        document.add(table);
                        document.add(ftable);
                } catch (DocumentException e) {
                        e.printStackTrace();
                } catch (MalformedURLException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }

        }

}
