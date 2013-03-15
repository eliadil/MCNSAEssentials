package com.mcnsa.essentials.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.MCNSAEssentials;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsManager {
	// keep track of permissions
	static ru.tehkode.permissions.PermissionManager permissions = null;
	
	public PermissionsManager() {
		// set up permissions
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
			permissions = PermissionsEx.getPermissionManager();
			MCNSAEssentials.log("permissions successfully loaded!");
		}
		else {
			MCNSAEssentials.error("PermissionsEx not found!");
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
};
