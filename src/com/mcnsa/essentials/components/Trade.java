package com.mcnsa.essentials.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.Logger;
import com.mcnsa.essentials.utilities.SoundUtility;

@ComponentInfo(friendlyName = "Trade",
				description = "Allows safe trading between players",
				permsSettingsPrefix = "trade")
public class Trade implements Listener {
	private enum SlotType { WALL, ACCEPT, REVOKE, STATUS, TRADEABLE };
	// define our actual trade window here
	public class TradeInventoryHolder implements InventoryHolder {
		private Player owner = null;
		private Inventory inventory = null;
		private String title = null;
		private HashMap<Integer, SlotType> slots = new HashMap<Integer, SlotType>();
		private boolean accepted = false;
		
		@Override
		public Inventory getInventory() {
			return inventory;
		}
		
		public TradeInventoryHolder(Player owner, Player other) {
			// store our owner
			this.owner = owner;
			
			// create our title
			title = new String("     You");
			while(title.length() + other.getName().length() < 32) {
				title += " ";
			}
			title += other.getName();
			
			// create our inventory
			inventory = Bukkit.getServer().createInventory(this,
					54,
					title);
			
			// fill in the slots
			int[] slotIDs = {4, 13, 22, 31, 40, 45, 46, 47, 48, 49, 50, 51, 52, 53};
			SlotType[] slotTypes = {SlotType.WALL, SlotType.WALL, SlotType.WALL, SlotType.WALL, SlotType.WALL,
					SlotType.WALL, SlotType.ACCEPT, SlotType.REVOKE, SlotType.WALL, SlotType.WALL, SlotType.WALL,
					SlotType.WALL, SlotType.WALL, SlotType.STATUS};
			for(int i = 0; i < slotIDs.length; i++) {
				slots.put(slotIDs[i], slotTypes[i]);
				switch(slotTypes[i]) {
				case WALL:
					inventory.setItem(slotIDs[i], new ItemStack(Material.OBSIDIAN));
					break;
				case ACCEPT:
					inventory.setItem(slotIDs[i], new ItemStack(Material.WOOL, 1, (short)14));
					break;
				case REVOKE:
					inventory.setItem(slotIDs[i], new ItemStack(Material.BLAZE_ROD));
					break;
				case STATUS:
					inventory.setItem(slotIDs[i], new ItemStack(Material.WOOL, 1, (short)0));
					break;
				}
			}
		}
		
		public SlotType getSlotType(int slotID) {
			if(!slots.containsKey(slotID)) {
				return SlotType.TRADEABLE;
			}
			return slots.get(slotID);
		}
		
		boolean getAccepted() {
			return accepted;
		}
		
		// change whether we're accepting the trade or not
		public void setAccepted(boolean accepted) {
			this.accepted = accepted;

			for(Integer id: slots.keySet()) {
				if(slots.get(id).equals(SlotType.ACCEPT)) {
					if(accepted) {
						inventory.setItem(id, new ItemStack(Material.WOOL, 1, (short)5));
					}
					else {
						inventory.setItem(id, new ItemStack(Material.WOOL, 1, (short)14));
					}
				}
			}
		}
		
		// change the status item (showing whether the other player accepted or not)
		public void setStatus(boolean accepted) {
			for(Integer id: slots.keySet()) {
				if(slots.get(id).equals(SlotType.STATUS)) {
					if(accepted) {
						inventory.setItem(id, new ItemStack(Material.WOOL, 1, (short)5));
					}
					else {
						inventory.setItem(id, new ItemStack(Material.WOOL, 1, (short)0));
					}
				}
			}
		}
		
		public void revokeItems(boolean partner) {
			// loop over all our slots
			for(int i = 0; i < inventory.getSize(); i++) {
				// skip UI components
				if(slots.containsKey(i)) {
					continue;
				}
				
				// skip the other player's stuff
				if(!partner && (i % 9) >= 4) {
					continue;
				}
				// or our stuff, if they're revoking
				else if(partner && (i % 9) < 4) {
					continue;
				}
				
				// get the itemstack
				ItemStack item = inventory.getItem(i);
				// only do anything with it if it exists
				if(item != null && item.getAmount() > 0) {
					// add it to our inventory
					if(!partner) {
						owner.getInventory().addItem(item.clone());
					}
					
					// and remove it from the trade
					inventory.setItem(i, new ItemStack(0));
				}
			}
		}
		
		public boolean canPlaceThere(int id) {			
			// make sure it's not part of the UI
			if(slots.containsKey(id) && !slots.get(id).equals(SlotType.TRADEABLE)) {
				return false;
			}
			
			// we can only place things on the left hand side
			int x = id % 9;
			return x < 4;
		}
		
		public void updatePartnerItem(ItemStack newItem, int oldSlotID) {
			inventory.setItem(oldSlotID + 5, newItem);
		}
		
		public LinkedList<ItemStack> getItems() {
			LinkedList<ItemStack> myItems = new LinkedList<ItemStack>();
			// loop over all our slots
			for(int i = 0; i < inventory.getSize(); i++) {
				// skip UI components
				if(slots.containsKey(i)) {
					continue;
				}
				
				// skip the other player's stuff
				if((i % 9) >= 4) {
					continue;
				}
				
				// get the itemstack
				ItemStack item = inventory.getItem(i);
				// only do anything with it if it exists
				if(item != null && item.getTypeId() != 0 && item.getAmount() > 0) {
					myItems.add(item);
				}
			}
			
			return myItems;
		}
	}
	
	// tool to keep track of trades
	public class TradeExchange {
		Player playerA = null, playerB = null;
		TradeInventoryHolder holderA = null, holderB = null;
		
		public boolean getAccepted(Player target) {
			if(target.equals(playerA)) {
				return holderA.accepted;
			}
			else {
				return holderB.accepted;
			}
		}
		
		private void doTrade() {
			// get the items
			LinkedList<ItemStack> itemsA = holderA.getItems();
			LinkedList<ItemStack> itemsB = holderB.getItems();
			
			// add them to the inventories
			playerA.getInventory().addItem(itemsB.toArray(new ItemStack[itemsB.size()]));
			playerB.getInventory().addItem(itemsA.toArray(new ItemStack[itemsB.size()]));
			
			// close the inventories
			playerA.closeInventory();
			playerB.closeInventory();
			
			// and clear both players of their trading metadata
			playerA.removeMetadata("tradeExchange", MCNSAEssentials.getInstance());
			playerB.removeMetadata("tradeExchange", MCNSAEssentials.getInstance());
			
			// send messages
			ColourHandler.sendMessage(playerA, "&cYou completed the trade with %s!", playerB.getName());
			ColourHandler.sendMessage(playerB, "&cYou completed the trade with %s!", playerA.getName());
			
			// play sounds
			SoundUtility.confirmSound(playerA);
			SoundUtility.confirmSound(playerB);
		}
		
		public void setAccepted(Player target, boolean accepted) {
			if(target.equals(playerA)) {
				holderA.setAccepted(accepted);
				holderB.setStatus(accepted);
			}
			else {
				holderB.setAccepted(accepted);
				holderA.setStatus(accepted);
			}
			
			// now check if both players have accepted
			// and if they have, do the trade!
			if(holderA.getAccepted() && holderB.getAccepted()) {
				doTrade();
			}
		}
		
		public void revoke(Player revoker) {
			if(revoker.equals(playerA)) {
				holderA.revokeItems(false);
				holderB.revokeItems(true);
			}
			else {
				holderB.revokeItems(false);
				holderA.revokeItems(true);
			}
		}
		
		public void cancelTrade(Player canceller) {
			// take all items back
			holderA.revokeItems(false);
			holderB.revokeItems(false);
			
			// close the inventories
			playerA.closeInventory();
			playerB.closeInventory();
			
			// and clear both players of their trading metadata
			playerA.removeMetadata("tradeExchange", MCNSAEssentials.getInstance());
			playerB.removeMetadata("tradeExchange", MCNSAEssentials.getInstance());
			
			// send messages
			if(canceller.equals(playerA)) {
				ColourHandler.sendMessage(playerA, "&cYou cancelled the trade with %s!", playerB.getName());
				ColourHandler.sendMessage(playerB, "&c%s cancelled the trade with you!", playerA.getName());
			}
			else {
				ColourHandler.sendMessage(playerB, "&cYou cancelled the trade with %s!", playerA.getName());
				ColourHandler.sendMessage(playerA, "&c%s cancelled the trade with you!", playerB.getName());
			}
			
			// play cancel sounds
			SoundUtility.cancelSound(playerA);
			SoundUtility.cancelSound(playerB);
		}
		
		public boolean canPlaceItem(Player target, int slotID) {
			if(target.equals(playerA)) {
				return holderA.canPlaceThere(slotID);
			}
			else {
				return holderB.canPlaceThere(slotID);
			}
		}
		
		public void updateTradingPartner(Player originator, ItemStack newStack, int slotID) {
			if(originator.equals(playerA)) {
				holderB.updatePartnerItem(newStack, slotID);
				ColourHandler.sendMessage(playerB, "&3%s updated their trade!", playerA.getName());
			}
			else {
				holderA.updatePartnerItem(newStack, slotID);
				ColourHandler.sendMessage(playerA, "&3%s updated their trade!", playerB.getName());
			}
			
			// clear both players' ready status
			holderA.setAccepted(false);
			holderB.setAccepted(false);
			holderA.setStatus(false);
			holderB.setStatus(false);
		}
	}
	
	private static Trade instance = null;
	public Trade() {
		instance = this;
		
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// utility function to access the current trade exchange for a player
	private static TradeExchange getExchange(Player player) {
		if(!player.hasMetadata("tradeExchange")) {
			return null;
		}
		
		List<MetadataValue> mvl = player.getMetadata("tradeExchange");
		if(mvl.size() != 1) {
			return null;
		}
		
		if(!(mvl.get(0).value() instanceof TradeExchange)) {
			return null;
		}
		TradeExchange exchange = (TradeExchange)mvl.get(0).value();
		return exchange;
	}
	
	// utility function for making sure that event is cancelled!
	private void cancelInventoryClick(InventoryClickEvent event) {
		event.setCurrentItem(event.getCurrentItem());
		event.setCursor(event.getCursor());
		event.setCancelled(true);
		event.setResult(Result.DENY);
	}
	
	// listen to player clicks
	// so they can't modify the trade UI
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		// make sure it was a player clicking
		if(!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		
		// get their exchange / see if they were in an exchange
		Player player = (Player)event.getWhoClicked();
		TradeExchange exchange = getExchange(player);
		if(exchange == null) {
			return;
		}
		
		// TODO: deal with shift-clicks
		// cancel shift clicks
		if(event.isShiftClick()) {
			Logger.debug("shift click");
			cancelInventoryClick(event);
		}
		
		// make sure we're clicking in our zone (not the player's inventory)
		int slotID = event.getRawSlot();
		Logger.debug("slotID: %d", slotID);
		if(slotID >= 54 || slotID < 0) {
			Logger.debug("slotID >= 54 || slotID < 0, cancelling");
			return;
		}
		
		// deal with UI clicks
		switch(exchange.holderA.getSlotType(slotID)) {
		case WALL:
			Logger.debug("wall");
			cancelInventoryClick(event);
			return;
			
		case ACCEPT:
			Logger.debug("accept");
			exchange.setAccepted(player, !exchange.getAccepted(player));
			cancelInventoryClick(event);
			return;
			
		case REVOKE:
			Logger.debug("revoke");
			Logger.debug("%s revokes!", player.getName());
			exchange.revoke(player);
			cancelInventoryClick(event);
			return;
			
		case STATUS:
			Logger.debug("status");
			cancelInventoryClick(event);
			return;
		}
		
		// make sure we can click there
		if(!exchange.canPlaceItem(player, slotID)) {
			cancelInventoryClick(event);
			if(event.getCursor() != null) {
				SoundUtility.errorSound(player);
				ColourHandler.sendMessage(player, "&cYou can't place items over there!");
			}
			return;
		}
		
		// if nothing is changing, we don't care
		ItemStack currentStack = event.getCurrentItem();
		ItemStack cursorStack = event.getCursor();
		if(currentStack.getTypeId() == 0 && cursorStack.getTypeId() == 0) {
			return;
		}
		
		// ok, deal with our new stack (for feeding into the other player's inventory)
		ItemStack newStack = null;
		if(event.isLeftClick()) {
			// add items in cursor to the stack
			if(currentStack.getTypeId() == cursorStack.getTypeId()) {
				newStack = currentStack.clone();
				newStack.setAmount(currentStack.getAmount() + cursorStack.getAmount());
			}
			else {
				newStack = cursorStack.clone();
			}
		}
		else if(event.isRightClick()) {
			// split the stack
			if(currentStack.getTypeId() != 0 && cursorStack.getTypeId() == 0) {
				newStack = currentStack.clone();
				newStack.setAmount(currentStack.getAmount() / 2);
			}
			
			// place all
			if(currentStack.getTypeId() != cursorStack.getTypeId() && cursorStack.getTypeId() != 0) {
				newStack = cursorStack.clone();
			}
			
			// add 1 item
			if(currentStack.getTypeId() == cursorStack.getTypeId() && cursorStack.getTypeId() != 0) {
				newStack = cursorStack.clone();
				newStack.setAmount(currentStack.getAmount() + 1);
			}
			
			// place 1 item
			if(currentStack.getTypeId() == 0 && cursorStack.getTypeId() != 0) {
				newStack = cursorStack.clone();
				newStack.setAmount(1);
			}
		}
		
		// ok, now update our trading parter
		exchange.updateTradingPartner(player, newStack, slotID);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryClose(InventoryCloseEvent event) {		
		// make sure it was a player clicking
		if(!(event.getPlayer() instanceof Player)) {
			return;
		}
		
		// get their exchange / see if they were in an exchange
		Player player = (Player)event.getPlayer();
		TradeExchange exchange = getExchange(player);
		if(exchange == null) {
			return;
		}
		
		// cancel the exchange
		exchange.cancelTrade(player);
	}

	public static void openTradeWindow(Player playerA, Player playerB) {
		// create an inventory
		TradeInventoryHolder tradeInventoryA = instance.new TradeInventoryHolder(playerA, playerB);
		TradeInventoryHolder tradeInventoryB = instance.new TradeInventoryHolder(playerB, playerA);
		
		// make an exchange
		TradeExchange exchange = instance.new TradeExchange();
		exchange.playerA = playerA;
		exchange.playerB = playerB;
		exchange.holderA = tradeInventoryA;
		exchange.holderB = tradeInventoryB;
		
		// add the exchange to both players
		playerA.setMetadata("tradeExchange", new FixedMetadataValue(MCNSAEssentials.getInstance(), exchange));
		playerB.setMetadata("tradeExchange", new FixedMetadataValue(MCNSAEssentials.getInstance(), exchange));
		
		// make each player open their inventory
		playerA.openInventory(tradeInventoryA.getInventory());
		playerB.openInventory(tradeInventoryB.getInventory());
	}
	
	@Command(command = "trade",
			arguments = {"target player"},
			description = "attempts to trade with the target player",
			permissions = {"request"},
			playerOnly = true)
	public static boolean trade(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// find our target player
		Player target = Bukkit.getServer().getPlayer(targetPlayer);
		if(target == null) {
			throw new EssentialsCommandException("I couldn't find the player '%s'!", targetPlayer);
		}
		
		// TODO: trading radius
		// TODO: trade requests (request trade, ability to cancel trade request, ignore trade requests)
		// TODO: ability to right-click on players to trade them
		// TODO: bug checking (especially duping)
		// TODO: register an event for this plugin getting disabled, so we can shut down any trades
		
		// ok, make them trade
		openTradeWindow(player, target);
		
		return true;
	}
}
