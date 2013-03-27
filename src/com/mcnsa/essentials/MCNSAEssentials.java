package com.mcnsa.essentials;

import java.util.LinkedList;

import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.essentials.interfaces.DisableHandler;
import com.mcnsa.essentials.managers.ComponentManager;
import com.mcnsa.essentials.managers.CommandsManager;
import com.mcnsa.essentials.managers.ConfigurationManager;
import com.mcnsa.essentials.managers.ConversationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.managers.InformationManager;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.managers.TranslationManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.ItemSelector;
import com.mcnsa.essentials.utilities.Logger;

public class MCNSAEssentials extends JavaPlugin {	
	// keep track of ourself
	static MCNSAEssentials instance = null;
	
	// our managers
	PermissionsManager permissionsManager = null;
	ComponentManager componentManager = null;
	ConfigurationManager configurationManager = null;
	TranslationManager translationManager = null;
	CommandsManager commandsManager = null;
	ConversationManager conversationManager = null;
	InformationManager informationManager = null;
	DatabaseManager databaseManager = null;
	
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
		
		// initialize our commands manager, loading commands in the process
		commandsManager = new CommandsManager();
		
		// load our conversation manager
		conversationManager = new ConversationManager();

		// initialize our database manager
		databaseManager = new DatabaseManager();
		
		// now load all our class's settings
		configurationManager.loadSettings(componentManager);
		this.saveConfig();
		
		// load our translations
		translationManager = new TranslationManager();
		translationManager.load(componentManager);
		
		// now load our components
		componentManager.initializeComponents();
		
		// load our commands
		commandsManager.loadCommands(componentManager);
		
		// initialize our conversations
		conversationManager.load();
		
		// load our command information
		informationManager = new InformationManager(componentManager);
		
		// initialize our colour handler
		ColourHandler.initialize();
		
		// load our item definitions
		ItemSelector.load();
		
		// and start our database
		databaseManager.enable();
		
		// we're done!
		Logger.log("&aPlugin enabled");
	}
	
	public void onDisable() {
		// shutdown
		for(DisableHandler disable: disableHandlers) {
			try {
				disable.onDisable();
			}
			catch(Exception e) {
				Logger.error("Failed to disable clazz %s (%s)!", disable.getClass().getSimpleName(), e.getMessage());
			}
		}
		Logger.log("&6Plugin disabled");
	}
	
	public static MCNSAEssentials getInstance() {
		return instance;
	}
	
	private static LinkedList<DisableHandler> disableHandlers = new LinkedList<DisableHandler>();
	public static void registerDisable(DisableHandler handler) {
		disableHandlers.add(handler);
	}
}
