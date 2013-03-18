package com.mcnsa.essentials.components;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.runnables.TPSTimerTask;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "Debug",
				description = "Various server debugging utilities",
				permsSettingsPrefix = "debug")
public class Debug {
	
	@Command(command = "ping",
			description = "pings the server to make sure it is still responding",
			permissions = {"ping"})
	public static boolean Ping(CommandSender sender) {
		String[] replies = {
				"Pong!",
				"Yup, I'm still here!",
				"I heard " + sender.getName() + " likes boys!",
				"I said a hip hop, the hippie, the hippie, to the hip hip hop, you don't stop!"
		};
		ColourHandler.sendMessage(sender, "&e" + replies[(new Random()).nextInt(replies.length)]);
		return true;
	}
	
	@Command(command = "tps",
			description = "tells you the current ticks per second (should be 20.0)",
			permissions = {"tps"})
	public static boolean ticksPerSecond(CommandSender sender) {
		return(ticksPerSecond(sender, 5.0f));
	}
	
	@Command(command = "tps",
			arguments = {"test time"},
			description = "tells you the current ticks per second (should be 20.0)",
			permissions = {"tps"})
	// pretty much a copy of commandbook's TPS
	public static boolean ticksPerSecond(CommandSender sender, float testTime) {		
		// alert them
		ColourHandler.sendMessage(sender, "&9Timing clock test for " + testTime + " seconds...");
		ColourHandler.sendMessage(sender, "&cDO NOT &9change the world time or do anything stressful during this test!");
		
		World world = Bukkit.getServer().getWorlds().get(0);
		int expectedTicks = (int)(20.0f * testTime);
		long startTime = System.currentTimeMillis();
		long startTicks = world.getFullTime();
		
		TPSTimerTask task = new TPSTimerTask(sender, testTime, world, expectedTicks, startTime, startTicks);
		
		// now schedule the test
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MCNSAEssentials.getInstance(), task, expectedTicks);
		
		return true;
	}
	
	@Command(command = "serverinfo",
			description = "tells you server information",
			permissions = {"serverinfo"})
	public static boolean serverInfo(CommandSender sender) {
		// get the current runtime data
		Runtime rt = Runtime.getRuntime();
		
		// send out some info!
		ColourHandler.sendMessage(sender, String.format("&eSystem: %s %s (%s)",
				System.getProperty("os.name"),
				System.getProperty("os.version"),
				System.getProperty("os.arch")));
		ColourHandler.sendMessage(sender, String.format("&eJava: %s %s (%s)",
				System.getProperty("java.vendor"),
				System.getProperty("java.version"),
				System.getProperty("java.vendor.url")));
		ColourHandler.sendMessage(sender, String.format("&eJVM: %s %s %s",
				System.getProperty("java.vm.vendor"),
				System.getProperty("java.vm.name"),
				System.getProperty("java.vm.version")));
		ColourHandler.sendMessage(sender, "&eAvailable processors: " + rt.availableProcessors());
		ColourHandler.sendMessage(sender, "&eAvailable total memory: " + Math.floor(rt.maxMemory() / 1024.0 / 1024.0) + " MB");
		ColourHandler.sendMessage(sender, "&eJVM allocated memory: " + Math.floor(rt.totalMemory() / 1024.0 / 1024.0) + " MB");
		ColourHandler.sendMessage(sender, "&eFree allocated memory: " + Math.floor(rt.freeMemory() / 1024.0 / 1024.0) + " MB");
		
		return true;
	}
	
	private static String[] metaKeys = {
		"godMode",
		"vanished",
		"mlEnabled",
		"mlOnDone",
		"mlText",
		"mlArgs"
	};
	@Command(command = "resetmeta",
			description = "resets all metadata associated with MCNSAEssentials",
			permissions = {"resetmeta"})
	public static boolean resetMetaData(CommandSender sender) {
		// get all online players
		Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		
		// loop through all players
		for(int i = 0; i < onlinePlayers.length; i++) {
			// remove all associated metadata keys
			for(String metaKey: metaKeys) {
				if(onlinePlayers[i].hasMetadata(metaKey)) {
					onlinePlayers[i].removeMetadata(metaKey, MCNSAEssentials.getInstance());
				}
			}
		}
		
		ColourHandler.sendMessage(sender, "&aAll metadata valuess cleared!");
		
		return true;
	}
}
