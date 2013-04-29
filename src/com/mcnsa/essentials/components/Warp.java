package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "Warp",
				description = "Lets players warp around",
				permsSettingsPrefix = "warps")
@DatabaseTableInfo(name = "warps",
					fields = { "owner TINYTEXT", "name TINYTEXT", "world TINYTEXT", "x FLOAT", "y FLOAT", "z FLOAT", "yaw FLOAT", "pitch FLOAT", "public BOOL" })
public class Warp {
	@Setting(node = "max-private-warps") public static int maxPrivateWarps = 5;
	@Setting(node = "warps-per-page") public static int warpsPerPage = 5;
	
	@Command(command = "warps",
			description = "lists warps available to you",
			permissions = {"list"})
	public static boolean warps(CommandSender sender) throws EssentialsCommandException {
		return warps(sender, 1);
	}

	@Command(command = "warps",
			arguments = {"page"},
			tabCompletions = {TabCompleteType.NUMBER},
			description = "lists warps available to you on a given page",
			permissions = {"list"})
	public static boolean warps(CommandSender sender, int page) throws EssentialsCommandException {
		// get a resultset of all our warps
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from warps where public=? or owner=? order by name asc;",
				true,
				sender.getName());
		if(results.size() == 0) {
			ColourHandler.sendMessage(sender, "&eThere aren't any warps available!");
			return true;
		}
		
		// calculate the number of pages
		int totalPages = results.size() / warpsPerPage;
		if(results.size() % 5 != 0) totalPages++;
		
		// make sure we have an appropriate page
		page -= 1;
		if(page < 0) {
			throw new EssentialsCommandException("Can't list negative pages!");
		}
		else if(page >= totalPages) {
			throw new EssentialsCommandException("There are only %d pages available!", totalPages);
		}
		
		// calculate the start and end warp indices
		int start = page * warpsPerPage;
		int end = start + warpsPerPage;
		if(end > results.size()) {
			end = results.size();
		}
		
		// list this page of the warps
		ColourHandler.sendMessage(sender, "&6Available warps (page %d/%d):", (page+1), totalPages);
		for(int i = start; i < end; i++) {
			ColourHandler.sendMessage(sender,
					"  &e%s &6[%s&6] &f%s&6(&f%d&6, &f%d&6, &f%d&6)",
					(String)results.get(i).get("name"),
					((Boolean)results.get(i).get("public") ? "&aPUBLIC" : "&cPRIVATE"),
					(String)results.get(i).get("world"),
					((Float)results.get(i).get("x")).intValue(),
					((Float)results.get(i).get("y")).intValue(),
					((Float)results.get(i).get("z")).intValue()
					);
		}
		
		return true;
	}
	
	@Command(command = "setwarp",
			arguments = {"name", "public/private"},
			tabCompletions = {TabCompleteType.STRING, TabCompleteType.STRING},
			description = "sets a warp",
			permissions = {"set"},
			playerOnly = true)
	public static boolean setWarp(CommandSender sender, String warpName, String privacy) throws EssentialsCommandException {
		Player player = (Player)sender;
		return setWarp(sender,
				warpName,
				privacy,
				player.getWorld().getName(),
				player.getLocation().getBlockX(),
				player.getLocation().getBlockY(),
				player.getLocation().getBlockZ());
	}
		
	@Command(command = "setwarp",
			arguments = {"name", "public/private"},
			tabCompletions = {TabCompleteType.STRING, TabCompleteType.STRING},
			description = "sets a warp",
			permissions = {"set"})
	public static boolean setWarp(CommandSender sender, String warpName, String privacy, String worldName, float x, float y, float z) throws EssentialsCommandException {
		// get yaw and pitch
		float yaw = 0, pitch = 0;
		if(sender instanceof Player) {
			yaw = ((Player)sender).getLocation().getYaw();
			pitch = ((Player)sender).getLocation().getPitch();
		}
		
		// sort out the privacy
		boolean isPublic = false;
		if(privacy.equalsIgnoreCase("public")) {
			// make sure they have the perms
			if(!PermissionsManager.playerHasPermission(sender, "warp.set.public")) {
				throw new EssentialsCommandException("You don't have permission to make public warps!");
			}
			isPublic = true;
		}
		else if(privacy.equalsIgnoreCase("private")) {
			// make sure they have the perms
			if(!PermissionsManager.playerHasPermission(sender, "warp.set.private")) {
				throw new EssentialsCommandException("You don't have permission to make private warps!");
			}
			isPublic = false;
		}
		else {
			throw new EssentialsCommandException("'%s' isn't a valid privacy option!", privacy);
		}
		
		// first, determine if our warp already exists
		// and count home many warps we have
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery("select * from warps where owner=?;",
				sender.getName());
		boolean exists = false;
		int resultIndex = 0;
		for(resultIndex = 0; resultIndex < results.size(); resultIndex++) {
			if(((String)results.get(resultIndex).get("name")).equals(warpName)) {
				exists = true;
				break;
			}
		}
		
		if(exists) {
			// update the old warp
			int insertionResults = DatabaseManager.updateQuery(
					"update warps set world=?, x=?, y=?, z=?, yaw=?, pitch=?, public=? where id=?;",
					worldName,
					x, y, z,
					yaw, pitch,
					isPublic,
					results.get(resultIndex).get("id"));
			
			// make sure it worked!
			if(insertionResults == 0) {
				throw new EssentialsCommandException("Failed to update the warp!");
			}
			
			ColourHandler.sendMessage(sender, "&aWarp '%s' has been updated!", warpName);
		}
		else {
			// only test for private warps
			if(results.size() > 0 && !isPublic) {
				// make sure we aren't going over our limit
				// count the number of private warps
				results = DatabaseManager.accessQuery("select id from warps where owner=? and public=?",
						sender.getName(), false);
				if(results.size() >= maxPrivateWarps) {
					throw new EssentialsCommandException("You have too many private warps! (Maximum is %d)",
							maxPrivateWarps);
				}
			}
			
			// ok, insert it
			
			// set our warp
			int insertionResults = DatabaseManager.updateQuery(
					"insert into warps (id, owner, name, world, x, y, z, yaw, pitch, public) values (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
					sender.getName(),
					warpName,
					worldName,
					x, y, z,
					yaw, pitch,
					isPublic);
			
			// make sure it worked!
			if(insertionResults == 0) {
				throw new EssentialsCommandException("Failed to add your warp!");
			}
			
			ColourHandler.sendMessage(sender, "&aWarp '%s' has been added!", warpName);
		}
		
		return true;
	}
	
	@Command(command = "deletewarp",
			arguments = {"warp name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "Deletes a given warp",
			permissions = "delete.self")
	public static boolean deleteWarp(CommandSender sender, String warpName) throws EssentialsCommandException {		
		// try to get that warp
		// get a resultset of all our warps
		ArrayList<HashMap<String, Object>> results = null;
		if(!PermissionsManager.playerHasPermission(sender, "warp.delete.any")) {
			results = DatabaseManager.accessQuery(
					"select * from warps where name=? and (public=? or owner=?) limit 1;",
					warpName,
					true,
					sender.getName());
			if(results.size() == 0) {
				throw new EssentialsCommandException("&eThe warp '%s' doesn't exist!", warpName);
			}
		}
		else {
			results = DatabaseManager.accessQuery(
					"select * from warps where name=? limit 1;",
					warpName);
			if(results.size() == 0) {
				throw new EssentialsCommandException("&eThe warp '%s' doesn't exist!", warpName);
			}
		}
		
		// make sure we have it
		if(results.size() != 1) {
			throw new EssentialsCommandException("&eThe warp '%s' doesn't exist!", warpName);
		}
		
		// ok, we have it
		// build a location
		HashMap<String, Object> result = results.get(0);
		
		// delete it!
		int numRowsDeleted = DatabaseManager.updateQuery("delete from warps where id=?",
				result.get("id"));
		if(numRowsDeleted == 0) {
			throw new EssentialsCommandException("Failed to delete the warp! Please contact an administrator!");
		}
		ColourHandler.sendMessage(sender, "&aWarp '%s' deleted!", warpName);
		
		return true;
	}
	
	@Command(command = "warp",
			arguments = {"warp name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "Warps you to the given warp",
			permissions = "warp.self",
			playerOnly = true)
	public static boolean warp(CommandSender sender, String warpName) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// try to get that warp
		// get a resultset of all our warps
		ArrayList<HashMap<String, Object>> results = null;
		if(!PermissionsManager.playerHasPermission(sender, "warp.warp.any")) {
			results = DatabaseManager.accessQuery(
					"select * from warps where name=? and (public=? or owner=?) limit 1;",
					warpName,
					true,
					sender.getName());
			if(results.size() == 0) {
				throw new EssentialsCommandException("&eThe warp '%s' doesn't exist!", warpName);
			}
		}
		else {
			results = DatabaseManager.accessQuery(
					"select * from warps where name=? limit 1;",
					warpName);
			if(results.size() == 0) {
				throw new EssentialsCommandException("&eThe warp '%s' doesn't exist!", warpName);
			}
		}
		
		// make sure we have it
		if(results.size() != 1) {
			throw new EssentialsCommandException("&eThe warp '%s' doesn't exist!", warpName);
		}
		
		// ok, we have it
		// build a location
		HashMap<String, Object> result = results.get(0);
		Location location = new Location(
				Bukkit.getServer().getWorld((String)result.get("world")),
				(Float)result.get("x"),
				(Float)result.get("y"),
				(Float)result.get("z"),
				(Float)result.get("yaw"),
				(Float)result.get("pitch"));
		
		// teleport them!
		player.teleport(location);
		ColourHandler.sendMessage(sender, "&6Woosh!");
		
		return true;
	}
}
