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

import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
* This Class represents a button-list for all the XML-files given in the directory
* specified by {@link DiabetesDoc#PROPERTIES} (key <code>&quot;xmlDir&quot;</code>).
* @author Stephan Lunowa
* @version 2.1 - last modified 2014-03-17
*/
public class FileListPane extends JScrollPane {
	/** @see Serializable */
	private static final long serialVersionUID = 1L;

	/** The {@code ActionListener} listening to the buttons. */
	private final ActionListener al;
	/** The {@code DiabetesDoc} for the data. */
	private final DiabetesDoc dd;
	/** The Panel to add all buttons. */
	private JPanel p = new JPanel();

	/**
	 * Creates a new {@link FileListPane} instance.
	 *
	 * @param al - The {@code ActionListener} listening to the buttons.
	 */
	public FileListPane(ActionListener al, DiabetesDoc dd) {
		this.al = al;
		this.dd = dd;
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.refreshList();
		setPreferredSize(new Dimension(p.getPreferredSize().width + 30, p.getPreferredSize().height));
	}

	/**
	 * Refreshes the {@code FileList}.
	 * This is always necessary, when the content of the directory given by
	 * {@link DiabetesDoc#PROPERTIES} (key <code>&quot;xmlDir&quot;</code>) has changed.
	 */
	public void refreshList() {
		if(dd.refreshData()) {
			p.removeAll();
			String[] days = dd.getDaysInData();
			p.setLayout(new GridLayout(days.length, 1));

			for(int i = days.length-1; i >= 0; i--) {
				JButton button = new JButton(Utils.localizeDateString(days[i]));
				button.setActionCommand(days[i]);
				button.addActionListener(al);
				p.add(button);
			}

			setViewportView(p);
		}
	}
}
