package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "Spawn",
				description = "Enables custom spawns",
				permsSettingsPrefix = "spawn")
public class Spawn implements Listener {
	@Setting(node = "world") public static String spawnWorld = "world";
	@Setting(node = "x") public static float spawnX = 0;
	@Setting(node = "y") public static float spawnY = 64;
	@Setting(node = "z") public static float spawnZ = 0;
	@Setting(node = "yaw") public static float spawnYaw = 0;
	@Setting(node = "pitch") public static float spawnPitch = 0;
	
	// our constructor
	public Spawn() {
		// and register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// internal utility function for changing the spawn
	private static void updateSpawn(Location spawnLocation) {
		spawnWorld = spawnLocation.getWorld().getName();
		spawnX = spawnLocation.getBlockX();
		spawnY = spawnLocation.getBlockY();
		spawnZ = spawnLocation.getBlockZ();
		spawnYaw = spawnLocation.getYaw();
		spawnPitch = spawnLocation.getPitch();
		spawnLocation.getWorld().setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
		
		// update the config
		MCNSAEssentials.getInstance().getConfig().set("spawn.world", spawnWorld);
		MCNSAEssentials.getInstance().getConfig().set("spawn.x", spawnX);
		MCNSAEssentials.getInstance().getConfig().set("spawn.y", spawnY);
		MCNSAEssentials.getInstance().getConfig().set("spawn.z", spawnZ);
		MCNSAEssentials.getInstance().getConfig().set("spawn.yaw", spawnYaw);
		MCNSAEssentials.getInstance().getConfig().set("spawn.pitch", spawnPitch);
		MCNSAEssentials.getInstance().saveConfig();
	}
	
	// bukkit event handler on respawn
	@EventHandler(priority = EventPriority.LOW)
	public void onRespawn(PlayerRespawnEvent event) {
		Location spawnLocation = new Location(
				Bukkit.getServer().getWorld(spawnWorld),
				spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
		event.setRespawnLocation(spawnLocation);
	}
	
	// bukkit event handler on player join
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().hasPlayedBefore()) {
			Location spawnLocation = new Location(
					Bukkit.getServer().getWorld(spawnWorld),
					spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
			event.getPlayer().teleport(spawnLocation);
		}
	}
	
	// bukkit event handler on player teleport
	@EventHandler(ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		// we have to do this to force bukkit to teleport them to the correct spawn
		Location loc = event.getTo();
		
		// change it to the proper spawn location
		if(loc.equals(loc.getWorld().getSpawnLocation())) {
			Location spawnLocation = new Location(
					Bukkit.getServer().getWorld(spawnWorld),
					spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
			event.setTo(spawnLocation);
		}
	}
	
	@Command(command = "spawn",
			description = "takes you to spawn",
			permissions = {"set"},
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
			permissions = {"set"},
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
			permissions = {"set"})
	public static boolean setSpawn(CommandSender sender, float x, float y, float z) {
		return setSpawn(sender, Bukkit.getServer().getWorlds().get(0).getName(), x, y, z);
	}

	@Command(command = "setspawn",
			description = "sets the spawn at the given coordinates in the given world",
					arguments = {"world name", "x", "y", "z"},
			permissions = {"set"})
	public static boolean setSpawn(CommandSender sender, String world, float x, float y, float z) {
		Location spawnLocation = new Location(Bukkit.getServer().getWorld(world), x, y, z, 0, 0);
		updateSpawn(spawnLocation);
		
		// report!
		ColourHandler.sendMessage(sender, "&aSpawn set to (" + spawnLocation.getBlockX()+ ", " + spawnLocation.getBlockY() + ", " + spawnLocation.getBlockZ() + ") in world: " + spawnLocation.getWorld().getName());
		
		return true;
	}
}
