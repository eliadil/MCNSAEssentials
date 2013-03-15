package com.mcnsa.essentials.components;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.utilities.ColourHandler;

public class Information {
	public static String motd = "Hello, %name%, welcome to MCNSA!\nThe time is now %time% and you're in world '%world%'";
	public static String[] rules = {"Don't be a derp, you derp"};
	
	private static String formatTime(long time) {
		int hours = (int) Math.floor(time / 1000);
		int minutes = (int) ((time % 1000) / 1000.0 * 60);
		
		return new String(hours + ":" + minutes);
	}
	
	private static void sendMOTD(Player player) {
		String motd = new String(Information.motd);
		motd = motd.replaceAll("%name%", player.getName());
		motd = motd.replaceAll("%time%", formatTime(player.getWorld().getTime()));
		motd = motd.replaceAll("%world%", player.getWorld().getName());
		
		String[] lines = motd.split("\n");
		for(int i = 0; i < lines.length; i++) {
			ColourHandler.sendMessage(player, lines[i]);
		}
	}
	
	@Command(command = "motd",
			description = "tells you the message of the day!",
			playerOnly = true)
	public static boolean motd(CommandSender sender) {
		sendMOTD((Player)sender);
		return true;
	}
}
