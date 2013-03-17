package com.mcnsa.essentials.components;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.command.CommandSender;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "Kit",
				description = "Commands to give sets of items",
				permsSettingsPrefix = "kit")
@DatabaseTableInfo(name = "kits",
					fields = { "name TINYTEXT", "items TINYTEXT" })
public class Kit {
	@Command(command = "kits",
			description = "lists all available kits",
			permissions = {"list"})
	public static boolean listKits(CommandSender sender) throws SQLException, EssentialsCommandException {
		// get a resultset of all our kits
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery("select * from kits;");

		ColourHandler.sendMessage(sender, "&6Available Kits:");
		for(HashMap<String, Object> row: results) {
			ColourHandler.sendMessage(sender, "%s &e(%s)", row.get("name"), row.get("items"));
		}
		
		return true;
	}

	@Command(command = "newkit",
			arguments = {"name", "items"},
			description = "Adds a new kit with a specific name and the list of items (separated with ;'s)",
			permissions = {"new"})
	public static boolean newKit(CommandSender sender, String kitName, String kitItems) throws EssentialsCommandException {
		// add our kit
		int results = DatabaseManager.updateQuery("insert into kits (id, name, items) values (NULL, ?, ?);", kitName, kitItems);
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to add a new kit!");
		}
		
		ColourHandler.sendMessage(sender, "&aYour kit '%s' has been added!", kitName);
		
		return true;
	}
}
