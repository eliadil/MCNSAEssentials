package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

public class Teleport {
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"player"},
			description = "teleports you to <player>",
			permissions = { "teleport.self" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String targetPlayer) {
		// try to find the player
		Player target = Bukkit.getServer().getPlayer(targetPlayer);
		if(target == null) {
			ColourHandler.sendMessage(sender, "&cI couldn't find player `" + targetPlayer + "' to teleport to!");
			return false;
		}
		
		// do it!
		Player player = (Player)sender;
		player.teleport(target);
		ColourHandler.sendMessage(sender, "&6You have been teleported to " + target.getName());
		
		return true;
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"target player[s]", "destination player"},
			description = "teleports <target player[s]> to <destination player>",
			permissions = { "teleport.other" })
	public static boolean teleport(CommandSender sender, String targetPlayer, String destinationPlayer) {
		// try to find the destination player
		Player destination = Bukkit.getServer().getPlayer(destinationPlayer);
		if(destination == null) {
			ColourHandler.sendMessage(sender, "&cI couldn't find player `" + destination + "' to teleport to!");
			return false;
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			ColourHandler.sendMessage(sender, "&cI couldn't find / parse target player[s] `" + targetPlayer + "' to teleport!");
			return false;
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// do it!
			target.teleport(destination);
			
			// alert everyone
			ColourHandler.sendMessage(target, "&6You have been teleported to " + destination.getName() + " by " + sender.getName());
			if(!destination.getName().equals(sender.getName())) {
				ColourHandler.sendMessage(destination, "&6" + target.getName() + " has been teleported to you by " + sender.getName());
			}
			ColourHandler.sendMessage(sender, "&6" + target.getName() + " has been teleported to " + destination.getName());
		}
		
		return true;
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"x", "y", "z"},
			description = "teleports you to the given coordinates in the given world",
			permissions = { "teleport.selfcoords" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String worldName, float x, float y, float z) {
		// get the player
		Player player = (Player)sender;
		
		// make sure the world exists
		World targetWorld = Bukkit.getServer().getWorld(worldName);
		if(targetWorld == null) {
			ColourHandler.sendMessage(sender, "&cI couldn't find world `" + worldName + "' to teleport into!");
			return false;
		}
		
		// build a location
		Location destination = new Location(targetWorld, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
		
		// do it!
		player.teleport(destination);
		ColourHandler.sendMessage(sender, "&6You have been teleported to (" + destination.getBlockX() + ", " + destination.getBlockY() + ", " + destination.getBlockZ() + ") in world: " + destination.getWorld().getName());
		
		return true;
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"target player[s]", "world name", "x", "y", "z"},
			description = "teleports <player[s]> to the given coordinates in the given world",
			permissions = { "teleport.othercoords" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String targetPlayer, String worldName, float x, float y, float z) {
		// make sure the world exists
		World targetWorld = Bukkit.getServer().getWorld(worldName);
		if(targetWorld == null) {
			ColourHandler.sendMessage(sender, "&cI couldn't find world `" + worldName + "' to teleport into!");
			return false;
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			ColourHandler.sendMessage(sender, "&cI couldn't find / parse target player[s] `" + targetPlayer + "' to teleport!");
			return false;
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// build their destination
			Location destination = new Location(targetWorld, x, y, z, target.getLocation().getYaw(), target.getLocation().getPitch());
			
			// do it!
			target.teleport(destination);
			
			// alert everyone
			ColourHandler.sendMessage(target, "&6You have been teleported to (" + destination.getBlockX() + ", " + destination.getBlockY() + ", " + destination.getBlockZ() + ") in world: " + destination.getWorld().getName() + " by " + sender.getName());
			ColourHandler.sendMessage(target, "&6" + target.getName() + " has been teleported to (" + destination.getBlockX() + ", " + destination.getBlockY() + ", " + destination.getBlockZ() + ") in world: " + destination.getWorld().getName());
		}
		
		return true;
	}
}
