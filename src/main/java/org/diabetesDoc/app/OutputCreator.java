package org.diabetesDoc.app;

////////////////////////////////////////////////////////////////////////////////
//
// This file is part of DiabetesDoc.
//
//   Copyright 2017 Stephan Lunowa
//
// DiabetesDoc is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// DiabetesDoc is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with DiabetesDoc. If not, see <http://www.gnu.org/licenses/>.
//
////////////////////////////////////////////////////////////////////////////////

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.ProgressMonitor;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.jdom2.JDOMException;

/**
 * This Class provides methods to create different types of output.<br>
 * The input is taken from the XML-files in the directory given
 * by the key <code>"xmlDir"</code> in {@link DiabetesDoc#PROPERTIES}.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-26
 */
final class OutputCreator {
	/**
	 * 1 cm for output as PDF.
	 */
	static final float CM = 28.35f;
	/**
	 * The width of the page-margin.
	 */
	static final float MARGIN_SIZE = 0.8f * CM;

	/**
	 * The height of a table-cell.
	 */
	static final float CELL_HEIGHT = 0.42f * CM;
	/**
	 * The width of a normal table-cell.
	 */
	static final float CELL_WIDTH  = 1.225f * CM;
	/**
	 * The width of the first table-cell.
	 */
	static final float FIRST_CELL_WIDTH  = 2.1f * CM;
	/**
	 * The width of the last table-cell.
	 */
	static final float LAST_CELL_WIDTH = 10.0f * CM;

	/**
	 * The font for PDF-output.
	 */
	static final PDFont PDF_FONT = PDType1Font.TIMES_ROMAN;
	/**
	 * The font size for the table in PDF-output.
	 */
	static final float PDF_FONT_SIZE_TABLE = 9.0f;

	/** Only static methods. */
	private OutputCreator() {}

	/**
	 * Creates a PDF-file with the data from the given days.
	 *
	 * @param startDate - The {@link Calendar}-date to start with (inclusive).
	 * @param endDate - The {@code Calendar}-date to end with (inclusive).
	 * @throws IOException If an I/O exception occurs while creating the PDF-file from the XML-files.
	 * @throws COSVisitorException If an exception occurs while creating the PDF-file.
	 * @see OutputCreator#createPDF(Calendar, Calendar, ProgressMonitor)
	 */
	public static void createPDF(Calendar startDate, Calendar endDate)
		throws IOException, COSVisitorException {
		createPDF(startDate, endDate, null);
	}

	/**
	 * Creates a PDF-file with the data from the given days.
	 *
	 * @param startDate - The {@link Calendar}-date to start with (inclusive).
	 * @param endDate - The {@code Calendar}-date to end with (inclusive).
	 * @param pm - The {@link ProgressMonitor} displaying the progress
	 *             from <code>0</code> to <code>100</code>.
	 * @throws IOException If an I/O exception occurs while creating the PDF-file from the XML-files.
	 * @throws COSVisitorException If an exception occurs while creating the PDF-file.
	 * @throws RuntimeException If the cancel-button of the {@code ProgressMonitor} was clicked.
	 * @see OutputCreator#createPDF(Calendar, Calendar)
	 */
	public static void createPDF(Calendar startDate, Calendar endDate, ProgressMonitor pm)
			throws IOException, COSVisitorException {
		File out = new File("pdf", Utils.toDateString(startDate) + "_" + Utils.toDateString(endDate) + ".pdf");
		out.getParentFile().mkdirs();

		// Create a new document
		PDDocument pdfDoc = new PDDocument();
		PDRectangle pageSize = new PDRectangle(
				PDPage.PAGE_SIZE_A4.getHeight(), PDPage.PAGE_SIZE_A4.getWidth()); // A4 - transverse
		PDPage page;
		PDPageContentStream pdfStream = null;

		pm.setProgress(1);

		// read XML-files
		int totalTime = Math.max(1, endDate.compareTo(startDate));
		List<Table> tables = new ArrayList<Table>();
		while(!startDate.after(endDate)) {
			if(pm != null) {
				pm.setProgress(1 + 39 * endDate.compareTo(startDate) / totalTime);
				if(pm.isCanceled())
					throw new IllegalStateException("Cancelled.");
			}
			try {
				tables.addAll( TableFactory.createTables(new File(
						"xml/" + Utils.toDateString(startDate) + ".xml")));
			} catch(IOException e) {
				// TODO: auto-generated catch-block
				e.printStackTrace();
			} catch (JDOMException e) {
				// TODO: auto-generated catch-block
				e.printStackTrace();
			}
			startDate.add(Calendar.DATE, 1);
		}

		// Add content
		for(int i = 0; i < tables.size(); i++) {
			if(pm != null) {
				pm.setProgress(40 + 60 * i / tables.size());
				if(pm.isCanceled())
					throw new IllegalStateException("Cancelled.");
			}
			// always 5 Tables per page
			if(i % 5 == 0) {
				page = new PDPage(pageSize);
				pdfDoc.addPage( page );
				if(pdfStream != null)
					pdfStream.close();
				pdfStream = new PDPageContentStream(pdfDoc, page);
				pdfStream.setLineWidth(0.05f);
					addPageHeader(pdfStream);
			}

			tables.get(i).toPDF(pdfStream, 0.8f*CM, 18.44f*CM - (i % 5) * 3.65f*CM);
		}
		if(pdfStream != null)
			pdfStream.close();

		// Save the created document
		pdfDoc.save(out);
		pdfDoc.close();
	}

	/**
	 * Adds the page header to the current page.
	 *
	 * @param pdfStream - The {@link PDPageContentStream} the header is written to.
	 * @throws IOException If an I/O exceotion occurs while writing to the Stream.
	 */
	private static void addPageHeader(PDPageContentStream pdfStream) throws IOException {
		pdfStream.drawLine( MARGIN_SIZE, 19.7f*CM, 28.9f*CM, 19.7f*CM);
		pdfStream.drawLine( MARGIN_SIZE, 19.0f*CM, 28.9f*CM, 19.0f*CM);
		pdfStream.drawLine( MARGIN_SIZE, 19.0f*CM,  0.8f*CM, 19.7f*CM);
		pdfStream.drawLine( MARGIN_SIZE + FIRST_CELL_WIDTH, 19.0f*CM,
				MARGIN_SIZE + FIRST_CELL_WIDTH, 19.7f*CM);
		pdfStream.drawLine(MARGIN_SIZE + FIRST_CELL_WIDTH + (Table.MAX_COLS+1) * CELL_WIDTH, 19.0f*CM,
				MARGIN_SIZE + FIRST_CELL_WIDTH + (Table.MAX_COLS+1) * CELL_WIDTH, 19.7f*CM);
		pdfStream.drawLine(28.9f*CM, 19.0f*CM, 28.9f*CM, 19.7f*CM);
		pdfStream.beginText();
		pdfStream.setFont(PDType1Font.TIMES_ROMAN, 12 );
		pdfStream.moveTextPositionByAmount( 1.0f*CM, 20.0f*CM );
		pdfStream.drawString( DiabetesDoc.getSetting("username")
				+ "  " + DiabetesDoc.getSetting("birthday"));
		pdfStream.setFont(PDType1Font.TIMES_BOLD, 12 );
		pdfStream.moveTextPositionByAmount( MARGIN_SIZE - 1.0f*CM, -0.8f*CM );
		drawCenteredString(pdfStream, Utils.localize("%output.date%"), FIRST_CELL_WIDTH, 12);
		pdfStream.moveTextPositionByAmount(FIRST_CELL_WIDTH, 0);
		drawCenteredString(pdfStream, Utils.localize("%output.overview%"),
				(Table.MAX_COLS + 1) * CELL_WIDTH, 12);
		pdfStream.moveTextPositionByAmount((Table.MAX_COLS + 1) * CELL_WIDTH, 0 );
		drawCenteredString(pdfStream, Utils.localize("%output.bg.long%"), LAST_CELL_WIDTH, 12);
		pdfStream.endText();
	}

	/**
	 * Draws the given String centered in x-direction with total width given by <code>width</code>.
	 *
	 * @param contentStream - The {@link PDPageContentStream} the String is drawn to.
	 * @param txt - The text to draw.
	 * @param width - The width of the area to draw the String centered in.
	 * @throws IOException If an I/O exception occurs while getting the width of the String.
	 */
	static void drawCenteredString(PDPageContentStream contentStream, String txt, float width)
			throws IOException {
		drawCenteredString(contentStream, txt, width, PDF_FONT_SIZE_TABLE);
	}

	/**
	 * Draws the given String centered in x-direction with total width given by <code>width</code>.
	 *
	 * @param contentStream - The {@link PDPageContentStream} the String is drawn to.
	 * @param txt - The text to draw.
	 * @param width - The width of the area to draw the String centered in.
	 * @param fontSize - The {@code PDFont}s size.
	 * @throws IOException If an I/O exception occurs while getting the width of the String.
	 */
	static void drawCenteredString(PDPageContentStream contentStream, String txt, float width, float fontSize)
			throws IOException {
		float centering;
		try {
			centering = (width - PDF_FONT.getStringWidth(txt) / 1000 * fontSize) / 2;
		} catch(IOException e) {
			centering = 0.0f;
		}
		contentStream.moveTextPositionByAmount(centering, 0);
		contentStream.drawString(txt);
		contentStream.moveTextPositionByAmount(-centering, 0);
	}
}
