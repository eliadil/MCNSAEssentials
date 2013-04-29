package com.mcnsa.essentials.components;

import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.StringUtils;

@ComponentInfo(friendlyName = "BetterSigns",
				description = "More functional signs",
				permsSettingsPrefix = "bettersigns")
public class BetterSigns implements Listener {
	public BetterSigns() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock().getType().equals(Material.SIGN_POST)
				|| event.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
			// get the sign's text
			Sign sign = (Sign)event.getClickedBlock().getState();
			String signText = StringUtils.implode("", sign.getLines()).trim();
			
			// add a "http://" if it starts with "www."
			if(signText.startsWith("www.")) {
				signText = "http://" + signText;
			}
			
			// try to parse it as a URL
			try {
				new URL(signText);
			}
			catch(Exception e) {
				// no can do
				return;
			}
			
			// yup, it's valid!
			ColourHandler.sendMessage(event.getPlayer(), "&6Sign url: &f" + signText);
		}
	}
}
