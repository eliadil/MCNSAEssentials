package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.TimeFormat;

public class Time {
	@Command(command = "time",
			aliases = {"thetime"},
			description = "tells you the current time in your world",
			permissions = {"time.tell"})
	public static boolean theTime(CommandSender sender) {
		World world = (sender instanceof Player) ? ((Player)sender).getWorld() : Bukkit.getServer().getWorlds().get(0);
		return theTime(sender, world.getName());
	}

	@Command(command = "time",
			aliases = {"thetime"},
			description = "tells you the current time in the given world",
			permissions = {"time.tell"})
	public static boolean theTime(CommandSender sender, String worldName) {
		// get the world
		World world = Bukkit.getServer().getWorld(worldName);
		if(world == null) {
			ColourHandler.sendMessage(sender, "&cI couldn't find the world `" + worldName + "'!");
			return false;
		}
		
		// tell them the time!
		ColourHandler.sendMessage(sender, "&dThe current time in world `" + world.getName() + "' is: &f" + TimeFormat.formatMinecraftTime(world.getTime()));
		
		return true;
	}
}
