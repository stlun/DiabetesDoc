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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 * This Class performs the actions of the MenuBar of the {@link DiabetesDoc}-Frame.
 * @author Stephan Lunowa
 * @version 2.2 - last modified 2017-10-10
 */
final class MenuListener implements ActionListener {
  /**
   * The {@link DiabetesDoc}-Frame, it belongs to.
   */
  DiabetesDoc frame;

  /**
   * Creates a new {@code MenuAction} instance belonging to the {@link DiabetesDoc}-Frame.
   * @param dd - The {@code DiabetesDoc}-Frame the {@code MenuAction} belongs to.
   */
  MenuListener(DiabetesDoc dd) {
    frame = dd;
  }

  /**
   * Performs the actions of the MenuItems of the {@link DiabetesDoc} main-frame.
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    switch(e.getActionCommand()) {
    case "%menu.read.fromSmartPix%":
      // find the path of the SmartPix-device
      final String smartPixPath = this.getSmartPixPath();
      if(smartPixPath == null)
        return;

      new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() throws Exception {
          File dir = new File(smartPixPath + "/REPORT/XML/");
          for(File f : dir.listFiles()) {
            if(f.isFile() && f.getName().toLowerCase().endsWith(".xml"))
            	XML_IO.parseDeviceInputFile(f);
          }
          return null;
        }
        @Override
        protected void done() {
          try {
            get();
            Dialogs.showInfoMsg("%info.successful.reading.ttl%",
            		"%info.successful.reading.SmartPix.msg%", frame);
            frame.fileListPane.refreshList();
            frame.revalidate();
            frame.repaint();
            Reminder.writeRemindingDate(14);
          } catch(Exception e) {
            Dialogs.showErrorMsg("%error.reading.ttl%",
            		"%error.reading.SmartPix.msg% \n" + e.getMessage(), frame);
          }
          super.done();
        }
      }.execute();
      // copy files
      new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() throws Exception {
          String dateStr = Utils.toDateString(Calendar.getInstance());
          new File("reports").mkdir();
          Path source = Paths.get(smartPixPath , "REPORT");
          Path destination = Paths.get("reports", dateStr);
          Files.walkFileTree(source, new TreeCopier(source, destination));
          try {
            Runtime.getRuntime().exec("reports/replaceBitmaps.sh");
          } catch(IOException e) {
            e.printStackTrace();
          }
          Files.createSymbolicLink(Paths.get(destination.toString(), "index.html"), Paths.get("_review.htm"));
          return null;
        }

        @Override
        protected void done() {
          try {
            get();
            Dialogs.showInfoMsg("%info.successful.copy.ttl%", "%info.successful.copy.msg%", frame);
          } catch(Exception e) {
            Dialogs.showErrorMsg("%error.copy.ttl%", "%error.copy.msg% \n" + e.getMessage(), frame);
            e.getCause().printStackTrace();
          }
          super.done();
        }
      }.execute();
      break;

    case "%menu.read.fromFile%":
      final File source = Dialogs.showFileChoiceMsg(frame,
          Utils.localize("%choose.file.ttl%", "XML"), "%choose.file.read%", false, new File("reports"), "xml");
      if(source != null) {
        new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() throws Exception {
            XML_IO.parseDeviceInputFile(source);
            return null;
          }
          @Override
          protected void done() {
            try {
            	get();
            	Dialogs.showInfoMsg("%info.successful.reading.ttl%",
            			Utils.localize("%info.successful.reading.ttl%", source.getPath()), frame);
              frame.fileListPane.refreshList();
              frame.revalidate();
              frame.repaint();
            } catch(Exception e) {
            	Dialogs.showErrorMsg("%error.reading.ttl%",
            			Utils.localize("%error.reading.file.msg% \n", source.getPath())	+ e.getMessage(), frame);
                  e.getCause().printStackTrace();
            }
            super.done();
          }
        }.execute();
      }
      break;

    case "%menu.output.asPDF%":
      createPDF();
      break;
    case "%menu.options.settings%":
      Dialogs.showSettingsChoiceDialog(frame);
      break;
    case "%menu.options.about%":
      Dialogs.showAboutMsg(frame);
      break;
    case "%menu.options.help%":
      try{
        Desktop.getDesktop().open( new File(
            Locale.getDefault().equals(Locale.GERMANY) ? "hilfe.html" : "help.html") );
      } catch(Exception ex) {
        Dialogs.showErrorMsg("%error.file.open.ttl%", Utils.localize("%error.file.open.msg%",
            Locale.getDefault().equals(Locale.GERMANY) ? "hilfe.html" : "help.html"), frame);
      }
      break;
    default:
      System.err.println(e.getActionCommand() + ": not handeled!");
    }
  }

  /**
   * Searches the path to the SmartPix device.
   * @return The path to the SmartPix device, if found; null, otherwise.
   */
  private String getSmartPixPath() {
    if(new File(DiabetesDoc.getSetting("smartPixPath"), "SMARTPIX.ICO").exists())
      return DiabetesDoc.getSetting("smartPixPath");
    
    String os = System.getProperty("os.name");
    if(os.equals("Linux")) {
      File f = new File("/media/" + System.getProperty("user.name") + "/SMART_PIX/SMARTPIX.ICO");
      if(f.exists()) {
        DiabetesDoc.setSetting("smartPixPath", f.getParent());
        return f.getParent();
      }
    } else if(os.startsWith("win") || os.startsWith("Win")) {
      for(char mnt = 'A'; mnt <= 'Z'; mnt++) {
        if(new File(mnt + ":\\SMARTPIX.ICO").exists()) {
          DiabetesDoc.setSetting("smartPixPath", mnt + ":\\");
          return mnt + ":\\";
        }
      }
    }
    
    File f = Dialogs.showFileChoiceMsg(frame, "SmartPix %choose%", "%choose%", true,
            				   new File(DiabetesDoc.getSetting("smartPixPath")));
    if(f != null) {
      DiabetesDoc.setSetting("smartPixPath", f.getAbsolutePath());
      return f.getAbsolutePath();
    }
    return null;
  }
  
  private void createPDF() {
    final String begin_end = Dialogs.showDayChoiceMsg(frame, "pdf");
    if(begin_end != null) {
      final ProgressMonitor progressMonitor = new ProgressMonitor(frame, Utils.localize("%info.output%", "pdf"), "", 0, 100);
      new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() throws Exception {
          String be[] = begin_end.split("_");
          progressMonitor.setProgress(1);
          OutputCreator.createPDF(Utils.toCalendar(be[0]), Utils.toCalendar(be[1]), progressMonitor);
          progressMonitor.setProgress(101);
          return null;
        }
        @Override
        protected void done() {
          progressMonitor.close();
          try {
            get();
            // show file
            try{
            	Desktop.getDesktop().open( new File("pdf/" + begin_end + ".pdf") );
            } catch(Exception e) {
            	Dialogs.showInfoMsg("%info.successful.output.ttl%", "%info.successful.output.msg%", frame);
            }
          } catch(Exception e) {
            if(e.getCause() instanceof RuntimeException	&& e.getCause().getMessage().equals("Cancelled.")) {
            	Dialogs.showErrorMsg("%error.output.ttl%", "%error.output.msg%", frame);
            } else {
            	Dialogs.showErrorMsg("%error.output.ttl%", "%error.output.msg% \n" + e.getMessage(), frame);
              e.getCause().printStackTrace();
            }
          }
          super.done();
        }
      }.execute();
    }
  }

  /**
     * A {@code FileVisitor} that copies a file-tree in lower case.
     * Skips unused files from REPORT.
     */
    static final class TreeCopier extends java.nio.file.SimpleFileVisitor<Path> {
      private static final Pattern regex = Pattern.compile("((?:\\w)+\\.(?:\\w)+)");
      private final Path source;
        private final Path target;

        TreeCopier(final Path source, final Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
          Path newdir = target.resolve(Paths.get(source.relativize(dir).toString().toLowerCase()));
          Files.copy(dir, newdir);
          newdir.toFile().setReadable(true);
          newdir.toFile().setWritable(true);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          String name = source.relativize(file).toString().toLowerCase();
          if(name.matches("img(?:/|\\\\)(?:rd.*|scanning)\\.gif") || name.matches("img(?:/|\\\\).+\\.png"))
            return FileVisitResult.CONTINUE; // skip these files

          Path newfile = target.resolve(Paths.get(name));
          if(name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".png")) {
            Files.copy(file, newfile);
          } else { // regex replace to correct paths in files
            List<String> lines = Files.readAllLines(file);
            List<String> replaced = new ArrayList<String>();
            for(String l : lines) replaced.add(this.regexReplace(l));
            Files.write(newfile, replaced);
          }
            newfile.toFile().setReadable(true);
          newfile.toFile().setWritable(true);
            return FileVisitResult.CONTINUE;
        }
        
        private String regexReplace(final String str) {
          StringBuffer buffer = new StringBuffer();
      Matcher m = regex.matcher(str);
      while (m.find()) {
        m.appendReplacement(buffer, m.group().toLowerCase());
      }
      m.appendTail(buffer);
      return buffer.toString();
        }
    }
}
