package com.mcnsa.essentials.utilities;

import org.bukkit.inventory.ItemStack;

public class ItemSelector {
	public static ItemStack selectItem(String name) {
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
		
		
	}
}
