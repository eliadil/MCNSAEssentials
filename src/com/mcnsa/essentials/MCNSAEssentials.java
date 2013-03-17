package com.mcnsa.essentials;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.essentials.managers.ComponentManager;
import com.mcnsa.essentials.managers.CommandsManager;
import com.mcnsa.essentials.managers.ConfigurationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.ColourHandler;

public class MCNSAEssentials extends JavaPlugin {
	// get the minecraft logger
	static Logger log = Logger.getLogger("Minecraft");
	
	// keep track of ourself
	static MCNSAEssentials instance = null;
	
	// our manager
	DatabaseManager databaseManager = null;
	PermissionsManager permissionsManager = null;
	ComponentManager componentManager = null;
	ConfigurationManager configurationManager = null;
	CommandsManager commandsManager = null;
	
	public MCNSAEssentials() {
		instance = this;
	}
	
	public void onEnable() {
		// initialize our datbase manager
		databaseManager = new DatabaseManager();
		databaseManager.connect();
		
		// initialize our permissions manager
		permissionsManager = new PermissionsManager();
		
		// ok, start loading our components
		componentManager = new ComponentManager();
		
		// load the configuration for all our components
		//this.saveDefaultConfig();
		configurationManager = new ConfigurationManager(this.getConfig(), componentManager);
		this.saveConfig();
		
		// initialize our commands manager, loading commands in the process
		commandsManager = new CommandsManager(componentManager);
		
		// we're done!
		log("plugin enabled");
	}
	
	public void onDisable() {
		// shutdown
		log("plugin disabled");
	}

	public static Logger log() {
		return log;
	}

	// for simpler logging
	public static void log(String format, Object... args) {
		log(String.format(format, args));
	}
	public static void log(String info) {
		ColourHandler.consoleMessage("[MCNSAEssentials] " + info);
	}

	// for error reporting
	public static void warning(String format, Object... args) {
		warning(String.format(format, args));
	}
	public static void warning(String info) {
		ColourHandler.consoleMessage("[MCNSAEssentials] &e<WARNING> " + info);
	}

	// for error reporting
	public static void error(String format, Object... args) {
		error(String.format(format, args));
	}
	public static void error(String info) {
		ColourHandler.consoleMessage("[MCNSAEssentials] &c<ERROR> " + info);
	}

	// for debugging
	// (disable for final release)
	public static void debug(String format, Object... args) {
		debug(String.format(format, args));
	}
	public static void debug(String info) {
		ColourHandler.consoleMessage("[MCNSAEssentials] &9<DEBUG> " + info);
	}
	
	public static MCNSAEssentials getInstance() {
		return instance;
	}
}
