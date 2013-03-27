package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Teleport",
				description = "Various teleportation commands",
				permsSettingsPrefix = "teleport")
public class Teleport implements Listener {
	@Setting(node = "max-teleport-history") public static int MAX_TELEPORT_HISTORY = 5;
	
	public Teleport() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// metadata utility functions
	private static boolean ignoringTeleport(Player player) {
		if(!player.hasMetadata("ignoreTP") || player.getMetadata("ignoreTP").size() != 1) {
			return false;
		}
		return player.getMetadata("ignoreTP").get(0).asBoolean();
	}
	
	private static void ignoreTeleport(Player player, boolean ignore) {
		if(ignore) {
			player.setMetadata("ignoreTP", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
		}
		else {
			player.removeMetadata("ignoreTP", MCNSAEssentials.getInstance());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void addTeleportHistory(Player player, Location location) throws EssentialsCommandException {
		LinkedList<Location> history = null;
		if(player.hasMetadata("tpHistory")) {
			if(player.getMetadata("tpHistory").get(0).value() instanceof LinkedList<?>) {
				history = (LinkedList<Location>)player.getMetadata("tpHistory").get(0).value();
			}
			else {
				throw new EssentialsCommandException("Error: something went horribly wrong!");
			}
		}
		else {
			history = new LinkedList<Location>();
		}
		
		// update their history
		history.add(location);
		
		// make sure it doesn't get too big
		if(history.size() > MAX_TELEPORT_HISTORY) {
			history.remove();
		}
		
		// update their metadata
		player.setMetadata("tpHistory", new FixedMetadataValue(MCNSAEssentials.getInstance(), history));
	}
	
	@SuppressWarnings("unchecked")
	private static Location getLastLocation(Player player) throws EssentialsCommandException {
		LinkedList<Location> history = null;
		if(player.hasMetadata("tpHistory")) {
			if(player.getMetadata("tpHistory").get(0).value() instanceof LinkedList<?>) {
				history = (LinkedList<Location>)player.getMetadata("tpHistory").get(0).value();
			}
			else {
				throw new EssentialsCommandException("Error: something went horribly wrong!");
			}
		}
		else {
			throw new EssentialsCommandException("You don't have anywhere to go!");
		}
		
		// make sure we have somewhere to go
		if(history.size() == 0) {
			player.removeMetadata("tpHistory", MCNSAEssentials.getInstance());
			throw new EssentialsCommandException("You don't have anywhere to go!");
		}
		
		// get our last TP location, popping it off
		Location lastLocation = history.removeLast();
		
		// update their metadata
		player.setMetadata("tpHistory", new FixedMetadataValue(MCNSAEssentials.getInstance(), history));
		
		// and return
		return lastLocation;
	}
	
	@SuppressWarnings("unchecked")
	private static Location getFirstLocationAndClear(Player player) throws EssentialsCommandException {
		LinkedList<Location> history = null;
		if(player.hasMetadata("tpHistory")) {
			if(player.getMetadata("tpHistory").get(0).value() instanceof LinkedList<?>) {
				history = (LinkedList<Location>)player.getMetadata("tpHistory").get(0).value();
			}
			else {
				throw new EssentialsCommandException("Error: something went horribly wrong!");
			}
		}
		else {
			throw new EssentialsCommandException("You don't have anywhere to go!");
		}
		
		// make sure we have somewhere to go
		if(history.size() == 0) {
			player.removeMetadata("tpHistory", MCNSAEssentials.getInstance());
			throw new EssentialsCommandException("You don't have anywhere to go!");
		}
		
		// get our last TP location, popping it off
		Location firstLocation = history.remove();
		
		// update their metadata
		player.removeMetadata("tpHistory", MCNSAEssentials.getInstance());
		
		// and return
		return firstLocation;
	}
	
	private static void clearHistory(Player player) {
		player.removeMetadata("tpHistory", MCNSAEssentials.getInstance());
	}
	
	// our bukkit event handlers
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) throws EssentialsCommandException {
		if(!ignoringTeleport(event.getPlayer())) {
			// set their last location
			addTeleportHistory(event.getPlayer(), event.getPlayer().getLocation());
		}
		else {
			// and re-enable teleport logging
			ignoreTeleport(event.getPlayer(), false);
		}
		
		// disable fall distance
		event.getPlayer().setFallDistance(0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) throws EssentialsCommandException {
		// set their last location
		addTeleportHistory(event.getPlayer(), event.getPlayer().getLocation());
	}
	
	// our commands
	@Command(command = "back",
			aliases = {"return", "tpback"},
			description = "sends you back to where you were before you last teleported",
			permissions = {"history.back"},
			playerOnly = true)
	public static boolean back(CommandSender sender) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// make sure we have somewhere else to go
		if(!player.hasMetadata("tpHistory")) {
			throw new EssentialsCommandException("You don't have anywhere to go!");
		}
		
		// get their last location
		Location lastLocation = getLastLocation(player);
		
		// disable teleport logging
		ignoreTeleport(player, true);
		
		// and send them there
		player.teleport(lastLocation);
		ColourHandler.sendMessage(player, "&6!hsooW");
		
		return true;
	}
	
	@Command(command = "origin",
			aliases = {"tporigin"},
			description = "sends you back to where you were before you started teleporting",
			permissions = {"history.origin"},
			playerOnly = true)
	public static boolean origin(CommandSender sender) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// make sure we have somewhere else to go
		if(!player.hasMetadata("tpHistory")) {
			throw new EssentialsCommandException("You don't have anywhere to go!");
		}
		
		// get their last location
		Location lastLocation = getFirstLocationAndClear(player);
		
		// disable teleport logging
		ignoreTeleport(player, true);
		
		// and send them there
		player.teleport(lastLocation);
		ColourHandler.sendMessage(player, "&6!hsooW");
		
		return true;
	}
	
	@Command(command = "tpclear",
			aliases = {"rewind"},
			description = "clears your teleport history",
			permissions = {"history.clear"},
			playerOnly = true)
	public static boolean clearHistory(CommandSender sender) throws EssentialsCommandException {
		clearHistory((Player)sender);
		ColourHandler.sendMessage(sender, "&6Your teleport history has been wiped!");
		return true;
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"player"},
			description = "teleports you to <player>",
			permissions = { "self" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		return teleport(sender, sender.getName(), /*"to", */targetPlayer);
	}
	
	@Command(command = "bring",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "teleports target player[s] to you",
			permissions = { "teleport.bring" },
			playerOnly = true)
	public static boolean bring(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		teleport(sender, targetPlayer, /*"to", */sender.getName());
		return true;
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"target player[s]", "destination player"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.PLAYER},
			description = "teleports <target player[s]> to <destination player>",
			permissions = { "other" })
	public static boolean teleport(CommandSender sender, String targetPlayer, /*String to, */String destinationPlayer) throws EssentialsCommandException {
		// ridddles request
		// match our 'to'
		/*if(!to.equalsIgnoreCase("to")) {
			throw new EssentialsCommandException("%s should be 'to'!", to);
		}*/
		
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
			tabCompletions = {TabCompleteType.NUMBER, TabCompleteType.NUMBER, TabCompleteType.NUMBER},
			description = "teleports you to the given coordinates in your current world",
			permissions = { "selfcoords" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, float x, float y, float z) throws EssentialsCommandException {
		return teleport(sender, sender.getName(), ((Player)sender).getWorld().getName(), x, y, z);
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"world", "x", "y", "z"},
			tabCompletions = {TabCompleteType.WORLD, TabCompleteType.NUMBER, TabCompleteType.NUMBER, TabCompleteType.NUMBER},
			description = "teleports you to the given coordinates in the given world",
			permissions = { "selfcoords" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String worldName, float x, float y, float z) throws EssentialsCommandException {
		return teleport(sender, sender.getName(), worldName, x, y, z);
	}
	
	@Command(command = "tp",
			aliases = {"teleport"},
			arguments = {"target player[s]", "world name", "x", "y", "z"},
					tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.WORLD, TabCompleteType.NUMBER, TabCompleteType.NUMBER, TabCompleteType.NUMBER},
			description = "teleports <player[s]> to the given coordinates in the given world",
			permissions = { "othercoords" },
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
