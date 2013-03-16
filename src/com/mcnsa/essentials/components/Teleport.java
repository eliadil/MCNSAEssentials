package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

public class Teleport {
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"player"},
			description = "teleports you to <player>",
			permissions = { "teleport.self" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		return teleport(sender, sender.getName(), targetPlayer);
	}
	
	@Command(command = "bring",
			arguments = {"target player[s]"},
			description = "teleports target player[s] to you",
			permissions = { "teleport.bring" },
			playerOnly = true)
	public static boolean bring(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		teleport(sender, targetPlayer, sender.getName());
		return true;
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"target player[s]", "destination player"},
			description = "teleports <target player[s]> to <destination player>",
			permissions = { "teleport.other" })
	public static boolean teleport(CommandSender sender, String targetPlayer, String destinationPlayer) throws EssentialsCommandException {
		// try to find the destination player
		Player destination = Bukkit.getServer().getPlayer(destinationPlayer);
		if(destination == null) {
			throw new EssentialsCommandException("&cI couldn't find player '%s' to teleport to!", destination);
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find target player[s] '%s' to teleport!", targetPlayer);
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
			description = "teleports you to the given coordinates in your current world",
			permissions = { "teleport.selfcoords" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, float x, float y, float z) throws EssentialsCommandException {
		return teleport(sender, sender.getName(), ((Player)sender).getWorld().getName(), x, y, z);
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"world", "x", "y", "z"},
			description = "teleports you to the given coordinates in the given world",
			permissions = { "teleport.selfcoords" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String worldName, float x, float y, float z) throws EssentialsCommandException {
		return teleport(sender, sender.getName(), worldName, x, y, z);
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"target player[s]", "world name", "x", "y", "z"},
			description = "teleports <player[s]> to the given coordinates in the given world",
			permissions = { "teleport.othercoords" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String targetPlayer, String worldName, float x, float y, float z) throws EssentialsCommandException {
		// make sure the world exists
		World targetWorld = Bukkit.getServer().getWorld(worldName);
		if(targetWorld == null) {
			throw new EssentialsCommandException("I couldn't find world '%s' to teleport to!", worldName);
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find target player[s] '%s' to teleport!", targetPlayer);
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
