package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.ItemSelector;

@ComponentInfo(friendlyName = "Mobs",
				description = "Utiltities for dealing with mobs",
				permsSettingsPrefix = "mobs")
public class Mobs {
	@Setting(node = "max-spawned-mobs") public static int MAX_SPAWNED_MOBS = 20;
	
	// taken from CommandBook
	// utility functions
	private static EntityType matchMobType(String name) throws EssentialsCommandException {
		EntityType match = null;
		
		StringBuilder sb = new StringBuilder();
		
		// loop over all entity types
		for(EntityType type: EntityType.values()) {
			// get it's class
			Class<?> clazz = type.getEntityClass();
			if(clazz == null) {
				continue;
			}
			
			// make sure it's a living entity that we can spawn
			if(!LivingEntity.class.isAssignableFrom(clazz) || !type.isSpawnable()) {
				continue;
			}
			
			// try to match the name straight up, regardless of _'s
			// (java enum matching)
			if(type.name().replace("_", "").equalsIgnoreCase(name.replace("_", ""))) {
				return type;
			}
			
			// (bukkit entity name matching)
			if(type.getName() != null) {
				if(type.getName().equalsIgnoreCase(name)) {
					return type;
				}
				
				// do a partial match
				if(type.getName().toLowerCase().startsWith(name.toLowerCase())) {
					match = type;
				}
				
				// build a list of valid mobs
				if(sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(type.getName());
			}
		}
		
		if(match != null) {
			return match;
		}
		
		throw new EssentialsCommandException("Unknown creature '%s'! Valid mobs: %s", name, sb.toString());
	}
	
	private static LivingEntity spawn(Location location, EntityType type, String special) throws EssentialsCommandException {
		// spawn our mob
		LivingEntity mob = (LivingEntity) location.getWorld().spawn(location, type.getEntityClass());
		
		// set special effects on them
		if(special.equals("")) {
			return mob;
		}
		if(mob instanceof Wolf) {
			if(special.contains("angry")) {
				((Wolf)mob).setAngry(true);
			}
			if(special.contains("sit") || special.contains("sitting")) {
				((Wolf)mob).setSitting(true);
			}
		}
		else if(mob instanceof Ocelot) {
			if(special.matches("black")) {
				((Ocelot)mob).setCatType(Ocelot.Type.BLACK_CAT);
			}
			else if(special.matches("red")) {
				((Ocelot)mob).setCatType(Ocelot.Type.RED_CAT);
			}
			else if(special.matches("siamese")) {
				((Ocelot)mob).setCatType(Ocelot.Type.SIAMESE_CAT);
			}
		}
		else if(mob instanceof Creeper) {
			if(special.matches("(power(ed)?|electric|lightning|shock(ed)?)")) {
				((Creeper)mob).setPowered(true);
			}
		}
		else if(mob instanceof Sheep) {
			if(special.matches("shear(ed)?")) {
				((Sheep)mob).setSheared(true);
			}
			((Sheep)mob).setColor(ItemSelector.matchDyeColour(special));
		}
		else if(mob instanceof Pig) {
			if(special.matches("saddle(d)?")) {
				((Pig)mob).setSaddle(true);
			}
		}
		else if(mob instanceof Slime) {
			try {
				((Slime)mob).setSize(Integer.parseInt(special));
			}
			catch(NumberFormatException e) {
				throw new EssentialsCommandException("Invalid slime size '%s'!", special);
			}
		}
		else if(mob instanceof Skeleton) {
			if(special.matches("wither")) {
				((Skeleton)mob).setSkeletonType(Skeleton.SkeletonType.WITHER);
			}
		}
		else if(mob instanceof PigZombie) {
			if(special.matches("angry")) {
				((PigZombie)mob).setAngry(true);
			}
		}
		else if(mob instanceof IronGolem) {
			if(special.matches("friendly")) {
				((IronGolem)mob).setPlayerCreated(true);
			}
		}
		else if(mob instanceof Villager) {
			Villager.Profession profession;
			try {
				profession = Villager.Profession.valueOf(special.toUpperCase());
			}
			catch(IllegalArgumentException e) {
				throw new EssentialsCommandException("Unknown profession '%s'!", special);
			}
			
			if(profession != null) {
				((Villager)mob).setProfession(profession);
			}
		}
		return mob;
	}
	
	// our commands
	@Command(command = "spawnmob",
			arguments = {"mob name:special"},
			tabCompletions = {TabCompleteType.MOB_NAME},
			description = "spawns the given mob at your location",
			permissions = {"spawn"},
			playerOnly = true)
	public static boolean spawn(CommandSender sender, String name) throws EssentialsCommandException {
		return spawn(sender, name, 1);
	}
	
	@Command(command = "spawnmob",
			arguments = {"mob name:special", "number"},
			tabCompletions = {TabCompleteType.MOB_NAME, TabCompleteType.NUMBER},
			description = "spawns the given number of mobs at your location",
			permissions = {"spawn"},
			playerOnly = true)
	public static boolean spawn(CommandSender sender, String name, int number) throws EssentialsCommandException {
		return spawn(sender, name, number, "");
	}
	
	@Command(command = "spawnmob",
			arguments = {"mob name:special", "number", "owner"},
			tabCompletions = {TabCompleteType.MOB_NAME, TabCompleteType.NUMBER, TabCompleteType.PLAYER},
			description = "spawns the given number of mobs at your location and sets their owner",
			permissions = {"spawn"},
			playerOnly = true)
	public static boolean spawn(CommandSender sender, String name, int number, String owner) throws EssentialsCommandException {
		// make sure our number is ok
		if(number < 0) {
			throw new EssentialsCommandException("Can't spawn negative amount of mobs!");
		}
		else if(number == 0) {
			return false;
		}
		else if(number > MAX_SPAWNED_MOBS) {
			throw new EssentialsCommandException("The maximum number of mobs you can spawn at once is %d!", MAX_SPAWNED_MOBS);
		}
		
		// parse special parts of the name
		String special = "";
		if(name.contains(":")) {
			String[] parts = name.split(":", 2);
			name = parts[0];
			special = parts[1];
		}
		
		// get our mob type
		EntityType type = matchMobType(name);
		
		// get our location
		Location location = ((Player)sender).getLocation();
		
		OfflinePlayer owningPlayer = null;
		if(!owner.equals("")) {
			owningPlayer = Bukkit.getServer().getPlayer(owner);
			if(owningPlayer == null) {
				//throw new EssentialsCommandException("Couldn't find player '%s' to make the owner!", owner);
				owningPlayer = Bukkit.getServer().getOfflinePlayer(owner);
				ColourHandler.sendMessage(sender, "&eWarning - player '%s' is offline and may not exist!", owningPlayer.getName());
			}
		}
		
		// now spawn them!
		String error = "";
		for(int i = 0; i < number; i++) {
			LivingEntity spawnedMob = spawn(location, type, special);
			
			// set their owner
			if(owningPlayer != null) {
				if(spawnedMob instanceof Tameable) {
					((Tameable)spawnedMob).setOwner(owningPlayer);
				}
				else {
					error = String.format("%s mobs aren't tameable!", spawnedMob.getType().getName());
				}
			}
		}
		if(!error.equals("")) {
			ColourHandler.sendMessage(sender, error);
		}
		
		return true;
	}
}
