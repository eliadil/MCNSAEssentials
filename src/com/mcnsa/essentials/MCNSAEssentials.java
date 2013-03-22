package com.mcnsa.essentials;

import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.essentials.managers.ComponentManager;
import com.mcnsa.essentials.managers.CommandsManager;
import com.mcnsa.essentials.managers.ConfigurationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.Logger;
import com.mcnsa.essentials.utilities.MultilineChatEntry;

public class MCNSAEssentials extends JavaPlugin {	
	// keep track of ourself
	static MCNSAEssentials instance = null;
	
	// our manager
	PermissionsManager permissionsManager = null;
	ComponentManager componentManager = null;
	ConfigurationManager configurationManager = null;
	CommandsManager commandsManager = null;
	DatabaseManager databaseManager = null;
	
	// our multiline chat entry handler
	MultilineChatEntry multilineChatEntry = null;
	
	public MCNSAEssentials() {
		instance = this;
	}
	
	public void onEnable() {		
		// initialize our permissions manager
		permissionsManager = new PermissionsManager();
		
		// ok, start loading our components
		componentManager = new ComponentManager();
		
		// load the configuration for all our components
		configurationManager = new ConfigurationManager(this.getConfig());
		configurationManager.loadDisabledComponents(componentManager);
		
		// now load our components
		componentManager.loadComponents();
		
		// initialize our commands manager, loading commands in the process
		commandsManager = new CommandsManager();

		// initialize our database manager
		databaseManager = new DatabaseManager();
		
		// initialize our chat handler
		multilineChatEntry = new MultilineChatEntry();
		
		// now load all our class's settings
		configurationManager.loadSettings(componentManager);
		this.saveConfig();
		
		// load our commands
		commandsManager.loadCommands(componentManager);
		
		// and start our database
		databaseManager.enable();
		
		// we're done!
		Logger.log("&aPlugin enabled");
	}
	
	public void onDisable() {
		// shutdown
		try {
			databaseManager.disable();
		}
		catch(Exception e) {
			Logger.error("Failed to disable database manager (%s)!", e.getMessage());
		}
		Logger.log("&6Plugin disabled");
	}
	
	public static MCNSAEssentials getInstance() {
		return instance;
	}
}
