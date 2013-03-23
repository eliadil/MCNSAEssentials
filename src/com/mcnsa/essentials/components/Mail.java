package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;

@ComponentInfo(friendlyName = "Mail",
				description = "Provides inter-player mail",
				permsSettingsPrefix = "mail")
@DatabaseTableInfo(name = "mail",
					fields = { "recipient TINYTEXT", "sender TINYTEXT", "date TIMESTAMP", "subject TINYTEXT", "contents TEXT", "unread BOOLEAN" })
public class Mail implements Listener {	
	public Mail() {
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
}
