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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Starter for the application.
 *
 */
public final class App {
  /**
   * Starts the DiabetesDoc-Application.
   *
   * @param args <code>[--remind]</code> (optional): starts the {@link Reminder}.
   */
  public static void main(String[] args) {
    DiabetesDoc.loadSettings();
    setLookAndFeel();
    if(args.length > 0 && args[0].equalsIgnoreCase("--remind")) {
      if(Reminder.isAfterRemindingDate())
        SwingUtilities.invokeLater( new Runnable(){ @Override public void run() { new Reminder(); } } );
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        @Override public void run() {
          DiabetesDoc dd = new DiabetesDoc();
          if(Reminder.isAfterRemindingDate()) {
            Dialogs.showInfoMsg("%info.remind.ttl%", "%info.remind.msg%", dd);
          }
        }
      });
    }
  }

  /**
   * Applies the system's own {@link javax.swing.LookAndFeel}.
   */
  private static void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      System.err.println("Could not load the system's LookAndFeel.");
    }
  }
}
