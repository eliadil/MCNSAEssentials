package com.mcnsa.essentials.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoundUtils {
	public enum SoundType {CONFIRM, CANCEL, ERROR, NOTIFICATION};
	
	public static void confirmSound(CommandSender sender) {
		playSound(sender, SoundType.CONFIRM);
	}
	
	public static void cancelSound(CommandSender sender) {
		playSound(sender, SoundType.CANCEL);
	}
	
	public static void errorSound(CommandSender sender) {
		playSound(sender, SoundType.ERROR);
	}
	
	public static void notifySound(CommandSender sender) {
		playSound(sender, SoundType.NOTIFICATION);
	}
	
	public static void playSound(CommandSender sender, SoundType type) {
		playSound(sender, type, false);
	}
	
	public static void playSound(CommandSender sender, SoundType type, boolean broadcast) {
		// get the sound
		Sound sound = null;
		switch(type) {
		case CONFIRM:
			sound = Sound.LEVEL_UP;
			break;
		case CANCEL:
			sound = Sound.ITEM_BREAK;
			break;
		case ERROR:
			sound = Sound.ZOMBIE_WOODBREAK;
			break;
		case NOTIFICATION:
			sound = Sound.WOLF_BARK;
			break;
		}
		
		playSound(sender, sound, broadcast);
	}
	
	public static void playSound(CommandSender sender, Sound sound, boolean broadcast) {
		// console can't make sounds!
		if(!(sender instanceof Player)) {
			return;
		}
		
		// get our player
		Player player = (Player)sender;
		
		// get our targets
		Player[] targets = new Player[]{player};
		if(broadcast) {
			targets = Bukkit.getServer().getOnlinePlayers();
		}
		
		for(Player online: targets) {
			online.playSound(online.getLocation(), sound, 1.0f, 1.0f);
		}
	}
}
