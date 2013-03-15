package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.utilities.ColourHandler;

public class Information implements Listener {
	public static String motd = "Hello, %name%, welcome to MCNSA!\nThe time is now %time% and you're in world '%world%'";
	public static String[] rules = {"Don't be a derp, you derp"};
	
	public Information() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	private static String formatTime(long time) {
		int hours = (int) Math.floor(time / 1000);
		int minutes = (int) ((time % 1000) / 1000.0 * 60);
		
		return new String(hours + ":" + minutes);
	}
	
	private static void sendMOTD(CommandSender sender) {
		World world = (sender instanceof Player) ? ((Player)sender).getWorld() : Bukkit.getServer().getWorlds().get(0);
		String motd = new String(Information.motd);
		motd = motd.replaceAll("%name%", sender.getName());
		motd = motd.replaceAll("%time%", formatTime(world.getTime()));
		motd = motd.replaceAll("%world%", world.getName());
		
		String[] lines = motd.split("\n");
		for(int i = 0; i < lines.length; i++) {
			ColourHandler.sendMessage(sender, lines[i]);
		}
	}
	
	private static void sendRules(CommandSender sender) {
		ColourHandler.sendMessage(sender, "&e--- &6RULES &e---");
		for(int i = 0; i < rules.length; i++) {
			ColourHandler.sendMessage(sender, "&6" + (i+1) + ". " + rules[i]);
		}
	}
	
	// bukkit event handler on player join
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		sendMOTD(event.getPlayer());
	}
	
	// our commands
	@Command(command = "motd",
			description = "tells you the message of the day!")
	public static boolean motd(CommandSender sender) {
		sendMOTD(sender);
		return true;
	}
	
	@Command(command = "rules",
			description = "lists the rules of the server")
	public static boolean rules(CommandSender sender) {
		sendRules(sender);
		return true;
	}
}
