package com.mcnsa.essentials.managers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.utilities.Logger;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsManager {
	@Setting(node = "global-permissions-prefix") public static String globalPermissionsPrefix = "mcnsaessentials.";
	
	// keep track of permissions
	static ru.tehkode.permissions.PermissionManager permissions = null;
	
	public PermissionsManager() {
		// set up permissions
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
			permissions = PermissionsEx.getPermissionManager();
			Logger.log("&aPermissions successfully loaded!");
		}
		else {
			Logger.error("PermissionsEx not found!");
		}
	}
	
	public static boolean playerHasPermission(CommandSender sender, String permission) {
		if(sender instanceof Player) {
			return playerHasPermission((Player)sender, permission, true);
		}
		else {
			return true;
		}
	}
	
	public static boolean playerHasPermission(CommandSender sender, String permission, boolean doPrefix) {
		if(sender instanceof Player) {
			return playerHasPermission((Player)sender, permission, doPrefix);
		}
		else {
			return true;
		}
	}
	
	public static boolean playerHasPermission(Player player, String permission, boolean doPrefix) {
		if(permissions != null) {
			return permissions.has(player, (doPrefix ? globalPermissionsPrefix : "") + permission);
		}
		else {
			return player.isOp();
		}
	}
	
	public static boolean playerHasPermission(String playerTarget, String permission) {
		Player player = Bukkit.getServer().getPlayer(playerTarget);
		if(permissions != null) {
			if(player != null) {
				return permissions.has(player, globalPermissionsPrefix + permission);
			}
			else {
				permissions.has(playerTarget, permission, Bukkit.getServer().getWorlds().get(0).getName());
			}
		}
		return false;
	}
	
	public static ArrayList<String> getGroups(Player player) {
		if(permissions == null) {
			return null;
		}
		
		PermissionGroup[] groups = permissions.getUser(player).getGroups();
		ArrayList<String> groupNames = new ArrayList<String>();
		for(PermissionGroup group: groups) {
			groupNames.add(group.getName());
		}
		
		return groupNames;
	}
};
