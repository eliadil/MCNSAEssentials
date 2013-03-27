package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;
import com.mcnsa.essentials.utilities.SoundUtility;
import com.mcnsa.essentials.utilities.SoundUtility.SoundType;

@ComponentInfo(friendlyName = "Vanish",
				description = "Allows players to vanish from sight",
				permsSettingsPrefix = "vanish")
public class Vanish implements Listener {
	@Setting(node = "smoke-on-vanish") public static boolean smokeOnVanish = true;
	
	public Vanish() {
		// register our event handlers
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// utility function to determine if someone is vanished or not
	private static boolean isVanished(Player player) {
		// if any of our metadata values come back as true,
		// we have god mode on
		for(MetadataValue val: player.getMetadata("vanished")) {
			if(val.asBoolean()) {
				return true;
			}
		}
		
		// guess not!
		return false;
	}
	
	// utility function to vanish / show a player
	private static void vanishShowPlayer(Player player, boolean doVanish) {
		// set their metadata
		if(doVanish) {
			player.setMetadata("vanished", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
		}
		else {
			player.removeMetadata("vanished", MCNSAEssentials.getInstance());
		}
		
		// now go through all players and vanish this player to them
		final Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		for(Player online: onlinePlayers) {
			// skip the player currently vanishing
			if(online.equals(player)) {
				continue;
			}
			
			if(doVanish) {
				// check permissions to see if we should hide from this player
				if(!PermissionsManager.playerHasPermission(player, "vanish.seeall")) {
					// nope, they can't see everyone!
					online.hidePlayer(player);
				}
			}
			else {
				// show them to the world!
				online.showPlayer(player);
			}
		}
		
		// now check if we should drop a smoke bomb at their location
		if(smokeOnVanish) {
			Random random = new Random();
			for(int i = 0; i < 10; i++) {
				player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, random.nextInt(9));
			}
		}
		
		// play a sound
		SoundUtility.playSound(player, SoundType.CONFIRM);
	}
	
	// bukkit listeners to hide us even better
	
	// prevent mobs from chasing us
	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		// make sure it's a player
		if(event.getTarget() instanceof Player) {
			Player player = (Player)event.getTarget();
			
			// check if they are vanished
			if(isVanished(player)) {
				event.setCancelled(true);
			}
		}
	}

	// deal with new players joining the server
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		// hide any hidden players from them
		// but only if they can't see everyone anyway
		if(PermissionsManager.playerHasPermission(player, "vanish.seeall")) {
			return;
		}
		// get all our online players that are vanished
		Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		for(Player online: onlinePlayers) {
			// and vanish vanished players to the logging-in player
			if(isVanished(online)) {
				player.hidePlayer(online);
			}
		}
	}
	
	// deal with opening inventory
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		// make sure the player is vanished before we do anything
		if(!isVanished(player)) {
			return;
		}
		
		// deal with right click
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			switch(event.getClickedBlock().getType()) {
			case CHEST:
				
				// cancel the opening thing
				event.setCancelled(true);
				
				// but actually open it
				final Chest chest = (Chest)event.getClickedBlock().getState();
				
				// create a copy of the chest
				final Inventory i = Bukkit.getServer().createInventory(event.getPlayer(), chest.getInventory().getSize());
				i.setContents(chest.getInventory().getContents());
				
				// and have the player open that
				event.getPlayer().openInventory(i);
				
				// alert them
				ColourHandler.sendMessage(player, "&eWARNING: you are opening a chest while vanished. You cannot edit it!");
				
				break;
				
			case ENDER_CHEST:
				// just open their ender chest
				event.setCancelled(true);
				player.openInventory(player.getEnderChest());
				break;
			}
		}
		// prevent trampling
		else if(event.getAction() == Action.PHYSICAL && event.getMaterial() == Material.SOIL) {
			event.setCancelled(true);
		}
	}
	
	// prevent vehicles from colliding with vanished players
	@EventHandler(ignoreCancelled = true)
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		// make sure it's a player who is vanished
		if(event.getEntity() instanceof Player && isVanished((Player)event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	// prevent vanished players from picking up stuff off the ground
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(isVanished(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	// our commands
	@Command(command = "vanish",
			aliases = {"poof"},
			description = "prevents players and mobs from seeing and interacting with you",
			permissions = {"self"},
			playerOnly = true)
	public static boolean vanish(CommandSender sender) throws EssentialsCommandException {
		return vanish(sender, sender.getName());
	}
	
	@Command(command = "vanish",
			aliases = {"poof"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "prevents players and mobs from seeing and interacting with the target player[s]",
			permissions = {"others"})
	public static boolean vanish(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find target player[s] '%s' to vanish!", targetPlayer);
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// make sure they're not already vanished
			if(isVanished(target)) {	
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&6You were already vanished!");
				}
				else {
					ColourHandler.sendMessage(sender, "&6%s was already vanished!", target.getName());
				}
				continue;
			}
			
			// vanish them
			vanishShowPlayer(target, true);
			
			// alert them		
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, "&6You have been vanished!");
			}
			else {
				ColourHandler.sendMessage(target, "&6You have been vanished by %s", sender.getName());
				ColourHandler.sendMessage(sender, "&6%s has been vanished!", target.getName());
			}
		}
		
		return true;
	}
	
	@Command(command = "unvanish",
			aliases = {"appear"},
			description = "unvanishes you",
			permissions = {"self"},
			playerOnly = true)
	public static boolean unvanish(CommandSender sender) throws EssentialsCommandException {
		return unvanish(sender, sender.getName());
	}
	
	@Command(command = "unvanish",
			aliases = {"appear"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "unvanishes target player[s]",
			permissions = {"others"})
	public static boolean unvanish(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find target player[s] '%s' to unvanish!", targetPlayer);
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// make sure they're already vanished
			if(!isVanished(target)) {	
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&6You weren't vanished!");
				}
				else {
					ColourHandler.sendMessage(sender, "&6%s wasn't vanished!", target.getName());
				}
				continue;
			}
			
			// vanish them
			vanishShowPlayer(target, false);
			
			// alert them		
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, "&6You have been appeared!");
			}
			else {
				ColourHandler.sendMessage(target, "&6You have been appeared by %s", sender.getName());
				ColourHandler.sendMessage(sender, "&6%s has been appeared!", target.getName());
			}
		}
		
		return true;
	}
}
