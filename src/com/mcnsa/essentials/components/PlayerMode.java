package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "PlayerMode",
				description = "Enables changing player modes",
				permsSettingsPrefix = "playermode")
public class PlayerMode implements Listener {
	static HashMap<String, PotionEffectType> potionEffectTypes = new HashMap<String, PotionEffectType>();
	
	public PlayerMode() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
		
		// build our effects list
		potionEffectTypes.clear();
		PotionEffectType[] types = PotionEffectType.values();
		for(PotionEffectType type: types) {
			if(type == null) {
				continue;
			}
			String key = type.getName().replaceAll("\\s+", "").replaceAll("_", "").toLowerCase();
			potionEffectTypes.put(key,  type);
		}
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
	
	// enable mods to fly
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(PermissionsManager.playerHasPermission(event.getPlayer(), "playermode.allowfly")) {
			event.getPlayer().setAllowFlight(true);
		}
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
		return gameMode(sender, targetPlayer, mode, "");
	}

	@Command(command = "gamemode",
			aliases = {"gm"},
			arguments = {"target player[s]", "mode name", "silent"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING, TabCompleteType.SILENT},
			description = "changes the game mode of the target player[s] (silently)",
			permissions = {"gamemode.others"})
	public static boolean gameMode(CommandSender sender, String targetPlayer, String mode, String silent) throws EssentialsCommandException {
		GameMode targetMode = null;
		if(mode.equalsIgnoreCase("survival") || mode.equalsIgnoreCase("s") || mode.equals("0")) {
			targetMode = GameMode.SURVIVAL;
		}
		else if(mode.equalsIgnoreCase("creative") || mode.equalsIgnoreCase("c") || mode.equals("1")) {
			targetMode = GameMode.CREATIVE;
		}
		else if(mode.equalsIgnoreCase("adventure") || mode.equalsIgnoreCase("a") || mode.equals("2")) {
			targetMode = GameMode.ADVENTURE;
		}
		else {
			throw new EssentialsCommandException("I don't know the gamemode '%s'", targetMode);
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
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
			if(!silent.equalsIgnoreCase("s") && !silent.equalsIgnoreCase("silent")) {
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&6Your game mode has been changed to: " + targetMode.name());
				}
				else {
					ColourHandler.sendMessage(target, "&6Your game mode has been changed to: " + targetMode.name() + " by " + sender.getName());
					ColourHandler.sendMessage(sender, "&6" + target.getName() + "'s game mode has been changed to: " + targetMode.name());
				}
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
		return enableGodMode(sender, targetPlayer, "");
	}
	
	@Command(command = "god",
			arguments = {"target player[s]", "silent"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.SILENT},
			description = "enables god mode on the target players (silently)",
			permissions = {"god.others"})
	public static boolean enableGodMode(CommandSender sender, String targetPlayer, String silent) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
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
				if(!silent.equalsIgnoreCase("s") && !silent.equalsIgnoreCase("silent")) {
					if(sender.getName().equals(target.getName())) {
						ColourHandler.sendMessage(target, "&6You already have god mode enabled!");
					}
					else {
						ColourHandler.sendMessage(sender, "&6" + target.getName() + " already had god mode enabled!");
					}
				}
				continue;
			}
			
			// enable the god metadata on them
			target.setMetadata("godMode", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
			
			// alert them
			if(!silent.equalsIgnoreCase("s") && !silent.equalsIgnoreCase("silent")) {
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&6God mode has been activated!");
				}
				else {
					ColourHandler.sendMessage(target, "&6Your god mode has been activated by " + sender.getName());
					ColourHandler.sendMessage(sender, "&6" + target.getName() + "'s god mode has been activated!");
				}
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
		return disableGodMode(sender, targetPlayer, "");
	}
	
	@Command(command = "ungod",
			arguments = {"target player[s]", "silent"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.SILENT},
			description = "disables god mode on the target players (silently)",
			permissions = {"god.others"})
	public static boolean disableGodMode(CommandSender sender, String targetPlayer, String silent) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
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
				if(!silent.equalsIgnoreCase("s") && !silent.equalsIgnoreCase("silent")) {
					if(sender.getName().equals(target.getName())) {
						ColourHandler.sendMessage(target, "&6You didn't have god mode enabled anyway!");
					}
					else {
						ColourHandler.sendMessage(sender, "&6" + target.getName() + " didn't have god mode enabled!");
					}
				}
				continue;
			}
			
			// enable the god metadata on them
			target.removeMetadata("godMode", MCNSAEssentials.getInstance());
			
			// also, set their velocity to 0
			target.setVelocity(new Vector(0, 0, 0));
			
			// alert them
			if(!silent.equalsIgnoreCase("s") && !silent.equalsIgnoreCase("silent")) {
				if(sender.getName().equals(target.getName())) {
					ColourHandler.sendMessage(target, "&6God mode has been deactivated!");
				}
				else {
					ColourHandler.sendMessage(target, "&6Your god mode has been deactivated by " + sender.getName());
					ColourHandler.sendMessage(sender, "&6" + target.getName() + "'s god mode has been deactivated!");
				}
			}
		}
		
		return true;
	}
	
	@Command(command = "effect",
			arguments = "effect",
			tabCompletions = {TabCompleteType.EFFECT},
			description = "gives yourself a potion effect",
			permissions = "effect.self",
			playerOnly = true)
	public static boolean effect(CommandSender sender, String effect) throws EssentialsCommandException {
		return effect(sender, sender.getName(), effect, 30, 1);
	}
	
	@Command(command = "effect",
			arguments = {"effect", "duration"},
			tabCompletions = {TabCompleteType.EFFECT, TabCompleteType.NUMBER},
			description = "gives yourself a potion effect",
			permissions = "effect.self",
			playerOnly = true)
	public static boolean effect(CommandSender sender, String effect, int duration) throws EssentialsCommandException {
		return effect(sender, sender.getName(), effect, duration, 1);
	}
	
	@Command(command = "effect",
			arguments = {"effect", "duration", "amplifier"},
			tabCompletions = {TabCompleteType.EFFECT, TabCompleteType.NUMBER, TabCompleteType.NUMBER},
			description = "gives yourself a potion effect",
			permissions = "effect.self",
			playerOnly = true)
	public static boolean effect(CommandSender sender, String effect, int duration, int amplifier) throws EssentialsCommandException {
		return effect(sender, sender.getName(), effect, duration, amplifier);
	}
	
	@Command(command = "effect",
			arguments = {"target player[s]", "effect"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.EFFECT},
			description = "gives yourself a potion effect",
			permissions = "effect.self",
			playerOnly = true)
	public static boolean effect(CommandSender sender, String targetPlayer, String effect) throws EssentialsCommandException {
		return effect(sender, targetPlayer, effect, 30, 1);
	}
	
	@Command(command = "effect",
			arguments = {"target player[s]", "effect", "duration"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.EFFECT, TabCompleteType.NUMBER},
			description = "gives yourself a potion effect",
			permissions = "effect.self",
			playerOnly = true)
	public static boolean effect(CommandSender sender, String targetPlayer, String effect, int duration) throws EssentialsCommandException {
		return effect(sender, targetPlayer, effect, duration, 1);
	}
	
	@Command(command = "effect",
			arguments = {"target player[s]", "effect", "duration", "amplifier"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.EFFECT, TabCompleteType.NUMBER, TabCompleteType.NUMBER},
			description = "gives the target players a potion effect",
			permissions = "effect.others")
	public static boolean effect(CommandSender sender, String targetPlayer, String effect, int duration, int amplifier) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to apply the effect to!", targetPlayer);
		}
		
		// try to find our effect type
		if(!potionEffectTypes.containsKey(effect.toLowerCase())) {
			throw new EssentialsCommandException("I don't understand the effect type '%s'!", effect);
		}
		
		// adjust our amplifier
		amplifier--;
		if(amplifier < 0) {
			amplifier = 0;
		}
		
		// create a potion effect from our parameters
		PotionEffect potionEffect = new PotionEffect(potionEffectTypes.get(effect.toLowerCase()), duration * 20, amplifier);
		
		// loop through all target players
		for(Player target: targetPlayers) {
			target.addPotionEffect(potionEffect, true);
			
			if(target.getName() != sender.getName()) {
				ColourHandler.sendMessage(target,
						"&3%s gave you %s%s for %ds!",
						sender.getName(),
						effect.substring(0, 1).toUpperCase() + effect.substring(1).toLowerCase(),
						amplifier == 0 ? "" : " " + String.valueOf(amplifier + 1),
						duration);
			}
			ColourHandler.sendMessage(sender,
					"&3%s%s applied to %s for %ds!",
					effect.substring(0, 1).toUpperCase() + effect.substring(1).toLowerCase(),
					amplifier == 0 ? "" : " " + String.valueOf(amplifier + 1),
					target.getName(),
					duration);
		}
		
		return true;
	}
	
	@Command(command = "effects",
			description = "lists all possible potion effects",
			permissions = "effect.list")
	public static boolean effects(CommandSender sender) throws EssentialsCommandException {
		StringBuilder sb = new StringBuilder();
		for(String effect: potionEffectTypes.keySet()) {
			if(sb.length() != 0) {
				sb.append("&3, ");
			}
			sb.append("&f").append(effect.substring(0, 1).toUpperCase()).append(effect.substring(1));
		}
		ColourHandler.sendMessage(sender, "&3Effect types: " + sb.toString());
		
		return true;
	}
}
