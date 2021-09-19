package com.dvc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.dvc.models.RecipentsData;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class PDFWriterFormatTwo implements PDFWriter {

	@Override
	public void writePDF(RecipentsData rdata, Document document, Font font, Font hfont, Font fontb, Font idFont,
			Font lblFont) {
		try {

			PdfPTable table = new PdfPTable(5);
			table.setTotalWidth(new float[] { 100, 100, 100, 100, 100 });
			table.setLockedWidth(true);
			PdfPTable headertable;
			PdfPTable firstbodytable = new PdfPTable(6);
			PdfPTable secondbodytable = new PdfPTable(4);
			PdfPTable thirdbodytable = new PdfPTable(4);
			if (rdata.getVoidStatus() > 0) {
				headertable = new PdfPTable(3);
				headertable.setWidths(new int[] { 1, 5, 2 });
				headertable.setTotalWidth(new float[] { 92, 310, 98 });
				headertable.setLockedWidth(true);
			} else {
				headertable = new PdfPTable(4);
				headertable.setWidths(new int[] { 1, 5, 2, 1 });
				headertable.setTotalWidth(new float[] { 92, 310, 98, 20 });
				headertable.setLockedWidth(true);
			}

			firstbodytable.setWidths(new int[] { 1, 5, 1, 3, 1, 3 });
			firstbodytable.setTotalWidth(new float[] { 50, 260, 50, 40, 50, 50 });
			firstbodytable.setLockedWidth(true);

			secondbodytable.setWidths(new int[] { 3, 3, 2, 3 });
			secondbodytable.setTotalWidth(new float[] { 100, 150, 80, 170 });
			secondbodytable.setLockedWidth(true);

			PdfPCell cell;
			cell = new PdfPCell();
			cell.setBorder(0);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			headertable.addCell(cell);
			PdfPCell cell1 = new PdfPCell();
			Paragraph p = new Paragraph("COVID-19 Vaccination Record Card", hfont);
			p.setAlignment(Element.ALIGN_CENTER);
			cell1.addElement(p);
			p = new Paragraph("(COVID-19 ကာကြယ္ေဆးထိုးမွတ္တမ္းကတ္ျပား)", hfont);
			p.setAlignment(Element.ALIGN_CENTER);
			cell1.addElement(p);
			cell1.setBorder(0);
			cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			headertable.addCell(cell1);
			// headertable.addCell(new PDFUtil().writePDFCell("COVID-19
			// ကာကြယ္ေဆးထိုးမွတ္တမ္းကတ္ျပား", hfont,
			// Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, 0, -1f, 0, -1f));

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
			cell1 = new PdfPCell();
			p = new Paragraph(rdata.getCertificateID(), idFont);
			p.setSpacingAfter(-4f);
			p.setAlignment(Element.ALIGN_LEFT);
			cell1.addElement(p);
			cell1.addElement(image);
			cell1.setBorder(0);
			cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			headertable.addCell(cell1);

			if (rdata.getVoidStatus() == 0) {
				URL resource = getClass().getClassLoader().getResource("logos/void.png");
				com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(resource);
				float imageWidth = 5;
				float imageHeight = 10;
				img.scaleAbsolute(imageWidth, imageHeight);
				cell = new PdfPCell(img, true);
				cell.setBorder(0);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				headertable.addCell(cell);
			}

			firstbodytable.addCell(new PDFUtil().writePDFCell("Name (အမည္)", lblFont, -1, -1, 0, -1f, 0, -1f));
			firstbodytable.addCell(new PDFUtil().writePDFCell(new PDFUtil().uni2zg(rdata.getRecipientsName()), fontb,
					-1, -1, 0, 0.5f, 0, -1f));
			firstbodytable.addCell(
					new PDFUtil().writePDFCell("Sex (က်ား/မ)", lblFont, -1, Element.ALIGN_RIGHT, 0, -1f, 0, -1f));
			firstbodytable.addCell(
					new PDFUtil().writePDFCell(new PDFUtil().convertString(rdata.getGender(), "Myanmar3", "Zawgyi_One"),
							fontb, -1, -1, 0, 0.5f, 0, -1f));
			firstbodytable.addCell(
					new PDFUtil().writePDFCell("Age (အသက္)", lblFont, -1, Element.ALIGN_RIGHT, 0, -1f, 0, -1f));

			if (new PDFUtil().convertString(rdata.getAge() + "", "Myanmar3", "Zawgyi_One").equals("")) {
				firstbodytable.addCell(new PDFUtil().writePDFCell(
						new PDFUtil().convertString(rdata.getAge() + "", "Myanmar3", "Zawgyi_One")
								+ new PDFUtil().convertString("              နှစ်", "Myanmar3", "Zawgyi_One"),
						fontb, -1, -1, 0, 0.5f, 0, -1f));
			} else {
				firstbodytable.addCell(new PDFUtil().writePDFCell(
						new PDFUtil().convertString(rdata.getAge() + "", "Myanmar3", "Zawgyi_One")
								+ new PDFUtil().convertString("     နှစ်", "Myanmar3", "Zawgyi_One"),
						fontb, -1, -1, 0, 0.5f, 0, -1f));
			}

			new PDFUtil().newRow(secondbodytable, fontb);

			secondbodytable.addCell(
					new PDFUtil().writePDFCell("Passport No. (ႏိုင္ငံကူးလက္မွတ္)", lblFont, -1, -1, 0, -1f, 0, -1f));
			secondbodytable.addCell(new PDFUtil().writePDFCell(
					new PDFUtil().convertString(rdata.getPassport(), "Myanmar3", "Zawgyi_One"), fontb, -1, -1, 0, 0.5f,
					0, -1f));
			secondbodytable.addCell(new PDFUtil().writePDFCell("Nationality (ႏိုင္ငံသား)", lblFont, -1,
					Element.ALIGN_RIGHT, 0, -1f, 0, -1f));
			secondbodytable.addCell(new PDFUtil().writePDFCell(
					new PDFUtil().convertString(rdata.getNationality(), "Myanmar3", "Zawgyi_One"), fontb, -1, -1, 0,
					0.5f, 0, -1f));

			new PDFUtil().newRow(thirdbodytable, fontb);
			thirdbodytable.setWidths(new int[] { 1, 2, 1, 5 });
			thirdbodytable.setTotalWidth(new float[] { 60, 70, 75, 295 });
			thirdbodytable.setLockedWidth(true);

			thirdbodytable.addCell(new PDFUtil().writePDFCell("Phone (ဖုန္းနံပါတ္)", lblFont, -1, -1, 0, -1f, 0, -1f));
			thirdbodytable.addCell(new PDFUtil().writePDFCell(
					new PDFUtil().convertString(rdata.getMobilePhone(), "Myanmar3", "Zawgyi_One"), fontb, -1, -1, 0,
					0.5f, 0, -1f));
			thirdbodytable.addCell(new PDFUtil().writePDFCell("Address (ေနရပ္လိပ္စာ)", lblFont, -1, Element.ALIGN_RIGHT,
					0, -1f, 0, -1f));
			String pattern = "^[a-zA-Z]*$";
			String s = rdata.getAddress1();
			String[] arr = s.split(" ");
			String data = arr[arr.length - 1];
			if (data.matches(pattern)) {
				if (rdata.getAddress1().length() > 140) {
					rdata.setAddress1(rdata.getAddress1().substring(0, 140));
				}
			} else {
				if (rdata.getAddress1().length() > 120) {
					rdata.setAddress1(rdata.getAddress1().substring(0, 120));
				}
			}
			PdfPCell addressCell = new PDFUtil().writePDFCell(
					new PDFUtil().convertString(rdata.getAddress1(), "Myanmar3", "Zawgyi_One"), fontb, -1, -1, 0, 0.5f,
					0, -1f);
			// addressCell.setNoWrap(true);
			thirdbodytable.addCell(addressCell);

			new PDFUtil().newRow(thirdbodytable, fontb);
			new PDFUtil().newRow(thirdbodytable, fontb);

			table.addCell(new PDFUtil().writePDFCell("VACCINATION", font, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
					-1, -1f, 2, -1f));
			table.addCell(new PDFUtil().writePDFCell("NAME/MANUFACTURER/\nLOT NO. OF VACCINE", font,
					Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f));
			table.addCell(new PDFUtil().writePDFCell("VACCINATION APPOINTMENT (D/M/Y)", font, Element.ALIGN_MIDDLE,
					Element.ALIGN_CENTER, -1, -1f, 2, -1f));

			cell = new PdfPCell(new Phrase(20, "DATE OF VACCINATION (D/M/Y)", font));
			cell.setRowspan(2);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);

			table.addCell(new PDFUtil().writePDFCell("VACCINATOR", font, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
					-1f, 0, 17f));
			table.addCell(new PDFUtil().writePDFCell("VACCINATION SITE", font, Element.ALIGN_MIDDLE,
					Element.ALIGN_CENTER, -1, -1f, 0, 17f));

			if (rdata.getvArrayList().size() == 0) {
				for (int i = 0; i < 3; i++) {
					String cellText = "";
					if (i == 0)
						cellText = "1st Dose";
					else if (i == 1)
						cellText = "2nd Dose";
					else if (i == 2)
						cellText = "Other";
					table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
							Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					if (cellText.equalsIgnoreCase("Other")) {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_TOP, Element.ALIGN_LEFT, -1,
								-1f, 2, -1f));
					} else {
						table.addCell(new PDFUtil().writePDFCell("Sinopharm \n\nLot No. ", fontb, Element.ALIGN_TOP,
								Element.ALIGN_LEFT, -1, -1f, 2, -1f));
					}
					if (cellText.equalsIgnoreCase("1st Dose")) {
						// \n23/8/2021\n\n8:30AM
						table.addCell(new PDFUtil().writePDFCell(
								"\n" + new PDFUtil().convertString(rdata.getFirstdosetime(), "Myanmar3", "Zawgyi_One")
										+ "\n\n"
										+ new PDFUtil().convertString(rdata.getFirstdosedate(), "Myanmar3",
												"Zawgyi_One"),
								fontb, Element.ALIGN_TOP, Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					} else if (cellText.equalsIgnoreCase("2nd Dose")) {
						table.addCell(new PDFUtil().writePDFCell(
								"\n" + new PDFUtil().convertString(rdata.getSeconddosetime(), "Myanmar3", "Zawgyi_One"),
								fontb, Element.ALIGN_TOP, Element.ALIGN_CENTER, -1, -1f, 2, -1f));

					} else {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 2, -1f));
					}
					table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
							-1f, 2, -1f));
					if (i < 2) {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
					} else {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
					}
				}
			} else if (rdata.getvArrayList().size() == 1) {
				for (int i = 0; i < rdata.getvArrayList().size(); i++) {
					String cellText = "";
					if (i == 0)
						cellText = "1st Dose";
					else if (i == 1)
						cellText = "2nd Dose";
					else if (i == 2)
						cellText = "Other";
					table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
							Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					table.addCell(table.addCell(new PDFUtil().writePDFCell(
							new PDFUtil().convertString(rdata.getvArrayList().get(i).getVaccineLotNo(), "Myanmar3",
									"Zawgyi_One"),
							fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f)));
					table.addCell(table.addCell(table.addCell(new PDFUtil().writePDFCell(
							new PDFUtil().convertString(rdata.getvArrayList().get(i).getNextVaccinationDate(),
									"Myanmar3", "Zawgyi_One"),
							fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f))));
					table.addCell(table.addCell(table.addCell(new PDFUtil().writePDFCell(
							new PDFUtil().convertString(rdata.getvArrayList().get(i).getVaccinationDate(), "Myanmar3",
									"Zawgyi_One"),
							fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f))));
					table.addCell(table.addCell(table.addCell(new PDFUtil().writePDFCell(
							new PDFUtil().convertString(rdata.getvArrayList().get(i).getT1(), "Myanmar3", "Zawgyi_One"),
							fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0, 20f))));
					table.addCell(table.addCell(table.addCell(new PDFUtil().writePDFCell(
							new PDFUtil().convertString(rdata.getvArrayList().get(i).getCenterName(), "Myanmar3",
									"Zawgyi_One"),
							fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0, 20f))));

				}

				for (int i = 1; i < 3; i++) {
					String cellText = "";
					if (i == 0)
						cellText = "1st Dose";
					else if (i == 1)
						cellText = "2nd Dose";
					else if (i == 2)
						cellText = "Other";
					table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
							Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
							-1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
							-1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
							-1f, 2, -1f));
					if (i < 2) {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
					} else {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
					}
				}
			} else {
				for (int i = 0; i < rdata.getvArrayList().size(); i++) {
					String cellText = "";
					if (i == 0)
						cellText = "1st Dose";
					else if (i == 1)
						cellText = "2nd Dose";
					else if (i == 2)
						cellText = "Other";
					table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
							Element.ALIGN_CENTER, -1, -1f, 2, -1f));

					table.addCell(
							new PDFUtil().writePDFCell(
									new PDFUtil().convertString(rdata.getvArrayList().get(i).getVaccineLotNo(),
											"Myanmar3", "Zawgyi_One"),
									fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell(
							new PDFUtil().convertString(rdata.getvArrayList().get(i).getNextVaccinationDate(),
									"Myanmar3", "Zawgyi_One"),
							fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					table.addCell(
							new PDFUtil().writePDFCell(
									new PDFUtil().convertString(rdata.getvArrayList().get(i).getVaccinationDate(),
											"Myanmar3", "Zawgyi_One"),
									fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell(
							new PDFUtil().convertString(rdata.getvArrayList().get(i).getT1(), "Myanmar3", "Zawgyi_One"),
							fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0, 25f));
					table.addCell(
							new PDFUtil().writePDFCell(
									new PDFUtil().convertString(rdata.getvArrayList().get(i).getCenterName(),
											"Myanmar3", "Zawgyi_One"),
									fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1, -1f, 0, 25f));
				}

				for (int i = 2; i < 3; i++) {
					String cellText = "";
					if (i == 0)
						cellText = "1st Dose";
					else if (i == 1)
						cellText = "2nd Dose";
					else if (i == 2)
						cellText = "Other";
					table.addCell(new PDFUtil().writePDFCell(cellText, fontb, Element.ALIGN_MIDDLE,
							Element.ALIGN_CENTER, -1, -1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
							-1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
							-1f, 2, -1f));
					table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER, -1,
							-1f, 2, -1f));
					if (i < 2) {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
					} else {
						table.addCell(new PDFUtil().writePDFCell("", fontb, Element.ALIGN_MIDDLE, Element.ALIGN_CENTER,
								-1, -1f, 0, 25f));
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

			cell1 = new PdfPCell();
			p = new Paragraph(
					"This is arranged by MCCOC under the permission and the instructions of Ministry of Health. Please visit the nearest Health Center/Hospital if there are any health issues after receiving Covid-19 vaccination. (က်န္းမာေရးဝန္ႀကီးဌာန၏ ခြင့္ျပဳခ်က္ႏွင့္ လမ္းၫႊန္ခ်က္မ်ားအတိုင္း MCCOC မွ စီစဥ္ေဆာင္႐ြက္ျခင္း ျဖစ္ပါသည္။  ကိုဗစ္ ၁၉ ကာကြယ္ေဆးထိုးႏွံၿပီး က်န္းမာေရး ျပႆနာ တစ္စုံတစ္ရာျဖစ္ပြါးပါက အနီးဆုံးက်န္းမာေရးဌာန/ေဆး႐ုံသို႔ သြားပါ။)",
					lblFont);
			p.setAlignment(Element.ALIGN_LEFT);
			p.setSpacingAfter(2f);
			cell1.addElement(p);
			cell1.setBorder(0);
			ftable.addCell(cell1);

			// ftable.addCell(new PDFUtil().writePDFCell(
			// "Contact Call Center xxxxx for more information about Covid 19 vaccine. If
			// you have any health problems after getting Covid 19 vaccine, go to the
			// nearest health center/hospital. (ကိုဗစ္ ၁၉ ကာကြယ္ေဆးႏွင့္ ပတ္သက္၍ သိ႐ွိလိုပါက
			// သို႔ဆက္သြယ္ပါ။ ကာကြယ္ေဆးထိုးႏွံၿပီး က်န္းမာ​ေရး ျပႆနာ
			// တစ္စံုတစ္ရာျဖစ္ပြားပါကအနီးဆံုးက်န္းမာေရးဌာန/ေဆးရံုသို႔ သြားပါ။)",
			// lblFont, -1, -1, 0, -1f, 0, -1f));

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
