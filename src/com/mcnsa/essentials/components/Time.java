package com.mcnsa.essentials.components;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.Logger;
import com.mcnsa.essentials.utilities.PlayerSelector;
import com.mcnsa.essentials.utilities.DateUtils;

@ComponentInfo(friendlyName = "Time",
				description = "Allows you to query and set the world time",
				permsSettingsPrefix = "time")
public class Time {
	@Setting(node = "dawn-offset-hours") public static long dawnOffsetHours = 8;
	@Setting(node = "broadcast-time-changes") public static boolean broadcastTimeChanges = true;
	@Setting(node = "broadcast-only-in-affected-world") public static boolean broadcastOnlyInAffectedWorld = true;
	
	@Command(command = "time",
			aliases = {"thetime"},
			description = "tells you the current time in your world",
			permissions = {"tell"})
	public static boolean theTime(CommandSender sender) throws EssentialsCommandException {
		World world = (sender instanceof Player) ? ((Player)sender).getWorld() : Bukkit.getServer().getWorlds().get(0);
		return theTime(sender, world.getName());
	}

	@Command(command = "time",
			aliases = {"thetime"},
			description = "tells you the current time in the given world",
			permissions = {"tell"})
	public static boolean theTime(CommandSender sender, String worldName) throws EssentialsCommandException {
		// get the world
		World world = Bukkit.getServer().getWorld(worldName);
		if(world == null) {
			throw new EssentialsCommandException("I couldn't find world '%s'!", worldName);
		}
		
		// tell them the time!
		ColourHandler.sendMessage(sender, "&dThe current time in world '" + world.getName() + "' is: &f" + DateUtils.formatMinecraftTime(world.getTime()));
		
		return true;
	}

	@Command(command = "settime",
			description = "sets the current time in your current world",
			permissions = {"set"},
			playerOnly = true)
	public static boolean setTime(CommandSender sender, String newTime) throws EssentialsCommandException {
		return setTime(sender, ((Player)sender).getWorld().getName(), newTime);
	}

	@Command(command = "settime",
			arguments = {"world"},
			tabCompletions = {TabCompleteType.WORLD},
			description = "sets the current time in the given world",
			permissions = {"set"})
	public static boolean setTime(CommandSender sender, String worldName, String newTime) throws EssentialsCommandException {
		// get the world
		World targetWorld = Bukkit.getServer().getWorld(worldName);
		if(targetWorld == null) {
			throw new EssentialsCommandException("I don't know what universe you're living in, but this one doesn't have the world '%s' in it!", worldName);
		}
		
		// ok, parse the time string
		long time = DateUtils.parseMinecraftTime(newTime);
		
		// and set it!
		targetWorld.setTime(time);
		
		// get a formatted new time
		newTime = DateUtils.formatMinecraftTime(time);
		
		// alert!
		if(broadcastTimeChanges) {
			// get our affected players
			ArrayList<Player> targetPlayers = null;
			if(broadcastOnlyInAffectedWorld) {
				targetPlayers = PlayerSelector.selectPlayersExact(String.format("world:%s;*", targetWorld.getName()));
			}
			else {
				targetPlayers = PlayerSelector.selectPlayersExact("*");
			}
			
			// broadcast it!
			for(Player targetPlayer: targetPlayers) {
				ColourHandler.sendMessage(targetPlayer, "&6The time in world '%s' has been changed to: %s by %s",
						targetWorld.getName(),
						newTime,
						sender.getName());
			}
		}
		
		// and log it
		Logger.log("&6The time in world '%s' has been changed to: %s by %s",
				targetWorld.getName(),
				newTime,
				sender.getName());
		ColourHandler.sendMessage(sender, "&6You have changed the time in world '%s' to %s!",
				targetWorld.getName(),
				newTime);
		
		return true;
	}
}
