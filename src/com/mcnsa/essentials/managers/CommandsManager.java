package com.mcnsa.essentials.managers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.ComponentManager.Component;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.Logger;
import com.mcnsa.essentials.utilities.SoundUtils;
import com.mcnsa.essentials.utilities.StringUtils;

public class CommandsManager implements TabExecutor {
	// keep track of all known aliases we're using
	private HashSet<String> knownAliases = new HashSet<String>();
	
	// an ``internal'' command structure class to inject into the commandmap with
	public class EssentialsCommand extends org.bukkit.command.Command {
		// keep track of our command executor
		private CommandExecutor commandExecutor = null;
		
		// and tab completor
		private TabCompleter tabCompleter = null;
		
		// just call our default constructor
		protected EssentialsCommand(String name) {
			super(name);
		}

		@Override
		public boolean execute(CommandSender sender, String commandLabel, String[] args) {
			if(commandExecutor != null) {
				// execute the command!
				return commandExecutor.onCommand(sender, this, commandLabel, args);
			}
			return false;
		}
		
		@Override
		public List<String> tabComplete(CommandSender sender, String label, String args[])
				throws IllegalArgumentException {
			return tabCompleter.onTabComplete(sender, this, label, args);
		}

		public void setExecutor(CommandExecutor commandExecutor) {
			this.commandExecutor = commandExecutor;
		}
		
		public void setCompleter(TabCompleter tabCompleter) {
			this.tabCompleter = tabCompleter;
		}
	}
	
	// keep track of our own command info
	public class CommandInfo {
		public Command command = null;
		public Method method = null;
		public ArrayList<String> permissions = null;
	}
	
	// our registered commands and command descriptions (for help)
	protected HashMap<String, CommandInfo> registeredCommands = new HashMap<String, CommandInfo>();
	protected HashMap<String, String> aliasMapping = new HashMap<String, String>();
	
	// call this to load our commands
	public void loadCommands(ComponentManager componentManager) {
		// use reflection to get access to bukkit's command map
		try {			
			// grab our field
			final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			
			// make it accessible
			boolean accessible = commandMapField.isAccessible();
			commandMapField.setAccessible(true);
			
			// now get the actual command map
			CommandMap commandMap = (CommandMap)commandMapField.get(Bukkit.getServer());
			
			// register commands from the class manager
			HashMap<String, Component> registeredComponents = componentManager.getRegisteredComponents();
			for(String component: registeredComponents.keySet()) {
				// and register it's methods
				// but only if its not disabled
				if(!registeredComponents.get(component).disabled) {
					registerComponentCommands(commandMap, registeredComponents.get(component));
				}
			}
			
			// now register all our commands and aliases with bukkit
			final Field commandMapKnownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
			commandMapKnownCommandsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, org.bukkit.command.Command> knownCommands = (HashMap<String, org.bukkit.command.Command>)commandMapKnownCommandsField.get(commandMap);
			for(String knownAlias: knownAliases) {
				// if it's already a bukkit command, overwrite it
				if(commandMap.getCommand(knownAlias) != null) {
					Logger.warning("Overwriting command '%s'!", knownAlias);
					commandMap.getCommand(knownAlias).unregister(commandMap);
				}
				
				// create an actual command, injecting it into the Bukkit commandMap
				EssentialsCommand essentialsCommand = new EssentialsCommand(knownAlias);
				/// manually overwrite the command
				knownCommands.put(knownAlias, essentialsCommand);
				// register our command map to our command
				essentialsCommand.register(commandMap);
				// set our new command's executor to be this class
				essentialsCommand.setExecutor(this);
				// and set our tab completor as well
				essentialsCommand.setCompleter(this);
			}
			
			// restore our commandMap to its former glory
			commandMapField.setAccessible(accessible);
		}
		catch(Exception e) {
			e.printStackTrace();
			Logger.error("Failed to load components / commands!");
		}
	}
	
	// utility function to ensure the function we're trying to load
	// only has basic parameter types
	private Boolean validParameterType(Class<?> type) {
		if(type == int.class) {
			return true;
		}
		else if(type == float.class) {
			return true;
		}
		else if(type == String.class) {
			return true;
		}
		else if(type == String[].class) {
			return true;
		}
		
		return false;
	}
	
	// create a custom command registration string that makes each one unique
	private String buildRegistrationString(CommandInfo ci) {
		String str = new String(ci.command.command());
		
		// add who can execute us
		if(ci.command.consoleOnly()) {
			str += ":c";
		}
		else if(ci.command.playerOnly()) {
			str += ":p";
		}
		else {
			str += ":b";
		}
		
		// now add our arguments
		Class<?>[] parameterTypes = ci.method.getParameterTypes();
		for(int i = 1; i < parameterTypes.length; i++) {
			if(parameterTypes[i] == int.class) {
				str += ":i";
			}
			else if(parameterTypes[i] == float.class) {
				str += ":f";
			}
			else if(parameterTypes[i] == String.class) {
				str += ":s";
			}
			else if(parameterTypes[i] == String[].class) {
				str += ":sa";
			}
		}
		
		return str;
	}
	
	// check to see if the command is already registered or not
	@SuppressWarnings("unused")
	private boolean commandIsRegistered(String command) {
		// check if we have it as an alias
		if(aliasMapping.containsKey(command)) {
			return true;
		}
		
		// no? Well maybe its the normal command then..
		for(String registration: registeredCommands.keySet()) {
			String[] parts = registration.split(":");
			if(parts[0].equals(command)) {
				return true;
			}
		}
		return false;
	}
	
	// go through a given class and register all the commands in it
	private void registerComponentCommands(CommandMap commandMap, Component component) {
		// get our class
		Class<?> cls = component.clazz;
		
		// loop through all our methods in the given class
		for(Method method: cls.getMethods()) {
			// make sure it has the "Command" annotation on it
			if(!method.isAnnotationPresent(Command.class)) {
				continue;
			}
			
			// ok, now make sure the command is static
			if(!Modifier.isStatic(method.getModifiers())) {
				Logger.warning("failed to register command: " + method.getName() + " (not static)");
				continue;
			}
			
			// get the command
			Command command = method.getAnnotation(Command.class);
			
			// create a command info
			CommandInfo ci = new CommandInfo();
			ci.command = command;
			ci.method = method;
			
			// make sure it has an appropriate return value
			if(method.getReturnType() != boolean.class){
				Logger.warning("failed to register command: " + method.getName() + " (doesn't return boolean)");
				continue;
			}
			
			// figure out the arguments
			Class<?>[] parameterTypes = ci.method.getParameterTypes();
			
			// make sure there is at least argument and it is a command sender
			if(parameterTypes.length < 1) {
				Logger.warning("failed to register command: " + method.getName() + " (doesn't have a CommandSender argument as arg0)");
				continue;
			}
			
			// now loop through all the parameter types
			// and fill in the arguments string
			boolean valid = true;
			for(int i = 1; i < parameterTypes.length && valid; i++) {
				if(!validParameterType(parameterTypes[i])) {
					// we don't know what this is!
					Logger.warning("failed to register command method: " + method.getName() + " (unhandle-able parameter type: " + parameterTypes[i].getName() + ")");
					valid = false;
				}
			}
			// check if we need to skip this method
			if(!valid) {
				continue;
			}
			
			// get the list of permissions			
			// and add it to our commandinfo
			String[] permissions = command.permissions();
			if(permissions.length > 0) {
				ci.permissions = new ArrayList<String>();
				
				// add all of our permissions
				// (we will match ANY of these)
				for(int i = 0; i < permissions.length; i++) {
					ci.permissions.add(component.componentInfo.permsSettingsPrefix() + "." + permissions[i]);
				}
			}
			
			// check to see if we have a player / console only annotation
			if(command.playerOnly() && command.consoleOnly()) {
				// we can't have both!
				Logger.warning("failed to register command method: " + method.getName() + " (can't have BOTH ConsoleOnly and PlayerOnly attributes!)");
				continue;
			}
			
			// make sure we're not repeating a command here
			if(registeredCommands.containsKey(ci.command.command())) {
				Logger.warning("failed to register command: " + ci.command.command() + " (command exists in another component)");
				continue;
			}
			
			// build a registration string
			String registrationString = buildRegistrationString(ci);
			
			// use a registration string to register it
			registeredCommands.put(registrationString, ci);
			
			// build an array of all our aliases (including the main command) for this command
			ArrayList<String> commandAndAliases = new ArrayList<String>();
			commandAndAliases.add(ci.command.command());
			for(int i = 0; i < ci.command.aliases().length; i++) {
				commandAndAliases.add(ci.command.aliases()[i]);
			}
			
			// register our aliases
			for(int i = 0; i < commandAndAliases.size(); i++) {
				// make sure its not disabled
				boolean disabled = false;
				for(String cmd: component.disabledCommands) {
					if(cmd.equalsIgnoreCase(commandAndAliases.get(i))) {
						disabled = true;
						break;
					}
				}
				if(disabled) {
					//Logger.debug("Command alias '%s' disabled!", commandAndAliases.get(i));
					continue;
				}
				
				// add this is as a known alias
				knownAliases.add(commandAndAliases.get(i));
				
				// yay!
				if(i == 0) {
					//MCNSAEssentials.log("&aregistered command: " + registrationString);
				}
				else {
					// add it to the alias mapping
					aliasMapping.put(commandAndAliases.get(i), commandAndAliases.get(0));
					
					//MCNSAEssentials.log("\t&aregistered alias `" + commandAndAliases.get(i) + "' for: " + registrationString);
				}
			}
			
			// add our command info to our component
			component.commands.add(ci);
		}
	}
	
	// utility function to determine if someone ignore permissions or not
	private static boolean ignoresPermissions(Player player) {
		// if any of our metadata values come back as true,
		// we are ignoring permissions
		for(MetadataValue val: player.getMetadata("ignorePermissions")) {
			if(val.asBoolean()) {
				return true;
			}
		}
		
		// guess not!
		return false;
	}

	@Override
	// here is where we actually handle commands
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		// handle aliases
		if(aliasMapping.containsKey(label)) {
			label = aliasMapping.get(label);
		}
		
		//Logger.debug("%s ran command %s with args: %s", sender.getName(), command.getName(), StringUtils.implode(", ", args));
		
		// find all our possibilities
		String lastFailMessage = "";
		for(String registrationToken: registeredCommands.keySet()) {
			//MCNSAEssentials.debug("testing " + registrationToken + " against command (" + label + ")");
			String[] registrationParts = registrationToken.split(":");

			if(!registrationParts[0].equals(label)) {
				// nope!
				//MCNSAEssentials.debug("failed " + registrationToken + ": not correct command (" + label + ")");
				continue;
			}
			
			// match the command sender type
			if(registeredCommands.get(registrationToken).command.playerOnly() && !registrationParts[1].equals("p")) {
				// nope, not this one!
				//MCNSAEssentials.debug("failed " + registrationToken + ": player only and not a player");
				continue;
			} 
			else if(registeredCommands.get(registrationToken).command.consoleOnly() && !registrationParts[1].equals("c")) {
				// nope, not this one!
				//MCNSAEssentials.debug("failed " + registrationToken + ": console only and not a console");
				continue;
			}
			
			// ok, this one matches the name and who can execute it
			Class<?>[] params = registeredCommands.get(registrationToken).method.getParameterTypes();
			
			// check the number of arguments
			boolean hasVarArg = false;
			for(int i = 0; i < params.length && !hasVarArg; i++) {
				if(params[i].equals(String[].class)) {
					hasVarArg = true;
				}
			}
			if(!hasVarArg) {
				if((params.length - 1) != args.length) {
					continue;
				}
			}
			
			// check the arguments one by one
			Object[] arguments = new Object[params.length];
			
			// fill in our CommandSender
			arguments[0] = sender;
			
			// skip the CommandSender
			boolean possible = true;
			for(int i = 1; i < params.length && possible; i++) {
				// parse ints next
				if(params[i].equals(int.class)) {
					try {
						int pi = Integer.parseInt(args[i - 1]);
						arguments[i] = pi;
					}
					catch(Exception e) {
						// we didn't supply an int..
						possible = false;
					}
				}
				// floats next
				else if(params[i].equals(float.class)) {
					try {
						float pi = Float.parseFloat(args[i - 1]);
						arguments[i] = pi;
					}
					catch(Exception e) {
						// we didn't supply an int..
						possible = false;
					}
				}
				// strings now
				else if(params[i].equals(String.class)) {
					arguments[i] = args[i - 1];
				}
				// string array
				else if(params[i].equals(String[].class)) {
					String[] argsArray = new String[args.length - (i - 1)];
					
					// fill in the array
					for(int j = 0; j < argsArray.length; j++) {
						argsArray[j] = args[i - 1 + j];
					}
					
					// store the argument
					arguments[i] = argsArray;
					
					// and get out of here, we're done
					i = params.length;
				}
				// something else?
				else {
					possible = false;
				}
			}
			
			if(possible) {
				// we found a possible function!
				CommandInfo ci = registeredCommands.get(registrationToken);
				
				// check permissions first
				if(ci.permissions != null && (sender instanceof Player)) {
					boolean hasPermission = false;
					
					// see if the player is currently ignoring permissions
					if(ignoresPermissions((Player)sender)) {
						hasPermission = true;
					}
					
					// loop through all the permissions and see if we have at least one
					for(Iterator<String> it = ci.permissions.iterator(); it.hasNext() && !hasPermission;) {
						if(PermissionsManager.playerHasPermission((Player)sender, it.next())) {
							hasPermission = true;
						}
					}
					
					// check to see if we have permission
					if(!hasPermission) {
						lastFailMessage = "&cSorry, you don't have permission to do that!";
						continue;
					}
				}
				
				// make sure we have the right person trying to do the command
				if(ci.command.playerOnly() && !(sender instanceof Player)) {
					//lastFailMessage = "&cSorry, that command is for players only";
					lastFailMessage = "playeronly";
					continue;
				}
				else if(ci.command.consoleOnly() && (sender instanceof Player)) {
					//lastFailMessage = "&cSorry, that command is for the console only";
					lastFailMessage = "consoleonly";
					continue;
				}
				
				// finally, call the method
				// the null is because the method must be static
				try {
					boolean result = (Boolean)ci.method.invoke(null, arguments);
					return result;
				}
				catch(Exception e) {					
					if(e.getCause() instanceof EssentialsCommandException) {
						ColourHandler.sendMessage(sender, "&c" + e.getCause().getMessage());
						SoundUtils.errorSound(sender);
						return true;
					}
					else {
						ColourHandler.sendMessage(sender, "&cSomething went wrong! Alert an administrator!");
						SoundUtils.errorSound(sender);
						Logger.error("failed to execute command: " + label + " (" + e.getMessage() + ")");
						e.printStackTrace();
						return false;
					}
				}
			}
		}
		
		// if we got here, we couldn't find a matching function
		if(lastFailMessage.equals("") || lastFailMessage.equals("playeronly") || lastFailMessage.equals("consoleonly")) {			
			// deal with aliases
			String commandName = command.getName();
			if(aliasMapping.containsKey(commandName)) {
				commandName = aliasMapping.get(commandName);
			}
			
			// get the usage string
			String usage = InformationManager.searchUsageString(sender, commandName, false);
			
			// inform them
			if(!usage.equals("")) {
				ColourHandler.sendMessage(sender, "&cInvalid command usage! Proper usage:");
				SoundUtils.notifySound(sender);
				ColourHandler.sendMessage(sender, usage);
			}
			else {
				// we didn't have any commands we could run
				String possibleUsage = InformationManager.searchUsageString(sender, commandName, true);
				
				if(!possibleUsage.equals("")) {
					// found something maybe?
					ColourHandler.sendMessage(sender, "&cInvalid command! Did you mean one of the following?");
					SoundUtils.notifySound(sender);
					ColourHandler.sendMessage(sender, possibleUsage);
				}
				else {
					// no possible matches
					if(lastFailMessage.equals("playeronly")) {
						ColourHandler.sendMessage(sender, "&cSorry, that command is for players only");
					}
					else if(lastFailMessage.equals("consoleonly")) {
						ColourHandler.sendMessage(sender, "&cSorry, that command is for the console only");
					}
					else {
						ColourHandler.sendMessage(sender, "&cInvalid command!");
					}
					SoundUtils.errorSound(sender);
				}
			}
		}
		else {
			ColourHandler.sendMessage(sender, lastFailMessage);
			SoundUtils.errorSound(sender);
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		// handle aliases
		if(aliasMapping.containsKey(label)) {
			label = aliasMapping.get(label);
		}
		
		// find all our possibilities
		LinkedHashSet<String> possibleArguments = new LinkedHashSet<String>();
		for(String registrationToken: registeredCommands.keySet()) {
			// get the registration parts to find our command
			String[] registrationParts = registrationToken.split(":");
			if(!registrationParts[0].equals(label)) {
				// not our command, continue
				continue;
			}
			
			// get our command info
			CommandInfo ci = registeredCommands.get(registrationToken);
			
			// get our argument count
			int argumentCount = args.length;
			
			// only allow commands that have at least as many arguments as the user provided
			if(ci.command.arguments().length < argumentCount) {
				continue;
			}
			
			/*// get our method parameters
			Class<?>[] params = ci.method.getParameterTypes();
			
			// check all our existing arguments for type
			boolean passedArgChecks = true;
			String imploded = "";
			for(String arg: args) {
				imploded += arg + ",";
			}
			// TODO: fix this
			Logger.debug("---\narguments: %s", imploded);
			Logger.debug("testing method %s", registrationToken);
			for(int i = 0; i < (argumentCount - 1) && passedArgChecks; i++) {
				// use [i + 1] on the params so we ignore the CommandSender argument
				// check if the argument is an integer
				if(params[i + 1].equals(int.class)) {
					Logger.debug("\tmethod param[%d] is an int", (i+1));
					// try to parse the given arg as an int
					try {
						Integer.parseInt(args[i]);
					}
					catch(NumberFormatException e) {
						// nope, not an int
						passedArgChecks = false;
						Logger.debug("\t\t%s failed: not int param");
						break;
					}
				}
				// check if the argument is a float
				else if(params[i + 1].equals(float.class)){
					Logger.debug("\tmethod param[%d] is a float", (i+1));
					// try to parse the given arg as a float
					try {
						Float.parseFloat(args[i]);
					}
					catch(NumberFormatException e) {
						// nope, not a float
						passedArgChecks = false;
						Logger.debug("\t\t%s failed: not float param", registrationToken);
						break;
					}
				}
				// check if the argument is a string
				else if(params[i + 1].equals(String.class)){
					// yes, we automatically accept Strings
					Logger.debug("\tmethod param[%d] is a string", (i+1));
				}
				else {
					// something else?
					passedArgChecks = false;
					Logger.debug("\t\t%s failed: unknown param", registrationToken);
					break;
				}
			}
			
			// make sure they passed our argument checks
			if(!passedArgChecks) {
				// nope, continue on!
				continue;
			}*/
			
			// ok, if they got here, that means that all arguments thus far have been decent
			Logger.debug("num pab completions: %d, arg count: %d", ci.command.tabCompletions().length, argumentCount);
			if(ci.command.tabCompletions().length >= argumentCount) {
				Logger.debug("custom handler");
				switch(ci.command.tabCompletions()[argumentCount - 1]) {
				case PLAYER:
					// a list of players
					LinkedList<String> onlineNames = new LinkedList<String>();
					Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
					for(Player online: onlinePlayers) {
						if(online.getName().startsWith(args[argumentCount - 1])) {
							onlineNames.add(online.getName());
						}
					}
					possibleArguments.addAll(onlineNames);
					break;
					
				case WORLD:
					// a list of worlds
					LinkedList<String> worldNames = new LinkedList<String>();
					List<World> worlds = Bukkit.getServer().getWorlds();
					for(World world: worlds) {
						if(world.getName().startsWith(args[argumentCount - 1])) {
							worldNames.add(world.getName());
						}
					}
					possibleArguments.addAll(worldNames);
					break;
					
				case ITEM_NAME:
					possibleArguments.add("<item name>");
					break;
					
				case MOB_NAME:
					possibleArguments.add("<mob name>");
					break;

				case NUMBER:
					possibleArguments.add("1");
					break;
					
				case STRING:
					possibleArguments.add("<" + registeredCommands.get(registrationToken).command.arguments()[argumentCount - 1].replaceAll("\\s+", "_") + ">");
					break;
					
				case IP:
					possibleArguments.add("0.0.0.0");
					break;
					
				case MINECRAFTTIME:
					possibleArguments.add("00:00");
					break;
					
				case DATE:
					Date now = new Date();
					possibleArguments.add(now.toString());
					break;
				}
			}
			else {
				// add it
				Logger.debug("derp");
				possibleArguments.add("<" + registeredCommands.get(registrationToken).command.arguments()[argumentCount - 1].replaceAll("\\s+", "_") + ">");
			}
		}
		
		// remoce duplicates
		LinkedList<String> resultingList = new LinkedList<String>();
		for(String arg: possibleArguments) {
			resultingList.add(arg);
		}
		return resultingList;
	}
}
