package org.diabetesDoc.app;

////////////////////////////////////////////////////////////////////////////////
//
// This file is part of DiabetesDoc.
//
// Copyright 2017 Stephan Lunowa
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * This Class creates {@link Table}s from XML-data with DTD <code>DAY</code>
 * (see <i>files/day.dtd</i>) for outputting it as CSV or PDF.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-26
 */
public class TableFactory {
	/**
	 * No constructor, only static methods.
	 */
	private TableFactory() {}

	/**
	 * Creates a new Table from the given XML-Document.
	 *
	 * @param xmlFile a XML-{@code File} with the DTD <code>DAY</code> (see <i>files/day.dtd</i>).
	 *
	 */
	static List<Table> createTables(File xmlFile) throws IOException, JDOMException {
		return createTables(XML_IO.SAX_BUILDER.build(xmlFile));
	}

	/**
	 * Creates a new {@link Table} from the given XML-Document.
	 *
	 * @param xmlDoc a XML-{@link Document} with the DTD <code>DAY</code> (see <i>files/day.dtd</i>).
	 */
	public static List<Table> createTables(Document xmlDoc) {
		List<Table> tables = new ArrayList<Table>();
		Table.Column c;
		Table t;

		t = new Table(xmlDoc.getRootElement().getAttributeValue("Dt"));
		tables.add(t);

		for(Element e : xmlDoc.getRootElement().getChildren()) {

			String time, cmd, remark;
			switch(e.getName()) {
			case "BG":
				// the amount of glucose in the blood
				String value = e.getAttributeValue("Val");
				// the time
				time = e.getAttributeValue("Tm");
				// flag for special moments (e.g. M1 = before meal)
				//String flag = e.getAttributeValue("Flg");
				// if an control was done
				String ctrl = e.getAttributeValue("Ctrl");
				// the amount of carbohydrates in gram
				String carb = e.getAttributeValue("Carb");
				// the insulin amount from manual disposal // TODO: ensure
				String insulin1 = e.getAttributeValue("Ins1");
				// the insulin amount from manual disposal // TODO: ensure
				String insulin2 = e.getAttributeValue("Ins2");
				// the insulin amount from manual disposal // TODO: ensure
				String insulin3 = e.getAttributeValue("Ins3");
				// an event (e.g sports)
				String event = e.getAttributeValue("Evt");
				// the meter device
				//String d = e.getAttributeValue("D");
				if(ctrl == null || ctrl.trim().isEmpty()) {
					c = new Table.Column(time, (value.equals("---") ? "" : value),
							(insulin1 == null ? "" : insulin1)
									+ (insulin2 == null ? "" : insulin2)
									+ (insulin3 == null ? "" : insulin3),
							(carb == null ? "" : String.format("%1.1f", Integer.parseInt(carb) / 12.0)));
					if(!t.addColumn(c)) {
						t = new Table(t.getDate(), t.getBR());
						tables.add(t);
						t.addColumn(c);
					}
				} else {
					t.addRemark(time, "Ctrl: " + value);
				}
				break;
			case "BOLUS":
				// the time
				time = e.getAttributeValue("Tm");
				// the type of insulin disposal
				String type = e.getAttributeValue("type");
				// TODO: add info
				cmd			= e.getAttributeValue("cmd");
				// the insulin amount
				String amount = e.getAttributeValue("amount");
				// a comment
				remark = e.getAttributeValue("remark");

				if(time.isEmpty()) {
					//t.addComment(remark, amount); // optional BOLUS total + BASAL total
				} else {
					c = new Table.Column(time, "", amount, "");
					if(!t.addColumn(c)) {
						t = new Table(t.getDate(), t.getBR());
						tables.add(t);
						t.addColumn(c);
					}
				}
				break;
			case "BASAL":
				// the time
				time = e.getAttributeValue("Tm");
				// the current amount for the BR (in IU/h)
				//String cbrf = e.getAttributeValue("cbrf");
				// the percentage of the temporary decrease of the BR
				String tBRdec = e.getAttributeValue("TBRdec");
				// the percentage of the temporary increase of the BR
				String tBRinc = e.getAttributeValue("TBRinc");
				// the active BR profile
				String profile = e.getAttributeValue("profile");
				// TODO: add info
				cmd = e.getAttributeValue("cmd");
				// a comment
				remark = e.getAttributeValue("remark");

				if(profile != null) {
					if(t.getBR() == null)
						t.setBR(profile);
					else if(!t.getBR().equals(profile))
						t.addRemark(time, Utils.localize("%output.brChanged%") + profile);
				}
				if(remark != null && (remark.equals("Run") || remark.equals("Stop"))) {
					t.addRemark(time, Utils.localize("%output.pump" + remark + "%"));
				} else if(remark != null && remark.matches("changed \\d")) {
					if(!t.getBR().equals("" + remark.charAt(8)))
						t.addRemark(time, Utils.localize("%output.brChanged%")
								+ " " + remark.charAt(8));
				} else if(tBRdec != null && (remark != null || time.equals("00:00"))) {
					t.addRemark(time, "TBR " + tBRdec);
				} else if(tBRinc != null && (remark != null || time.equals("00:00"))) {
					t.addRemark(time, "TBR " + tBRinc);
				} else if(remark != null && remark.startsWith("TBR End")){
					t.addRemark(time, Utils.localize("%output.tbrEnd%"));
				} else {
					// TODO implement other basal data
				}
				break;
			case "EVENT":
				// the time
				time		= e.getAttributeValue("Tm");
				// a short info (e.g. E1, W8, ... or 1.2IU)
				String shortinfo = e.getAttributeValue("shortinfo");
				// the event's description
				String description = e.getAttributeValue("description");

				if(shortinfo == null) {// e.g. for cartridge changed
					if(!description.equals("cartridge changed"))
						t.addRemark(time, description);
				} else if(shortinfo.equals("E4")) {		// E4 == occlusion
					t.addRemark(time, Utils.localize("%output.occlusion%"));
				} else if(shortinfo.endsWith("IU")) {	// prime infusion-set
					t.addRemark(time, Utils.localize("%output.prime%"));
				} else if(!( shortinfo.equals("E1")		// E1 == cartridge empty
						|| shortinfo.equals("W1")		// W1 == cartridge low
						|| shortinfo.equals("W2")		// W2 == battery low
						|| shortinfo.equals("W8"))) {	// W8 == bolus cancelled
					t.addRemark(time, description + " (" + shortinfo + ")");
				}
				break;
			}
		}
		return tables;
	}
}
