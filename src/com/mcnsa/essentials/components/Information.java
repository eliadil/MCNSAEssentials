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
import com.mcnsa.essentials.utilities.TimeFormat;

public class Information implements Listener {
	public static String motd = "Hello, %name%, welcome to MCNSA!\nThe time is now %time% and you're in world '%world%'";
	public static String[] rules = {"Don't be a derp, you derp"};
	
	public Information() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	private static void sendMOTD(CommandSender sender) {
		World world = (sender instanceof Player) ? ((Player)sender).getWorld() : Bukkit.getServer().getWorlds().get(0);
		String motd = new String(Information.motd);
		motd = motd.replaceAll("%name%", sender.getName());
		motd = motd.replaceAll("%time%", TimeFormat.formatMinecraftTime(world.getTime()));
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
			description = "tells you the message of the day!",
			permissions = "info.motd")
	public static boolean motd(CommandSender sender) {
		sendMOTD(sender);
		return true;
	}
	
	@Command(command = "rules",
			description = "lists the rules of the server",
			permissions = "info.rules")
	public static boolean rules(CommandSender sender) {
		sendRules(sender);
		return true;
	}
	
	@Command(command = "who",
			aliases = {"list"},
			description = "lists people who are on the server",
			permissions = "info.who")
	public static boolean who(CommandSender sender) {
		// get a list of all online players
		Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		String messageString = String.format("&6Current online players (&f%d&6): ", onlinePlayers.length);
		for(int i = 0; i < onlinePlayers.length; i++) {
			if(i != 0) {
				messageString += "&6, ";
			}
			messageString += "&f" + onlinePlayers[i].getName();
		}
		ColourHandler.sendMessage(sender, messageString);
		
		return true;
	}
}
