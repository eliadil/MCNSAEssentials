package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "PlayerMode",
				description = "Enables changing player modes (and enabling god mode)",
				permsSettingsPrefix = "playermode")
public class PlayerMode implements Listener {
	public PlayerMode() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// utility function to determine if someone has godmode or not
	private static boolean hasGodMode(Player player) {
		// if any of our metadata values come back as true,
		// we have god mode on
		for(MetadataValue val: player.getMetadata("godMode")) {
			if(val.asBoolean()) {
				return true;
			}
		}
		
		// guess not!
		return false;
	}
	
	// handle all bukkit events relating to taking damage
	@EventHandler(ignoreCancelled = true)
	public void onCombust(EntityCombustEvent event) {		
		// make sure it's a player
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			
			// check if they have god mode
			if(hasGodMode(player)) {
				// cancel & extinguish
				event.setCancelled(true);
				player.setFireTicks(0);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		// make sure it's a player
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			
			// check if they have god mode
			if(hasGodMode(player)) {
				// cancel & extinguish
				event.setCancelled(true);
				player.setFireTicks(0);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		// make sure it's a player
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			
			// check if they have god mode and the food was dropping
			if(event.getFoodLevel() < player.getFoodLevel() && hasGodMode(player)) {
				// cancel
				event.setCancelled(true);
			}
		}
	}
	
	// also, prevent mobs from targeting us if godded
	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		// make sure it's a player
		if(event.getTarget() instanceof Player) {
			Player player = (Player)event.getTarget();
			
			// check if they have god mode
			if(hasGodMode(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	// our commands	
	@Command(command = "gamemode",
			aliases = {"gm"},
			arguments = {"mode name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "changes your game mode",
			permissions = {"gamemode.self"},
			playerOnly = true)
	public static boolean gameMode(CommandSender sender, String mode) throws EssentialsCommandException {
		return gameMode(sender, sender.getName(), mode);
	}

	@Command(command = "gamemode",
			aliases = {"gm"},
			arguments = {"target player[s]", "mode name"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING},
			description = "changes the game mode of the target player[s]",
			permissions = {"gamemode.others"})
	public static boolean gameMode(CommandSender sender, String targetPlayer, String mode) throws EssentialsCommandException {
		GameMode targetMode = null;
		if(mode.equalsIgnoreCase("survival") || mode.equalsIgnoreCase("s")) {
			targetMode = GameMode.SURVIVAL;
		}
		else if(mode.equalsIgnoreCase("creative") || mode.equalsIgnoreCase("c")) {
			targetMode = GameMode.CREATIVE;
		}
		else if(mode.equalsIgnoreCase("adventure") || mode.equalsIgnoreCase("a")) {
			targetMode = GameMode.ADVENTURE;
		}
		else {
			throw new EssentialsCommandException("I don't know the gamemode '%s'", targetMode);
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to change the gamemode of!", targetPlayer);
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// change their game mode
			target.setGameMode(targetMode);

			if(targetMode.equals(GameMode.SURVIVAL) || targetMode.equals(GameMode.ADVENTURE)) {
				// also, set their velocity to 0
				target.setVelocity(new Vector(0, 0, 0));
			}
			
			// and alert them!
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, "&6Your game mode has been changed to: " + targetMode.name());
			}
			else {
				ColourHandler.sendMessage(target, "&6Your game mode has been changed to: " + targetMode.name() + " by " + sender.getName());
				ColourHandler.sendMessage(sender, "&6" + target.getName() + "'s game mode has been changed to: " + targetMode.name());
			}
		}
		
		return true;
	}
	
	// god mode
	@Command(command = "god",
			description = "enables god mode on yourself",
			permissions = {"god.self"},
			playerOnly = true)
	public static boolean enableGodMode(CommandSender sender) throws EssentialsCommandException {
		return enableGodMode(sender, sender.getName());
	}
	
	@Command(command = "god",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "enables god mode on the target players",
			permissions = {"god.others"})
	public static boolean enableGodMode(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to enable god mode on!", targetPlayer);
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// only if they don't already have god mode enabled
			if(hasGodMode(target)) {
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&6You already have god mode enabled!");
				}
				else {
					ColourHandler.sendMessage(sender, "&6" + target.getName() + " already had god mode enabled!");
				}
				continue;
			}
			
			// enable the god metadata on them
			target.setMetadata("godMode", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
			
			// alert them
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, "&6God mode has been activated!");
			}
			else {
				ColourHandler.sendMessage(target, "&6Your god mode has been activated by " + sender.getName());
				ColourHandler.sendMessage(sender, "&6" + target.getName() + "'s god mode has been activated!");
			}
		}
		
		return true;
	}

	@Command(command = "ungod",
			description = "disables god mode on yourself",
			permissions = {"god.self"},
			playerOnly = true)
	public static boolean disableGodMode(CommandSender sender) throws EssentialsCommandException {
		return disableGodMode(sender, sender.getName());
	}
	
	@Command(command = "ungod",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "disables god mode on the target players",
			permissions = {"god.others"})
	public static boolean disableGodMode(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to disable god mode on!", targetPlayer);
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// only if they already have god mode enabled
			if(!hasGodMode(target)) {
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&6You didn't have god mode enabled anyway!");
				}
				else {
					ColourHandler.sendMessage(sender, "&6" + target.getName() + " didn't have god mode enabled!");
				}
				continue;
			}
			
			// enable the god metadata on them
			target.removeMetadata("godMode", MCNSAEssentials.getInstance());
			
			// also, set their velocity to 0
			target.setVelocity(new Vector(0, 0, 0));
			
			// alert them
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, "&6God mode has been deactivated!");
			}
			else {
				ColourHandler.sendMessage(target, "&6Your god mode has been deactivated by " + sender.getName());
				ColourHandler.sendMessage(sender, "&6" + target.getName() + "'s god mode has been deactivated!");
			}
		}
		
		return true;
	}
}
