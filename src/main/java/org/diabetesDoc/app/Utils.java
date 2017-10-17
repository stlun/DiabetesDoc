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
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * This class provides useful methods for different tasks.
 * @author Stephan
 * @version 2.1 - last modified 2014-03-18
 */
public class Utils {
	/** The last used {@link ResourceBundle} for language binding. */
	private static ResourceBundle currResourceBundle;

	/**
	 * The language of the last used {@link ResourceBundle}
	 * @see Utils#currResourceBundle
	 */
	private static Locale currLang;

	/** No constructor, only static methods. */
	private Utils() {}

	private static Element factors[];
	static {
		try {
			File[] f = new File("xml/bolusfactors/").listFiles();
			if(f != null) {
  			factors = new Element[f.length];
	  		for(int i = 0; i < f.length; i++) {
	  			try {
		  			factors[i] = XML_IO.SAX_BUILDER.build(f[i]).getRootElement();
			  	} catch(IOException | JDOMException e) {
		  			e.printStackTrace();
			  	}
			  }
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the language's {@link ResourceBundle}, whereby the
	 * language is set from {@link Locale#getDefault().
	 * Currently only return German or otherwise English.
	 *
	 * @return The language's {@code ResourceBundle}.
	 * @see Utils#getLang(Locale)
	 */
	public static ResourceBundle getLang() throws IOException {
		if(Locale.getDefault().equals(currLang)) {
			// all done
		} else {
			currLang = Locale.getDefault();
			if(currLang.equals(Locale.GERMANY)) {
				currResourceBundle = new PropertyResourceBundle(Utils.class.getResourceAsStream("/locale/lang_de.properties"));
			} else {
				currResourceBundle = new PropertyResourceBundle(Utils.class.getResourceAsStream("/locale/lang_en.properties"));
			}
		}
		return currResourceBundle;
	}

	/**
	 * Localizes the given text to the language set by {@link Locale#getDefault()}.
	 *
	 * @param txt - The text to localize.
	 * @return The localized text.
	 * @see Utils#localize(String, String)
	 */
	public static String localize(String txt) {
		return localize(txt, null);
	}

	/**
	 * Localizes the given text to the language set by {@link Locale#getDefault()}
	 * and replaces the given part.
	 *
	 * @param txt - The text to localize.
	 * @param replacement - The replacement to do in the text.
	 * @return The localized text.
	 * @see Utils#localize(String)
	 */
	public static String localize(String txt, String replacement) {
		try {
			ResourceBundle rb = getLang();
			String key, value;
			Enumeration<String> keys = rb.getKeys();
			while(keys.hasMoreElements()) {
				key = keys.nextElement();
				if(txt.contains("%" + key + "%")) {
					value = rb.getString(key);
					txt = txt.replaceAll("%" + key + "%", value);
				}
			}

		} catch(IOException e) {
			e.printStackTrace();
		}

		if(replacement != null) {
			txt = MessageFormat.format(txt, replacement);
		}
		return txt;
	}

	/**
	 * Localize the day to the language set by {@link Locale#getDefault()}.
	 *
	 * @param dayOfWeek - The day of week, given by {@link Calendar#MONDAY} to {@link Calendar#SUNDAY}.
	 * @return The localized day.
	 */
	public static String localizeDayOfWeek(Calendar date) {
		return String.format("%tA", date);
	}

	/**
	 * Converts the given {@link Calendar} to a date {@code String}.
	 *
	 * @param c - The {@code Calendar} date.
	 * @return The {@code String} with the format <code>YYYY-MM-DD</code>.
	 */
	public static String toDateString(final Calendar c) {
		return String.format("%tF", c);
	}

	/**
	 * Converts the given {@link Calendar} to a localized date {@code String}.
	 *
	 * @param c - The {@code Calendar} date.
	 * @return The {@code String} with the locale format
	 *         (<code>DD.MM.YYYY</code> for DE,
	 *          <code>YYYY-MM-DD</code> otherwise).
	 */
	public static String localizeDateString(final Calendar c) {
		if(Locale.getDefault().equals(Locale.GERMANY)) {
			return String.format("%td.%<tm.%<tY", c);
		} else {
			return String.format("%tF", c);
		}
	}

	/**
	 * Converts the given date {@code String} to a {@link Calendar}.
	 *
	 * @param date - The date with the format <code>YYYY-MM-DD</code>,
	 *               <code>MM/DD/YY</code> (since 2000) or <code>DD.MM.YYYY</code>
	 * @return The {@code Calendar} representation (with <code>hh:mm:ss == 00:00:00</code>).
	 */
	public static Calendar toCalendar(final String date) {
		Calendar c = Calendar.getInstance();
		c.clear();
		if(date.charAt(4) == '-') // GB and ISO-8601
			c.set(Integer.parseInt(date.split("-")[0]),
					Integer.parseInt(date.split("-")[1])-1,
					Integer.parseInt(date.split("-")[2]));
		else if(date.charAt(2) == '.') // DE
			c.set(Integer.parseInt(date.split(".")[2]),
					Integer.parseInt(date.split(".")[1])-1,
					Integer.parseInt(date.split(".")[0]));
		else // US
			c.set(Integer.parseInt("20" + date.split("/")[2]),
					Integer.parseInt(date.split("/")[0])-1,
					Integer.parseInt(date.split("/")[1]));
		return c;
	}

	/**
	 * Converts the given date {@code String} to a localized date {@code String}.
	 *
	 * @param date - The date with the format <code>YYYY-MM-DD</code>,
	 *               <code>MM/DD/YY</code> (since 2000) or <code>DD.MM.YYYY</code>
	 * @return The {@code String} with the localized format
	 *         (<code>DD.MM.YYYY</code> for DE,
	 *          <code>MM/DD/YYYY</code> for US,
	 *          <code>YYYY-MM-DD</code> otherwise).
	 */
	public static String localizeDateString(String date) {
		return Utils.localizeDateString(Utils.toCalendar(date));
	}

	/**
	 * Returns the difference of time between the first and the second time.
	 *
	 * @param time1 - The first time with the format <code>hh:mm</code>.
	 * @param time2 - The first time with the format <code>hh:mm</code>.
	 * @return The time difference in minutes.
	 */
	public static int difference(String time1, String time2) {
		String t1[] = time1.split(":");
		String t2[] = time2.split(":");

		return ( Integer.parseInt(t2[0]) - Integer.parseInt(t1[0]) ) * 60
				+ Integer.parseInt(t2[1]) - Integer.parseInt(t1[1]);
	}
	
	public static double getBolusFactorCarbs(Calendar date) {
		List<Element> periods = factors[0].getChildren();
		/*for(int i = 0; i < factors.length; i++) {
			if(factors[i] == null) continue;
			if(Utils.toCalendar(factors[i].getAttributeValue("begin")).before(date))
				periods = factors[i].getChildren();
		}*/
		String time = String.format("%tR", date);
		double factor = 0;
		for(Element period : periods) {
			if(difference(time, period.getAttributeValue("begin")) < 0)
				factor = Double.parseDouble(period.getAttributeValue("khfactor"));
		}
		return factor;
	}
}
