package com.mcnsa.essentials.runnables;

import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.mcnsa.essentials.utilities.ColourHandler;


public class TPSTimerTask implements Runnable {
	private CommandSender sender = null;
	private float expectedTime = 0;
	private World world = null;
	private int expectedTicks = 0;
	private long startTime = 0;
	private long startTicks = 0;
	
	public TPSTimerTask(CommandSender sender, float expectedTime, World world, int expectedTicks, long startTime, long startTicks) {
		this.sender = sender;
		this.expectedTime = expectedTime;
		this.world = world;
		this.expectedTicks = expectedTicks;
		this.startTime = startTime;
		this.startTicks = startTicks;
	}
	
	@Override
	public void run() {
		long nowTime = System.currentTimeMillis();
		long nowTicks = world.getFullTime();
		
		float elapsedSecs = (nowTime - startTime) / 1000.0f;
		int elapsedTicks = (int)(nowTicks - startTicks) + 1;
		
		float error = ((int)(((expectedTime - elapsedSecs) / elapsedSecs * 100) * 10)) / 10.0f;
		float clockRate = ((int)((elapsedTicks / elapsedSecs) * 10)) / 10.0f;
		
		// make sure the bukkit scheduler is working properly
		if(expectedTicks != elapsedTicks) {
			ColourHandler.sendMessage(sender, "&eWARNING: Bukkit scheduler was innacurate: expected "
					+ expectedTicks + ", got " + elapsedTicks + " ticks");
		}
		
		// now see if we're accurate
		ColourHandler.sendMessage(sender, "&9Current ticks per second: &f" + clockRate + " &9(" + error + "% error)");
		if(Math.round(clockRate) == 20) {
			ColourHandler.sendMessage(sender, "&9Rating: &aEXCELLENT");
		}
		else {
			if(elapsedSecs > expectedTime) {
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
}