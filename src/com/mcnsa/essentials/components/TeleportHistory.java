package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "TeleportHistory",
				description = "Allows you to return to where you were before you teleported",
				permsSettingsPrefix = "teleporthistory")
public class TeleportHistory implements Listener {
	public TeleportHistory() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// metadata utility functions
	private static boolean ignoringTeleport(Player player) {
		if(player.getMetadata("ignoreTP").size() != 1) {
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
	
	private static void setLastLocation(Player player, Location location) {
		player.setMetadata("tpHistory", new FixedMetadataValue(MCNSAEssentials.getInstance(), location));
	}
	
	private static void removeLastLocation(Player player) {
		player.removeMetadata("tpHistory", MCNSAEssentials.getInstance());
	}
	
	private static Location getLastLocation(Player player) throws EssentialsCommandException {
		if(player.getMetadata("tpHistory").size() != 1) {
			throw new EssentialsCommandException("tpHistory != 1");
		}
		MetadataValue mv = player.getMetadata("tpHistory").get(0);
		return (Location)mv.value();
	}
	
	// our bukkit event handlers
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(!ignoringTeleport(event.getPlayer())) {
			// set their last location
			setLastLocation(event.getPlayer(), event.getPlayer().getLocation());
			
			// and re-enable teleport logging
			ignoreTeleport(event.getPlayer(), false);
		}
	}
	
	@Command(command = "back",
			aliases = {"return"},
			description = "sends you back to where you were before you last teleported",
			permissions = {"back"},
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
		
		// remove it from the history book
		removeLastLocation(player);
		
		return true;
	}
}
