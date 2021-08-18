package com.dvc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.dvc.models.RecipentsData;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WriteExcel {
    public byte[] recipientsToExcel(List<RecipentsData> recipientList) {
        String[] HEADERs = { "CID", "Name", "NRC", "Passport", "Nationality", "Father's Name", "Date of Birth",
                "Organization", "Mobile", "Ref" };
        String SHEET = "recipients";
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Sheet sheet = workbook.createSheet(SHEET);
            int index = 0;
            while (index < 10) {
                sheet.setColumnWidth(index, 25 * 256);
                index++;
            }
            XSSFFont font = (XSSFFont) workbook.createFont();
            font.setFontHeightInPoints((short) 11);
            font.setFontName("Arial");
            font.setBold(true);
            XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle contentStyle = (XSSFCellStyle) workbook.createCellStyle();
            XSSFFont contentFont = (XSSFFont) workbook.createFont();
            contentFont.setFontHeightInPoints((short) 11);
            contentFont.setFontName("Arial");
            contentStyle.setFont(contentFont);
            contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // Header
            Row headerRow = sheet.createRow(0);
            headerRow.setHeight((short) (24 * 20));
            for (int col = 0; col < HEADERs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs[col]);
                cell.setCellStyle(style);
            }
            int rowIdx = 1;
            for (RecipentsData recipient : recipientList) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeight((short) (24 * 20));
                row.createCell(0)
                        .setCellValue(recipient.getCertificateID() == null ? "" : recipient.getCertificateID());
                row.createCell(1)
                        .setCellValue(recipient.getRecipientsName() == null ? "" : recipient.getRecipientsName());
                row.createCell(2).setCellValue(recipient.getNRIC() == null ? "" : recipient.getNRIC());
                row.createCell(3).setCellValue(recipient.getPassport() == null ? "" : recipient.getPassport());
                row.createCell(4).setCellValue(recipient.getNationality() == null ? "" : recipient.getNationality());
                row.createCell(5).setCellValue(recipient.getFatherName() == null ? "" : recipient.getFatherName());
                row.createCell(6).setCellValue(recipient.getDob() == null ? "" : recipient.getDob());
                row.createCell(7).setCellValue(recipient.getOccupation() == null ? "" : recipient.getOccupation());
                row.createCell(8).setCellValue(recipient.getMobilePhone() == null ? "" : recipient.getMobilePhone());
                row.createCell(9).setCellValue(recipient.getBatchRefCode() == null ? "" : recipient.getBatchRefCode());

                row.getCell(0).setCellStyle(contentStyle);
                row.getCell(1).setCellStyle(contentStyle);
                row.getCell(2).setCellStyle(contentStyle);
                row.getCell(3).setCellStyle(contentStyle);
                row.getCell(4).setCellStyle(contentStyle);
                row.getCell(5).setCellStyle(contentStyle);
                row.getCell(6).setCellStyle(contentStyle);
                row.getCell(7).setCellStyle(contentStyle);
                row.getCell(8).setCellStyle(contentStyle);
                row.getCell(9).setCellStyle(contentStyle);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}