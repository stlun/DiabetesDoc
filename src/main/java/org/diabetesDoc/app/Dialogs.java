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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.Calendar;
import java.util.Locale;

/**
* This Class provides standard dialogs for the application.
* @author Stephan Lunowa
* @version 2.1 - last modified 2014-03-12
*/
class Dialogs {
  /** No constructor, only static methods. */
  private Dialogs() {}

  /**
   * Shows an info-dialog.
   *
   * @param title - The title of the info-dialog.
   * @param msg - The text of the info-dialog.
   * @param owner - The owning {@code Component} of the info-dialog.
   */
  static void showInfoMsg(String title, String msg, Component owner) {
    JOptionPane.showMessageDialog(owner, Utils.localize(msg),
        Utils.localize(title), JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows an warning-dialog.
   *
   * @param title - The title of the warning-dialog.
   * @param msg - The text of the warning-dialog.
   * @param owner - The owning {@code Component} of the warning-dialog.
   */
  static void showWarningMsg(String title, String msg, Component owner) {
    JOptionPane.showMessageDialog(owner, Utils.localize(msg),
        Utils.localize(title), JOptionPane.WARNING_MESSAGE);
  }

  /**
   * Shows an warning-dialog.
   *
   * @param title - The title of the error-dialog.
   * @param msg - The text of the error-dialog.
   * @param owner - The owning {@code Component} of the error-dialog.
   */
  static void showErrorMsg(String title, String msg, Component owner) {
    JOptionPane.showMessageDialog(owner, Utils.localize(msg),
        Utils.localize(title), JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows the <code>&quot;about DiabetesDoc&quot;</code>-dialog.
   */
  static void showAboutMsg(Component owner) {
    String aboutMsg = Utils.localize("%info.about.msg%", DiabetesDoc.getVersion());
    String aboutTtl = Utils.localize("%info.about.ttl%", DiabetesDoc.getVersion());

    JEditorPane aboutPane = new JEditorPane("text/html", aboutMsg);
    aboutPane.setEditable(false);
    JOptionPane.showMessageDialog(owner, aboutPane, aboutTtl, JOptionPane.PLAIN_MESSAGE);
  }

  /**
   * Shows a dialog to choose a start and end date.
   *
   * @param owner - The owning {@code Component} of the dialog.
   * @param type - The type of output (e.g. <code>&quot;CSV&quot;</code> or <code>&quot;CSV&quot;</code>).
   * @return The period given in the format <code>&quot;YYYY-MM-DD_YYYY-MM-DD&quot;</code>
   *         (<code>start_end</code>).
   */
  static String showDayChoiceMsg(JFrame owner, String type) {
    final DayListModel fromDayListModel = new DayListModel();
    final DayListModel toDayListModel = new DayListModel();
    Integer years[] = new Integer[50];
    Calendar c = Calendar.getInstance();
    for(int i = 0; i < years.length; i++) {
      years[i] = c.get(Calendar.YEAR);
      c.add(Calendar.YEAR, -1);
    }

    final JComboBox<Integer> fromDays   = new JComboBox<Integer>(fromDayListModel);
    fromDays.setSelectedIndex(0);
    final JComboBox<Integer> fromMonths = new JComboBox<Integer>(
        new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12});
    final JComboBox<Integer> fromYears  = new JComboBox<Integer>(years);
    final JComboBox<Integer> toDays   = new JComboBox<Integer>(toDayListModel);
    toDays.setSelectedIndex(0);
    final JComboBox<Integer> toMonths = new JComboBox<Integer>(
        new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12});
    final JComboBox<Integer> toYears  = new JComboBox<Integer>(years);

    ItemListener itemListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          fromDayListModel.updateDays((Integer)fromMonths.getSelectedItem(),
            	(Integer)fromYears.getSelectedItem());
          if((Integer)fromDays.getSelectedItem() > fromDayListModel.getSize())
            fromDays.setSelectedIndex(fromDays.getItemCount()-1);
          toDayListModel.updateDays( (Integer)toMonths.getSelectedItem(),
            	(Integer)toYears.getSelectedItem());
          if((Integer)toDays.getSelectedItem() > toDayListModel.getSize())
            toDays.setSelectedIndex(toDays.getItemCount()-1);
        }
      }
    };
    fromMonths.addItemListener(itemListener);
    fromYears.addItemListener(itemListener);
    toMonths.addItemListener(itemListener);
    toYears.addItemListener(itemListener);
    itemListener.itemStateChanged(new ItemEvent(toMonths, 0, 1, ItemEvent.SELECTED));

    JPanel mainPnl = new JPanel(new GridLayout(2,1));
    JPanel fromPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JPanel toPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    fromPnl.add(new JLabel(Utils.localize("%option.output.day.start%")));
    toPnl.add(new JLabel(Utils.localize("%option.output.day.end%")));
    if(Locale.getDefault().equals(Locale.GERMANY)) {
      fromPnl.add(fromDays);
      fromPnl.add(new JLabel("."));
      fromPnl.add(fromMonths);
      fromPnl.add(new JLabel("."));
      fromPnl.add(fromYears);
      toPnl.add(toDays);
      toPnl.add(new JLabel("."));
      toPnl.add(toMonths);
      toPnl.add(new JLabel("."));
      toPnl.add(toYears);
    } else {
      fromPnl.add(fromYears);
      fromPnl.add(new JLabel("-"));
      fromPnl.add(fromMonths);
      fromPnl.add(new JLabel("-"));
      fromPnl.add(fromDays);
      toPnl.add(toYears);
      toPnl.add(new JLabel("-"));
      toPnl.add(toMonths);
      toPnl.add(new JLabel("-"));
      toPnl.add(toDays);
    }
    mainPnl.add(fromPnl);
    mainPnl.add(toPnl);

    if( JOptionPane.showConfirmDialog(owner, mainPnl,
        Utils.localize("%option.output.days%", type),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        == JOptionPane.OK_OPTION ) {
      String fromMonth = fromMonths.getSelectedItem().toString(),
          fromDay = fromDays.getSelectedItem().toString(),
          toMonth = toMonths.getSelectedItem().toString(),
          toDay = toDays.getSelectedItem().toString();
      return fromYears.getSelectedItem().toString() + "-"
          + ((fromMonth.length() == 2) ? fromMonth : "0" + fromMonth) + "-"
          + ((fromDay.length() == 2) ? fromDay : "0" + fromDay)
          + "_"
          + toYears.getSelectedItem().toString() + "-"
          + ((toMonth.length() == 2) ? toMonth : "0" + toMonth) + "-"
          + ((toDay.length() == 2) ? toDay : "0" + toDay);
    } else {
      return null;
    }
  }

  /**
   * The Class DayListModel representing the days of the given month and year.
   */
  private static class DayListModel extends DefaultComboBoxModel<Integer> {
    /** @see Serializable */
    private static final long serialVersionUID = 1L;

    /** The number of days of the month. */
    private int daysOfMonth = 28;

    /**
     * @see ListModel#getSize()
     */
    @Override
    public int getSize() {
      return daysOfMonth;
    }

    /**
     * @see ListModel#getElementAt(int)
     */
    @Override
    public Integer getElementAt(int i) {
      return (i + 1);
    }

    /**
     * Updates the {@link DayListModel} to the new month and year.
     * @param month - The month.
     * @param year - The year.
     */
    private void updateDays(int month, int year) {
      Calendar c = Calendar.getInstance();
      c.set(year, month, 1);
      c.add(Calendar.DAY_OF_MONTH, -1);
      int newSize = c.get(Calendar.DAY_OF_MONTH);

      if(newSize > daysOfMonth)
        fireIntervalAdded(this, daysOfMonth+1, newSize);
      else if(newSize < daysOfMonth)
        fireIntervalRemoved(this, newSize+1, daysOfMonth);

      daysOfMonth = newSize;
    }
  }

  /**
   * Shows a dialog to choose a file.
   *
   * @param owner - The owning {@code Component} the dialog belongs to.
   * @param title - The title of the dialog.
   * @param btnTxt - The button's text.
   * @param dirChoose - Sets, if only directories are selectable.
   * @param oldDir - The old directory, to start in.
   * @return The chosen file or <code>null</code>, if the user cancelled the dialog.
   */
  static File showFileChoiceMsg(Component owner, String title, String btnTxt,
      boolean dirChoose, File oldDir) {
    return showFileChoiceMsg(owner, title, btnTxt, dirChoose, oldDir, null);
  }

  /**
   * Shows a dialog to choose a file.
   *
   * @param owner - The owning {@code Component} the dialog belongs to.
   * @param title - The title of the dialog.
   * @param btnTxt - The button's text.
   * @param dirChoose - Sets, if only directories are selectable.
   * @param oldDir - The old directory, to start in.
   * @param type - The type of files to filter (e.g. <code>&quot;xml&quot;</code>).
   * @return The chosen file or <code>null</code>, if the user cancelled the dialog.
   */
  static File showFileChoiceMsg(Component owner, String title, String btnTxt,
      boolean dirChoose, File oldDir, String type) {
    JFileChooser fileChooser = new JFileChooser(oldDir);
    fileChooser.setDialogTitle(Utils.localize(title));
    if(dirChoose)
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if(type != null) {
      fileChooser.setFileFilter(new FileNameExtensionFilter(type.toUpperCase(), type));
    }
    int ret = fileChooser.showDialog(owner, Utils.localize(btnTxt));
    if(ret == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    } else {
      return null;
    }
  }

  /**
   * Shows a dialog to choose the settings.
   *
   * @param diabDoc - The {@link DiabetesDoc}-Frame the dialog belongs to.
   */
  static void showSettingsChoiceDialog(final DiabetesDoc diabDoc) {
    String choose = Utils.localize("%choose%");
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbCon = new GridBagConstraints();
    final JTextField[] txt = new JTextField[3];
    final JButton button = new JButton(choose);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        File f = showFileChoiceMsg(diabDoc, "%choose.directory.ttl%", "%choose%",
            	true, new File(txt[0].getText()));
        if(f != null) txt[0].setText(f.getAbsolutePath());
      }
    });

    // Eingabefelder
    gbCon.ipady = 1;
    gbCon.fill = GridBagConstraints.HORIZONTAL;
    gbCon.gridx = 0;
    gbCon.gridy = 0;
    panel.add(new JLabel("SmartPix Directory: "), gbCon);
    gbCon.gridy = 1;
    panel.add(new JLabel("Username: "), gbCon);
    gbCon.gridy = 2;
    panel.add(new JLabel("Birthday: "), gbCon);

    gbCon.gridx = 1;
    gbCon.gridy = 0;
    txt[0] = new JTextField(DiabetesDoc.getSetting("smartPixPath"), 40);
    panel.add(txt[0], gbCon);
    gbCon.gridy = 1;
    txt[1] = new JTextField(DiabetesDoc.getSetting("username"), 40);
    panel.add(txt[1], gbCon);
    gbCon.gridy = 2;
    txt[2] = new JTextField(DiabetesDoc.getSetting("birthday"), 40);
    panel.add(txt[2], gbCon);

    gbCon.gridx = 2;
    gbCon.gridy = 0;
    panel.add(button, gbCon);
    gbCon.gridy = 1;
    panel.add(new JLabel(""), gbCon);
    gbCon.gridy = 2;
    panel.add(new JLabel(""), gbCon);

    int ret = JOptionPane.showConfirmDialog(diabDoc, panel,
        Utils.localize("%menu.options.settings%"),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if(ret == JOptionPane.OK_OPTION) {
      if( !(txt[0].getText().trim().isEmpty()) )
        DiabetesDoc.setSetting("smartPixPath", txt[0].getText().trim());
      if( !(txt[1].getText().trim().isEmpty()) )
        DiabetesDoc.setSetting("username",   txt[1].getText().trim());
      if( !(txt[2].getText().trim().isEmpty()) )
        DiabetesDoc.setSetting("birthday",   txt[2].getText().trim());
    }
  }
}
