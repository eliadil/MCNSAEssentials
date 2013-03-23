package com.mcnsa.essentials.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.utilities.Logger;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsManager {
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
			return playerHasPermission((Player)sender, permission);
		}
		else {
			return true;
		}
	}
	
	public static boolean playerHasPermission(Player player, String permission) {
		if(permissions != null) {
			return permissions.has(player, "mcnsaessentials." + permission);
		}
		else {
			return player.isOp();
		}
	}
	
	public static boolean playerHasPermission(String playerTarget, String permission) {
		Player player = Bukkit.getServer().getPlayer(playerTarget);
		if(permissions != null) {
			if(player != null) {
				return permissions.has(player, "mcnsaessentials." + permission);
			}
			else {
				permissions.has(playerTarget, permission, Bukkit.getServer().getWorlds().get(0).getName());
			}
		}
		return false;
	}
};
