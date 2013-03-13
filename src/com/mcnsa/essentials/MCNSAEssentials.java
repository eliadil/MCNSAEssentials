package com.mcnsa.essentials;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.essentials.utilities.ColourHandler;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class MCNSAEssentials extends JavaPlugin {
	// get the minecraft logger
	static Logger log = Logger.getLogger("Minecraft");

	// keep track of permissions
	static PermissionManager permissions = null;
	
	public void onEnable() {
		// set up permissions
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
			MCNSAEssentials.permissions = PermissionsEx.getPermissionManager();
			log("permissions successfully loaded!");
		}
		else {
			error("PermissionsEx not found!");
		}
		
		
	}
	
	public void onDisable() {
		// shutdown
		log("plugin disabled");
	}

	public static Logger log() {
		return log;
	}

	// for simpler logging
	public static void log(String info) {
		ColourHandler.consoleMessage("[MCNSAEssentials] " + info);
	}

	// for error reporting
	public static void warning(String info) {
		ColourHandler.consoleMessage("[MCNSAEssentials] &e<WARNING> " + info);
	}

	// for error reporting
	public static void error(String info) {
		ColourHandler.consoleMessage("[MCNSAEssentials] &c<ERROR> " + info);
	}

	// for debugging
	// (disable for final release)
	public static void debug(String info) {
		log.info("[MCNSAEssentials] <DEBUG> " + info);
	}
	
	public static boolean playerHasPermission(Player player, String permission) {
		if(permissions != null) {
			return permissions.has(player, "mcnsaessentials." + permission);
		}
		else {
			return player.isOp();
		}
	}
}
