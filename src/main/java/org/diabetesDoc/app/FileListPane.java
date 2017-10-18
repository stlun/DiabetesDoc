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
import java.nio.file.*;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
* This Class represents a button-list for all the XML-files given in the directory
* specified by {@link DiabetesDoc#PROPERTIES} (key <code>&quot;xmlDir&quot;</code>).
* @author Stephan Lunowa
* @version 0.1 - last modified 2017-10-18
*/
final class FileListPane extends javax.swing.JScrollPane {
  /** @see java.io.Serializable */
  private static final long serialVersionUID = 1L;

  /** The {@code ActionListener} listening to the buttons. */
  private final ActionListener al;

  /** The Panel to add all buttons. */
  private final JPanel p = new JPanel();

  /**
   * Creates a new {@link FileListPane} instance.
   *
   * @param al - The {@code ActionListener} listening to the buttons.
   */
  public FileListPane(ActionListener al) {
    this.al = al;
    setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    this.refreshList();
  }

  /**
   * Refreshes the {@code FileList}.
   * This is always necessary, when the content of the directory given by
   * {@link DiabetesDoc#PROPERTIES} (key <code>&quot;xmlDir&quot;</code>) has changed.
   */
  public void refreshList() {
    p.removeAll();
    ArrayList<String> days = new ArrayList<>();
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("xml"), "*.xml")) {
      for(Path entry: stream) {
        days.add(entry.getFileName().toString());
      }
    } catch(DirectoryIteratorException | java.io.IOException e) {
      e.printStackTrace();
    }
    java.util.Collections.sort(days);

    p.setLayout(new java.awt.GridLayout(days.size(), 1));
    for(int i = days.size()-1; i >= 0; i--) {
      days.set(i, days.get(i).substring(0, days.get(i).length()-4));
      JButton button = new JButton(Utils.localizeDateString(days.get(i)));
      button.setActionCommand(days.get(i));
      button.addActionListener(al);
      p.add(button);
    }

    setViewportView(p);
    setPreferredSize(new java.awt.Dimension(p.getPreferredSize().width + 30, p.getPreferredSize().height));
  }
}
