package com.mcnsa.essentials.components;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.ItemSelector;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Items",
				description = "Allows easy giving and enchanting of items",
				permsSettingsPrefix = "items")
public class Items {
	@Command(command = "givehead",
			aliases = {"head"},
			arguments = {"head type / player name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "gives you a head",
			permissions = {"head"},
			playerOnly = true)
	public static boolean giveHead(CommandSender sender, String headName) throws EssentialsCommandException {
		return giveHead(sender, sender.getName(), headName);
	}
	
	@Translation(node = "given-head") public static String givenHead = "&aYou've been given the head of %head% by %player%";
	@Command(command = "givehead",
			aliases = {"head"},
			arguments = {"target player[s]", "head type / player name"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING},
			description = "gives target player[s] a head",
			permissions = {"head"})
	public static boolean giveHead(CommandSender sender, String targetPlayer, String headName) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to give head to!", targetPlayer);
		}
		
		// parse our head type
		short durability = 3;
		if(headName.equalsIgnoreCase("skeleton")) {
			durability = 0;
			headName = "a skeleton";
		}
		else if(headName.equalsIgnoreCase("wither")) {
			durability = 1;
			headName = "a wither";
		}
		else if(headName.equalsIgnoreCase("zombie")) {
			durability = 2;
			headName = "a zombie";
		}
		else if(headName.equalsIgnoreCase("creeper")) {
			durability = 4;
			headName = "a creeper";
		}
		
		// loop through all target players
		for(Iterator<Player> it = targetPlayers.iterator(); it.hasNext();) {
			// grab our player
			Player player = it.next();
			
			// create an item stack to give out
			ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1, durability);
			
			// add meta data if it's a player name
			if(durability == 3) {
				SkullMeta meta = (SkullMeta)stack.getItemMeta();
				meta.setOwner(headName);
				stack.setItemMeta(meta);
			}
			
			// and add it to their inventory
			player.getInventory().addItem(stack);
			
			// alert them!
			ColourHandler.sendMessage(player, givenHead
					.replaceAll("%head%", headName)
					.replaceAll("%player%", sender.getName()));
		}
		
		return true;
	}
	
	@Command(command = "i",
			aliases = {"item"},
			arguments = {"item"},
			tabCompletions = {TabCompleteType.ITEM_NAME},
			description = "gives you an item",
			permissions = {"give.self"},
			playerOnly = true)
	public static boolean item(CommandSender sender, String item) throws EssentialsCommandException {
		return item(sender, item, 1);
	}
	
	@Command(command = "i",
			aliases = {"item"},
			arguments = {"item", "amount"},
			tabCompletions = {TabCompleteType.ITEM_NAME, TabCompleteType.NUMBER},
			description = "gives you an item",
			permissions = {"give.self"},
			playerOnly = true)
	public static boolean item(CommandSender sender, String item, int number) throws EssentialsCommandException {
		return give(sender, sender.getName(), item, number);
	}
	
	@Command(command = "give",
			arguments = {"target player[s]", "item"},
				tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.ITEM_NAME},
			description = "gives target player[s] an item",
			permissions = {"give.other"})
	public static boolean give(CommandSender sender, String targetPlayer, String item) throws EssentialsCommandException {
		return give(sender, targetPlayer, item, 1);
	}
	
	@Translation(node = "given-item") public static String givenItem =
			"&a%player% gave you %amount% '&f%item%&a'!";
	@Command(command = "give",
			arguments = {"target player[s]", "item", "amount"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.ITEM_NAME, TabCompleteType.NUMBER},
			description = "gives target player[s] an item",
			permissions = {"give.other"})
	public static boolean give(CommandSender sender, String targetPlayer, String item, int number) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to give a kit to!", targetPlayer);
		}
		
		// parse our item
		ItemStack itemStack = ItemSelector.selectItem(item);
		
		// set the amount
		number = number > itemStack.getMaxStackSize() ? itemStack.getMaxStackSize() : number;
		itemStack.setAmount(number);
		
		// loop over all our targets
		for(Player target: targetPlayers) {
			// give them the item
			target.getInventory().addItem(itemStack);

			ColourHandler.sendMessage(target, givenItem
					.replaceAll("%player%", sender.getName())
					.replaceAll("%amount%", String.valueOf(number))
					.replaceAll("%item%", itemStack.getType().name().replaceAll("_", " ").toLowerCase()));
		}
		
		return true;
	}
}
