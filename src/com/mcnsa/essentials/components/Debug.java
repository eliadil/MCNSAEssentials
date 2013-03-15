package com.mcnsa.essentials.components;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.utilities.ColourHandler;

public class Debug {
	@Command(command = "ping",
			description = "pings the server to make sure it is still responding",
			permissions = {"debug.ping"})
	public static boolean Ping(CommandSender sender) {
		String[] replies = {
				"Pong!",
				"Yup, I'm still here!",
				"I heard " + sender.getName() + " likes boys!",
				"I said a hip hop, the hippie, the hippie, to the hip hip hop, you don't stop!"
		};
		ColourHandler.sendMessage(sender, "&e" + (new Random()).nextInt(replies.length));
		return true;
	}
	
	@Command(command = "tps",
			description = "tells you the current ticks per second (should be 20.0)",
			permissions = {"debug.tps"})
	public static boolean ticksPerSecond(CommandSender sender) {
		return(ticksPerSecond(sender, 5.0f));
	}
	
	@Command(command = "tps",
			arguments = {"test time"},
			description = "tells you the current ticks per second (should be 20.0)",
			permissions = {"tps"})
	// pretty much a copy of commandbook's TPS
	public static boolean ticksPerSecond(final CommandSender sender, final float testTime) {
		// alert them
		ColourHandler.sendMessage(sender, "&9Timing clock test for " + testTime + " seconds...");
		ColourHandler.sendMessage(sender, "&cDO NOT &9change the world time or do anything stressful during this test!");
		
		final World world = Bukkit.getServer().getWorlds().get(0);
		final int expectedTicks = (int)(20.0f * testTime);
		final long startTime = System.currentTimeMillis();
		final long startTicks = world.getFullTime();
		
		// now schedule the test
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MCNSAEssentials.getInstance(), new Runnable() {
			@Override
			public void run() {
				long nowTime = System.currentTimeMillis();
				long nowTicks = world.getFullTime();
				
				float elapsedSecs = (nowTime - startTime) / 1000.0f;
				int elapsedTicks = (int)(nowTicks - startTicks);
				
				float error = ((int)(((testTime - elapsedSecs) / elapsedSecs * 100) * 10)) / 10.0f;
				float clockRate = elapsedTicks / elapsedSecs;
				
				// make sure the bukkit scheduler is working properly
				if(expectedTicks != elapsedTicks) {
					ColourHandler.sendMessage(sender, "&eWARNING: Bukkit scheduler was innacurate; expected "
							+ expectedTicks + ", got " + elapsedTicks + " ticks");
				}
				
				// now see if we're accurate
				ColourHandler.sendMessage(sender, "&9Current ticks per second: &f" + clockRate + " &9(" + error + "% error)");
				if(Math.round(clockRate) == 20) {
					ColourHandler.sendMessage(sender, "&9Rating: &aEXCELLENT");
				}
				else {
					if(elapsedSecs > testTime) {
						// clock is behind
						if(clockRate < 5) {
							ColourHandler.sendMessage(sender, "&9Rating: &4RIDICULOUSLY BAD");
						}
						else if(clockRate < 10) {
							ColourHandler.sendMessage(sender, "&9Rating: &cVERY BAD");
						}
						else if(clockRate < 15) {
							ColourHandler.sendMessage(sender, "&9Rating: &6POOR");
						}
						else {
							ColourHandler.sendMessage(sender, "&9Rating: &eNOT BAD");
						}
					}
					else {
						// clock is ahead
						ColourHandler.sendMessage(sender, "&9Rating: &dWEIRD (clock is AHEAD)");
					}
				}
			}
		}, expectedTicks);
		
		return true;
	}
	
	@Command(command = "serverinfo",
			description = "tells you server information",
			permissions = {"debug.serverinfo"})
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
		ColourHandler.sendMessage(sender, "&eAvailable total memory: " + Math.floor(rt.maxMemory() / 1024.0 / 1024.0 / 1024.0) + " GB");
		ColourHandler.sendMessage(sender, "&eJVM allocated memory: " + Math.floor(rt.totalMemory() / 1024.0 / 1024.0 / 1024.0) + " GB");
		ColourHandler.sendMessage(sender, "&eFree allocated memory: " + Math.floor(rt.freeMemory() / 1024.0 / 1024.0 / 1024.0) + " GB");
		
		return true;
	}
}
