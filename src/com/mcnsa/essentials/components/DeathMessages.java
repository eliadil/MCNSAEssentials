package com.mcnsa.essentials.components;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "DeathMessages",
				description = "Provides custom death messages",
				permsSettingsPrefix = "deathmessage")
public class DeathMessages implements Listener {
	private Random random = new Random();
	private HashMap<String, List<String>> deathMessages = new HashMap<String, List<String>>();

	private static File fileConfig = new File(MCNSAEssentials.getInstance().getDataFolder(), "deathmessages.yml");
	private static YamlConfiguration yamlConfig = new YamlConfiguration();
	public DeathMessages() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
		
		// extract a default config
		if(!fileConfig.exists()) {
			MCNSAEssentials.getInstance().saveResource("deathmessages.yml", false);
		}
		
		// load items from a config file
		try {
			yamlConfig.load(fileConfig);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// now load all our messages
		Set<String> keys = yamlConfig.getKeys(false);
		for(String key: keys) {
			List<String> messages = yamlConfig.getStringList(key);
			deathMessages.put(key, messages);
		}
	}
	
	// utility functions
	private DamageCause getLastDamageCause(Player player) throws EssentialsCommandException {
		if(!player.hasMetadata("lastDamageCause")) {
			throw new EssentialsCommandException("%s doesn't have a last damage cause", player.getName());
		}
		List<MetadataValue> mvl = player.getMetadata("lastDamageCause");
		if(mvl.size() != 1) {
			throw new EssentialsCommandException("Last Damage Cause .size != 1");
		}
		return (DamageCause)mvl.get(0).value();
	}
	
	private String getAttacker(Player player) {
		if(!player.hasMetadata("lastDamager")) {
			return "";
		}
		List<MetadataValue> mvl = player.getMetadata("lastDamager");
		if(mvl.size() != 1) {
			return "";
		}
		return mvl.get(0).asString();
	}
		
		private String getAttackerWielding(Player player) {
			if(!player.hasMetadata("lastDamagerWielding")) {
				return "";
			}
			List<MetadataValue> mvl = player.getMetadata("lastDamagerWielding");
			if(mvl.size() != 1) {
				return "";
			}
			return mvl.get(0).asString();
		}
	
	// event handlers
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		// make sure it's a player
		if(!(event.getEntity() instanceof Player)) {
			return;
		}
		
		// store their last damage cause
		Player player = (Player)event.getEntity();
		DamageCause cause = event.getCause();
		player.setMetadata("lastDamageCause", new FixedMetadataValue(MCNSAEssentials.getInstance(), cause));

		String damagerName = "unknown";
		if(event instanceof EntityDamageByEntityEvent) {
			// get our damager
			EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent)event;
			Entity damager = damageByEntityEvent.getDamager();
			
			// determine if it was a player or not
			if(damager instanceof Player) {
				// get the player's name
				Player damagingPlayer = (Player)damager;
				damagerName = damagingPlayer.getName();
				
				// and what they were wielding
				ItemStack held = damagingPlayer.getItemInHand();

				// and store it
				player.setMetadata("lastDamagerWielding",
						new FixedMetadataValue(MCNSAEssentials.getInstance(),
								held.getType().toString().replaceAll("_", " ").toLowerCase()));
			}
			else {
				// something else?
				damagerName = damager.getType().getName();
				if(damager instanceof LivingEntity) {
					// a mob
					LivingEntity mob = (LivingEntity)damager;
					
					// get it's equipment
					EntityEquipment equipment = mob.getEquipment();
					
					// and what it's holding
					ItemStack held = equipment.getItemInHand();
					
					// and store it
					player.setMetadata("lastDamagerWielding",
							new FixedMetadataValue(MCNSAEssentials.getInstance(),
									held.getType().toString().replaceAll("_", " ").toLowerCase()));
				}
				else if(damager instanceof Projectile) {
					// a projectile perchance?
					Projectile projectile = (Projectile)damager;
					damagerName = projectile.getShooter().getType().getName();
					
					// and store it
					player.setMetadata("lastDamagerWielding",
							new FixedMetadataValue(MCNSAEssentials.getInstance(),
									projectile.getType().toString().replaceAll("_", " ").toLowerCase()));
				}
			}
			
		}
		else if(event instanceof EntityDamageByBlockEvent) {
			// get our damager
			EntityDamageByBlockEvent damageByBlockEvent = (EntityDamageByBlockEvent)event;
			Block damager = damageByBlockEvent.getDamager();
			damagerName = damager.getType().toString().replaceAll("_", " ").toLowerCase();
		}
		player.setMetadata("lastDamager", new FixedMetadataValue(MCNSAEssentials.getInstance(), damagerName));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerDied(PlayerDeathEvent event) throws EssentialsCommandException {
		// get the player
		Player player = event.getEntity();
		
		// make sure they have the death metadata
		if(!player.hasMetadata("lastDamageCause")) {
			return;
		}
		
		// get their last damage cause
		DamageCause lastDamage = getLastDamageCause(player);
		
		// get an appropriate key to use
		// by default, just use the name
		String key = lastDamage.toString().replaceAll("_", "").toLowerCase();
		switch(lastDamage) {
		case CONTACT:
			key = "cactus";
			break;
			
		case ENTITY_ATTACK:
			String attacker = getAttacker(player).replaceAll("_", "").toLowerCase();
			if(deathMessages.containsKey(attacker)) {
				// must be a mob
				key = attacker;
			}
			else {
				// most likely a player
				key = "player";
			}
			break;
			
		case ENTITY_EXPLOSION:
			key = "creeper";
			break;
			
		case FIRE_TICK:
			key = "fire";
			break;
		}
		
		// and change it
		if(deathMessages.containsKey(key)) {
			event.setDeathMessage(ColourHandler.processColours(deathMessages.get(key).get(random.nextInt(deathMessages.get(key).size()))
					.replaceAll("%player%", player.getName())
					.replaceAll("%attacker%", getAttacker(player))
					.replaceAll("%wielding%", getAttackerWielding(player))));
		}
	}
}
