package com.mcnsa.essentials.components;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.interfaces.MultilineChatHandler;
import com.mcnsa.essentials.managers.ConversationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.Logger;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Kick",
				description = "Commands to kick players",
				permsSettingsPrefix = "kick")
@DatabaseTableInfo(name = "kicklogs",
					fields = { "kickee TINYTEXT", "kicker TINYTEXT", "date TIMESTAMP", "reason TINYTEXT" })
public class Kick implements MultilineChatHandler {
	private static Kick instance = null;
	
	public Kick() {
		instance = this;
	}
	
	public static void recordKick(String kicker, String kickee, String reason) throws EssentialsCommandException {
		// add our kick
		int results = DatabaseManager.updateQuery(
				"insert into kicklogs (id, kickee, kicker, date, reason) values (NULL, ?, ?, ?, ?);",
				kicker,
				kickee,
				new Timestamp(System.currentTimeMillis()),
				reason);
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to log kick!");
		}
	}
	
	@Command(command = "kick",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "kicks the target player[s] for a given reason",
			permissions = {"kick"},
			consoleOnly = true)
	public static boolean kickFromConsole(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to kick!", targetPlayer);
		}
		
		// dummy reason
		String reason = "no good reason";
		String playerName = sender.getName();
		
		// loop over all our targets
		String playerListString = "";
		for(Player target: targetPlayers) {
			if(!playerListString.equals("")) {
				playerListString += "&6, ";
			}
			ColourHandler.sendMessage(target, "&cYou have been kicked by %s: %s", playerName, reason);
			target.kickPlayer(reason);
			playerListString += "&e" + target.getName();
			
			// log it
			Logger.log("%s kicked %s: %s", playerName, target.getName(), reason);
			recordKick(playerName, target.getName(), reason);
		}
		
		return true;
	}
	
	@Command(command = "kick",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "kicks the target player[s]",
			permissions = {"kick"},
			playerOnly = true)
	public static boolean kick(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to kick!", targetPlayer);
		}
		
		// call our multiline chat handler
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, targetPlayers);
		ConversationManager.startConversation(sender, instance, targetPlayers);
		
		return true;
	}

	@Override
	public void onChatComplete(CommandSender sender, String reason, Object... playerList) throws EssentialsCommandException {
		// kick everyone on our list
		@SuppressWarnings("unchecked")
		ArrayList<Player> targetPlayers = (ArrayList<Player>)playerList[0];
		
		String playerName = sender.getName();
		String playerListString = "";
		for(Player target: targetPlayers) {
			// make sure the target still exists
			if(target == null) {
				continue;
			}
			
			if(!playerListString.equals("")) {
				playerListString += "&6, ";
			}
			ColourHandler.sendMessage(target, "&cYou have been kicked by %s: %s", playerName, reason);
			target.kickPlayer(reason);
			playerListString += "&e" + target.getName();
			
			// log it
			Logger.log("%s kicked %s: %s", playerName, target.getName(), reason);
			recordKick(playerName, target.getName(), reason);
		}
		
		// send the message to the kicking player
		if(sender != null) {
			ColourHandler.sendMessage(sender, "&6You kicked the following people:");
			ColourHandler.sendMessage(sender, playerListString);
		}
	}
}
