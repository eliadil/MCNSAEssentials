package com.mcnsa.essentials.components;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;
import com.mcnsa.essentials.utilities.StringUtils;

@ComponentInfo(friendlyName = "ModTools",
				description = "Various tools for moderating not included elsewhere",
				permsSettingsPrefix = "modtools")
public class ModTools {
	// our commands
	@Translation(node = "openinv.only-online") public static String onlyOnline = "Sorry, you can only open inventories of online players";
	@Translation(node = "openinv.opened-inventory") public static String openedInventory = "&9You have opened %player%'s inventory!";
	@Command(command = "openinv",
			aliases = {"openinventory", "inventory"},
			arguments = {"target player"},
			description = "opens that target player's inventory",
			permissions = {"openinv"},
			playerOnly = true)
	public static boolean openInventory(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get our player
		Player player = (Player)sender;
		
		// get our target player
		Player target = Bukkit.getServer().getPlayer(targetPlayer);
		if(target == null) {
			throw new EssentialsCommandException(onlyOnline);
		}
		
		// now open the player's inventory
		player.openInventory(target.getInventory());
		
		ColourHandler.sendMessage(sender, openedInventory.replaceAll("%player%", target.getName()));
		
		return true;
	}
	
	@Translation(node = "sudo.invalid-targets") public static String invalidTargets = "I couldn't find target player[s] '%targetPlayers%' to sudo!";
	@Translation(node = "sudo.exempt") public static String exempt = "&c%player% is exempt from sudo!";
	@Translation(node = "sudo.made-to-run-command") public static String madeToRunCommand = "&3%player% made you run the command: %command%";
	@Translation(node = "sudo.not-valid-command") public static String notValidCommand = "Not a valid command: %command%";
	@Translation(node = "sudo.has-run-command") public static String hasRunCommand = "&3%player% has run your command!";
	@Command(command = "sudo",
			arguments = {"target player[s]", "command"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING},
			description = "causes another player to execute a command",
			permissions = {"sudo.call"})
	public static boolean sudo(CommandSender sender, String targetPlayer, String[] commandParts) throws EssentialsCommandException {
		if(targetPlayer.equalsIgnoreCase("console")) {
			return sudoAsConsole(sender, commandParts);
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException(invalidTargets.replaceAll("%targetPlayers%", targetPlayer));
		}
		
		// build our command
		String command = StringUtils.implode(" ", commandParts);
		if(command.startsWith("/")) {
			command = command.substring(1);
		}
		
		// loop over our player
		for(Player player: targetPlayers) {
			// see if they're exempt from sudo
			/*if(PermissionsManager.playerHasPermission(player, "modtools.sudo.exempt")) {
				ColourHandler.sendMessage(sender, exempt.replaceAll("%player%", player.getName()));
				continue;
			}*/
			
			// warn them
			ColourHandler.sendMessage(player, madeToRunCommand.replaceAll("%player%", sender.getName()).replaceAll("%command%", command));
			
			// ok, execute the command
			// but disable permissions checking first
			player.setMetadata("ignorePermissions", new FixedMetadataValue(MCNSAEssentials.getInstance(), true));
			boolean result = Bukkit.getServer().dispatchCommand(player, command);
			player.removeMetadata("ignorePermissions", MCNSAEssentials.getInstance());
			
			if(!result) {
				throw new EssentialsCommandException(notValidCommand.replaceAll("%command%", command));
			}
			else {
				ColourHandler.sendMessage(sender, hasRunCommand.replaceAll("%player%", player.getName()));
			}
		}
		
		return true;
	}
	
	public static boolean sudoAsConsole(CommandSender sender, String[] commandParts) throws EssentialsCommandException {
		// build our command
		String command = StringUtils.implode(" ", commandParts);
		if(command.startsWith("/")) {
			command = command.substring(1);
		}
		
		boolean result = Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
		
		if(!result) {
			throw new EssentialsCommandException(notValidCommand.replaceAll("%command%", command));
		}
		else {
			ColourHandler.sendMessage(sender, hasRunCommand.replaceAll("%player%", "console"));
		}
		
		return true;
	}
	
	@Command(command = "essentialsreload",
			description = "reloads essentials configuration",
			permissions = {"reload"})
	public static boolean openInventory(CommandSender sender) throws EssentialsCommandException {		
		ColourHandler.sendMessage(sender, "&3Reloading MCNSAEssentials configuration...");
		
		MCNSAEssentials.getInstance();
		MCNSAEssentials.getInstance();
		// now load all our class's settings
		MCNSAEssentials.getConfigurationManager().loadSettings(MCNSAEssentials.getComponentManager());
		MCNSAEssentials.getInstance().saveConfig();	
		
		ColourHandler.sendMessage(sender, "&MCNSAEssentials configuration reloaded!");
		
		return true;
	}
}
