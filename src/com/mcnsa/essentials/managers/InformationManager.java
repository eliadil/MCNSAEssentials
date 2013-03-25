package com.mcnsa.essentials.managers;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
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
			"&6/%command% &e%args%\n&7%description%";
	@Translation(node = "information.usage-argument-format") public static String usageArgumentFormat = 
			"&7<&f%arg%&7>";
	public static String formatUsage(Command command) {
		// create our args string
		StringBuilder args = new StringBuilder();
		for(String arg: command.arguments()) {
			args.append(usageArgumentFormat.replaceAll("%arg%", arg)).append(" ");
		}
		
		// get our usage
		String usage =  usageFormat
				.replaceAll("%command%", command.command())
				.replaceAll("%args%", args.toString().trim())
				.replaceAll("%description%", command.description());
		
		// word wrap that sucker
		String[] lines = ChatPaginator.wordWrap(usage, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH);
		
		// don't indent the first line
		// but indent subsequent lines
		StringBuilder usageBlock = new StringBuilder();
		for(int i = 0; i < lines.length; i++) {
			if(i != 0) {
				usageBlock.append("\t");
			}
			usageBlock.append(lines[i]).append("\n");
		}
		
		// return
		return usageBlock.toString().trim();
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
	
	// attempt to partially match our command
	private static boolean softMatch(String realCommand, String softCommand) {
		if(realCommand.startsWith(softCommand)) {
			return true;
		}
		else if(realCommand.contains(softCommand)) {
			return true;
		}
		return false;
	}
	
	public static LinkedList<String> searchUsage(CommandSender sender, String command, boolean softMatch) {
		LinkedList<String> commandsUsage = new LinkedList<String>();
		
		// go through all our components and match all commands
		for(Component component: components) {
			// loop over all registered commands
			for(CommandInfo ci: component.commands) {
				// check if we found a proper command
				if(ci.command.command().equalsIgnoreCase(command) || (softMatch && softMatch(ci.command.command(), command))) {
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
	public static String searchUsageString(CommandSender sender, String command, boolean softMatch) {
		LinkedList<String> usageStrings = searchUsage(sender, command, softMatch);
		StringBuilder usage = new StringBuilder();
		for(String usageString: usageStrings) {
			usage.append(usageString).append("\n");
		}
		return usage.toString().trim();
	}
	
	// get a list of all components with commands the player can use
	public static LinkedList<Component> listAvailableComponents(CommandSender sender) {
		LinkedList<Component> possibleComponents = new LinkedList<Component>();
		
		// go through all available components
		for(Component component: components) {
			boolean valid = false;
			// loop over all registered commands
			for(CommandInfo ci: component.commands) {
				// try to find a command that we can use
				if(hasPermission(sender, component.componentInfo, ci.command)) {
					valid = true;
					break;
				}
			}
			
			// we have a valid component, return it!
			if(valid) {
				possibleComponents.add(component);
			}
		}
		
		return possibleComponents;
	}
	
	public static Component findComponent(String componentName) throws EssentialsCommandException {
		// find our component
		Component foundComponent = null;
		for(Component component: components) {
			if(component.componentInfo.friendlyName().equalsIgnoreCase(componentName)) {
				foundComponent = component;
				break;
			}
		}
		if(foundComponent == null) {
			throw new EssentialsCommandException("I couldn't find the component '%s'!", componentName);
		}
		return foundComponent;
	}
	
	// get a list of all commands within a given component
	public static LinkedList<CommandsManager.CommandInfo> listComponentCommands(CommandSender sender, Component component) {
		LinkedList<CommandsManager.CommandInfo> possibleCommands = new LinkedList<CommandsManager.CommandInfo>();
		
		// loop over all registered commands
		for(CommandInfo ci: component.commands) {
			// make sure we have permission first
			if(hasPermission(sender, component.componentInfo, ci.command)) {
				// add it
				possibleCommands.add(ci);
			}
		}
		
		// yup!
		return possibleCommands;
	}
}
