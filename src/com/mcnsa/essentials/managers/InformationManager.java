package com.mcnsa.essentials.managers;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.managers.CommandsManager.CommandInfo;
import com.mcnsa.essentials.managers.ComponentManager.Component;

public class InformationManager {
	private static LinkedList<Component> components = new LinkedList<Component>();
	
	public InformationManager(ComponentManager componentManager) {
		// pull all of our registered components that aren't disabled
		HashMap<String, Component> registeredComponents = componentManager.getRegisteredComponents();
		for(String key: registeredComponents.keySet()) {
			if(!registeredComponents.get(key).disabled) {
				components.add(registeredComponents.get(key));
			}
		}
	}
	
	@Translation(node = "information.usage-format") public static String usageFormat = 
			"&6/%command% &e%args%\n\t&7%description%";
	@Translation(node = "information.usage-argument-format") public static String usageArgumentFormat = 
			"&7<&f%arg%&7>";
	public static String formatUsage(Command command) {
		// create our args string
		StringBuilder args = new StringBuilder();
		for(String arg: command.arguments()) {
			args.append(usageArgumentFormat.replaceAll("%arg%", arg)).append(" ");
		}
		
		return usageFormat
				.replaceAll("%command%", command.command())
				.replaceAll("%args%", args.toString().trim())
				.replaceAll("%description%", command.description());
	}
	
	private static boolean hasPermission(CommandSender sender, ComponentInfo componentInfo, Command command) {
		// make sure we can access it properly based on console or not
		// player only command
		if(command.playerOnly() && !(sender instanceof Player)) {
			return false;
		}
		// console only command
		else if(command.consoleOnly() && sender instanceof Player) {
			return false;
		}
		
		// if we don't have any permissions, we're good
		if(command.permissions().length == 0) {
			return true;
		}
		
		// check if we have any of the permissions necessary
		for(String permission: command.permissions()) {
			if(PermissionsManager.playerHasPermission(sender, componentInfo.permsSettingsPrefix() + "." + permission)) {
				return true;
			}
		}
		
		// nope
		return false;
	}
	
	public static LinkedList<String> searchUsage(CommandSender sender, String command) {
		LinkedList<String> commandsUsage = new LinkedList<String>();
		
		// go through all our components and match all commands
		for(Component component: components) {
			// loop over all registered commands
			for(CommandInfo ci: component.commands) {
				// check if we found a proper command
				if(ci.command.command().equalsIgnoreCase(command)) {
					// make sure we have permission first
					if(hasPermission(sender, component.componentInfo, ci.command)) {
						// add it
						commandsUsage.add(formatUsage(ci.command));
					}
				}
			}
		}
		
		// return our list!
		return commandsUsage;
	}
	
	// utility function to get it all as one big string
	public static String searchUsageString(CommandSender sender, String command) {
		LinkedList<String> usageStrings = searchUsage(sender, command);
		StringBuilder usage = new StringBuilder();
		for(String usageString: usageStrings) {
			usage.append(usageString).append("\n");
		}
		return usage.toString().trim();
	}
}
