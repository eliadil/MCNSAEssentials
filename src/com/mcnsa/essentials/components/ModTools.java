package com.mcnsa.essentials.components;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;
import com.mcnsa.essentials.utilities.StringUtils;

@ComponentInfo(friendlyName = "ModTools",
				description = "Various tools for moderating not included elsewhere",
				permsSettingsPrefix = "modtools")
public class ModTools {	
	// our commands
	@Command(command = "openinv",
			aliases = {"openinventory", "inventory"},
			arguments = {"target player"},
			description = "opens that target player's inventory",
			permissions = {"openinv"},
			playerOnly = true)
	public static boolean openInventory(CommandSender sender, String targetPlayer) {
		return true;
	}
	
	@Command(command = "sudo",
			arguments = {"target player[s]", "command"},
			description = "causes another player to execute a command",
			permissions = {"sudo.call"})
	public static boolean sudo(CommandSender sender, String targetPlayer, String... commandParts) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find target player[s] '%s' to sudo!", targetPlayer);
		}
		
		// build our command
		String command = StringUtils.implode(" ", commandParts);
		if(command.startsWith("/")) {
			command = command.substring(1);
		}
		
		// loop over our player
		for(Player player: targetPlayers) {
			// see if they're exempt from sudo
			if(PermissionsManager.playerHasPermission(player, "modtools.sudo.exempt")) {
				ColourHandler.sendMessage(sender, "&c%s is exempt from sudo!", player.getName());
				continue;
			}
			
			// warn them
			ColourHandler.sendMessage(player, "&3%s made you run the command: %s", sender.getName(), command);
			
			// ok, execute the command
			// but disable permissions checking first
			player.setMetadata("ignorePermissions", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
			boolean result = Bukkit.getServer().dispatchCommand(player, command);
			player.removeMetadata("ignorePermissions", MCNSAEssentials.getInstance());
			
			if(!result) {
				throw new EssentialsCommandException("Not a valid command: %s", command);
			}
			else {
				ColourHandler.sendMessage(sender, "&3%s has run your command!", player.getName());
			}
		}
		
		return true;
	}
}
