package com.mcnsa.essentials.components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.Logger;

@ComponentInfo(friendlyName = "Backpack",
				description = "Gives players a backpack to use",
				permsSettingsPrefix = "backpack")
public class Backpack implements Listener {
	// taken from ThePickleMan's implementation at
	// https://github.com/ThePickleMan/HatCraft/
	public class BackpackInventory implements InventoryHolder {
		private Player player = null;
		private Inventory inventory = null;
		
		public Player getPlayer() {
			return player;
		}
		
		@Override
		public Inventory getInventory() {
			return inventory;
		}
		
		public BackpackInventory(int size, Player player, ItemStack[] contents) {
			// store our player
			this.player = player;
			
			// store our inventory
			inventory = Bukkit.getServer().createInventory(this,
					size,
					String.format("%s's Backpack", player.getName(), size));
			
			// and fill it up
			if(contents != null) {
				inventory.setContents(contents);
			}
		}
	}
	
	private static int getMaxBackpackSize(Player player) {
		for(int size = 54; size > 0; size -= 9) {
			if(PermissionsManager.playerHasPermission(player, String.format("backpack.size.%d", size))) {
				return size;
			}
		}
		return 0;
	}
	
	private static Backpack instance = null;
	private File configFile = null;
	private static YamlConfiguration config = null;
	
	public Backpack() {
		instance = this;
		
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
		
		// load our backpack file
		configFile = new File(MCNSAEssentials.getInstance().getDataFolder(), "backpacks.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
	}
	
	// bukkit events
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) throws IOException {
		// make sure they're closing a backpack
		if(event.getInventory().getHolder() instanceof BackpackInventory) {
			// grab our backpack
			BackpackInventory backpack = (BackpackInventory)event.getInventory().getHolder();
			
			// get our backpack key
			String key = backpack.getPlayer().getName();
			
			// get our inventory
			ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
			Collections.addAll(stacks, backpack.getInventory().getContents());
			
			// now save it to file
			config.set(key, stacks);
			config.save(configFile);
		}
	}
	
	// our commands
	@Command(command = "backpack",
			description = "opens your backpack",
			permissions = {"self"},
			playerOnly = true)
	public static boolean backpack(CommandSender sender) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// get our stored inventory
		String configKey = player.getName();
		ItemStack[] inv = config.contains(configKey) ? config.getList(configKey).toArray(new ItemStack[0]) : null;
		
		// get the backpack size
		int size = getMaxBackpackSize(player);
		if(size == 0) {
			throw new EssentialsCommandException("You don't have a backpack to open!");
		}
		Logger.debug("Backpack size: %d", size);
		
		// and open the backpack
		BackpackInventory backpack = instance.new BackpackInventory(size, player, inv);
		player.openInventory(backpack.getInventory());
		
		return true;
	}
}
