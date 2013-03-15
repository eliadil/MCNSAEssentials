package com.mcnsa.essentials.components;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.utilities.ColourHandler;

public class Teleport {
	@Command(command = "tp",
			aliases = {"teleport"},
			usage = "/tp <player>",
			description = "teleports you to <player>",
			permissions = { "teleport" },
			playerOnly = true)
	public static boolean teleport(CommandSender sender, String targetPlayer) {
		// try to find the player
		Player target = Bukkit.getServer().getPlayer(targetPlayer);
		if(target == null) {
			ColourHandler.sendMessage(sender, "&cI couldn't find player `" + targetPlayer + "' to teleport to!");
			return false;
		}
		
		// do it!
		Player player = (Player)sender;
		player.teleport(target);
		ColourHandler.sendMessage(player, "&6Woosh!");
		
		return true;
	}
}
