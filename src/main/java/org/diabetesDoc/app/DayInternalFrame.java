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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-23
 */
class DayInternalFrame extends JInternalFrame {
	/** @see Serializable */
	private static final long serialVersionUID = 1L;

	private boolean changesSaved = true;
	private final String file;
	private ImagePanel imgPanel;
	private JTable dataTable;
	private DayTableModel dayTableModel = new DayTableModel();

	private JTextPane textPane = new JTextPane();
	private Action saveAction = new SaveAction();
	private UndoManager undo = new UndoManager();
	private Action undoAction = new UndoAction();
	private Action redoAction = new RedoAction();

	DayInternalFrame(final String file, final String title, final JDesktopPane dp) {
		super(title, false, true);
		this.file = file;
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override public void internalFrameClosing(InternalFrameEvent e) { close(); }
		});

		setJMenuBar(createMenuBar());

		try {
			textPane.setEditorKit(new XMLEditorKit());
			textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			textPane.read(new FileReader(file), file);
			textPane.getStyledDocument().addUndoableEditListener(new UndoableEditListener() {
				@Override
				public void undoableEditHappened(UndoableEditEvent e) {
					undo.addEdit(e.getEdit());
					updateGUI();
				}
			});
			textPane.setContentType("text/xml");
			textPane.setDragEnabled(true);

			dataTable = new JTable(dayTableModel);
			dayTableModel.refreshData();
			dataTable.setEnabled(false);
			imgPanel = new ImagePanel(textPane);

			JPanel previewPnl = new JPanel(new BorderLayout());
			JScrollPane tableScrollPane = new JScrollPane(dataTable);
			tableScrollPane.setMinimumSize(new Dimension(320, 120));
			JSplitPane innerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					tableScrollPane, imgPanel);
			previewPnl.add(innerSplitPane, BorderLayout.CENTER);
			previewPnl.add(dayTableModel.getRemarksLabel(), BorderLayout.SOUTH);
			JScrollPane textScrollPane = new JScrollPane(textPane);
			textScrollPane.setMinimumSize(new Dimension(400, 200));
			JSplitPane outerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
					textScrollPane, previewPnl);
			getContentPane().add(outerSplitPane);

			dp.add(this);
			setSize(dp.getSize());
			try{
				setMaximum(true);
			} catch(PropertyVetoException e) {} // do nothing

			setVisible(true);
			outerSplitPane.setBorder(null);
			innerSplitPane.setDividerSize(5);
			innerSplitPane.setResizeWeight(0.75);
			innerSplitPane.setDividerLocation(0.4);
			if(innerSplitPane.getDividerLocation() < tableScrollPane.getMinimumSize().width)
				innerSplitPane.setDividerLocation(tableScrollPane.getMinimumSize().width);
			outerSplitPane.setBorder(null);
			outerSplitPane.setResizeWeight(0.8);
			outerSplitPane.setDividerLocation(0.6);

		} catch(FileNotFoundException e) {
			Dialogs.showErrorMsg("%error.file.open.ttl%", Utils.localize("%error.file.notFound.msg%", file), this);
			this.close();
		} catch(IOException e) {
			Dialogs.showErrorMsg("%error.file.open.ttl%", Utils.localize("%error.file.open.msg%", file), this);
			this.close();
		}
	}

	private void setChangesSaved(final boolean saved) {
		changesSaved = saved;
		if(changesSaved) {
			setTitle(getTitle().replace("*", ""));
		} else if(!getTitle().startsWith("*")) {
			setTitle("*" + getTitle());
		}
	}
	boolean changesSaved() {
		return changesSaved;
	}

	void updateGUI() {
		setChangesSaved(!undo.canUndo());
		undoAction.setEnabled(undo.canUndo());
		undoAction.putValue(Action.NAME, undo.getUndoPresentationName());
		redoAction.setEnabled(undo.canRedo());
		redoAction.putValue(Action.NAME, undo.getRedoPresentationName());
		saveAction.setEnabled(!changesSaved);
		dayTableModel.refreshData();
		imgPanel.repaint();
	}

	private boolean saveFile() {
		try {
			FileWriter fw = new FileWriter(file);
			textPane.write(fw);
			fw.close();
			undo.discardAllEdits();
			updateGUI();
			return true;
		} catch(IOException e) {
			Dialogs.showErrorMsg("%error.file.save.ttl%",
					 Utils.localize("%error.file.save.msg%", file), this);
			return false;
		}
	}

	boolean close() {
		if(changesSaved) {
			dispose();
			return true;
		}
		int ret = JOptionPane.showConfirmDialog(this,
				Utils.localize("%option.close.msg%", getTitle().replace("*", "")),
				Utils.localize("%option.close.ttl%"), JOptionPane.YES_NO_CANCEL_OPTION);
		if(ret == JOptionPane.YES_OPTION && saveFile()) {
			dispose();
			return true;
		} else if(ret == JOptionPane.NO_OPTION) {
			dispose();
			return true;
		} else {
			return false;
		}
	}

	private JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu file = new JMenu(Utils.localize("%menu.file%"));
		file.setMnemonic(Utils.localize("%menu.file%").charAt(0));
		JMenuItem fileSave = new JMenuItem(saveAction);
		file.add(fileSave);
		JMenuItem fileClose = new JMenuItem(Utils.localize("%menu.file.closeFile%"));
		fileClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		fileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK));
		file.add(fileClose);
		mb.add(file);
		JMenu edit = new JMenu(Utils.localize("%menu.edit%"));
		edit.setMnemonic(Utils.localize("%menu.edit%").charAt(0));
		edit.add(undoAction);
		edit.add(redoAction);
		edit.add(new JSeparator());

		Action cutAction = null, copyAction = null, pasteAction = null;
		for (Action a : textPane.getActions()) {
			if(a.getValue(Action.NAME).equals("cut-to-clipboard")) {
				cutAction = a;
			} else if(a.getValue(Action.NAME).equals("copy-to-clipboard")) {
				copyAction = a;
			} else if(a.getValue(Action.NAME).equals("paste-from-clipboard")) {
				pasteAction = a;
			}
		}
		JMenuItem editCut = new JMenuItem(cutAction);
		editCut.setText(Utils.localize("%menu.edit.cut%"));
		editCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
		edit.add(editCut);
		JMenuItem editCopy = new JMenuItem(copyAction);
		editCopy.setText(Utils.localize("%menu.edit.copy%"));
		editCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		edit.add(editCopy);
		JMenuItem editPaste = new JMenuItem(pasteAction);
		editPaste.setText(Utils.localize("%menu.edit.paste%"));
		editPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
		edit.add(editPaste);
		edit.add(new JSeparator());

		JMenuItem editAddBG = new JMenuItem(Utils.localize("%menu.edit.addBG%"));
		editAddBG.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textPane.replaceSelection("\n  <BG Val=\"---\" Dt=\"\" Tm=\"\" Flg=\"\" Ctrl=\"\" "
						+ "Carb=\"\" Ins1=\"\" Ins2=\"\" Ins3=\"\" Evt=\"\" D=\"\" />\n");
			}
		});
		edit.add(editAddBG);
		JMenuItem editAddBOLUS = new JMenuItem(Utils.localize("%menu.edit.addBOLUS%"));
		editAddBOLUS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textPane.replaceSelection(
						"\n  <BOLUS Dt=\"\" Tm=\"\" type=\"\" amount=\"\" cmd=\"\" remark=\"\" />\n");
			}
		});
		edit.add(editAddBOLUS);
		JMenuItem editAddBASAL = new JMenuItem(Utils.localize("%menu.edit.addBASAL%"));
		editAddBASAL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textPane.replaceSelection("\n  <BASAL Dt=\"\" Tm=\"\" cbrf=\"\" profile=\"\" TBRinc=\"\" "
						+ "TBRdec=\"\" cmd=\"\" remark=\"\" />\n");
			}
		});
		edit.add(editAddBASAL);
		JMenuItem editAddEVENT = new JMenuItem(Utils.localize("%menu.edit.addEVENT%"));
		editAddEVENT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textPane.replaceSelection("\n  <EVENT Dt=\"\" Tm=\"\" shortinfo=\"\" description=\"\" />\n");
			}
		});
		edit.add(editAddEVENT);
		mb.add(edit);
		return mb;
	}

	/**
	 *
	 * @author Stephan Lunowa
	 * @version 2.1
	 */
	private class SaveAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		SaveAction() {
			super(Utils.localize("%menu.file.save%"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
			setEnabled(false);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			saveFile();
			updateGUI();
		}
	}
	/**
	 *
	 * @author Stephan Lunowa
	 * @version 2.1
	 */
	private class RedoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		RedoAction() {
			super(undo.getRedoPresentationName());
			setEnabled(false);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			updateGUI();
		}
	}
	/**
	 *
	 * @author Stephan Lunowa
	 * @version 2.1
	 */
	private class UndoAction extends AbstractAction {
	    private static final long serialVersionUID = 1L;

	    UndoAction() {
	        super(undo.getUndoPresentationName());
	        setEnabled(false);
	        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
	    }

	    @Override
	    public void actionPerformed(ActionEvent e) {
	        try {
	            undo.undo();
	        } catch (CannotUndoException ex) {
	            System.out.println("Unable to undo: " + ex);
	            ex.printStackTrace();
	        }
	        updateGUI();
	    }
	}

	private class DayTableModel extends DefaultTableModel {
		/** @see Serializable */
		private static final long serialVersionUID = 1L;

		private JLabel remarksLbl = new JLabel();
		private List<String[]> data = new ArrayList<String[]>();

		private DayTableModel() {
			remarksLbl.setBorder(new EmptyBorder(2, 5, 2, 2));
		}

		@Override
		public int getColumnCount() {
			return 6;
		}
		@Override
		public String getColumnName(int i) {
			switch(i) {
			case 0: return Utils.localize("%output.time%");
			case 1: return Utils.localize("%output.bg%");
			case 2: return Utils.localize("%output.IU% %output.carb.breadUnit%");
			case 3: return Utils.localize("%output.IU% %output.IU.corr%");
			case 4: return Utils.localize("%output.IU% %output.IU.total%");
			case 5: return Utils.localize("%output.carb.breadUnit%");
			default: return null;
			}
		}
		@Override
		public int getRowCount() {
			return (data == null) ? 0 : data.size();
		}
		@Override
		public Object getValueAt(int row, int column) {
			return data.get(row)[column];
		}

		private void refreshData() {
			data.clear();
			remarksLbl.setText(Utils.localize("<html><body><b>%output.remarks%:</b><br/>"));
			try {
				for(Table t : TableFactory.createTables(
						XML_IO.SAX_BUILDER.build(new StringReader(textPane.getText())))) {
					data.addAll(t.getData());
					remarksLbl.setText(remarksLbl.getText() + " " + t.getRemarks());
				}
			} catch(Exception e) {}
			remarksLbl.setText(remarksLbl.getText() + "</body></html>");
			fireTableDataChanged();
		}

		private JLabel getRemarksLabel() {
			return remarksLbl;
		}
	}
}
