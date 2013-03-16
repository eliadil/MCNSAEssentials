package com.mcnsa.essentials.utilities;

import java.util.HashMap;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.essentials.exceptions.CommandException;

public class ItemSelector {
	private static HashMap<String, Integer> itemNames = new HashMap<String, Integer>();
	
	// format: <id/name>:<data value/name>|<enchantment 1 name:level>,<enchantment 2 name:level>,...
	public static ItemStack selectItem(String name) throws CommandException {
		int id, dmg = 0;
		String dataName = null;
		String enchantmentName = null;
		
		// do we have an enchantment name?
		if(name.contains("|")) {
			String[] parts = name.split("\\|", 2);
			name = parts[0];
			enchantmentName = parts[1];
		}
		
		// do we have a data name?
		if(name.contains(":")) {
			String[] parts = name.split(":", 2);
			dataName = parts[1];
			name = parts[0];
		}
		
		// try to parse the name to get an item ID
		try {
			id = Integer.parseInt(name);
		}
		catch(NumberFormatException e) {
			// ok, it wasn't a number
			// maybe it was a name?
			Integer idTemp = itemNames.get(name);
			
			if(idTemp != null) {
				// yup, we found it
				id = idTemp;
			}
			else {
				// nope, unfortunately :s
				throw new CommandException("Couldn't find item type known by %s!", name);
			}
		}
		
		// parse the damage / data string
		if(dataName != null) {
			dmg = parseItemData(id, dataName);
		}
		
		// ok, create an item now
		ItemStack stack = new ItemStack(id, 1, (short)dmg);
		
		// try to enchant it
		if(enchantmentName != null) {
			// get a list of all desired enchantments
			String[] enchantments = enchantmentName.split(",");
			// and loop over them all
			for(String enchantmentStr: enchantments) {
				int level = 1;
				if(enchantmentStr.contains(":")) {
					// we have a specific level
					String[] parts = enchantmentStr.split(":");
					enchantmentStr = parts[0];
					try {
						level = Integer.parseInt(parts[1]);
					}
					catch(NumberFormatException e) {
						// do nothing
					}
				}
				
				// ok, we have our level sorted out
				// make an enchantment
				Enchantment enchantment = null;
				final String testName = enchantmentStr.toLowerCase().replace("[_\\-]", "");
				for(Enchantment possible: Enchantment.values()) {
					if(possible.getName().toLowerCase().replace("[_\\-]", "").equals(testName)) {
						// we found it!
						enchantment = possible;
						break;
					}
				}
				
				// make sure we found it
				if(enchantment == null) {
					throw new CommandException("Unknown enchantment '%s'!", enchantmentStr);
				}
				
				// make sure we can enchant our stack
				if(!enchantment.canEnchantItem(stack)) {
					throw new CommandException("Item '%s' cannot be enchanted with %s!", name, enchantment.getName().toLowerCase().replace("[_\\-]", ""));
				}
				
				// and make sure the level is acceptable too
				if(level > enchantment.getMaxLevel()) {
					throw new CommandException("Level %d is over the maximum enchantment level for the %s enchantment!", level, enchantment.getName().toLowerCase().replace("[_\\-]", ""));
				}
				
				// ok, if we made it here, we're good
				// enchant the stack
				stack.addEnchantment(enchantment, level);
			}
		}
		
		// ok, we're done
		return stack;
	}
	
	public static int parseItemData(int itemID, String filter) throws CommandException {
		// first, check to see if it was a straight up number
		try {
			return Integer.parseInt(filter);
		}
		catch(NumberFormatException e) {
			// we don't care
		}
		
		// ok, it wasn't a #
		// handle it based on what the itemID is
		switch(itemID) {
		case ItemID.WOOD:
			if(filter.equalsIgnoreCase("oak")) {
				return 1;
			}
			else if(filter.equalsIgnoreCase("redwood") || filter.equalsIgnoreCase("spruce")) {
				return 1;
			}
			else if(filter.equalsIgnoreCase("birch")) {
				return 2;
			}
			else if(filter.equalsIgnoreCase("jungle")) {
				return 3;
			}
			
			// if we got here, we didn't find it
			throw new CommandException("Unkown wood type '%s'!", filter);
			
		// TODO: more data values
		default:
			throw new CommandException("Invalid data value of '%s'!", filter);
		}
	}
	
	static {
		// TODO: load items from database
		// use some default things for now
	}
}
