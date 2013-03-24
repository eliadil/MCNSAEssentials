package com.mcnsa.essentials.utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;

public class ItemSelector {
	private static HashMap<String, Integer> itemNames = new HashMap<String, Integer>();
	
	// format: <id/name>:<data value/name>|<enchantment 1 name:level>,<enchantment 2 name:level>,...
	public static ItemStack selectItem(String name) throws EssentialsCommandException {
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
				throw new EssentialsCommandException("Couldn't find item type known by %s!", name);
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
					throw new EssentialsCommandException("Unknown enchantment '%s'!", enchantmentStr);
				}
				
				// make sure we can enchant our stack
				if(!enchantment.canEnchantItem(stack)) {
					throw new EssentialsCommandException("Item '%s' cannot be enchanted with %s!", name, enchantment.getName().toLowerCase().replace("[_\\-]", ""));
				}
				
				// and make sure the level is acceptable too
				if(level > enchantment.getMaxLevel()) {
					throw new EssentialsCommandException("Level %d is over the maximum enchantment level for the %s enchantment!", level, enchantment.getName().toLowerCase().replace("[_\\-]", ""));
				}
				
				// ok, if we made it here, we're good
				// enchant the stack
				stack.addEnchantment(enchantment, level);
			}
		}
		
		// ok, we're done
		return stack;
	}
	
	public static DyeColor matchDyeColour(String colour) throws EssentialsCommandException {		
		// taken from CommandBook
		if(colour.equalsIgnoreCase("random")) {
		    return DyeColor.getByDyeData((byte) new Random().nextInt(15));
		}
		try {
		    DyeColor match = DyeColor.valueOf(colour.toUpperCase());
		    if (match != null) {
		        return match;
		    }
		}
		catch (IllegalArgumentException ignored) {
			
		}
		throw new EssentialsCommandException("Unknown dye color name of '%s'!", colour);
	}
	
	public static int parseItemData(int itemID, String filter) throws EssentialsCommandException {
		// first, check to see if it was a straight up number
		try {
			return Integer.parseInt(filter);
		}
		catch(NumberFormatException e) {
			// we don't care
		}
		
		// TODO: load data value aliases
		
		throw new EssentialsCommandException("Invalid data value of '%s'!", filter);
	}
	
	private static File fileConfig = new File(MCNSAEssentials.getInstance().getDataFolder(), "items.yml");
	private static YamlConfiguration yamlConfig = new YamlConfiguration();
	public static void load() {
		// extract a default config
		if(!fileConfig.exists()) {
			MCNSAEssentials.getInstance().saveResource("items.yml", false);
		}
		
		// load items from a config file
		try {
			yamlConfig.load(fileConfig);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// get our items configuration section
		ConfigurationSection items = yamlConfig.getConfigurationSection("items");
		
		// get all our keys (names)
		Set<String> keys = items.getKeys(false);
		for(String itemName: keys) {
			itemNames.put(itemName, items.getInt(itemName));
		}
	}
}
