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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * Main Class of the DiabetesDoc-Application<br>
 * The Class manages the GUI-Mainframe, the loading and saving of the Properties
 * and the handling of all {@link ActionEvent}s of the GUI-Mainframe.
 * @author Stephan Lunowa
 * @version 0.1b - last modified 2017-10-11
 */
public class DiabetesDoc extends JFrame {
  /** @see Serializable */
  private static final long serialVersionUID = 1L;

  /**
   * The version number of the program.
   * Also used as {@link DiabetesDoc#serialVersionUID}.
   */
  static final long VERSION = 0_001L;

  /** The icon of the Application */
  static final Image ICON = Toolkit.getDefaultToolkit().getImage(DiabetesDoc.class.getResource("/DiabetesDoc.png"));

  /**
   * The path of the file containing the properties. 
   */
  private static final String SETTINGS_PATH = "DiabetesDoc.properties";
  /**
   * Settings used in the whole program.
   */
  private static final Properties SETTINGS;
  /**
   * Standard properties, if the properties-file cannot be read.
   */
  static {
    Properties defaults = new Properties();
    defaults.setProperty("smartPixPath", "");
    defaults.setProperty("username", "");
    defaults.setProperty("birthday", "");
    defaults.setProperty("frameSize", "800,600");
    defaults.setProperty("framePosition", "100,50");
    SETTINGS = new Properties(defaults);
  }

	/** The data from the backend. */
	private HashMap<String, Element> dayMap;

	/**
	 * File-list on the left side of the Mainframe.
	 */
	FileListPane fileListPane;

	/**
	 * Desktop-area for the {@link DayInternalFrame}s.
	 */
	private JDesktopPane desktopPane;

	/**
	 * This {@code ActionListener} opens the files given by the buttons
	 * of the {@link DiabetesDoc#fileListPane}.
	 */
	private ActionListener fileListActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for(JInternalFrame f : desktopPane.getAllFrames()) {
				if(f.getTitle().replace("*", "").equals(Utils.localizeDateString(e.getActionCommand()))) {
					desktopPane.moveToFront(f);
					desktopPane.getDesktopManager().activateFrame(f);
					return;
				}
			}
			new DayInternalFrame( "xml/" + e.getActionCommand() + ".xml",
					Utils.localizeDateString(e.getActionCommand()), desktopPane);
		}
	};

	/**
	 * Creates the GUI-Mainframe.
	 * Loads all important properties and shows then the GUI-Mainframe.
	 */
	public DiabetesDoc() {
		super("DiabetesDoc v" + getVersion());

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				for(JInternalFrame f : desktopPane.getAllFrames()) {
					if( !((DayInternalFrame)f).close() )
						return;
				}
				setSetting("frameSize", getWidth() + "," + getHeight());
				setSetting("framePosition", getLocationOnScreen().x + "," + getLocationOnScreen().y);
				saveSettings();
				dispose();
			}
		});

		setIconImage(ICON);
		setMinimumSize(new Dimension(800,600));

		Rectangle frameBounds;
		try {
			String[] size = getSetting("frameSize").split(",");
			String[] pos = getSetting("framePosition").split(",");
			frameBounds = new Rectangle(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]),
										Integer.parseInt(size[0]),Integer.parseInt(size[1]));
		} catch(NumberFormatException e) {
			frameBounds = new Rectangle(100,50, 800,600);
		}
		setBounds(frameBounds);

		setJMenuBar(MenuFactory.getMenuBar(this));

		fileListPane = new FileListPane(fileListActionListener, this);
		desktopPane = new JDesktopPane();

		getContentPane().add(fileListPane, BorderLayout.WEST);
		getContentPane().add(desktopPane, BorderLayout.CENTER);

		setVisible(true);
	}

	/**
	 * Tries to load the data from file <code>reports/data.xml</code>.
	 * @return true, if successful; false, otherwise
	 */
	public boolean refreshData() {
		File data = new File("reports/data.xml");
		if(data.isFile() && data.canRead()) {
			try {
				dayMap = XML_IO.parseFile(data);
				return true;
			} catch(IOException | JDOMException e) {}
		}
		return false;
	}
	
	/**
	 * @return the sorted array of days of the data.
	 */
	public String[] getDaysInData() {
		if(dayMap == null || dayMap.isEmpty())
			return new String[0];
		String[] days = new String[dayMap.size()];
		dayMap.keySet().toArray(days);
		Arrays.sort(days);
		return days;
	}

	/**
	 * The version number as String.
	 *
	 * @return String the version number with the format X.X
	 */
	public static String getVersion() {
		return String.format( "%d.%d", (VERSION / 1000L), (VERSION % 1000L) );
	}

	/**
	 * Returns the setting with the given key.
	 * @param key - The setting's key.
	 * @return The setting's value or <code>null</code>, if no such setting is set.
	 */
	public static String getSetting(final String key) {
		return SETTINGS.getProperty(key);
	}

	/**
	 * Sets the setting with the given key to the given value.
	 * @param key - The setting's key.
	 * @param value - The setting's value.
	 */
	static void setSetting(final String key, final String value) {
		SETTINGS.setProperty(key, value);
	}

	/**
	 * Loads the Settings.
	 * Uses the file whose path is given by {@link DiabetesDoc#SETTINGS_PATH}.
	 */
	public static void loadSettings() {
		File f = new File(SETTINGS_PATH);
		if(f.exists()) {
			try {
				FileReader fr = new FileReader(f);
				SETTINGS.load(fr);
				fr.close();

				// successful
				return;
			} catch (IOException e) {
				Dialogs.showErrorMsg("%error.file.open.ttl%", "%error.cfgFile.open.msg%", null);
			}
		}
	}

	/**
	 * Saves the {@link DiabetesDoc#SETTINGS}.
	 * Uses the file whose path is given by {@link DiabetesDoc#SETTINGS_PATH}.
	 */
	public static void saveSettings() {
		File f = new File(SETTINGS_PATH);
		if(f.getParentFile() != null && !f.getParentFile().exists())
			f.getParentFile().mkdirs();

		try {
			FileWriter fw = new FileWriter(f);
			SETTINGS.store(fw, "################################################################\n"
							 + "# This is the configuration file for the DiabetesDoc software. #\n"
							 + "# This file is auto-generated and should not be changed.       #\n"
							 + "################################################################\n" );
			fw.close();
		} catch(IOException e) {
			Dialogs.showErrorMsg("%error.file.save.ttl%",
					"%error.cfgFile.save.msg% \n" + e.getMessage(), null);
		}
	}
}
