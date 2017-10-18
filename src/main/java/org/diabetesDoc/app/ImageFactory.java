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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * This class creates images from XML-files with the DTD <code>DAY</code> (see <i>files/day.dtd</i>).
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-13
 */
final class ImageFactory {
	/** The horizontal offset in pixels. */
	private static final int X_OFF = 100;
	/** The vertical offset in pixels. */
	private static final int Y_OFF = 580;
	/** The minimal displayed blood glucose value in mg/dl. */
	private static final int BG_MIN = 40;
	/** The maximal displayed blood glucose value in mg/dl.*/
	private static final int BG_MAX = 320;
	/** The length of a day in minutes. */
	private static final int DAY_TIME_LENGTH = 24*60;
	/** The diagram's width. */
	private static final int DIAGRAM_WIDTH = 1920;
	/** The diagram's height. */
	private static final int DIAGRAM_HEIGHT = 565;

	/** No constructor, only static methods. */
	private ImageFactory() {}


	/**
	 * The image's width.
	 */
	public static final int IMAGE_WIDTH = 2050;
	/**
	 * The image's height.
	 */
	public static final int IMAGE_HEIGHT = 650;


	/**
	 * Creates an image of the given XML-document.
	 *
	 * @param xmlTxt - The XML-document with DTD DAY (see <i>files/day.dtd</i>) as String.
	 * @return The {@link BufferedImage} of the day's data.
	 * @throws IOException If an I/O exception occurs while reading the XML-String.
	 * @throws JDOMException If an JDOM exception occurs while parsing the XML-String.
	 */
	public static BufferedImage getImage(String xmlTxt) throws IOException, JDOMException {
		return getImage(XML_IO.SAX_BUILDER.build(new java.io.StringReader(xmlTxt)));
	}

	/**
	 * Creates an image of the given XML-document.
	 *
	 * @param xmlDoc the XML-{@link Document} with DTD DAY (see <i>files/day.dtd</i>).
	 * @return The {@link BufferedImage} of the day's data.
	 */
	public static BufferedImage getImage(Document xmlDoc) {
		BufferedImage img;
		Graphics g;
		String timeStr[];
		int time, bz, posX, posY;

		ImageIcon icon = new ImageIcon(ImageFactory.class.getResource("/template.png"));
		img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
		g = img.getGraphics();
		g.drawImage(icon.getImage(), 0,0, icon.getImageObserver());
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 42));

		for(Element e : xmlDoc.getRootElement().getChildren()) {
			if(e.getName().equals("BG") && !e.getAttributeValue("Val").equals("---")) {
				timeStr = e.getAttributeValue("Tm").split(":");
				time = Integer.parseInt(timeStr[0]) * 60 + Integer.parseInt(timeStr[1]);
				bz = Integer.parseInt(e.getAttributeValue("Val"));
				posX = X_OFF + (time*DIAGRAM_WIDTH)/DAY_TIME_LENGTH;
				posY = Y_OFF - ( Math.min(Math.max(0, bz - BG_MIN), BG_MAX - BG_MIN) * DIAGRAM_HEIGHT )
						/ (BG_MAX - BG_MIN);
				if(bz < BG_MIN) {
					g.fillPolygon(new int[]{posX-10, posX  , posX+10},
							new int[]{posY-10, posY, posY-10} , 3);
				} else if(bz > BG_MAX) {
					g.fillPolygon(new int[]{posX-10, posX  , posX+10},
							new int[]{posY+10  , posY, posY+10}, 3);
				} else {
					g.fillPolygon(
							new int[]{posX-4, posX-10, posX  , posX+10, posX+4, posX+10, posX  , posX-10},
							new int[]{posY  , posY-10, posY-4, posY-10, posY  , posY+10, posY+4, posY+10}, 8);
				}
				g.drawString("" + bz,
						Math.max( X_OFF + 10, Math.min( posX-30, X_OFF + DIAGRAM_WIDTH - 60)),
						Math.max( Y_OFF - DIAGRAM_HEIGHT + 50, posY-25 ));
			} else {
				// TODO implement other symbols
			}
		}

		g.dispose();

		return img;
	}
}
