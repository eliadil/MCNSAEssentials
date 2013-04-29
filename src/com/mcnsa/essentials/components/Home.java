package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Home",
				description = "Lets players specify homes",
				permsSettingsPrefix = "homes")
@DatabaseTableInfo(name = "homes",
					fields = { "owner TINYTEXT", "name TINYTEXT", "world TINYTEXT", "x FLOAT", "y FLOAT", "z FLOAT" })
public class Home implements Listener {
	@Setting(node = "max-homes") public static int maxHomes = 5;
	
	public Home() {
		// and register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		try {
			// first try to get our "default" home
			ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
					"select * from homes where owner=? and name=?;",
					event.getPlayer().getName(), "default");
			// if we don't have a "default" home, get the first one
			if(results.size() != 1) {
				// grab our first home
				results = DatabaseManager.accessQuery(
						"select * from homes where owner=? order by id limit 1;",
						event.getPlayer().getName());
				if(results.size() != 1){
					// we don't have a home, cancel it!
					return;
				}
			}
			
			// we DO have a home!
			// make a location
			Location location = new Location(
					Bukkit.getServer().getWorld((String)results.get(0).get("world")),
					(Float)results.get(0).get("x"),
					(Float)results.get(0).get("y"),
					(Float)results.get(0).get("z"));

			// take us there!
			event.setRespawnLocation(location);
		}
		catch(Exception e) {
			// ignore if something goes wrong!
		}
	}
	
	@Command(command = "homes",
			description = "lists your homes",
			permissions = {"list.self"},
			playerOnly = true)
	public static boolean homes(CommandSender sender) throws EssentialsCommandException {
		return homes(sender, sender.getName());
	}

	@Translation(node = "no-homes-defined") public static String noHomesDefined = "&e'%player%' doesn't have any homes defined!";
	@Translation(node = "players-homes-headers") public static String playersHomesHeader = "%player%&6's homes:";
	@Command(command = "homes",
			arguments = {"player"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "lists the homes of the given player",
			permissions = {"list.other"})
	public static boolean homes(CommandSender sender, String playerTarget) throws EssentialsCommandException {
		// try to get our targetPlayer
		playerTarget = PlayerSelector.selectSinglePlayer(playerTarget).getName();
		
		// get a resultset of all our kits
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery("select * from homes where owner=?;", playerTarget);
		if(results.size() == 0) {
			ColourHandler.sendMessage(sender, noHomesDefined.replaceAll("%player%", playerTarget));
			return true;
		}
		
		ColourHandler.sendMessage(sender, playersHomesHeader.replaceAll("%player%", playerTarget));
		String homeList = "";
		for(int i = 0; i < results.size(); i++) {
			if(i != 0) {
				homeList += "&6, ";
			}
			homeList += "&f" + (String)results.get(i).get("name") + " &e(" + results.get(i).get("x") + ", " + results.get(i).get("y") + ", " + results.get(i).get("z") + ")";
		}
		ColourHandler.sendMessage(sender, homeList);
		
		return true;
	}

	@Command(command = "sethome",
		description = "sets your default home to your current location",
		permissions = {"set.self"},
		playerOnly = true)
	public static boolean sethome(CommandSender sender) throws EssentialsCommandException {
		return setHome(sender, sender.getName(), "default");
	}

	@Command(command = "sethome",
		arguments = {"home name"},
		tabCompletions = {TabCompleteType.STRING},
		description = "sets a to your current location",
		permissions = {"set.self"},
		playerOnly = true)
	public static boolean sethome(CommandSender sender, String homeName) throws EssentialsCommandException {
		return setHome(sender, sender.getName(), homeName);
	}

	@Translation(node = "home-been-set") public static String homeBeenSet = "&a%player%'s home '%home%' has been set!";
	@Translation(node = "too-many-homes") public static String tooManyHomes = "&6%player% already has too many homes!";
	@Command(command = "sethome",
		arguments = {"target player[s]", "home name"},
		tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING},
		description = "sets target player[s] home[s] to your current location",
		permissions = {"set.other"},
		playerOnly = true)
	public static boolean setHome(CommandSender sender, String targetPlayer, String homeName) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// try to get our targetPlayer
		targetPlayer = PlayerSelector.selectSinglePlayer(targetPlayer).getName();
		
		// first, determine if our home already exists
		// and count home many homes we have
		// get a resultset of all our kits
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery("select * from homes where owner=?;", targetPlayer);
		int currentNumberOfHomes = results.size();
		boolean exists = false;
		for(int i = 0; i < results.size(); i++) {
			if(((String)results.get(i).get("name")).equals(homeName)) {
				exists = true;
				break;
			}
		}
		
		// change what we do based on whether it exists or not
		if(exists) {
			// set our home
			int insertionResults = DatabaseManager.updateQuery(
					"updates homes set world=?, x=?, y=?, z=? where owner=? and homeName=?;",
					player.getWorld().getName(),
					player.getLocation().getBlockX(),
					player.getLocation().getBlockY(),
					player.getLocation().getBlockZ(),
					targetPlayer,
					homeName);
			
			// make sure it worked!
			if(insertionResults == 0) {
				throw new EssentialsCommandException("Failed to set your home!");
			}
			
			ColourHandler.sendMessage(sender,
					homeBeenSet.replaceAll("%player%", targetPlayer)
					.replaceAll("%home%", homeName));
		}
		else {
			// insert, but only if we don't have too many homes already
			if(currentNumberOfHomes >= maxHomes) {
				ColourHandler.sendMessage(sender, tooManyHomes.replaceAll("%player%", targetPlayer));
				return true;
			}
			
			// set our home
			int insertionResults = DatabaseManager.updateQuery(
					"insert into homes (id, owner, name, world, x, y, z) values (NULL, ?, ?, ?, ?, ?, ?);",
					targetPlayer,
					homeName,
					player.getWorld().getName(),
					player.getLocation().getBlockX(),
					player.getLocation().getBlockY(),
					player.getLocation().getBlockZ());
			
			// make sure it worked!
			if(insertionResults == 0) {
				throw new EssentialsCommandException("Failed to set your home!");
			}

			ColourHandler.sendMessage(sender,
					homeBeenSet.replaceAll("%player%", targetPlayer)
					.replaceAll("%home%", homeName));
		}
		
		return true;
	}

	@Command(command = "home",
		description = "takes you to your default home",
		permissions = {"self"},
		playerOnly = true)
	public static boolean home(CommandSender sender) throws EssentialsCommandException {
		// first try to get our "default" home
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery("select * from homes where owner=? and name=?;", sender.getName(), "default");
		// if we don't have a "default" home, get the first one
		if(results.size() != 1){
			// grab our first name
			results = DatabaseManager.accessQuery("select * from homes where owner=? limit 1;", sender.getName());
			if(results.size() != 1){
				throw new EssentialsCommandException("You don't have any homes defined");
			}
		}
		
		// get our home name
		String homeName = (String)results.get(0).get("name");
		
		// do it
		return home(sender, sender.getName(), homeName);
	}

	@Command(command = "home",
		arguments = {"home name"},
		tabCompletions = {TabCompleteType.STRING},
		description = "takes you to your saved home",
		permissions = {"other"},
		playerOnly = true)
	public static boolean home(CommandSender sender, String homeName) throws EssentialsCommandException {
		return home(sender, sender.getName(), homeName);
	}

	@Translation(node = "welcome-home") public static String welcomeHome =
			"&6Welcome to your home '%home%'!";
	@Translation(node = "welcome-home-other") public static String welcomeHomeOther =
			"&6Welcome to %player%'s home '%home%'!";
	@Command(command = "home",
		arguments = {"player", "home name"},
		tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING},
		description = "takes you to the players saved home",
		permissions = {"other"},
		playerOnly = true)
	public static boolean home(CommandSender sender, String playerTarget, String homeName) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// try to get our targetPlayer
		playerTarget = PlayerSelector.selectSinglePlayer(playerTarget).getName();
		
		// first, determine if our home already exists
		// and count home many homes we have
		// get a resultset of all our kits
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from homes where owner=? and name=?;", playerTarget, homeName);
		for(int i = 0; i < results.size(); i++) {
			if(((String)results.get(i).get("name")).equals(homeName)) {
				// we found it!
				// make a location
				Location location = new Location(
						Bukkit.getServer().getWorld((String)results.get(i).get("world")),
						(Float)results.get(i).get("x"),
						(Float)results.get(i).get("y"),
						(Float)results.get(i).get("z"));
				
				// teleport us!
				player.teleport(location);
				
				// alert
				if(sender.getName().equals(playerTarget)){
					ColourHandler.sendMessage(sender, welcomeHome.replaceAll("%home%", homeName));
				}
				else {
					ColourHandler.sendMessage(sender, welcomeHomeOther
							.replaceAll("%home%", homeName)
							.replaceAll("%player%", playerTarget));
				}
				return true;
			}
		}
		
		ColourHandler.sendMessage(sender, "&6%s doesn't have a home named '%s'!", playerTarget, homeName);
		return false;
	}
}
