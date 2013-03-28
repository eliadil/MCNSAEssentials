package com.mcnsa.essentials.utilities;

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
	
	public static long parseTime(String stringTime) throws EssentialsCommandException {
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
		
		// couldn't find it?
		throw new EssentialsCommandException("Unknown time input format '%s'!", stringTime);
	}
}
