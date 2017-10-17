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
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/**
 * This Class visualizes the XML-data from a {@link JTextComponent} as image
 * by using the {@link ImageFactory} to create these.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-19
 */
class ImagePanel extends JPanel {
	/** @see Serializable */
	private static final long serialVersionUID = 1L;

	private JTextComponent textComp;

	ImagePanel(JTextComponent textComp) {
		this.textComp = textComp;
		setMinimumSize(new Dimension(ImageFactory.IMAGE_WIDTH/8, ImageFactory.IMAGE_HEIGHT/8));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width, height;
		if(getWidth() * ImageFactory.IMAGE_HEIGHT < getHeight() * ImageFactory.IMAGE_WIDTH) {
			width = getWidth();
			height = ImageFactory.IMAGE_HEIGHT*getWidth()/ImageFactory.IMAGE_WIDTH;
		} else {
			width = ImageFactory.IMAGE_WIDTH*getHeight()/ImageFactory.IMAGE_HEIGHT;
			height = getHeight();
		}

		try {
			g.drawImage(ImageFactory.getImage(textComp.getText()),
					(getWidth() - width)/2,(getHeight() - height)/2, width, height, this);
		} catch(Exception e) {
			g.setColor(Color.RED);
			g.drawString(Utils.localize("%error.xml.notValid%"), 10, 20);
			g.drawString(e.getMessage(), 10, 40);
		}
	}
}
