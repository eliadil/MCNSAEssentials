package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
//import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;
import com.mcnsa.essentials.utilities.SoundUtils;

@ComponentInfo(friendlyName = "Freeze",
				description = "Allows mods to freeze players in their tracks",
				permsSettingsPrefix = "freeze")
public class Freeze implements Listener {
	@Setting(node = "allowed-commands") String[] allowedCommands = {
		"c"
	};
	
	public Freeze() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// utility function to determine if someone is frozen or not
	private static boolean isFrozen(Entity entity) {
		if(entity instanceof Player) {
			return isFrozen((Player)entity);
		}
		return false;
	}
	
	private static boolean isFrozen(Player player) {
		// if any of our metadata values come back as true,
		// we have god mode on
		for(MetadataValue val: player.getMetadata("frozen")) {
			if(val.asBoolean()) {
				return true;
			}
		}
		
		// guess not!
		return false;
	}

	// bukkit event handlers
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(BlockDamageEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(BlockPlaceEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(SignChangeEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(EntityCombustEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(EntityCreatePortalEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(EntityDamageEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(EntityInteractEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(EntityRegainHealthEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(EntityShootBowEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(EntityTameEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(FoodLevelChangeEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(SheepDyeWoolEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(HangingBreakByEntityEvent event) {
		if(isFrozen(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerBedEnterEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerBucketEmptyEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerBucketFillEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerCommandPreprocessEvent event) {
		if(isFrozen(event.getPlayer())) {
			// check the command
			for(String allowedCommand: allowedCommands) {
				String command = event.getMessage().substring(1).split("\\s")[0];
				if(command.equalsIgnoreCase(allowedCommand)) {
					return;
				}
			}
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerDropItemEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerEggThrowEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setHatching(false);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerExpChangeEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setAmount(0);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerFishEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerGameModeChangeEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerInteractEntityEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerInteractEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerMoveEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerPickupItemEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(PlayerShearEntityEvent event) {
		if(isFrozen(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(VehicleDamageEvent event) {
		if(isFrozen(event.getAttacker())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(VehicleDestroyEvent event) {
		if(isFrozen(event.getAttacker())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(VehicleEnterEvent event) {
		if(isFrozen(event.getEntered())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void checkFrozenEvent(VehicleExitEvent event) {
		if(isFrozen(event.getExited())) {
			event.setCancelled(true);
		}
	}
	
	// commands for controlling freezing
	@Command(command = "freeze",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "freezes the target players in their place",
			permissions = {"freeze"})
	public static boolean freeze(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to freeze!", targetPlayer);
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// only if they aren't already frozen
			if(isFrozen(target)) {
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&bYou are already frozen!");
				}
				else {
					ColourHandler.sendMessage(sender, "&b" + target.getName() + " was already frozen!");
				}
				continue;
			}
			
			// enable the frozen metadata on them
			target.setMetadata("frozen", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
			
			// play a sound
			SoundUtils.errorSound(target);
			
			// alert them
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, "&bYou froze yourself!");
			}
			else {
				ColourHandler.sendMessage(target, "&bYou have been frozen by %s! You may only talk until you are unfrozen!", sender.getName());
				ColourHandler.sendMessage(sender, "&b" + target.getName() + " has been frozen!");
			}
		}
		
		return true;
	}
	
	@Command(command = "unfreeze",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "unfreezes the target players in their place",
			permissions = {"unfreeze"})
	public static boolean unfreeze(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to unfreeze!", targetPlayer);
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// only if they aren't already frozen
			if(!isFrozen(target)) {
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&bYou weren't even frozen!");
				}
				else {
					ColourHandler.sendMessage(sender, "&b%s wasn't frozen anyway!", target.getName());
				}
				continue;
			}
			
			// remove the metadata
			target.removeMetadata("frozen", MCNSAEssentials.getInstance());
			
			// play a sound
			SoundUtils.confirmSound(target);
			
			// alert them
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, "&bYou unfroze yourself!");
			}
			else {
				ColourHandler.sendMessage(target, "&bYou have been unfrozen by %s!", sender.getName());
				ColourHandler.sendMessage(sender, "&b" + target.getName() + " has been unfrozen!");
			}
		}
		
		return true;
	}
}
