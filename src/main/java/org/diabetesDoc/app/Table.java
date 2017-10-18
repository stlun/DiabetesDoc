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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

import static org.diabetesDoc.app.OutputCreator.*;

/**
 * This Class represents the tables of days, created by the {@link TableFactory}.
 * Every Table has a date, an active BR and contains {@link Table#MAX_COLS}
 * {@link Column}s and a remark-line.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-26
 */
final class Table {
  /**
   * The maximal difference of time in minutes to combine data.
   */
  static final int MAX_TIME_DIFFERENCE = 30;

  /**
   * The number of {@link Column}s per {@code Table}.
   */
  static final int MAX_COLS = 12;

  /**
   * The maximal length of a remark-line at output.
   */
  private static final int MAX_COMMENT_LINE_LENGTH = 110;

  /**
   * The separator sign for output as CSV.
   */
  private static final char CSV_SEPARATOR = ';';

  /** The Table's date. */
  private String date;

  /** The day's active BR. */
  private String br;

  /** The table's columns. */
  private Column[] cols = new Column[MAX_COLS];

  /** The table's remark-line. Remarks are separated by <code>&quot;,  &quot;</code> */
  private StringBuilder remarks = new StringBuilder();

  /** The number of the current, empty {@link Column}. */
  private int currCol = 0;

  /**
   * Creates a new {@code Table} with the given date.
   *
   * @param date - The Table's date.
   * @see Table#Table(String, String)
   */
  Table(String date) {
    this(date, null);
  }

  /**
   * Creates a new {@code Table} with the given date, BR and image.
   *
   * @param date - The Table's date.
   * @param br - The Table's BR.
   * @see Table#Table(String)
   */
  Table(String date, String br) {
    this.date = date;
    this.br = br;
  }

  /**
   * Adds the given {@link Column} to the {@code Table}.
   *
   * @param c - The {@code Column} to add.
   * @return <b><code>true</code></b>, if the {@code Column} was added;
   *         <b><code>false</code></b>, if {@link Table#MAX_COLS} has been reached.
   */
  boolean addColumn(Column c) {
    if(currCol > 0 && Utils.difference(cols[currCol-1].getCell(0), c.getCell(0)) < 30) {
      cols[currCol-1].addColumnData(c);
    } else {
      if(currCol == MAX_COLS)
        return false;
      cols[currCol] = c;
      currCol++;
    }
    return true;
  }

  /**
   * Adds a remark to the remark-line.
   *
   * @param time - The remark's time.
   * @param txt - The remark's text.
   */
  void addRemark(String time, String txt) {
    int pos = -1;
    String oldTime;
    boolean found = false;

    if(txt.equals(Utils.localize("%output.pumpRun%"))) {
      while( (pos = remarks.indexOf(Utils.localize("%output.pumpStop%"), pos+1))
          != -1 ) {
        oldTime = remarks.substring(pos-7, pos-2);
        if(Utils.difference(oldTime, time) <= MAX_TIME_DIFFERENCE) {
          remarks.delete(pos - 7, pos + Utils.localize("%output.pumpStop%").length());
          found = true;
          break;
        }
      }
      if (!found) {
        remarks.append( ((remarks.length() == 0) ? "" : ",  ") + time + ": " + txt );
      }
    } else if(txt.equals(Utils.localize("%output.tbrEnd%"))) {
      while( (pos = remarks.indexOf("TBR ", pos+1))
          != -1 ) {
        if(remarks.length() < pos + 7 || remarks.charAt(pos+7) != '%')
          continue;
        oldTime = remarks.substring(pos-7, pos-2);
        if(Utils.difference(oldTime, time) <= MAX_TIME_DIFFERENCE / 2) {
          remarks.delete(pos-7, pos + 8);
          found = true;
          break;
        }
      }
      if(!found) {
        remarks.append( ((remarks.length() == 0) ? "" : ",  ") + time + ": " + txt );
      }
    } else if(txt.matches("TBR (\\s|\\d)(\\s|\\d)\\d%")) {
      while( (pos = remarks.indexOf(Utils.localize("%output.tbrEnd%"), pos+1))
          != -1 ) {
        oldTime = remarks.substring(pos-7, pos-2);
        if(Utils.difference(oldTime, time) <= MAX_TIME_DIFFERENCE && pos >= 13
            && remarks.substring(pos-13, pos-9).equals(txt.substring(4))) {
          remarks.delete(pos-9, pos + Utils.localize("%output.tbrEnd%").length());
          found = true;
          break;
        }
      }
      if(!found) {
        remarks.append( ((remarks.length() == 0) ? "" : ",  ") + time + ": " + txt );
      }
    } else {
      remarks.append( ((remarks.length() == 0) ? "" : ",  ") + time + ": " + txt );
    }
  }

  /**
   * Finishes the {@code Table}'s content for output.
   */
  void finish() {
    int pos = -1, pos2;
    while((pos = remarks.indexOf("TBR ", pos+1)) != -1) {
      if(remarks.length() < pos+7 || remarks.charAt(pos+7) != '%')
        continue;
      if((pos2 = remarks.indexOf(Utils.localize("%output.tbrEnd%"), pos+1))
          != -1) {
        String time = remarks.substring(pos2-7, pos2-2);
        remarks.delete(pos2-7, pos2 + Utils.localize("%output.tbrEnd%").length())
            .insert(pos-2, " - " + time);
        // return to new position
        pos = remarks.indexOf("TBR ", pos+1);
      } else {

      }
    }
    while((pos = remarks.indexOf(",  ,")) != -1)
      remarks.delete(pos, pos+3);
    if (remarks.length() >= 3) {
      if (remarks.substring(0, 3).equals(",  "))
        remarks.delete(0, 3);
      if (remarks.substring(remarks.length() - 3).equals(",  "))
        remarks.delete(remarks.length() - 3, remarks.length());
    }
  }

  /**
   * @return The date of the {@code Table}.
   */
  String getDate() {
    return date;
  }

  /**
   * @return The BR of the {@code Table}.
   */
  String getBR() {
    return br;
  }
  /**
   * @param br - The new BR of the {@code Table}.
   */
  void setBR(String br) {
    this.br = br;
  }

  /**
   * @return The remarks of the {@code Table}.
   */
  public String getRemarks() {
    finish();
    return remarks.toString();
  }

  /**
   * @return The data contained as List of columns.
   */
  public List<String[]> getData() {
    finish();
    List<String[]> data = new ArrayList<String[]>();
    for(Column c : cols) {
      if(c == null) break;
      String[] cells = new String[6];
      for(int j = 0; j < 6; j++)
        cells[j] = c.getCell(j);
      data.add(cells);
    }
    return data;
  }

  /**
   * @return The {@code Table} as HTML-String.
   * Has to be included in &lt;table&gt;-Tags.
   */
  String toHTMLString() {
    finish();

    int dayOfWeek = Utils.toCalendar(date).get(Calendar.DAY_OF_WEEK);
    String[] rows = new String[8];
    rows[0] = "<tr><td class=\"upper\">" + Utils.localizeDateString(date)
        + "</td><td>" + Utils.localize("%output.time%") + "</td>";
    rows[1] = "<tr><td class=\"lower\">" + Utils.localizeDayOfWeek(Utils.toCalendar(date))
        + "</td><td>" + Utils.localize("%output.bg%") + "</td>";
    rows[2] = "<tr><td rowspan=\"2\">"
        + ((dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)?
            Utils.localize("%output.weekend%") : "")
        + "</td><td>" + Utils.localize("%output.IU% %output.carb.breadUnit%") + "</td>";
    rows[3] = "<tr><td>" + Utils.localize("%output.IU% %output.IU.corr%") + "</td>";
    rows[4] = "<tr><td rowspan=\"2\">" + (br != null ? "Basalrate " + br : "")
        + "</td><td>" + Utils.localize("%output.IU% %output.IU.total%") + "</td>";
    rows[5] = "<tr><td>" + Utils.localize("%output.carb.breadUnit%") + "</td>";
    for(Column c : cols) {
      rows[0] += "<td>" + ((c != null)? c.getCell(0) : "") + "</td>";
      rows[1] += "<td>" + ((c != null)? c.getCell(1) : "") + "</td>";
      rows[2] += "<td>" + ((c != null)? c.getCell(2) : "") + "</td>";
      rows[3] += "<td>" + ((c != null)? c.getCell(3) : "") + "</td>";
      rows[4] += "<td>" + ((c != null)? c.getCell(4) : "") + "</td>";
      rows[5] += "<td>" + ((c != null)? c.getCell(5) : "") + "</td>";
    }
    rows[0] += "<td class=\"last\" rowspan=\"8\">Bild</td>";
    for(int i = 0; i < 6; i++)
      rows[i] += "</tr>";
    String row = getRemarks();
    while(row.length() > MAX_COMMENT_LINE_LENGTH) {
      row = row.substring(0, row.lastIndexOf(",  "));
    }
    rows[6] = "<tr><td rowspan=\"2\">" + Utils.localize("%output.remarks%")
        + "</td><td class=\"remarks upper\" colspan=\"13\">" + row + "</td></tr>";
    rows[7] = "<tr><td class=\"remarks lower\" colspan=\"13\">"
        + remarks.substring(Math.min(row.length() + 3, remarks.length())) + "</td></tr>";

    return rows[0] + "\n" + rows[1] + "\n" + rows[2] + "\n" + rows[3] + "\n"
        + rows[4] + "\n" + rows[5] + "\n" + rows[6] + "\n" + rows[7] + "\n";
  }

  /**
   * Adds the {@code Table} to the given {@link PDPageContentStream} with left-upper corner
   * at xPos, yPos.
   *
   * @param pdfStream - The {@code PDPageContentStream} to which the {@code Table} is added.
   * @param xPos - The left horizontal position of the {@code Table}.
   * @param yPos - The upper vertical position of the {@code Table}.
   * @throws java.io.IOException If an I/O exception occurs while writing to the {@code PDPageContentStream}.
   */
  void toPDF(PDPageContentStream pdfStream, float xPos, float yPos) throws java.io.IOException {
    finish();
    int dayOfWeek = Utils.toCalendar(date).get(Calendar.DAY_OF_WEEK);

    pdfStream.setLineWidth(0.05f);
    ///////////////////////
    // table borders
    ///////////////////////
    pdfStream.drawPolygon(
        new float[]{xPos, xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH,
            	xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, xPos},
        new float[]{yPos + 0.75f*CELL_HEIGHT, yPos + 0.75f*CELL_HEIGHT,
            	yPos - 7.25f*CELL_HEIGHT, yPos - 7.25f*CELL_HEIGHT} );
    // horizontal lines
    pdfStream.drawLine(xPos, yPos + 0.75f*CELL_HEIGHT + 0.07f*CM,
        xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, yPos + 0.75f*CELL_HEIGHT + 0.07f*CM);
    pdfStream.drawLine(xPos + FIRST_CELL_WIDTH, yPos - 0.25f*CELL_HEIGHT,
        xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, yPos - 0.25f*CELL_HEIGHT);
    pdfStream.drawLine(xPos, yPos - 1.25f*CELL_HEIGHT,
        xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, yPos - 1.25f*CELL_HEIGHT);
    pdfStream.drawLine(xPos + FIRST_CELL_WIDTH, yPos - 2.25f*CELL_HEIGHT,
        xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, yPos - 2.25f*CELL_HEIGHT);
    pdfStream.drawLine(xPos, yPos - 3.25f*CELL_HEIGHT,
        xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, yPos - 3.25f*CELL_HEIGHT);
    pdfStream.drawLine(xPos + FIRST_CELL_WIDTH, yPos - 4.25f*CELL_HEIGHT,
        xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, yPos - 4.25f*CELL_HEIGHT);
    pdfStream.drawLine(xPos, yPos - 5.25f*CELL_HEIGHT,
        xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH, yPos - 5.25f*CELL_HEIGHT);
    // vertical lines
    pdfStream.drawLine(xPos + FIRST_CELL_WIDTH, yPos + 0.75f*CELL_HEIGHT,
        xPos + FIRST_CELL_WIDTH, yPos - 7.25f*CELL_HEIGHT);
    for(int i = 1; i <= MAX_COLS; i++)
      pdfStream.drawLine(xPos + FIRST_CELL_WIDTH + i*CELL_WIDTH, yPos + 0.75f*CELL_HEIGHT,
          xPos + FIRST_CELL_WIDTH + i*CELL_WIDTH, yPos - 5.25f*CELL_HEIGHT);

    pdfStream.beginText();
    pdfStream.setFont( PDF_FONT , PDF_FONT_SIZE_TABLE );

    //////////////////////
    // first column
    //////////////////////
    pdfStream.moveTextPositionByAmount(xPos, yPos);
    drawCenteredString(pdfStream, Utils.localizeDateString(date), FIRST_CELL_WIDTH);
    pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localizeDayOfWeek(Utils.toCalendar(date)), FIRST_CELL_WIDTH);
    // in two cells
    pdfStream.moveTextPositionByAmount(0, -1.5f*CELL_HEIGHT);
    drawCenteredString(pdfStream,
        (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY )
        ? Utils.localize("%output.weekend%") : "",
        FIRST_CELL_WIDTH);
    // in two cells
    pdfStream.moveTextPositionByAmount(0, -2.0f*CELL_HEIGHT);
    if(br != null)
      drawCenteredString(pdfStream, "Basalrate " + br, FIRST_CELL_WIDTH);
    // in two cells
    pdfStream.moveTextPositionByAmount(0, -2.0f*CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localize("%output.remarks%"), FIRST_CELL_WIDTH);

    ///////////////////////
    // second column
    ///////////////////////
    pdfStream.moveTextPositionByAmount(FIRST_CELL_WIDTH, +6.5f*CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localize("%output.time%"), CELL_WIDTH);
    pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localize("%output.bg%"), CELL_WIDTH);
    pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localize("%output.IU% %output.carb.breadUnit%"), CELL_WIDTH);
    pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localize("%output.IU% %output.IU.corr%"), CELL_WIDTH);
    pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localize("%output.IU% %output.IU.total%"), CELL_WIDTH);
    pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
    drawCenteredString(pdfStream, Utils.localize("%output.carb.breadUnit%"), CELL_WIDTH);

    /////////////////////////
    // add other columns
    /////////////////////////
    for(int i = 0; i < MAX_COLS; i++){
      pdfStream.moveTextPositionByAmount(CELL_WIDTH,  +6*CELL_HEIGHT);
      for(int j = 0; j < 6; j++) {
        pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
        if(cols[i] != null) {
          drawCenteredString(pdfStream, cols[i].getCell(j), CELL_WIDTH);
        }
      }
    }
    ////////////////////////////
    // add comments
    ////////////////////////////
    String row = getRemarks();
    while(row.length() > MAX_COMMENT_LINE_LENGTH) {
      row = row.substring(0, row.lastIndexOf(",  "));
    }
    pdfStream.moveTextPositionByAmount(0.1f*CM - MAX_COLS*CELL_WIDTH, -CELL_HEIGHT);
    pdfStream.drawString(row);
    pdfStream.moveTextPositionByAmount(0, -CELL_HEIGHT);
    pdfStream.drawString(remarks.substring(Math.min(row.length() + 3, remarks.length())));

    pdfStream.endText();

    ////////////////////////////
    // add image
    ////////////////////////////
    pdfStream.setLineWidth(0.3f);
    pdfStream.setNonStrokingColor(0.925);
    pdfStream.fillRect(xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH + 0.5f*CM,
        yPos - 3.5f*CELL_HEIGHT, (LAST_CELL_WIDTH - 0.5f*CM), 4*CELL_HEIGHT);
    pdfStream.fillRect(xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH + 0.5f*CM,
        yPos - 6.5f*CELL_HEIGHT, (LAST_CELL_WIDTH - 0.5f*CM), CELL_HEIGHT);
    pdfStream.setNonStrokingColor(0.25);
    for(int i = 0; i < 8; i++) {
      if(i % 2 == 0 || i == 7)
        pdfStream.setStrokingColor(0.25);
      else
        pdfStream.setStrokingColor(0.75);
      pdfStream.drawLine(xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH + 0.45f*CM,
          yPos + (0.5f - i)*CELL_HEIGHT,
          xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH
            	+ LAST_CELL_WIDTH,
          yPos + (0.5f - i)*CELL_HEIGHT);
    }
    for(int i = 0; i < 25; i++)
      pdfStream.drawLine(
          xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH + 0.5f*CM
            	+ (LAST_CELL_WIDTH - 0.5f*CM) * i / 24,
          yPos + ((i % 3 == 0) ? 0.5f : - 6.5f)*CELL_HEIGHT,
          xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH + 0.5f*CM
            	+ (LAST_CELL_WIDTH - 0.5f*CM) * i / 24,
          yPos - 6.5f*CELL_HEIGHT - 0.05f*CM);
    pdfStream.beginText();
    pdfStream.setFont( org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD , PDF_FONT_SIZE_TABLE * 0.7f);
    pdfStream.moveTextPositionByAmount(xPos + FIRST_CELL_WIDTH + (MAX_COLS+1)*CELL_WIDTH + 0.225f*CM,
        yPos - 6.7f*CELL_HEIGHT);
    for(int i = 0; i < 8; i++) {
      drawCenteredString(pdfStream, "" + (40*(i+1)), 0.4f, PDF_FONT_SIZE_TABLE * 0.7f);
      pdfStream.moveTextPositionByAmount(0, CELL_HEIGHT);
    }
    pdfStream.moveTextPositionByAmount(0.275f*CM, - 8.5f*CELL_HEIGHT);
    for(int i = 0; i < 25; i+=3) {
      drawCenteredString(pdfStream, "" + i, 0.4f, PDF_FONT_SIZE_TABLE * 0.7f);
      pdfStream.moveTextPositionByAmount((LAST_CELL_WIDTH - 0.5f*CM) / 8, 0);
    }
    pdfStream.setStrokingColor(0.0);
    pdfStream.setNonStrokingColor(0.0);
    pdfStream.moveTextPositionByAmount(-(LAST_CELL_WIDTH - 0.5f*CM) * 9/8, 0.55f*CELL_HEIGHT);
    for(Column c : cols) {
      if(c != null && !c.getCell(1).isEmpty()) {
        float x = (LAST_CELL_WIDTH - 0.5f*CM) * Utils.difference("00:00", c.getCell(0)) / 1440f,
            y = 7 * CELL_HEIGHT * (Integer.parseInt(c.getCell(1)) - 40) / 280f;
        if(x < 0 || x > (LAST_CELL_WIDTH - 0.5f*CM))
          continue;
        if(y < 0) {
          pdfStream.moveTextPositionByAmount(x, 0);
          drawCenteredString(pdfStream, "v", 0, PDF_FONT_SIZE_TABLE * 0.7f);
          pdfStream.moveTextPositionByAmount(0, 0.5f*CELL_HEIGHT);
          drawCenteredString(pdfStream, c.getCell(1), 0, PDF_FONT_SIZE_TABLE * 0.7f);
          pdfStream.moveTextPositionByAmount(-x, -0.5f*CELL_HEIGHT);
        } else if(y > 7 * CELL_HEIGHT) {
          pdfStream.moveTextPositionByAmount(x, 7 * CELL_HEIGHT);
          drawCenteredString(pdfStream, "^", 0, PDF_FONT_SIZE_TABLE * 0.7f);
          pdfStream.moveTextPositionByAmount(0, -0.5f*CELL_HEIGHT);
          drawCenteredString(pdfStream, c.getCell(1), 0, PDF_FONT_SIZE_TABLE * 0.7f);
          pdfStream.moveTextPositionByAmount(-x, -6.5f * CELL_HEIGHT);
        } else {
          pdfStream.moveTextPositionByAmount(x, y);
          drawCenteredString(pdfStream, "x", 0, PDF_FONT_SIZE_TABLE * 0.7f);
          pdfStream.moveTextPositionByAmount(0, 0.5f*CELL_HEIGHT);
          drawCenteredString(pdfStream, c.getCell(1), 0, PDF_FONT_SIZE_TABLE * 0.7f);
          pdfStream.moveTextPositionByAmount(-x, -y-0.5f*CELL_HEIGHT);
        }
      }
    }
    pdfStream.endText();
  }

  /**
  * This Class represents the {@link Table}'s columns.
  * Every {@code Column} contains six cells.
  * @author Stephan Lunowa
  * @version 2.1 - last modified 2014-03-17
  */
  static class Column {
    private Measurement measurement;
    
    /**
     * Creates a new {@code Column} instance with the given entries.
     * <code>null</code> is replaced by an empty String (<code>&quot;&quot;</code>).
     *
     * @param time - The time of the entry (format <code>&quot;hh:mm&quot;</code>).
     * @param bg - The blood glucose value.
     * @param iu_carb - The insulin units for carbohydrates.
     * @param iu_corr - The insulin units for correction of the BG.
     * @param iu_total - The total insulin units.
     * @param carbs - The carbohydrates.
     */
    Column(String time, String bg, String iu_total, String carbs) {
      measurement = new Measurement("2014-01-01", time, bg, iu_total, carbs);
    }

    /**
     * The Table column's cells. Their values:
     * <ul>
     *   <li>cell <b>0</b>: time</li>
     *   <li>cell <b>1</b>: blood glucose value</li>
     *   <li>cell <b>2</b>: insulin units for carbohydrates</li>
     *   <li>cell <b>3</b>: insulin units for correction</li>
     *   <li>cell <b>4</b>: total insulin units </li>
     *   <li>cell <b>5</b>: carbohydrates</li>
     * </ul>
     * @param i - The cell's index.
     * @return The content of the <code>i</code>-th cell.
     */
    String getCell(int i) {
      switch (i) {
      case 0: return measurement.getTime();
      case 1:
        int bg = measurement.getBG();
        return (bg != 0) ? "" + bg : "";
      case 2:
        double iu = measurement.getIU_carb();
        return (iu > 0.04)? String.format("%1.1f", iu) : "";
      case 3: 
        iu = measurement.getIU_corr();
        return (Math.abs(iu) > 0.04)? String.format("%1.1f", iu) : "";
      case 4: 
        iu = measurement.getIU_total();
        return (iu > 0.04)? String.format("%1.1f", iu) : "";
      case 5: 
        double carbs = measurement.getCarbs();
        return (carbs > 0.04)? String.format("%1.1f", carbs) : "";
      default: return "";
      }
    }
    
    /**
     * Adds the other {@code Column}'s data to this one's.
     *
     * @param other - The other {@code Column} whose data is to be added.
     */
    private void addColumnData(Column other) {
      measurement.add(other.measurement);
    }
  }
}
