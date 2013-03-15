package com.mcnsa.essentials.utilities;

public class TimeFormat {
	public static String formatMinecraftTime(long time) {
		int hours = (int) Math.floor(time / 1000);
		int minutes = (int) ((time % 1000) / 1000.0 * 60);
		
		return new String(hours + ":" + minutes);
	}
}
