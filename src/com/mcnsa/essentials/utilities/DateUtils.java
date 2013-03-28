package com.mcnsa.essentials.utilities;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.mcnsa.essentials.exceptions.EssentialsCommandException;

public class DateUtils {
	public static long dawnOffsetHours = 8;
	
	public static String formatMinecraftTime(long time) {
		// adjust the time based on dawn offset
		time += dawnOffsetHours * 1000;
		
		// clamp it to within 24 hours
		while(time > 24000) {
			time -= 24000;
		}
		
		// get the actual hours and minutes
		int hours = (int) Math.floor(time / 1000);
		int minutes = (int) ((time % 1000) / 1000.0 * 60);
		
		// and format the string
		return String.format("%02d:%02d", hours, minutes);
	}
	
	public static long parseMinecraftTime(String stringTime) throws EssentialsCommandException {
		// match just an integer to start with
		try {
			int time = Integer.parseInt(stringTime);
			
			// if it was just an hour (not true minecraft time)
			// deal with that
			if(time <= 24) {
				return ((time - dawnOffsetHours) % 24) * 1000;
			}
		}
		catch(NumberFormatException e) {
			// wasn't an integer, try again!
		}
		
		// try some shortcuts
		if(stringTime.equalsIgnoreCase("dawn")) {
			return 22000;
		}
		else if(stringTime.equalsIgnoreCase("sunrise")) {
			return 23000;
		}
		else if(stringTime.equalsIgnoreCase("morning") || stringTime.equalsIgnoreCase("day")) {
			return 24000;
		}
		else if(stringTime.equalsIgnoreCase("noon")) {
			return 4000;
		}
		else if(stringTime.equalsIgnoreCase("afternoon")) {
			return 6000;
		}
		else if(stringTime.equalsIgnoreCase("evening")) {
			return 8000;
		}
		else if(stringTime.equalsIgnoreCase("sunset")) {
			return 13000;
		}
		else if(stringTime.equalsIgnoreCase("dusk")) {
			return 13500;
		}
		else if(stringTime.equalsIgnoreCase("night")) {
			return 14000;
		}
		else if(stringTime.equalsIgnoreCase("midnight")) {
			return 16000;
		}
		
		// nope? ok, try some regex
		if(stringTime.matches("[0-9]+:[0-9]+")) {
			// break it up
			String[] parts = stringTime.split(":");
			int hours = 0, minutes = 0;
			try {
				hours = Integer.parseInt(parts[0]);
				minutes = Integer.parseInt(parts[1]);
			}
			catch(Exception e) {
				throw new EssentialsCommandException("Unknown time input format '%s'!", stringTime);
			}
			
			// ok, calculate and adjust
			return (long)((((hours - dawnOffsetHours) % 24) * 1000) + (minutes % 60) / 60.0 * 1000);
		}
		
		// couldn't find it?
		throw new EssentialsCommandException("Unknown time input format '%s'!", stringTime);
	}
	
	// utility function to figure when the next given weekday is
	private static long nextWeekDay(int newDay) {
		// figure out what day of the week we're on now
		Calendar now = Calendar.getInstance();
		int currentDay = now.get(Calendar.DAY_OF_WEEK);
		
		// figure out how many days to add
		int daysFromNow = newDay - currentDay;
		if(daysFromNow < 1) {
			daysFromNow += 7;
		}
		// add it
		
		now.add(Calendar.DAY_OF_YEAR, daysFromNow);
		
		// and return
		return now.getTimeInMillis();
	}
	
	// atempt to parse a real-world date string
	public static Timestamp parseTimestamp(String stringTime) throws EssentialsCommandException {
		// try some shortcuts first
		if(stringTime.equalsIgnoreCase("tomorrow")) {
			return new Timestamp(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
		}
		else if(stringTime.equalsIgnoreCase("sunday")) {
			return new Timestamp(nextWeekDay(Calendar.SUNDAY));
		}
		else if(stringTime.equalsIgnoreCase("monday")) {
			return new Timestamp(nextWeekDay(Calendar.MONDAY));
		}
		else if(stringTime.equalsIgnoreCase("tuesday")) {
			return new Timestamp(nextWeekDay(Calendar.TUESDAY));
		}
		else if(stringTime.equalsIgnoreCase("wednesday")) {
			return new Timestamp(nextWeekDay(Calendar.WEDNESDAY));
		}
		else if(stringTime.equalsIgnoreCase("thursday")) {
			return new Timestamp(nextWeekDay(Calendar.THURSDAY));
		}
		else if(stringTime.equalsIgnoreCase("friday")) {
			return new Timestamp(nextWeekDay(Calendar.FRIDAY));
		}
		else if(stringTime.equalsIgnoreCase("saturday")) {
			return new Timestamp(nextWeekDay(Calendar.SATURDAY));
		}
		
		// go through a list of different formats
		String[] formats = new String[]{
				"yyyy-MM-dd hh:mm:ss a",
				"yyyy-MM-dd hh:mm:ssa",
				"yy-MM-dd hh:mm:ss a",
				"yy-MM-dd hh:mm:ssa",
				"yyyy-MM-dd hh:mm a",
				"yy-MM-dd hh:mm a",
				"yyyy-MM-dd hh:mma",
				"yy-MM-dd hh:mma",
				"yyyy-MM-dd HH:mm:ss",
				"yyyy-MM-dd HH:mm:ss",
				"yy-MM-dd HH:mm:ss",
				"yy-MM-dd HH:mm:ss",
				"yyyy-MM-dd HH:mm",
				"yy-MM-dd HH:mm",
				"yyyy-MM-dd HH:mm",
				"yy-MM-dd HH:mm",
				"yyyy-MM-dd",
				"yy-MM-dd",
				"MMM dd, yyyy",
				"MMM dd, yy",
				"EEE MMM dd, yyyy",
				"EEE MMM dd, yy"
		};
		
		// try some different patterns
		SimpleDateFormat dateParser = new SimpleDateFormat();
		for(String format: formats) {
			try {
				dateParser.applyPattern(format);
				Date date = dateParser.parse(stringTime);
				return new Timestamp(date.getTime());
			}
			catch(ParseException ignored) { }
		}
		
		throw new EssentialsCommandException("Unknown time input format '%s'!", stringTime);
	}
	
	public static String formatTimestamp(Timestamp timestamp) {
		if(timestamp.toString().equals(Timestamp.valueOf("2020-02-02 02:02:02").toString())) {
			return "forever";
		}
		
		SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm a");
		return format.format(new Date(timestamp.getTime()));
	}
}
