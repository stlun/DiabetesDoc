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

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * This Class provides methods to remind the user to read the data from SmartPix.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2017-10-11
 */
final class Reminder extends javax.swing.JFrame implements java.awt.event.ActionListener {
  /** @see java.io.Serializable */
  private static final long serialVersionUID = 1l;

  /** File, in which the next reminding date is saved. */
  static final File DATE_FILE = new File(".reminder");

  /** For Choosing the waiting time. */
  private final JComboBox<String> waitingTimeChoice = new JComboBox<String>();

  /** For Waiting. */
  private final Timer timer;

  /**
   * Creates a new Reminder-Frame and sets it visible.
   */
  Reminder() {
    super(Utils.localize("%info.remind.ttl%"));
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    timer = new Timer(0, this);
    timer.setActionCommand("setVisible");
    timer.setRepeats(false);

    setIconImage(DiabetesDoc.ICON);

    Calendar date = getRemindingDate();

    JLabel txtLbl = new JLabel(Utils.localize("%info.remind.date.msg%", Utils.localizeDateString(date)));
    add(txtLbl, BorderLayout.CENTER);

    JPanel eastPnl = new JPanel();
      JButton diabetesDoc = new JButton("DiabetesDoc");
      diabetesDoc.addActionListener(this);
      eastPnl.add(diabetesDoc);
    add(eastPnl, BorderLayout.EAST);

    JPanel southPnl = new JPanel();
      waitingTimeChoice.addItem("1 " + Utils.localize("%option.hour%"));
      waitingTimeChoice.addItem("2 " + Utils.localize("%option.hours%"));
      waitingTimeChoice.addItem("4 " + Utils.localize("%option.hours%"));
      southPnl.add(waitingTimeChoice);
      JButton spaeterBtn = new JButton(Utils.localize("%option.remindLater%"));
      spaeterBtn.addActionListener(this);
      JButton verwerfenBtn = new JButton(Utils.localize("%option.remindNextStart%"));
      verwerfenBtn.addActionListener(this);
    southPnl.add(spaeterBtn);
    southPnl.add(verwerfenBtn);
    add(southPnl, BorderLayout.SOUTH);

    setLocation(300, 300);
    pack();
    setAlwaysOnTop(true);
    timer.start();
  }

  /**
   * Returns the reminding date or today, if the file {@link Reminder#DATE_FILE} cannot be read.
   *
   * @return The reminding date (with <code>hh:mm:ss == 00:00:00</code>).
   */
  static Calendar getRemindingDate() {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    try (BufferedReader br = new BufferedReader(new java.io.FileReader(DATE_FILE))) {
      c = Utils.toCalendar( br.readLine() );
    } catch(java.io.FileNotFoundException e) { // do nothing (first start or moved)
    } catch(IOException e) {
      e.printStackTrace();
    } catch(NumberFormatException e) {
      e.printStackTrace();
    }
    return c;
  }


  /**
   * Writes the reminding date into the File {@link Reminder#DATE_FILE}.
   *
   * @param days - count of days until the reminding (today == 0).
   */
  static void writeRemindingDate(int days) {
    Calendar date = Calendar.getInstance();
    date.add(Calendar.DAY_OF_YEAR, days);

    if(DATE_FILE.getParentFile() != null && !DATE_FILE.getParentFile().exists())
      DATE_FILE.getParentFile().mkdirs();
    try (FileWriter fw = new FileWriter(DATE_FILE)) {
      fw.write( Utils.toDateString(date) );
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks, if the reminding date has been reached.
   *
   * @return <b>true</b>, if today's date is equal or after the reminding date; <br>
   *       <b>false</b>, otherwise.
   */
  static boolean isAfterRemindingDate() {
    return Calendar.getInstance().after(getRemindingDate());
  }

  /**
   * Sets the Frame invisible for the given time, if the <code>&quot;Later&quot;</code>-Button was
   * pressed, or disposes the Frame, if the <code>&quot;Next Start&quot;</code>-Button was pressed.
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    if(e.getActionCommand().equals(Utils.localize("%option.remindLater%"))) {
      if(waitingTimeChoice.getSelectedItem().equals("1 " + Utils.localize("%option.hour%")))
        timer.setInitialDelay(3600000); // in ms = 1 hour
      else if(waitingTimeChoice.getSelectedItem().equals(
          "2 " + Utils.localize("%option.hours%")))
        timer.setInitialDelay(7200000); // in ms = 2 hours
      else // 4 hours
        timer.setInitialDelay(14400000); // in ms = 4 hours
      setVisible(false);
      timer.restart();
    } else if(e.getActionCommand().equals("setVisible")) {
      if(isAfterRemindingDate())
        setVisible(true);
      else
        dispose();
    } else if(e.getActionCommand().equals("DiabetesDoc")) {
      new DiabetesDoc();
    } else { // "next system start"
      dispose();
    }
  }

}
