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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * This Class provides methods for I/O of XML-data.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2017-10-18
 */
final class XML_IO {
	/** The parser for XML-data. */
	public static final SAXBuilder SAX_BUILDER = new SAXBuilder();

	/** The output generator for XML-{@link Document}s. */
	private static final XMLOutputter XML_OUTPUTTER = new XMLOutputter( Format.getPrettyFormat() );

	/**
	 * The {@link Comparator} to compare {@link Element}s of a XML-{@link Document}
	 * with DTD <code>DAY</code> (see <i>files/day.dtd</i>).
	 */
	static final Comparator<Element> DAY_ELEMENT_COMPARATOR = new Comparator<Element>() {
		/**
		 * Compares the two {@link Element}s by the attribute Dt (date), Tm (time) and by the name.
		 *
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final Element e1, final Element e2) {
			// compare dates
			if( !(e1.getAttributeValue("Dt")).equals(e2.getAttributeValue("Dt")) )
					return (e1.getAttributeValue("Dt")).compareTo(e2.getAttributeValue("Dt"));
			// compare times
			if( !(e1.getAttributeValue("Tm")).equals(e2.getAttributeValue("Tm")) )
					return (e1.getAttributeValue("Tm")).compareTo(e2.getAttributeValue("Tm"));
			// compare names
			if( !(e1.getName()).equals(e2.getName()) )
					return (e1.getName()).compareTo(e2.getName());
			// almost equal
			return 0;
		}
	};

	/** No Constructor, only static methods. */
	private XML_IO() {}

	/**
	 * Parses the given device input {@code File} as XML-data.
	 * Writes the results into the directory <code>xmlDir</code>
	 * given by {@link DiabetesDoc#PROPERTIES}.
	 * The output has the DTD <code>DAY</code> (see <i>files/day.dtd</i>).
	 *
	 * @param f - The {@code File} to parse from.
	 * @throws IOException If an I/O exception occurs while reading and writing the files.
	 * @throws JDOMException If an error occurs while parsing the XML-data of the files.
	 * @throws IllegalArgumentException If the file <code>f</code> contains unknown XML-data.
	 */
	public static void parseDeviceInputFile(File f) throws IOException, JDOMException {
		Element root = SAX_BUILDER.build(f).getRootElement();

		if(root.getChild("IP") != null) { // data from insulin pump
			String readingDate = root.getChild("IP").getAttributeValue("Dt");

			// read and write insulin pump profiles
			new File("xml/ipprofiles").mkdirs();
			Element profile;
			while((profile = root.getChild("IP").getChild("IPPROFILE") ) != null) {
				profile.detach();

				DocType profileType = new DocType("IPPROFILE",
						"file:///" + new File("files/IPPROFILE.dtd").getAbsolutePath());
				XML_OUTPUTTER.output(new Document(profile, profileType ),
						new FileWriter("xml/ipprofiles/" + readingDate + "_"
								+ profile.getAttributeValue("Name") + ".xml"));
			}

			// read pump data
			HashMap<String, Element> days = extractDays(root.getChild("IPDATA"), 100);

			// write days
			for(Element day : days.values()) {
				writeDay(day);
			}
		} else if(root.getChild("DEVICE") != null) { // data from bg-devive
			// read device data
			HashMap<String, Element> days = extractDays(root.getChild("BGDATA"), 100);

			// write days
			for(Element day : days.values()) {
				writeDay(day);
			}
		} else {
			throw new IllegalArgumentException("The file contains not supported xml-data.");
		}
	}

	private static HashMap<String, Element> extractDays(Element root, int count) {
		HashMap<String, Element> days = new HashMap<String, Element>(count);
		
		// read data
		while(!root.getChildren().isEmpty()) {
			Element data = root.getChildren().get(0);
			data.detach();
			String date = data.getAttributeValue("Dt");
			if(days.containsKey(date)) {
				days.get(date).addContent(data);
			} else {
				Element day = new Element("DAY");
				day.setAttribute("Dt", date);
				day.addContent(data);
				days.put(date, day);
			}
		}
		
		for(Element day : days.values())
			day.sortChildren(DAY_ELEMENT_COMPARATOR);

		return days;
	}

	/**
	 * Writes the given day into the file <code>YYYY-MM-DD.xml</code> in the
	 * directory <code>xmlDir</code> given by {@link DiabetesDoc#PROPERTIES}.
	 * Its DTD is <code>DAY</code> (see <i>files/day.dtd<i>).
	 *
	 * @param day - The XML-{@link Element} representing the day.
	 * @throws IOException If an I/O exception occurs while reading or writing the file.
	 * @throws JDOMException If an error occurs while parsing the XML-data of the file (if existing).
	 */
	private static void writeDay(Element day) throws IOException, JDOMException {
		File file = new File("xml", day.getAttributeValue("Dt") + ".xml");
		file.getParentFile().mkdirs();

		if(file.exists()) {
			for(Element oldData : SAX_BUILDER.build(file).getRootElement().getChildren()) {
				oldData.detach();

				java.util.List<Element> dayData = day.getChildren();
				int currPos;
				for(currPos = 0; currPos < dayData.size(); currPos++) {
					if(DAY_ELEMENT_COMPARATOR.compare(oldData, dayData.get(currPos)) == 0)
						break;
				}
				if(currPos == dayData.size())
					day.addContent(oldData);
			}
			day.sortChildren(DAY_ELEMENT_COMPARATOR);
		}

		DocType dayType = new DocType("DAY", "file:///" + new File("files/DAY.dtd").getAbsolutePath());
		XML_OUTPUTTER.output(new Document(day, dayType ), new FileWriter(file));
	}
}
