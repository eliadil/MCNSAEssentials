package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.utilities.ColourHandler;

public class Spawn implements Listener {
	private static Location spawnLocation = null;
	
	// our constructor
	public Spawn() {
		// get our spawn location
		spawnLocation = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
		
		// and register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// internal utility function for changing the spawn
	private static void updateSpawn(Location spawnLocation) {
		Spawn.spawnLocation = spawnLocation;
		spawnLocation.getWorld().setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
	}
	
	// bukkit event handler on respawn
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(Spawn.spawnLocation);
	}
	
	// bukkit event handler on player join
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().hasPlayedBefore()) {
			event.getPlayer().teleport(spawnLocation);
		}
	}
	
	// bukkit event handler on player teleport
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		// we have to do this to force bukkit to teleport them to the correct spawn
		Location loc = event.getTo();
		if(event.isCancelled()) {
			return;
		}
		
		// change it to the proper spawn location
		if(loc.equals(loc.getWorld().getSpawnLocation())) {
			event.setTo(spawnLocation);
		}
	}
	
	@Command(command = "spawn",
			description = "takes you to spawn",
			permissions = {"spawn.set"},
			playerOnly = true)
	public static boolean spawn(CommandSender sender) {
		// grab our player
		Player player = (Player)sender;
		
		// and teleport them
		ColourHandler.sendMessage(sender, "&6Woosh!");
		return player.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation(), TeleportCause.COMMAND);
	}

	@Command(command = "setspawn",
			description = "sets the spawn at your current location",
			permissions = {"spawn.set"},
			playerOnly = true)
	public static boolean setSpawn(CommandSender sender) {
		// grab our player
		Player player = (Player)sender;
		
		// set the spawn to their location
		Location spawnLocation = player.getLocation();
		updateSpawn(spawnLocation);

		// report!
		ColourHandler.sendMessage(sender, "&aSpawn set to (" + spawnLocation.getBlockX()+ ", " + spawnLocation.getBlockY() + ", " + spawnLocation.getBlockZ() + ") in world: " + spawnLocation.getWorld().getName());
		
		return true;
	}

	@Command(command = "setspawn",
			description = "sets the spawn at the given coordinates in the default world",
			arguments = {"x", "y", "z"},
			permissions = {"spawn.set"})
	public static boolean setSpawn(CommandSender sender, float x, float y, float z) {
		return setSpawn(sender, Bukkit.getServer().getWorlds().get(0).getName(), x, y, z);
	}

	@Command(command = "setspawn",
			description = "sets the spawn at the given coordinates in the given world",
					arguments = {"world name", "x", "y", "z"},
			permissions = {"spawn.set"})
	public static boolean setSpawn(CommandSender sender, String world, float x, float y, float z) {
		Location spawnLocation = new Location(Bukkit.getServer().getWorld(world), x, y, z, 0, 0);
		updateSpawn(spawnLocation);
		
		// report!
		ColourHandler.sendMessage(sender, "&aSpawn set to (" + spawnLocation.getBlockX()+ ", " + spawnLocation.getBlockY() + ", " + spawnLocation.getBlockZ() + ") in world: " + spawnLocation.getWorld().getName());
		
		return true;
	}
}
