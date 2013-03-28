package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;
import com.mcnsa.essentials.utilities.SoundUtils;

@ComponentInfo(friendlyName = "Fun",
				description = "Some fun commands",
				permsSettingsPrefix = "fun")
public class Fun {
	@Setting(node = "broadcast") public static boolean broadcast = true;
	@Setting(node = "broadcastlimit") public static int broadcastLimit = 6;
	
	@Translation(node = "and-more-people") public static String andMorePeople = "&eand many more people...";
	
	@Translation(node = "hat.not-holding-anything") public static String notHoldingAnything = "You're not holding anything!";
	@Translation(node = "hat.hat-on-head") public static String hatOnHead = "&dMy, but you're looking fashionable!";	
	@Command(command = "hat",
			description = "puts whatever you're holding in your hand onto your head",
			permissions = {"hat"},
			playerOnly = true)
	public static boolean hat(CommandSender sender) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// get our currently held item
		ItemStack stack = player.getInventory().getItemInHand();
		
		// make sure it exists
		if(stack == null || stack.getAmount() == 0) {
			throw new EssentialsCommandException(notHoldingAnything);
		}
		
		// see if anything is currently on our head
		ItemStack stackOnHead = player.getInventory().getHelmet();
		
		// and swap!
		player.getInventory().setHelmet(stack);
		player.getInventory().setItemInHand(stackOnHead);
		
		// alert!
		ColourHandler.sendMessage(sender, hatOnHead);
		
		return true;
	}
	
	@Translation(node = "slap.couldnt-find-players") public static String couldntFindSlapTargets = "I couldn't find / parse target player[s] '%players%' to slap!";
	@Translation(node = "slap.slapped-self") public static String slappedSelf = "&eYou slapped yourself!";
	@Translation(node = "slap.slapped-by") public static String slappedBy = "&eYou were slapped by %slapper%!";
	@Translation(node = "slap.broadcasted-slap") public static String broadcastedSlap = "&e%slappee% was slapped by %slapper%!";
	@Command(command = "slap",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "slaps target player[s]",
			permissions = {"slap"})
	public static boolean slap(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException(couldntFindSlapTargets.replaceAll("%players%", targetPlayer));
		}
		
		// get our random number generator
		Random random = new Random();
		
		// loop through all target players
		int count = 0;
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// set their velocity
			target.setVelocity(new Vector(
					random.nextDouble() * 2.0 - 1,
					random.nextDouble(),
					random.nextDouble() * 2.0 - 1));
			
			// and alert them!
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, slappedSelf);
			}
			else {
				ColourHandler.sendMessage(target, slappedBy.replaceAll("%slapper%", sender.getName()));
			}
			
			// play a sound
			SoundUtils.playSound(target, Sound.FALL_SMALL, broadcast);
			
			// broadcast?
			if(broadcast && count < broadcastLimit) {
				Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
				for(int i = 0; i < onlinePlayers.length; i++) {
					if(!target.equals(onlinePlayers[i]) && !sender.equals(onlinePlayers[i])) {
						ColourHandler.sendMessage(onlinePlayers[i], broadcastedSlap.replaceAll("%slapper%", sender.getName()).replaceAll("%slappee%", target.getName()));
					}
				}
			}
			
			count++;
		}
		if(count >= broadcastLimit) {
			Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
			for(int i = 0; i < onlinePlayers.length; i++) {
				ColourHandler.sendMessage(onlinePlayers[i], andMorePeople);
			}
		}
		
		return true;
	}
	
	@Translation(node = "rocket.couldnt-find-players") public static String couldntFindRocketTargets = "I couldn't find / parse target player[s] '%players%' to rocket!";
	@Translation(node = "rocket.rocketed-self") public static String rocketedSelf = "&eYou rocketed yourself!";
	@Translation(node = "rocket.rocketed-by") public static String rocketedBy = "&eYou were rocketed by %rocketer%!";
	@Translation(node = "rocket.broadcasted-rocket") public static String broadcastedRocket = "&e%rocketee% was slapped by %rocketer%!";
	@Command(command = "rocket",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "rockets target player[s]",
			permissions = {"rocket"})
	public static boolean rocket(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException(couldntFindRocketTargets.replaceAll("%player%", targetPlayer));
		}
		
		// loop through all target players
		int count = 0;
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// set their velocity
			target.setVelocity(new Vector(0, 4, 0));
			
			// and alert them!
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, rocketedSelf);
			}
			else {
				ColourHandler.sendMessage(target, rocketedBy.replaceAll("%rocketer%", sender.getName()));
			}
			
			// play a sound
			SoundUtils.playSound(target, Sound.EXPLODE, broadcast);
			
			// broadcast?
			if(broadcast && count < broadcastLimit) {
				Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
				for(int i = 0; i < onlinePlayers.length; i++) {
					if(!target.equals(onlinePlayers[i]) && !sender.equals(onlinePlayers[i])) {
						ColourHandler.sendMessage(onlinePlayers[i], broadcastedRocket.replaceAll("%rocketer%", sender.getName()).replaceAll("%rocketee%", target.getName()));
					}
				}
			}
			
			count++;
		}
		if(count >= broadcastLimit) {
			Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
			for(int i = 0; i < onlinePlayers.length; i++) {
				ColourHandler.sendMessage(onlinePlayers[i], andMorePeople);
			}
		}
		
		return true;
	}
	
	@Translation(node = "immolate.couldnt-find-players") public static String couldntFindImmolateTargets = "I couldn't find / parse target player[s] '%players%' to immolate!";
	@Translation(node = "immolate.immolated-self") public static String immolatedSelf = "&eYou immolated yourself!";
	@Translation(node = "immolate.immolated-by") public static String immolatedBy = "&eYou were immolated by %immolater%!";
	@Translation(node = "immolate.broadcasted-immolate") public static String broadcastedImmolate = "&e%immolatee% was immolated by %immolater%!";
	@Command(command = "immolate",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "immolates target player[s]",
			permissions = {"immolate"})
	public static boolean immolate(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException(couldntFindImmolateTargets.replaceAll("%player%", targetPlayer));
		}
		
		// loop through all target players
		int count = 0;
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// get the player
			Player target = it.next();
			
			// light them on fire
			target.setFireTicks(240);
			
			// and alert them!
			if(sender.getName().equals(target.getName())) {
				ColourHandler.sendMessage(target, immolatedSelf);
			}
			else {
				ColourHandler.sendMessage(target, immolatedBy.replaceAll("%immolator%", sender.getName()));
			}
			
			// play a sound
			SoundUtils.playSound(target, Sound.FIRE, broadcast);
			
			// broadcast?
			if(broadcast && count < broadcastLimit) {
				Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
				for(int i = 0; i < onlinePlayers.length; i++) {
					if(!target.equals(onlinePlayers[i]) && !sender.equals(onlinePlayers[i])) {
						ColourHandler.sendMessage(onlinePlayers[i], broadcastedImmolate.replaceAll("%immolator%", sender.getName()).replaceAll("%immolatee%", target.getName()));
					}
				}
			}
			
			count++;
		}
		if(count >= broadcastLimit) {
			Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
			for(int i = 0; i < onlinePlayers.length; i++) {
				ColourHandler.sendMessage(onlinePlayers[i], andMorePeople);
			}
		}
		
		return true;
	}
	
	@Setting(node = "kitten-explode-timer-seconds") public static float kittenExplodeTimerSeconds = 1.5f;
	@Command(command = "kittycannon",
			description = "fire the kittycannon!",
			permissions = {"kittycannon"},
			playerOnly = true)
	public static boolean kittyCannon(CommandSender sender) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// spawn the cat
		final Ocelot cat = (Ocelot)player.getWorld().spawn(player.getEyeLocation(), EntityType.OCELOT.getEntityClass());
		// make sure it spawned
		if(cat == null) {
			throw new EssentialsCommandException("Failed to spawn kitty!");
		}
		
		// randomize the cat type
		Random random = new Random();
		cat.setCatType(Ocelot.Type.values()[random.nextInt(Ocelot.Type.values().length)]);
		
		// tame it and make it a kittem
		cat.setTamed(true);
		cat.setBaby();
		
		// launch it!
		cat.setVelocity(player.getEyeLocation().getDirection().multiply(2));
		
		// now explode the kitty in a few seconds!
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MCNSAEssentials.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						// get the kittens location
						Location kittyLocation = cat.getLocation();
						// remove the kitty
						cat.remove();
						// and make an explosion!
						kittyLocation.getWorld().createExplosion(kittyLocation, 0, false);
					}
		}, (long)(kittenExplodeTimerSeconds * 20.0f));
		
		return true;
	}
}
