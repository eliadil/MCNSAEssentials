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
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.PermissionsManager;

@ComponentInfo(friendlyName = "Backpack",
				description = "Gives players a backpack to use",
				permsSettingsPrefix = "backpack")
public class Backpack implements Listener {
	@Translation(node = "you-dont-have-a-backpack") public static String youDontHaveABackpack = "You don't have a backpack to open!";
	@Translation(node = "player-doesnt-have-a-backpack") public static String playerDoesntHaveABackpack = "%player% doesn't have a backpack to open!";
	
	// taken from ThePickleMan's implementation at
	// https://github.com/ThePickleMan/HatCraft/
	public class BackpackInventory implements InventoryHolder {		
		private String ownerName = null;
		private Inventory inventory = null;
		
		public String getPlayer() {
			return ownerName;
		}
		
		@Override
		public Inventory getInventory() {
			return inventory;
		}
		
		public BackpackInventory(int size, String ownerName, ItemStack[] contents) {
			// store our player
			this.ownerName = ownerName;
			
			// store our inventory
			inventory = Bukkit.getServer().createInventory(this,
					size,
					String.format("%s's Backpack", ownerName, size));
			
			// and fill it up
			if(contents != null) {
				inventory.setContents(contents);
			}
		}
	}
	
	private static int getMaxBackpackSize(String player) {
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
			String key = backpack.getPlayer();
			
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
		return backpack(sender, sender.getName());
	}

	@Command(command = "backpack",
			arguments = {"target player"},
			description = "opens target player's backpack",
			permissions = {"other"},
			playerOnly = true)
	public static boolean backpack(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get our player
		Player target = Bukkit.getServer().getPlayer(targetPlayer);
		if(target != null) {
			targetPlayer = target.getName();
		}
		
		// get our stored inventory
		String configKey = targetPlayer;
		ItemStack[] inv = config.contains(configKey) ? config.getList(configKey).toArray(new ItemStack[0]) : null;
		
		if(inv == null) {
			if(sender.getName().equalsIgnoreCase(targetPlayer)) {
				throw new EssentialsCommandException(youDontHaveABackpack);
			}
			else {
				throw new EssentialsCommandException(playerDoesntHaveABackpack.replaceAll("%player%", targetPlayer));
			}
		}
		
		// get the backpack size
		int size = getMaxBackpackSize(targetPlayer);
		if(size == 0) {
			if(sender.getName().equalsIgnoreCase(targetPlayer)) {
				throw new EssentialsCommandException(youDontHaveABackpack);
			}
			else {
				throw new EssentialsCommandException(playerDoesntHaveABackpack.replaceAll("%player%", targetPlayer));
			}
		}
		
		// and open the backpack
		BackpackInventory backpack = instance.new BackpackInventory(size, targetPlayer, inv);
		((Player)sender).openInventory(backpack.getInventory());
		return true;
	}
}
