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

import java.util.Calendar;

public class Measurement {
	private Calendar date;
	private int bg;
	private double iu_total;
	private double iu_carb;
	private double iu_corr;
	private double carbs;

	public Measurement(String date, String time, String bg, String bolus, 
			String carbs) {
		this(toCalendar(date, time), 
				(bg != null && !bg.equals("")) ? Integer.parseInt(bg) : 0,
				(bolus != null && !bolus.equals("")) ?
						Double.parseDouble(bolus) : 0,
				(carbs != null && !carbs.equals("")) ?
						Double.parseDouble(carbs) : 0);
	}
	
	public Measurement(Calendar date, int bg, double bolus, double carbs) {
		this.date = date;
		this.bg = bg;
		this.iu_total = bolus;
		this.carbs = carbs;
		iu_carb = carbs * Utils.getBolusFactorCarbs(date);
		iu_corr = iu_total - iu_carb;
	}

	public String getDate() {
		return String.format("%tF", date);
	}
	public String getLocalizedDate() {
		return Utils.localizeDateString(date);
	}
	public String getTime() {
		return String.format("%tR", date);
	}
	public int getBG() {
		return bg;
	}
	public double getIU_total() {
		return iu_total;
	}
	public double getIU_corr() {
		return iu_corr;
	}
	public double getIU_carb() {
		return iu_carb;
	}
	public double getCarbs() {
		return carbs;
	}
	
	public void add(Measurement m) {
		if(bg == 0 || m.bg == 0)
			bg += m.bg;
		else
			bg = (bg + m.bg) / 2;
		iu_total += m.iu_total;
		carbs += m.carbs;
		
		iu_carb = carbs * Utils.getBolusFactorCarbs(date);
		iu_corr = iu_total - iu_carb;
	}
	
	private static Calendar toCalendar(String date, String time) {
		Calendar d = Utils.toCalendar(date);
		d.set(Calendar.HOUR, Integer.parseInt(time.substring(0, 2)));
		d.set(Calendar.MINUTE, Integer.parseInt(time.substring(3, 5)));
		return d;
	}
}
