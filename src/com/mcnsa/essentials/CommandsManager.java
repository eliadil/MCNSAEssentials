package com.mcnsa.essentials;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.mcnsa.essentials.utilities.Command;

// NOTE: use reflection to register commands natively
// http://forums.bukkit.org/threads/register-command-without-plugin-yml.112932/#post-1430463

public class CommandsManager {
	public class CommandInfo {
		public Command command;
		public Method method;
	}
	
	protected HashMap<String, CommandInfo> registeredCommands = new HashMap<String, CommandInfo>();
	protected HashMap<String, String> descriptions = new HashMap<String, String>();
	
	void registerCommands(Class<?> cls) {
		// loop through all our methods in the given class
		for(Method method: cls.getMethods()) {
			// make sure it has the "Command" annotation on it
			if(!method.isAnnotationPresent(Command.class)) {
				continue;
			}
			
			// ok, now make sure the command is static
			if(!Modifier.isStatic(method.getModifiers())) {
				MCNSAEssentials.warning("failed to register command: " + method.getName() + " (not static)");
				continue;
			}
			
			// get the command
			Command command = method.getAnnotation(Command.class);
			
			// create a command info
			CommandInfo ci = new CommandInfo();
			ci.command = command;
			ci.method = method;
			
			// register all the aliases
			String[] aliases = command.aliases();
			for(int i = 0; i < aliases.length; i++) {
				// only register it if doesn't exist yet
				if(registeredCommands.containsKey(aliases[i])) {
					MCNSAEssentials.warning("failed to register command: " + aliases[i] + " (already existed)");
					continue;
				}
				else {
					// register it
					registeredCommands.put(aliases[i], ci);
				}
			}
		}
	}
}
