package com.dvc.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.dvc.models.RecipentsData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PDFUtil {
    public static final String BLOB_CONNECTION_ENV_NAME = "AzureStorageConnectionString";
    public static final String BLOB_CONTAINER_NAME = "dvrspdf";
    public static final int INDEX_ZERO = 0;
    public static final int INDEX_ONE = 1;
    public static final String EXCEL_EXTENSION = ".xlsx";
    public static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final int MAX_WRITABLE_TOTAL_RECORDS = 100000;

    private static final String QR_EXTENSION = "png";
    public static final int MAX_RECORDS_PER_PDF = Integer.parseInt(System.getenv("MAX_RECORDS_PER_PDF"));
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String PDF_EXTENSION = ".pdf";
    private static final String FOLDER_NAME_SUFFIX = "recordcards";

    public final void newRow(PdfPTable table, Font fontb) {
        PdfPCell cell = new PdfPCell(new Phrase("", fontb));
        cell.setBorder(0);
        cell.setFixedHeight(7f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("", fontb));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("", fontb));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("", fontb));
        cell.setBorder(0);
        table.addCell(cell);
    }

    public final java.io.OutputStream generateQR(String id, long pid) {
        String charset = "UTF-8";
        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        try {
            if (id != null && !id.equals("")) {
                if (id.length() > 300 && id.length() < 400)
                    return generateQRcode(id, charset, hashMap, 270, 270);
                else if (id.length() > 400 && id.length() < 460)
                    return generateQRcode(id, charset, hashMap, 380, 380);
                else if (id.length() > 460 && id.length() < 500)
                    return generateQRcode(id, charset, hashMap, 400, 400);
                else if (id.length() > 500 && id.length() < 550)
                    return generateQRcode(id, charset, hashMap, 300, 300);
                else if ((id.length() > 550 && id.length() < 600) || (id.length() > 600 && id.length() < 650))
                    return generateQRcode(id, charset, hashMap, 350, 350);
                else if (id.length() > 650 && id.length() < 700)
                    return generateQRcode(id, charset, hashMap, 450, 450);
                else if (id.length() > 700 && id.length() < 790)
                    return generateQRcode(id, charset, hashMap, 385, 385);
                else if (id.length() > 790 && id.length() < 850)
                    return generateQRcode(id, charset, hashMap, 400, 400);
                else
                    return generateQRcode(id, charset, hashMap, 250, 250);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final java.io.OutputStream generateQRcode(String data, String charset, Map map, int h, int w)
            throws WriterException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.OutputStream outputStream = baos;
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, w, h,
                com.google.common.collect.ImmutableMap.of(com.google.zxing.EncodeHintType.MARGIN, 0));
        MatrixToImageWriter.writeToStream(matrix, QR_EXTENSION, outputStream);
        return outputStream;
    }

    public final byte[] writePdf(List<RecipentsData> rdata)
            throws DocumentException, MalformedURLException, IOException {
        Document document = new Document(PageSize.A5.rotate(), 0, 0, 0, 0);
        // document.setMargins(0, 0, 10, 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pd = PdfWriter.getInstance(document, baos);
        document.open();
        document.setPageSize(PageSize.A5.rotate());
        final String FONT = getClass().getClassLoader().getResource("fonts/ZawgyiOne2008.ttf").toString();

        Font lblFont = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 7f, Font.NORMAL,
                BaseColor.BLACK);
        Font font = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 8f, Font.NORMAL,
                BaseColor.BLACK);
        Font hfont = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, Font.BOLD,
                BaseColor.BLACK);
        Font fontb = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 8f, Font.NORMAL,
                BaseColor.BLACK);
        Font idFont = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, Font.NORMAL,
                BaseColor.BLACK);

        rdata.forEach(recipientsData -> {
            if ((recipientsData.getNationality() != null && !recipientsData.getNationality().isEmpty()
                    && !recipientsData.getNationality().equalsIgnoreCase("Myanmar"))
                    && (recipientsData.getNRIC() == null || recipientsData.getNRIC().isEmpty())
                    && (recipientsData.getPassport() != null && !recipientsData.getPassport().isEmpty())) {
                new CustomPDFWriter(new PDFWriterFormatTwo(), recipientsData, document, font, hfont, fontb, idFont,
                        lblFont);
            } else {
                new CustomPDFWriter(new PDFWriterFormatThree(), recipientsData, document, font, hfont, fontb, idFont,
                        lblFont);
            }
        });
        document.close();
        return baos.toByteArray();

    }

    public final String convertString(String txt, String src, String desc) {
        String str3 = "";
        try {
            if (txt != null && !txt.equals("") && !txt.equals("0")) {
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("fontmap/fontmap.json");

                try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                        BufferedReader l_bufferedReader = new BufferedReader(streamReader)) {

                    String l_str = "";
                    String l_fonts = "";
                    while ((l_str = l_bufferedReader.readLine()) != null) {
                        l_fonts += l_str;
                    }
                    l_bufferedReader.close();

                    JSONParser l_parser = new JSONParser();
                    JSONObject l_json = (JSONObject) l_parser.parse(l_fonts);

                    String str = "0x";
                    int num = 0;
                    String input = txt;
                    String newValue = "";
                    String str5 = "";
                    HashMap<String, Integer> hasMap = new HashMap<String, Integer>();
                    JSONObject obj2 = (JSONObject) l_parser.parse(l_json.get(src).toString());
                    JSONObject obj3 = (JSONObject) l_parser.parse(l_json.get(desc).toString());

                    String jsonStr = obj2.get("val").toString().replace("[", "");
                    jsonStr = jsonStr.replace("]", "");
                    String[] numStrArr = jsonStr.split(",");
                    int[] numArray = new int[numStrArr.length];
                    for (int i = 0; i < numStrArr.length; i++) {
                        numArray[i] = Integer.parseInt(numStrArr[i]);
                    }

                    jsonStr = obj3.get("val").toString().replace("[", "");
                    jsonStr = jsonStr.replace("]", "");
                    numStrArr = jsonStr.split(",");
                    int[] numArray2 = new int[numStrArr.length];
                    for (int i = 0; i < numStrArr.length; i++) {
                        numArray2[i] = Integer.parseInt(numStrArr[i]);
                    }

                    List<int[]> array = Arrays.asList(new int[] { 0, 10, 13, 9, 8, 0x20 });
                    for (int i = 0; i < numArray.length; i++) {
                        if (array.indexOf(numArray[i]) == -1)
                            hasMap.put(String.valueOf(numArray[i]), i);
                    }
                    if (obj2.get("order") != null) {
                        JSONObject obj4 = (JSONObject) l_parser.parse(obj2.get("order").toString());
                        JSONObject obj5 = (JSONObject) l_parser.parse(obj4.get("consonent").toString());
                        JSONObject obj6 = (JSONObject) l_parser.parse(obj5.get("rev").toString());

                        for (Object str6 : obj6.keySet()) {

                            Pattern r = Pattern.compile(String.valueOf(str6));
                            Matcher m = r.matcher(input);
                            if (m.find()) {
                                input = input.replaceAll(String.valueOf(str6), obj6.get(str6.toString()).toString());
                            }
                        }
                    }
                    JSONObject obj7 = (JSONObject) l_parser.parse(obj2.get("ext").toString());
                    for (Object str7 : obj7.keySet()) {

                        newValue = "";
                        jsonStr = obj7.get(str7.toString()).toString().replace("[", "");
                        jsonStr = jsonStr.replace("]", "");
                        numStrArr = jsonStr.split(",");
                        int[] numArray4 = new int[numStrArr.length];
                        for (int i = 0; i < numStrArr.length; i++) {
                            numArray4[i] = Integer.parseInt(numStrArr[i]);
                        }

                        for (int m = 0; m < numArray4.length; m++) {
                            newValue = newValue + str + fillZero(numArray4[m]);
                        }
                        input = input.replace(String.valueOf(((char) Integer.parseInt((str7.toString())))), newValue);
                    }

                    char[] chArray = input.toCharArray();
                    for (int j = 0; j < chArray.length; j++) {
                        if ((j < (chArray.length - 1))
                                && ((String.valueOf(chArray[j]) + String.valueOf(chArray[j + 1])).equals(str))) {
                            num = 5;
                        }
                        if (num > 0) {
                            str3 = str3 + chArray[j];
                            num--;
                        } else {
                            boolean flag = false;
                            char ch2 = chArray[j];
                            int num5 = ch2;
                            if (num5 < 0x21) {
                                str3 = str3 + ch2;
                            } else {
                                if (!flag) {
                                    if (hasMap.containsKey(String.valueOf(num5))) {
                                        str3 = str3 + unichr(numArray2[hasMap.get(String.valueOf(num5))]);
                                    } else {
                                        str3 = str3 + ch2;
                                    }
                                }
                            }
                        }
                    }
                    JSONObject obj8 = (JSONObject) l_parser.parse(obj3.get("ext").toString());
                    for (Object str8 : obj8.keySet()) {
                        newValue = "";
                        str5 = "";
                        jsonStr = obj8.get(str8.toString()).toString().replace("[", "");
                        jsonStr = jsonStr.replace("]", "");
                        numStrArr = jsonStr.split(",");
                        int[] numArray5 = new int[numStrArr.length];
                        for (int i = 0; i < numStrArr.length; i++) {
                            numArray5[i] = Integer.parseInt(numStrArr[i]);
                        }
                        if (numArray5.length > 1) {
                            for (int n = 0; n < numArray5.length; n++) {
                                String str9 = fillZero(numArray5[n]);
                                newValue = newValue + str + str9;
                                str5 = str5 + unichr(numArray2[numArray5[n]]);
                            }
                            if (str3.indexOf(newValue) != -1) {
                                str3 = str3.replace(newValue, unichr(str8.toString()));
                            }
                            if (str3.indexOf(str5) != -1) {
                                str3 = str3.replace(str5, unichr(str8.toString()));
                            }
                        }
                    }
                    for (int k = 0; k < numArray2.length; k++) {
                        newValue = str + fillZero(k);
                        if (str3.indexOf(newValue) != -1) {
                            str3 = str3.replace(newValue, unichr(numArray2[k]));
                        }
                    }
                    if (obj3.get("ac2") != null) {
                        JSONObject obj9 = (JSONObject) l_parser.parse(obj3.get("ac2").toString());
                        for (Object str10 : obj9.keySet()) {
                            if (str3.matches(str10.toString())) {
                                str3 = str3.replaceAll(str10.toString(), obj9.get(str10.toString()).toString());
                            }
                        }
                    }
                    if (obj3.get("order") != null) {
                        JSONObject obj10 = (JSONObject) l_parser.parse(obj3.get("order").toString());
                        JSONObject obj11 = (JSONObject) l_parser.parse(obj10.get("consonent").toString());
                        JSONObject obj12 = (JSONObject) l_parser.parse(obj11.get("fwd").toString());
                        for (Object str11 : obj12.keySet()) {
                            if (str3.matches(str11.toString())) {
                                str3 = str3.replaceAll(str11.toString(), obj12.get(str11.toString()).toString());
                            }
                        }
                        jsonStr = obj10.get("vowel").toString().replace("[", "");
                        jsonStr = jsonStr.replace("]", "");
                        String[] strArray = jsonStr.split(",");
                        for (int num8 = 0; num8 < (strArray.length - 2); num8++) {
                            String str12 = "";
                            for (int num9 = num8; num9 < (strArray.length - 1); num9++) {
                                str12 = str12 + strArray[num9 + 1];
                            }
                            String l_regex = "([" + str12 + "]+)(" + strArray[num8] + ")";
                            if (str3.matches(l_regex)) {
                                str3.replaceAll(l_regex, "$2$1");
                            }
                        }
                        JSONObject obj13 = (JSONObject) l_parser.parse(obj10.get("after").toString());
                        for (Object str13 : obj13.keySet()) {
                            if (str3.matches(str13.toString())) {
                                str3 = str3.replaceAll(str13.toString(), obj13.get(str13.toString()).toString());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return str3;
    }

    private String fillZero(int charcode) {
        DecimalFormat df = new DecimalFormat("000");
        return df.format(charcode);
    }

    private final String unichr(int charcode) {
        char ch = (char) charcode;
        return String.valueOf(ch);
    }

    private final String unichr(String charcode) {
        char ch = (char) Integer.parseInt(charcode);
        return String.valueOf(ch);
    }

    private final String returnedString(String email) {
        String ret = "";
        ret = email.replace(".", "____");
        ret = ret.replace("@", "___");
        return ret;
    }

    private byte[] zipBytes(Map<String, byte[]> pdfDataMap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
            for (Entry<String, byte[]> pdfData : pdfDataMap.entrySet()) {
                ZipEntry entry = new ZipEntry(pdfData.getKey());
                entry.setSize(pdfData.getValue().length);
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(pdfData.getValue());
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public byte[] downloadZippedByte(String path, BlobContainerClient containerClient) {
        Map<String, byte[]> mapFiles = new HashMap<>();
        for (BlobItem blobItem : containerClient.listBlobs()) {
            String fileName = blobItem.getName();
            if (fileName.contains("/") && fileName.startsWith(path + "/") && (fileName.endsWith(".pdf")
                    || fileName.endsWith(".xls") || fileName.endsWith(".xlsx") || fileName.endsWith(".csv"))) {
                BlobClient blob = containerClient.getBlobClient(fileName);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                blob.download(outputStream);

                final byte[] bytes = outputStream.toByteArray();
                mapFiles.put(fileName.split("/")[1], bytes);
            }
        }
        return zipBytes(mapFiles);
    }

    public final void writeFileAtBlobStorage(String fileName, byte[] byteArr, String tmpFolderName, String contentType,
            BlobContainerClient containerClient) {
        BlobClient blobClient = containerClient.getBlobClient(tmpFolderName + "/" + fileName);
        if (blobClient.exists()) {
            blobClient.delete();
            blobClient = containerClient.getBlobClient(tmpFolderName + "/" + fileName);
        }
        InputStream targetStream = new ByteArrayInputStream(byteArr);
        blobClient.upload(targetStream, byteArr.length);
    }

    public void checkOverride(boolean isOverride, BlobContainerClient containerClient, String folderName,
            String pdfFileName, List<RecipentsData> subListData, List<String> existedPDFList) {
        try {
            getRemovableFileList(existedPDFList, folderName + "/" + pdfFileName);
            if (!isOverride) {
                if (!containerClient.getBlobClient(folderName + "/" + pdfFileName).exists()) {
                    byte[] byteArr = writePdf(subListData);
                    new PDFUtil().writeFileAtBlobStorage(pdfFileName, byteArr, folderName, PDF_CONTENT_TYPE,
                            containerClient);
                }
            } else {
                byte[] byteArr = writePdf(subListData);
                new PDFUtil().writeFileAtBlobStorage(pdfFileName, byteArr, folderName, PDF_CONTENT_TYPE,
                        containerClient);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getRemovableFileList(List<String> pdfNameList, String pdfName) {
        if (pdfNameList.size() > 0) {
            if (pdfNameList.indexOf(pdfName) != -1) {
                pdfNameList.remove(pdfName);
            }
        }
    }

    public List<String> getExistedPDFNameList(BlobContainerClient containerClient, final String blobFolderName) {
        return containerClient.listBlobs().stream()
                .filter(e -> e.getName().contains("/") && e.getName().contains(blobFolderName + "/")
                        && (e.getName().endsWith(".pdf") || e.getName().endsWith(".xlsx")))
                .map(e -> e.getName()).collect(Collectors.toList());
    }

    public void checkAndWritePDF(String folderName, ArrayList<RecipentsData> rList, boolean isOverride,
            List<String> existedPDFList, BlobContainerClient containerClient) {
        if (rList.size() > PDFUtil.MAX_RECORDS_PER_PDF) {
            int numberOfTotalSize = rList.size();
            for (int index = PDFUtil.INDEX_ZERO; index < rList.size(); index += PDFUtil.MAX_RECORDS_PER_PDF) {
                List<RecipentsData> subListData = new ArrayList<>();
                if (numberOfTotalSize >= index + PDFUtil.MAX_RECORDS_PER_PDF) {
                    subListData = rList.subList(index, index + PDFUtil.MAX_RECORDS_PER_PDF);
                } else {
                    subListData = rList.subList(index, numberOfTotalSize);
                }
                String pdfFileName = PDFUtil.PDF_EXTENSION;
                if (subListData.size() == PDFUtil.INDEX_ONE) {
                    pdfFileName = generateFilePrefix(rList) + subListData.get(PDFUtil.INDEX_ZERO).getCertificateID()
                            + PDFUtil.PDF_EXTENSION;
                } else {
                    pdfFileName = generateFilePrefix(rList) + subListData.get(PDFUtil.INDEX_ZERO).getCertificateID()
                            + "-" + subListData.get((subListData.size() - PDFUtil.INDEX_ONE)).getCertificateID()
                            + PDFUtil.PDF_EXTENSION;
                }

                new PDFUtil().checkOverride(isOverride, containerClient, folderName, pdfFileName, subListData,
                        existedPDFList);

            }
        } else {
            String pdfName = "";
            if (rList.size() == PDFUtil.INDEX_ONE) {
                pdfName = generateFilePrefix(rList) + rList.get(PDFUtil.INDEX_ZERO).getCertificateID()
                        + rList.get((rList.size() - PDFUtil.INDEX_ONE)).getCertificateID() + PDFUtil.PDF_EXTENSION;
            } else {
                pdfName = generateFilePrefix(rList) + rList.get(PDFUtil.INDEX_ZERO).getCertificateID() + "-"
                        + rList.get((rList.size() - PDFUtil.INDEX_ONE)).getCertificateID() + PDFUtil.PDF_EXTENSION;
            }
            new PDFUtil().checkOverride(isOverride, containerClient, folderName, pdfName, rList, existedPDFList);
        }
    }

    public String generateFilePrefix(ArrayList<RecipentsData> rList) {
        String prefix = "";
        if (rList.get(INDEX_ZERO).getBatchRefCode() != null && rList.get(INDEX_ZERO).getBatchRefCode().contains("-")) {
            String batchRefCode = rList.get(INDEX_ZERO).getBatchRefCode();
            int index = batchRefCode.lastIndexOf("-");
            prefix = batchRefCode.substring(0, index) + "-";
        }
        return prefix;
    }

    public String generateFolderName(ArrayList<RecipentsData> rList) {
        String folderName;
        if (rList.size() > 0) {
            if (rList.get(INDEX_ZERO).getBatchRefCode() != null
                    && rList.get(INDEX_ZERO).getBatchRefCode().contains("-")) {
                String refarr[] = rList.get(INDEX_ZERO).getBatchRefCode().split("-");
                folderName = refarr[INDEX_ZERO] + "-" + refarr[INDEX_ONE] + "-" + FOLDER_NAME_SUFFIX;
            } else {
                folderName = FOLDER_NAME_SUFFIX;
            }
        } else {
            folderName = FOLDER_NAME_SUFFIX;
        }
        return folderName;
    }

    public PdfPCell writePDFCell(String text, Font font, int vAlign, int hAlign, int border, float borderWidthBottom,
            int rowspan, float fixedHeight) {
        PdfPCell pdfPCell = new PdfPCell(new Phrase(text, font));
        if (vAlign > -1) {
            pdfPCell.setVerticalAlignment(vAlign);
        }
        if (hAlign > -1) {
            pdfPCell.setHorizontalAlignment(hAlign);
        }
        if (border > -1) {
            pdfPCell.setBorder(border);
        }
        if (borderWidthBottom > -1f) {
            pdfPCell.setBorderWidthBottom(borderWidthBottom);
        }
        if (fixedHeight > -1f) {
            pdfPCell.setFixedHeight(fixedHeight);
        }
        if (rowspan > 0) {
            pdfPCell.setRowspan(rowspan);
        }
        return pdfPCell;
    }

    public String generateFolderNameForBatch(ArrayList<RecipentsData> rList) {
        return generateFilePrefix(rList) + rList.get(INDEX_ZERO).getCertificateID() + "-"
                + rList.get(rList.size() - 1).getCertificateID() + "-" + FOLDER_NAME_SUFFIX;
    }

    public final String uni2zg(String input) {

        String rule = "[ { \"from\": \"\u1004\u103a\u1039\", \"to\": \"\u1064\" }, { \"from\": \"\u1039\u1010\u103d\", \"to\": \"\u1096\" }, { \"from\": \"\u102b\u103a\", \"to\": \"\u105a\" }, { \"from\": \"\u100b\u1039\u100c\", \"to\": \"\u1092\" }, { \"from\": \"\u102d\u1036\", \"to\": \"\u108e\" }, { \"from\": \"\u104e\u1004\u103a\u1038\", \"to\": \"\u104e\" }, { \"from\": \"[\u1025\u1009](?=[\u1039\u102f\u1030])\", \"to\": \"\u106a\" }, { \"from\": \"[\u1025\u1009](?=[\u1037]?[\u103a])\", \"to\": \"\u1025\" }, { \"from\": \"\u100a(?=[\u1039\u103d])\", \"to\": \"\u106b\" }, { \"from\": \"(\u1039[\u1000-\u1021])(\u102D){0,1}\u102f\", \"to\": \"$1$2\u1033\" }, { \"from\": \"(\u1039[\u1000-\u1021])\u1030\", \"to\": \"$1\u1034\" }, { \"from\": \"\u1014(?=[\u102d\u102e]?[\u1030\u103d\u103e\u102f\u1039])\", \"to\": \"\u108f\" }, { \"from\": \"\u1039\u1000\", \"to\": \"\u1060\" }, { \"from\": \"\u1039\u1001\", \"to\": \"\u1061\" }, { \"from\": \"\u1039\u1002\", \"to\": \"\u1062\" }, { \"from\": \"\u1039\u1003\", \"to\": \"\u1063\" }, { \"from\": \"\u1039\u1005\", \"to\": \"\u1065\" }, { \"from\": \"\u1039\u1006\", \"to\": \"\u1066\" }, { \"from\": \"\u1039\u1007\", \"to\": \"\u1068\" }, { \"from\": \"\u1039\u1008\", \"to\": \"\u1069\" }, { \"from\": \"\u1039\u100b\", \"to\": \"\u106c\" }, { \"from\": \"\u1039\u100c\", \"to\": \"\u106d\" }, { \"from\": \"\u100d\u1039\u100d\", \"to\": \"\u106e\" }, { \"from\": \"\u100e\u1039\u100d\", \"to\": \"\u106f\" }, { \"from\": \"\u1039\u100f\", \"to\": \"\u1070\" }, { \"from\": \"\u1039\u1010\", \"to\": \"\u1071\" }, { \"from\": \"\u1039\u1011\", \"to\": \"\u1073\" }, { \"from\": \"\u1039\u1012\", \"to\": \"\u1075\" }, { \"from\": \"\u1039\u1013\", \"to\": \"\u1076\" }, { \"from\": \"\u1039[\u1014\u108f]\", \"to\": \"\u1077\" }, { \"from\": \"\u1039\u1015\", \"to\": \"\u1078\" }, { \"from\": \"\u1039\u1016\", \"to\": \"\u1079\" }, { \"from\": \"\u1039\u1017\", \"to\": \"\u107a\" }, { \"from\": \"\u1039\u1018\", \"to\": \"\u107b\" }, { \"from\": \"\u1039\u1019\", \"to\": \"\u107c\" }, { \"from\": \"\u1039\u101c\", \"to\": \"\u1085\" }, { \"from\": \"\u103f\", \"to\": \"\u1086\" }, { \"from\": \"\u103d\u103e\", \"to\": \"\u108a\" }, { \"from\": \"(\u1064)([\u1000-\u1021])([\u103b\u103c]?)\u102d\", \"to\": \"$2$3\u108b\" }, { \"from\": \"(\u1064)([\u1000-\u1021])([\u103b\u103c]?)\u102e\", \"to\": \"$2$3\u108c\" }, { \"from\": \"(\u1064)([\u1000-\u1021])([\u103b\u103c]?)\u1036\", \"to\": \"$2$3\u108d\" }, { \"from\": \"(\u1064)([\u1000-\u1021])([\u103b\u103c]?)([\u1031]?)\", \"to\": \"$2$3$4$1\" }, { \"from\": \"\u101b(?=([\u102d\u102e]?)[\u102f\u1030\u103d\u108a])\", \"to\": \"\u1090\" }, { \"from\": \"\u100f\u1039\u100d\", \"to\": \"\u1091\" }, { \"from\": \"\u100b\u1039\u100b\", \"to\": \"\u1097\" }, { \"from\": \"([\u1000-\u1021\u108f\u1029\u1090])([\u1060-\u1069\u106c\u106d\u1070-\u107c\u1085\u108a])?([\u103b-\u103e]*)?\u1031\", \"to\": \"\u1031$1$2$3\" }, { \"from\": \"\u103c\u103e\", \"to\": \"\u103c\u1087\" }, { \"from\": \"([\u1000-\u1021\u108f\u1029])([\u1060-\u1069\u106c\u106d\u1070-\u107c\u1085])?(\u103c)\", \"to\": \"$3$1$2\" }, { \"from\": \"\u103a\", \"to\": \"\u1039\" }, { \"from\": \"\u103b\", \"to\": \"\u103a\" }, { \"from\": \"\u103c\", \"to\": \"\u103b\" }, { \"from\": \"\u103d\", \"to\": \"\u103c\" }, { \"from\": \"\u103e\", \"to\": \"\u103d\" }, { \"from\": \"([^\u103a\u100a])\u103d([\u102d\u102e]?)\u102f\", \"to\": \"$1\u1088$2\" }, { \"from\": \"([\u101b\u103a\u103c\u108a\u1088\u1090])([\u1030\u103d])?([\u1032\u1036\u1039\u102d\u102e\u108b\u108c\u108d\u108e]?)(\u102f)?\u1037\", \"to\": \"$1$2$3$4\u1095\" }, { \"from\": \"([\u102f\u1014\u1030\u103d])([\u1032\u1036\u1039\u102d\u102e\u108b\u108c\u108d\u108e]?)\u1037\", \"to\": \"$1$2\u1094\" }, { \"from\": \"([\u103b])([\u1000-\u1021])([\u1087]?)([\u1036\u102d\u102e\u108b\u108c\u108d\u108e]?)\u102f\", \"to\": \"$1$2$3$4\u1033\" }, { \"from\": \"([\u103b])([\u1000-\u1021])([\u1087]?)([\u1036\u102d\u102e\u108b\u108c\u108d\u108e]?)\u1030\", \"to\": \"$1$2$3$4\u1034\" }, { \"from\": \"([\u103a\u103c\u100a\u1020\u1025])([\u103d]?)([\u1036\u102d\u102e\u108b\u108c\u108d\u108e]?)\u102f\", \"to\": \"$1$2$3\u1033\" }, { \"from\": \"([\u103a\u103c\u100a\u101b])(\u103d?)([\u1036\u102d\u102e\u108b\u108c\u108d\u108e]?)\u1030\", \"to\": \"$1$2$3\u1034\" }, { \"from\": \"\u100a\u103d\", \"to\": \"\u100a\u1087\" }, { \"from\": \"\u103d\u1030\", \"to\": \"\u1089\" }, { \"from\": \"\u103b([\u1000\u1003\u1006\u100f\u1010\u1011\u1018\u101a\u101c\u101a\u101e\u101f])\", \"to\": \"\u107e$1\" }, { \"from\": \"\u107e([\u1000\u1003\u1006\u100f\u1010\u1011\u1018\u101a\u101c\u101a\u101e\u101f])([\u103c\u108a])([\u1032\u1036\u102d\u102e\u108b\u108c\u108d\u108e])\", \"to\": \"\u1084$1$2$3\" }, { \"from\": \"\u107e([\u1000\u1003\u1006\u100f\u1010\u1011\u1018\u101a\u101c\u101a\u101e\u101f])([\u103c\u108a])\", \"to\": \"\u1082$1$2\" }, { \"from\": \"\u107e([\u1000\u1003\u1006\u100f\u1010\u1011\u1018\u101a\u101c\u101a\u101e\u101f])([\u1033\u1034]?)([\u1032\u1036\u102d\u102e\u108b\u108c\u108d\u108e])\", \"to\": \"\u1080$1$2$3\" }, { \"from\": \"\u103b([\u1000-\u1021])([\u103c\u108a])([\u1032\u1036\u102d\u102e\u108b\u108c\u108d\u108e])\", \"to\": \"\u1083$1$2$3\" }, { \"from\": \"\u103b([\u1000-\u1021])([\u103c\u108a])\", \"to\": \"\u1081$1$2\" }, { \"from\": \"\u103b([\u1000-\u1021])([\u1033\u1034]?)([\u1032\u1036\u102d\u102e\u108b\u108c\u108d\u108e])\", \"to\": \"\u107f$1$2$3\" }, { \"from\": \"\u103a\u103d\", \"to\": \"\u103d\u103a\" }, { \"from\": \"\u103a([\u103c\u108a])\", \"to\": \"$1\u107d\" }, { \"from\": \"([\u1033\u1034])\u1094\", \"to\": \"$1\u1095\" }, {  \"from\": \"\u108F\u1071\",  \"to\" : \"\u108F\u1072\" }, {  \"from\": \"([\u1000-\u1021])([\u107B\u1066])\u102C\",  \"to\": \"$1\u102C$2\" }, {  \"from\": \"\u102C([\u107B\u1066])\u1037\",  \"to\": \"\u102C$1\u1094\" }]";

        return replace_with_rule(rule, input);
    }

    public static String replace_with_rule(String rule, String output) {
        try {
            JSONArray rule_array = new JSONArray(rule);
            int max_loop = rule_array.length();
            // because of JDK 7 bugs in Android
            output = output.replace("null", "\uFFFF\uFFFF");
            for (int i = 0; i < max_loop; i++) {
                org.json.JSONObject obj = rule_array.getJSONObject(i);
                String from = obj.getString("from");
                String to = obj.getString("to");
                output = output.replaceAll(from, to);
                output = output.replace("null", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        output = output.replace("\uFFFF\uFFFF", "null");
        return output;
    }
}