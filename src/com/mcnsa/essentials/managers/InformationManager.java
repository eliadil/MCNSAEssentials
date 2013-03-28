package com.mcnsa.essentials.managers;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.ComponentManager.Component;
import com.mcnsa.essentials.utilities.StringUtils;

public class InformationManager {
	public class CommandHelp {
		public String command = null;
		public String[] aliases = null;
		public String[] args = null;
		public String description = null;
		public String[] permissions = null;
		public Boolean playerOnly = null;
		public Boolean consoleOnly = null;
	}
	
	public class HelpSection {
		public String name = null;
		public String description = null;
		public LinkedList<CommandHelp> commands = new LinkedList<CommandHelp>();
	}

	// store our instance
	public static InformationManager instance = null;
	
	//private static LinkedList<Component> components = new LinkedList<Component>();
	private static LinkedList<HelpSection> sections = new LinkedList<HelpSection>();

	private static File fileConfig = new File(MCNSAEssentials.getInstance().getDataFolder(), "help.yml");
	private static YamlConfiguration yamlConfig = new YamlConfiguration();
	public InformationManager(ComponentManager componentManager) {
		// get our instance
		instance = this;

		// pull all of our registered components that aren't disabled
		HashMap<String, Component> registeredComponents = componentManager.getRegisteredComponents();
		for(String key: registeredComponents.keySet()) {
			if(!registeredComponents.get(key).disabled) {
				//components.add(registeredComponents.get(key));
				Component component = registeredComponents.get(key);
				
				// fill out our help section
				HelpSection section = new HelpSection();
				section.name = component.componentInfo.friendlyName();
				section.description = component.componentInfo.description();
				section.commands = new LinkedList<CommandHelp>();
				
				// get all our commands
				for(CommandsManager.CommandInfo commandInfo: component.commands) {
					// fill out a help command
					CommandHelp command = new CommandHelp();
					command.command = commandInfo.command.command();
					command.aliases = commandInfo.command.aliases();
					command.args = commandInfo.command.arguments();
					command.description = commandInfo.command.description();
					command.permissions = new String[commandInfo.command.permissions().length];
					for(int i = 0; i < commandInfo.command.permissions().length; i++) {
						command.permissions[i] =
								PermissionsManager.globalPermissionsPrefix
								+ component.componentInfo.permsSettingsPrefix()
								+ "." + commandInfo.command.permissions()[i];
					}
					command.playerOnly = commandInfo.command.playerOnly();
					command.consoleOnly = commandInfo.command.consoleOnly();
					
					// add it
					section.commands.add(command);
				}
				
				// add our help section
				sections.add(section);
			}
		}
		
		// now also pull all of our components from our help file
		// extract a default config
		if(!fileConfig.exists()) {
			MCNSAEssentials.getInstance().saveResource("help.yml", false);
		}
		
		// load help from a config file
		try {
			yamlConfig.load(fileConfig);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// get all of our sections / components
		Set<String> keys = yamlConfig.getKeys(false);
		for(String key: keys) {
			// create a section
			HelpSection section = new HelpSection();
			
			// fill in some information
			section.name = key;
			section.description = yamlConfig.getString(key + ".description");
			section.commands = new LinkedList<CommandHelp>();
			
			// get our commands
			ConfigurationSection commandSection = yamlConfig.getConfigurationSection(key + ".commands");
			Set<String> commandKeys = commandSection.getKeys(false);
			for(String commandKey: commandKeys) {
				/*String description = commandSection.getString(commandKey + ".description");
				Logger.debug(description);*/
				
				// create a new command help
				CommandHelp ch = new CommandHelp();
				ch.command = commandKey.toLowerCase();
				
				// fill it in with data from the config file
				List<String> aliases = commandSection.getStringList(commandKey + ".aliases");
				ch.aliases = aliases.toArray(new String[aliases.size()]);
				List<String> args = commandSection.getStringList(commandKey + ".arguments");
				ch.args = args.toArray(new String[aliases.size()]);
				ch.description = commandSection.getString(commandKey + ".description");
				List<String> permissions = commandSection.getStringList(commandKey + ".permissions");
				ch.permissions = permissions.toArray(new String[permissions.size()]);
				ch.playerOnly = commandSection.getBoolean(commandKey + ".playerOnly", false);
				ch.consoleOnly = commandSection.getBoolean(commandKey + ".consoleOnly", false);
				
				// add it
				section.commands.add(ch);
			}
			
			// add our help section
			sections.add(section);
		}
	}
	
	@Translation(node = "information.usage-format") public static String usageFormat = 
			"&6/%command% &e%args%\n&7%description%";
	@Translation(node = "information.usage-argument-format") public static String usageArgumentFormat = 
			"&7<&f%arg%&7>";
	public static String formatUsage(CommandHelp command) {
		// create our args string
		StringBuilder args = new StringBuilder();
		for(String arg: command.args) {
			if(arg != null) {
				args.append(usageArgumentFormat.replaceAll("%arg%", arg)).append(" ");
			}
		}
		
		// get our usage
		String usage =  usageFormat
				.replaceAll("%command%", command.command)
				.replaceAll("%args%", args.toString().trim())
				.replaceAll("%description%", command.description);
		
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
	
	private static boolean hasPermission(CommandSender sender, CommandHelp command) {
		// make sure we can access it properly based on console or not
		// player only command
		if(command.playerOnly && !(sender instanceof Player)) {
			return false;
		}
		// console only command
		else if(command.consoleOnly && sender instanceof Player) {
			return false;
		}
		
		// if we don't have any permissions, we're good
		if(command.permissions.length == 0) {
			return true;
		}
		
		// check if we have any of the permissions necessary
		for(String permission: command.permissions) {
			if(PermissionsManager.playerHasPermission(sender, permission, false)) {
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
		
		// loop over all our help sections
		for(HelpSection section: sections) {
			// loop over all registered commands
			for(CommandHelp ch: section.commands) {
				// check if we found a proper command
				if(ch.command.equalsIgnoreCase(command) || (softMatch && softMatch(ch.command, command))) {
					// make sure we have permission
					if(hasPermission(sender, ch)) {
						// add it
						commandsUsage.add(formatUsage(ch));
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
	public static LinkedList<HelpSection> listAvailableSections(CommandSender sender) {
		LinkedList<HelpSection> possibleSections = new LinkedList<HelpSection>();
		
		// go through all available sections
		for(HelpSection section: sections) {
			boolean valid = false;
			// loop over all registered commands
			for(CommandHelp ch: section.commands) {
				// try to find a command that the sender can use
				if(hasPermission(sender, ch)) {
					valid = true;
					break;
				}
			}
			
			// if we have a valid component, return it
			if(valid) {
				possibleSections.add(section);
			}
		}
		
		return possibleSections;
	}
	
	public static HelpSection findSection(String sectionName) throws EssentialsCommandException {
		// find our component
		HelpSection foundSection = null;
		for(HelpSection section: sections) {
			if(section.name.equalsIgnoreCase(sectionName)) {
				foundSection = section;
				break;
			}
		}
		if(foundSection == null) {
			throw new EssentialsCommandException("I couldn't find the section '%s'!", sectionName);
		}
		return foundSection;
	}
	
	// get a list of all commands within a given component
	public static LinkedList<CommandHelp> listSectionCommands(CommandSender sender, HelpSection section) {
		LinkedList<CommandHelp> possibleCommands = new LinkedList<CommandHelp>();
		
		// loop over all registered commands
		for(CommandHelp ch: section.commands) {
			// make sure we have permission first
			if(hasPermission(sender, ch)) {
				// add it
				possibleCommands.add(ch);
			}
		}
		
		// yup!
		return possibleCommands;
	}

	public void dumpCommandInformation() throws EssentialsCommandException {
		// open our file
		File fileDump = new File(MCNSAEssentials.getInstance().getDataFolder(), "commands.md");
		try {
			PrintWriter out = new PrintWriter(new FileWriter(fileDump));

			// write the header
			out.println("## Components\n");

			// get all of our components
			HashMap<String, Component> components = MCNSAEssentials.getComponentManager().getRegisteredComponents();
			// loop through all our components
			for(String componentName: components.keySet()) {
				// grab our component
				Component component = components.get(componentName);

				// print out the component header
				out.println("### " + component.componentInfo.friendlyName() + "\n");
				out.println(component.componentInfo.description() + "\n");

				// start building a table
				out.println("<table>");
				out.println("\t<tr>");
				out.println("\t\t<th>Command</th>");
				out.println("\t\t<th>Aliases</th>");
				out.println("\t\t<th>Arguments</th>");
				out.println("\t\t<th>Permission Node[s]</th>");
				out.println("\t\t<th>Description</th>");
				out.println("\t</tr>");

				// get our commands
				LinkedList<CommandsManager.CommandInfo> commands = component.commands;
				// loop over them all
				for(CommandsManager.CommandInfo ci: commands) {
					// get our command
					Command command = ci.command;

					// add a row to the table
					out.println("\t<tr>");
					out.println("\t\t<td>" + command.command() + "</td>");
					out.println("\t\t<td>" + StringUtils.implode(", ", command.aliases()) + "</td>");
					out.println("\t\t<td>" + StringUtils.implode(", ", command.arguments()) + "</td>");
					out.println("\t\t<td>" + StringUtils.implode(", ", command.permissions()) + "</td>");
					out.println("\t\t<td>" + command.description() + "</td>");
					out.println("\t</tr>");
				}

				// finish off our table
				out.println("</table>\n");
			}

			// close our file
			out.close();

		}
		catch(Exception e) {
			throw new EssentialsCommandException("Failed to dump commands!");
		}
	}
}
