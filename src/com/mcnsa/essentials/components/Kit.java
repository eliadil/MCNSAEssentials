package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.ItemSelector;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Kit",
				description = "Commands to give sets of items",
				permsSettingsPrefix = "kits")
@DatabaseTableInfo(name = "kits",
					fields = { "name TINYTEXT", "items TINYTEXT" })
public class Kit {
	@Translation(node = "available-kits-header") public static String availableKitsHeader = "&6Available Kits:";
	@Translation(node = "available-kits-format") public static String availableKitsFormat = "%s &e(%s)";
	@Command(command = "kits",
			description = "lists all available kits",
			permissions = {"list"})
	public static boolean listKits(CommandSender sender) throws EssentialsCommandException {
		// get a resultset of all our kits
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery("select * from kits;");

		ColourHandler.sendMessage(sender, availableKitsHeader);
		for(HashMap<String, Object> row: results) {
			ColourHandler.sendMessage(sender, availableKitsFormat
					.replaceAll("%name%", (String)row.get("name"))
					.replaceAll("%items%", (String)row.get("items")));
		}
		
		return true;
	}

	@Translation(node = "kit-added") public static String kitAdded = "&aYour kit '%name%' has been added!";
	@Command(command = "newkit",
			aliases = {"addkit"},
			arguments = {"name", "items"},
			tabCompletions = {TabCompleteType.STRING, TabCompleteType.STRING},
			description = "Adds a new kit with a specific name and the list of items (separated with ;'s)",
			permissions = {"new"})
	public static boolean newKit(CommandSender sender, String kitName, String kitItems) throws EssentialsCommandException {
		// add our kit
		int results = DatabaseManager.updateQuery("insert into kits (id, name, items) values (NULL, ?, ?);",
				kitName, kitItems);
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to add a new kit!");
		}
		
		ColourHandler.sendMessage(sender, kitAdded.replaceAll("%name%", kitName));
		
		return true;
	}

	@Command(command = "kit",
			arguments = {"desired kit"},
			tabCompletions = {TabCompleteType.STRING},
			description = "gives you your desired kit",
			permissions = {"give.self"},
			playerOnly = true)
	public static boolean kit(CommandSender sender, String kit) throws EssentialsCommandException {
		return kit(sender, sender.getName(), kit);
	}

	@Translation(node = "recieved-kit") public static String recievedKit = "&aYou recieved the kit '&f%kit%&a'!";
	@Command(command = "kit",
			arguments = {"target player[s]", "desired kit"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING},
			description = "gives target player[s] your desired kit",
			permissions = {"give.others"})
	public static boolean kit(CommandSender sender, String targetPlayer, String kit) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to give a kit to!", targetPlayer);
		}
		
		// make sure our kit is valid
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from kits where name=?;",
				kit);
		if(results.size() != 1) {
			throw new EssentialsCommandException("I couldn't find the kit '%s' to give!", kit);
		}
		
		// get our kit string
		String kitDefinition = (String)results.get(0).get("items");
		String[] kitItems = kitDefinition.split(";");
		
		// get our kit items
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		for(String kitItem: kitItems) {
			ItemStack item = ItemSelector.selectItem(kitItem);
			items.add(item);
		}
		
		// loop over all our targets
		for(Player target: targetPlayers) {
			// give our players their items
			target.getInventory().addItem(items.toArray(new ItemStack[items.size()]));
			
			ColourHandler.sendMessage(target, recievedKit.replaceAll("%kit%", kit));
		}
		
		return true;
	}
}
