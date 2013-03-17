package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.TimeFormat;

@ComponentInfo(friendlyName = "Time",
				description = "Allows you to query and set the world time",
				permsSettingsPrefix = "time")
public class Time {
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
		ColourHandler.sendMessage(sender, "&dThe current time in world '" + world.getName() + "' is: &f" + TimeFormat.formatMinecraftTime(world.getTime()));
		
		return true;
	}
}
