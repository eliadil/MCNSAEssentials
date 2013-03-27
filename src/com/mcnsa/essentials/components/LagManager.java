package com.mcnsa.essentials.components;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "LagManager",
				description = "A tool for dealing with server lag",
				permsSettingsPrefix = "lagmanager")
public class LagManager {
	@Setting(node = "find.default-radius") public static float defaultRadius = 100;
	@Setting(node = "find.quick") public static boolean quickFind = true;

	@Command(command = "findlag",
			description = "tries to find the region with the most loaded entities",
			permissions = {"find"})
	public static boolean findLag(CommandSender sender) throws EssentialsCommandException {
		return findLag(sender, defaultRadius);
	}

	@Command(command = "findlag",
			arguments = {"world name"},
			tabCompletions = {TabCompleteType.WORLD},
			description = "tries to find the region with the most loaded entities in the given world",
			permissions = {"find"})
	public static boolean findLag(CommandSender sender, String worldName) throws EssentialsCommandException {
		return findLag(sender, worldName, defaultRadius);
	}

	@Command(command = "findlag",
			arguments = {"entity radius"},
			tabCompletions = {TabCompleteType.NUMBER},
			description = "tries to find the region with the most loaded entities using the given radius",
			permissions = {"find"})
	public static boolean findLag(CommandSender sender, float radius) throws EssentialsCommandException {
		// get a default world to search in
		World world = Bukkit.getServer().getWorlds().get(0);
		if(sender instanceof Player) {
			world = ((Player)sender).getWorld();
		}
		return findLag(sender, world.getName(), radius);
	}

	@Translation(node = "find.world-not-found") public static String worldNotFound = "I couldn't find the world '%world%'!";
	@Translation(node = "find.no-entities") public static String noEntities = "There aren't any entities to cause lag in world '%world%'!";
	@Translation(node = "find.starting") public static String startingFind = "&aBeginning search, this may take a while...";
	@Translation(node = "find.results") public static String findResults = 
			"&3Found maximum cluster of &f%totalEntities%&3 entities at (&f%x%&3, &f%y%&3, &f%z%&3) in a &f%radius%&3-block radius in world '&f%world%&3'!" +
			" (&f%numMobs%&3 mobs, &f%numItems%&3 items)";
	@Command(command = "findlag",
			arguments = {"world name", "entity radius"},
			tabCompletions = {TabCompleteType.WORLD, TabCompleteType.NUMBER},
			description = "tries to find the region with the most loaded entities using the given radius in the given world",
			permissions = {"find"})
	public static boolean findLag(CommandSender sender, String worldName, float radius) throws EssentialsCommandException {
		// find our world
		World world = Bukkit.getServer().getWorld(worldName);
		if(world == null) {
			throw new EssentialsCommandException(worldNotFound.replaceAll("%world%", worldName));
		}
		// get all the entities in our world
		List<Entity> entities = world.getEntities();
		if(entities.size() == 0) {
			throw new EssentialsCommandException(noEntities.replaceAll("%world%", worldName));
		}
		
		// go!
		ColourHandler.sendMessage(sender, startingFind);
		
		// keep track of globals
		int maxTotal = 0;
		int maxNumMobs = -1;
		int maxNumItems = -1;
		Location maxLocation = null;
		
		// loop over them all
		for(Entity entity: entities) {
			int total = 0;
			int numMobs = 0;
			int numItems = 0;
			
			// count our entities here
			List<Entity> nearbyEntities = entity.getNearbyEntities(radius, 126, radius);
			total = nearbyEntities.size();
			if(!quickFind) {
				for(Entity nearbyEntity: nearbyEntities) {
					if(nearbyEntity instanceof Item) {
						numItems++;
					}
					else if(nearbyEntity instanceof LivingEntity) {
						numMobs++;
					}
				}
			}
			
			// see if we hit a max
			if(total > maxTotal) {
				// yup
				maxTotal = total;
				maxNumMobs = numMobs;
				maxNumItems = numItems;
				maxLocation = entity.getLocation();
			}
		}
		
		// now report!
		ColourHandler.sendMessage(sender, findResults
				.replaceAll("%totalEntities%", String.valueOf(maxTotal))
				.replaceAll("%x%", String.valueOf(maxLocation.getBlockX()))
				.replaceAll("%y%", String.valueOf(maxLocation.getBlockY()))
				.replaceAll("%z%", String.valueOf(maxLocation.getBlockZ()))
				.replaceAll("%radius%", String.valueOf(radius))
				.replaceAll("%world%", world.getName())
				.replaceAll("%numMobs%", quickFind ? "unknown" : String.valueOf(maxNumMobs))
				.replaceAll("%numItems%", quickFind ? "unknown" : String.valueOf(maxNumItems)));
		
		return true;
	}
}
