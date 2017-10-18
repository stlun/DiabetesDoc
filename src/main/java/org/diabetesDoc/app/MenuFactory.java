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
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * This Class performs the actions from the menu-bar of the {@link DiabetesDoc} frame.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-12
 */
final class MenuFactory {
	/** No constructor, only static methods. */
	private MenuFactory() {}

	static JMenuBar getMenuBar(DiabetesDoc dd) {
		MenuListener al = new MenuListener(dd);
		JMenuBar bar = new JMenuBar();
		bar.add( getReadMenu(al) );
		bar.add( getOutputMenu(al) );
		bar.add( getOptionsMenu(al) );

		return bar;
	}

	private static JMenu getReadMenu(ActionListener al) {
		String name = Utils.localize("%menu.read%");
		JMenu menu = new JMenu(name);
		menu.setMnemonic(name.charAt(0));

		menu.add( getMenuItem("%menu.read.fromSmartPix%", al, 'R') );
		menu.add( getMenuItem("%menu.read.fromFile%", al, 'F') );

		return menu;
	}

	private static JMenu getOutputMenu(ActionListener al) {
		String name = Utils.localize("%menu.output%");
		JMenu menu = new JMenu(name);
		menu.setMnemonic(name.charAt(0));

		menu.add( getMenuItem("%menu.output.asPDF%", al, 'P') );

		return menu;
	}

	private static JMenu getOptionsMenu(ActionListener al) {
		String name = Utils.localize("%menu.options%");
		JMenu menu = new JMenu(name);
		menu.setMnemonic(name.charAt(0));

		menu.add( getMenuItem("%menu.options.settings%", al) );
		menu.add( getMenuItem("%menu.options.about%", al) );
		menu.add(new javax.swing.JSeparator());
		menu.add( getMenuItem("%menu.options.help%", al, 'H', KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)) );

		return menu;
	}

	private static JMenuItem getMenuItem(String name, ActionListener al) {
		JMenuItem item = new JMenuItem(Utils.localize(name));
		item.addActionListener(al);
		item.setActionCommand(name);
		return item;
	}
	private static JMenuItem getMenuItem(String name, ActionListener al, char mnemonic) {
		return getMenuItem(name, al, mnemonic, KeyStroke.getKeyStroke(mnemonic, KeyEvent.CTRL_MASK));
	}
	private static JMenuItem getMenuItem(String name, ActionListener al, char mnemonic, KeyStroke accelerator) {
		JMenuItem item = getMenuItem(name, al);
		item.setMnemonic(mnemonic);
		item.setAccelerator(accelerator);
		return item;
	}
}
