package com.mcnsa.essentials.utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.interfaces.MultilineChatHandler;

public class MultilineChatEntry implements Listener {	
	public MultilineChatEntry() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// utility function to determine if someone has godmode or not
	private static boolean isEnteringMultilineChat(Player player) {
		// if any of our metadata values come back as true,
		// we have god mode on
		for(MetadataValue val: player.getMetadata("mlEnabled")) {
			if(val.asBoolean()) {
				return true;
			}
		}
		
		// guess not!
		return false;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		// see if they player is entering chat
		Player player = event.getPlayer();
		if(isEnteringMultilineChat(player)) {
			// yup!
			// capture it
			String line = event.getMessage();
			
			if(line.equals("done")) {
				onDone(player);
			}
			else if(line.equals("cancel")){
				onCancel(player);
			}
			else {
				String mlText = "";
				for(MetadataValue val: player.getMetadata("mlText")) {
					mlText = val.asString();
					break;
				}
				
				mlText += " " + line;
				
				// remove the old mlText
				player.removeMetadata("mlText", MCNSAEssentials.getInstance());
				// add the new mlText
				player.setMetadata("mlText", new FixedMetadataValue(MCNSAEssentials.getInstance(), mlText));
				
				ColourHandler.sendMessage(player, "&9Added text: &f%s", line);
			}
			
			// cancel it!
			event.setCancelled(true);
		}
	}
	
	public static void onDone(Player player) {
		// get the various meta datas
		String mlText = "";
		for(MetadataValue val: player.getMetadata("mlText")) {
			mlText = val.asString();
			break;
		}
		MultilineChatHandler mlOnDone = null;
		for(MetadataValue val: player.getMetadata("mlOnDone")) {
			mlOnDone = (MultilineChatHandler)val.value();
			break;
		}
		Object[] mlArgs = null;
		for(MetadataValue val: player.getMetadata("mlArgs")) {
			mlArgs = (Object[])val.value();
			break;
		}
		
		// and call it
		try {
			mlOnDone.onChatComplete(player, mlText, mlArgs);
		}
		catch(EssentialsCommandException e) {
			ColourHandler.sendMessage(player, "&c" + e.getMessage());
		}
		finally {
			// destroy all the meta datas
			player.removeMetadata("mlEnabled", MCNSAEssentials.getInstance());
			player.removeMetadata("mlOnDone", MCNSAEssentials.getInstance());
			player.removeMetadata("mlText", MCNSAEssentials.getInstance());
			player.removeMetadata("mlArgs", MCNSAEssentials.getInstance());
		}
	}
	
	public static void onCancel(Player player) {
		// destroy all the meta datas
		player.removeMetadata("mlEnabled", MCNSAEssentials.getInstance());
		player.removeMetadata("mlOnDone", MCNSAEssentials.getInstance());
		player.removeMetadata("mlText", MCNSAEssentials.getInstance());
		player.removeMetadata("mlArgs", MCNSAEssentials.getInstance());
		
		// tell them
		ColourHandler.sendMessage(player, "&9Multiline text entry cancelled");
	}

	public static void scheduleMultilineTextEntry(Player player, MultilineChatHandler onDone, Object... args) throws EssentialsCommandException {
		// make sure they're not already doing entering text
		if(isEnteringMultilineChat(player)) {
			throw new EssentialsCommandException("You are already entering multi-line text!");
		}

		// set the player's metadata
		player.setMetadata("mlEnabled", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
		player.setMetadata("mlOnDone", new FixedMetadataValue(MCNSAEssentials.getInstance(), onDone));
		player.setMetadata("mlText", new FixedMetadataValue(MCNSAEssentials.getInstance(), new String("")));
		player.setMetadata("mlArgs", new FixedMetadataValue(MCNSAEssentials.getInstance(), args));
		
		// and tell them
		ColourHandler.sendMessage(player, "&aYou are now entering multiline text. Continue entering text, line-by-line, until you're done. When you're done, send 'done' by itself on its own line (or 'cancel' to stop).");
	}
}
