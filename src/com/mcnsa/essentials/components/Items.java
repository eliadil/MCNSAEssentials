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
			description = "gives you a head",
			permissions = {"head"},
			playerOnly = true)
	public static boolean giveHead(CommandSender sender, String headName) throws EssentialsCommandException {
		return giveHead(sender, sender.getName(), headName);
	}
	
	@Command(command = "givehead",
			aliases = {"head"},
			arguments = {"target player[s]", "head type / player name"},
			description = "gives target player[s] a head",
			permissions = {"head"})
	public static boolean giveHead(CommandSender sender, String targetPlayer, String headName) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
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
			if(!player.getName().equals(sender.getName())) {
				ColourHandler.sendMessage(player, "&aYou've been given the head of " + headName + " by " + sender.getName());
			}
			else {
				ColourHandler.sendMessage(player, "&aHere's the head of " + headName + "!");
			}
		}
		
		return true;
	}
	
	@Command(command = "i",
			aliases = {"item"},
			arguments = {"item"},
			description = "gives you an item",
			permissions = {"give.self"},
			playerOnly = true)
	public static boolean item(CommandSender sender, String item) throws EssentialsCommandException {
		return item(sender, item, 1);
	}
	
	@Command(command = "i",
			aliases = {"item"},
			arguments = {"item", "amount"},
			description = "gives you an item",
			permissions = {"give.self"},
			playerOnly = true)
	public static boolean item(CommandSender sender, String item, int number) throws EssentialsCommandException {
		return give(sender, sender.getName(), item, number);
	}
	
	@Command(command = "give",
			arguments = {"target player[s]", "item"},
			description = "gives target player[s] an item",
			permissions = {"give.other"})
	public static boolean give(CommandSender sender, String targetPlayer, String item) throws EssentialsCommandException {
		return give(sender, targetPlayer, item, 1);
	}
	
	@Command(command = "give",
			arguments = {"target player[s]", "item", "amount"},
			description = "gives target player[s] an item",
			permissions = {"give.other"})
	public static boolean give(CommandSender sender, String targetPlayer, String item, int number) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
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
			
			if(sender.getName().equalsIgnoreCase(target.getName())) {
				ColourHandler.sendMessage(target, "&aHere's your %d &f%s&a!", number, item);
			}
			else {
				ColourHandler.sendMessage(target, "&a%s gave you %d '&f%s&a'!", sender.getName(), number, item);
			}
		}
		
		return true;
	}
}
