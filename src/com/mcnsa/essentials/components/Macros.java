package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.interfaces.MultilineChatHandler;
import com.mcnsa.essentials.managers.ConversationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "Macros",
				description = "Easy way to group numerous commands into a single command",
				permsSettingsPrefix = "macros")
@DatabaseTableInfo(name = "macros",
					fields = { "name TINYTEXT", "permission TINYTEXT", "tempGroup TINYTEXT", "command TEXT" })
public class Macros implements MultilineChatHandler {	
	static Macros instance = null;
	public Macros() {
		instance = this;
	}
	
	// utility command to ignore the temporary group
	@Command(command  = "definemacro",
			arguments = {"macro name", "required permission"},
			tabCompletions = {TabCompleteType.STRING, TabCompleteType.STRING},
			description = "start defining a new macro",
			permissions = {"define"})
	public static boolean defineMacro(CommandSender sender, String macroName, String requiredPermission) throws EssentialsCommandException {
		return defineMacro(sender, macroName, requiredPermission, "");
	}
	
	// allow players to define macros
	@Command(command  = "definemacro",
			arguments = {"macro name", "required permission", "temporary group"},
			tabCompletions = {TabCompleteType.STRING, TabCompleteType.STRING, TabCompleteType.STRING},
			description = "start defining a new macro",
			permissions = {"define"})
	public static boolean defineMacro(CommandSender sender, String macroName, String requiredPermission, String tempGroup) throws EssentialsCommandException {
		// first determine if our macro exists or not
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from macros where name=? limit 1;",
				macroName);
		Boolean exists = results.size() != 0;
		
		// make sure the group they're considering adding to exists
		if(!tempGroup.equals("")) {
			// yup, there is
			// add the player to it
			PermissionGroup group = PermissionsManager.permissions.getGroup(tempGroup);
			
			// make sure it exists
			if(group == null) {
				throw new EssentialsCommandException("Required group doesn't exist!");
			}
		}
		
		// alert them
		ColourHandler.sendMessage(sender, "Begin entering commands for the macro '%s'...", macroName);
		
		// and start a conversation
		ConversationManager.startConversation(sender, instance, macroName, requiredPermission, tempGroup, exists);
		
		return true;
	}

	// actually store defined macros in the database here
	@Override
	public void onChatComplete(CommandSender sender, String commands, Object... args) throws EssentialsCommandException {
		// make sure we received all of our arguments correctly
		if(args.length != 4) {
			throw new EssentialsCommandException("Something went wrong! Please contact an administrator!");
		}
		
		// grab our arguments
		String macroName = (String)args[0];
		String requiredPermission = (String)args[1];
		String tempGroup = (String)args[2];
		Boolean exists = (Boolean)args[3];
		
		// update the database
		if(exists) {
			// update an existing record
			int result = DatabaseManager.updateQuery(
					"update macros set permission=?, tempGroup=?, command=? where name=?;",
					requiredPermission,
					tempGroup,
					commands,
					macroName);
			
			// make sure it worked
			if(result != 1) {
				throw new EssentialsCommandException("Failed to update the macro '%s'!", macroName);
			}
		}
		else {
			// insert a new record
			// update an existing record
			int result = DatabaseManager.updateQuery(
					"insert into macros(id, name, permission, tempGroup, command) values(NULL, ?, ?, ?, ?);",
					macroName,
					requiredPermission,
					tempGroup,
					commands);
			
			// make sure it worked
			if(result != 1) {
				throw new EssentialsCommandException("Failed to add the macro '%s'!", macroName);
			}
		}
		
		// alert them
		ColourHandler.sendMessage(sender, "Your macro '%s' has been saved!", macroName);
	}
	
	// list all macros that the sender can use
	@Command(command  = "macros",
			aliases = "listmacros",
			description = "list all macros that you have permission to use",
			permissions = {"list"})
	public static boolean listMacros(CommandSender sender) throws EssentialsCommandException {
		// get a list of all our macros
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from macros order by name asc;");
		
		// build a string of all macros we can use
		StringBuilder validMacrosString = new StringBuilder();
		// loop over all our results and check the permissions node for each
		for(HashMap<String, Object> result: results) {
			if(PermissionsManager.playerHasPermission(sender, "macros.run." + result.get("permission"))) {
				// yup, they have permission
				if(!validMacrosString.toString().equals("")) {
					validMacrosString.append("&6, ");
				}
				validMacrosString.append("&f").append(result.get("name"));
			}
		}
		
		// tell them
		ColourHandler.sendMessage(sender, "&6You have access to the following macros: %s", validMacrosString);
		
		return true;
	}
	
	// list all the commands that a macro will run
	@Command(command  = "viewmacro",
			aliases = {"inspectmacro", "macrocommands"},
			arguments = {"macro name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "inspect the commands that a given macro will execute",
			permissions = {"inspect"})
	public static boolean viewMacro(CommandSender sender, String macroName) throws EssentialsCommandException {
		// first determine if our macro exists or not
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from macros where name=? limit 1;",
				macroName);
		Boolean exists = results.size() != 0;
		
		// make sure it exists
		// and that they have permission to see it
		if(!exists || !PermissionsManager.playerHasPermission(sender, "macros.run." + results.get(0).get("permission"))) {
			throw new EssentialsCommandException("The macro '%s' doesn't exist!", macroName);
		}
		
		// inform them!
		ColourHandler.sendMessage(sender, "&6The macro '&f%s&6' executes the following commands:", macroName);
		ColourHandler.sendMessage(sender, (String)results.get(0).get("command"));
		
		return true;
	}
	
	// delete an existing macro
	@Command(command  = "deletemacro",
			arguments = {"macro name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "delete an existing macro",
			permissions = {"delete"})
	public static boolean deleteMacro(CommandSender sender, String macroName) throws EssentialsCommandException {
		// first determine if our macro exists or not
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from macros where name=? limit 1;",
				macroName);
		Boolean exists = results.size() != 0;
		
		// make sure it exists
		if(!exists) {
			throw new EssentialsCommandException("The macro '%s' doesn't exist!", macroName);
		}
		
		// ok, it exists. Delete it!
		int result = DatabaseManager.updateQuery(
				"delete from macros where name=? limit 1;",
				macroName);
		
		// make sure we deleted it
		if(result != 1) {
			throw new EssentialsCommandException("Failed to delete the macro '%s'!", macroName);
		}
		
		// and tell them
		ColourHandler.sendMessage(sender, "The macro '%s' has been deleted!", macroName);
		
		return true;
	}
	
	// actually run a macro
	@Command(command  = "macro",
			arguments = {"macro name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "run an existing macro",
			permissions = {"run"})
	public static boolean runMacro(CommandSender sender, String macroName) throws EssentialsCommandException {
		// first determine if our macro exists or not
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from macros where name=? limit 1;",
				macroName);
		Boolean exists = results.size() != 0;
		
		// make sure it exists
		// and that they have permission to see it
		if(!exists || !PermissionsManager.playerHasPermission(sender, "macros.run." + results.get(0).get("permission"))) {
			throw new EssentialsCommandException("The macro '%s' doesn't exist!", macroName);
		}
		
		// if there is a temporary group specified, add them
		String tempGroup = (String)results.get(0).get("tempGroup");
		// but only if they're a player
		if(sender instanceof Player && !tempGroup.equals("")) {
			// yup, there is
			// add the player to it
			PermissionGroup group = PermissionsManager.permissions.getGroup(tempGroup);
			
			// make sure it exists
			if(group == null) {
				throw new EssentialsCommandException("Required group doesn't exist!");
			}
			
			// get our user
			PermissionUser user = PermissionsManager.permissions.getUser((Player)sender);
			if(user == null) {
				throw new EssentialsCommandException("Something went wrong - you don't exist in the permissions record!");
			}
			
			// add them to the group
			user.addGroup(group);
		}
		
		// get the commands
		String[] commands = ((String)results.get(0).get("command")).split("\n");
		
		// inform them that they're doing something
		ColourHandler.sendMessage(sender, "&6Running commands...");
		
		// and loop over the commands, running each one
		for(String command: commands) {
			if(command.startsWith("/")) {
				command = command.substring(1);
			}
			if(sender instanceof Player) {
				((Player)sender).performCommand(command);
			}
			else {
				Bukkit.getServer().dispatchCommand(sender, command);
			}
		}
		
		// and that they're done
		ColourHandler.sendMessage(sender, "&6Done!");
		
		// remove them from the temporary group
		if(sender instanceof Player && !tempGroup.equals("")) {
			// yup, there is
			// add the player to it
			PermissionGroup group = PermissionsManager.permissions.getGroup(tempGroup);
			
			// make sure it exists
			if(group == null) {
				throw new EssentialsCommandException("Group no longer exists");
			}
			
			// get our user
			PermissionUser user = PermissionsManager.permissions.getUser((Player)sender);
			if(user == null) {
				throw new EssentialsCommandException("Something went wrong - you don't exist in the permissions record!");
			}
			
			// remove them from the group
			user.removeGroup(group);
		}
		
		return true;
	}
}
