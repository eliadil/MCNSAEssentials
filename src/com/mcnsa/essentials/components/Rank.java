package com.mcnsa.essentials.components;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.exceptions.RankingException;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.interfaces.MultilineChatHandler;
import com.mcnsa.essentials.managers.ConversationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.managers.PermissionsManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Rank",
				description = "Allows easy promotion, demotion, and hot-dogging",
				permsSettingsPrefix = "rank")
@DatabaseTableInfo(name = "ranks",
fields = { "targetPlayer TINYTEXT", "sourcePlayer TINYTEXT", "oldGroup TINYTEXT", "newGroup TINYTEXT", "date TIMESTAMP", "reason TINYTEXT" })
public class Rank implements MultilineChatHandler {
	@Setting(node = "hotdog-group-name") public static String hotdogGroup = "hotdog";
	
	private static Rank instance = null;
	private static PermissionManager permissionsManager = null;
	
	public Rank() {
		Rank.instance = this;
		
		// set up permissions
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
			permissionsManager = PermissionsEx.getPermissionManager();
		}
	}
	
	public enum RankAction {
		PROMOTE, DEMOTE, HOTDOG
	}
	
	private static void logRankChange(Player target, CommandSender source, PermissionGroup oldGroup, PermissionGroup newGroup, Timestamp timestamp, String reason) throws EssentialsCommandException {
		int results = DatabaseManager.updateQuery(
				"insert into ranks (id, targetPlayer, sourcePlayer, oldGroup, newGroup, date, reason) values (NULL, ?, ?, ?, ?, ?, ?);",
				target.getName(),
				source.getName(),
				oldGroup.getName(),
				newGroup.getName(),
				timestamp,
				reason);
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to log promotion!");
		}
	}
	
	private static void promote(CommandSender promoter, Player targetPlayer, String reason) throws EssentialsCommandException {
		// get their current group
		PermissionGroup oldGroup = permissionsManager.getUser(targetPlayer).getRankLadders().get("default");
		
		// promote them
		PermissionGroup newGroup = null;
		try {
			PermissionUser playerPromoter = (promoter instanceof Player) ? permissionsManager.getUser((Player)promoter) : null;
			newGroup = permissionsManager.getUser(targetPlayer).promote(playerPromoter, "default");
		}
		catch (RankingException e) {
			throw new EssentialsCommandException("%s isn't promoteable by you!", targetPlayer.getName());
		}
		
		// and log it
		logRankChange(targetPlayer, promoter, oldGroup, newGroup, new Timestamp(System.currentTimeMillis()), reason);
		ColourHandler.sendMessage(promoter, "&a%s has been promoted to rank '%s'!", targetPlayer.getName(), newGroup.getName());
		ColourHandler.sendMessage(targetPlayer, "&aYou have been promoted to rank '%s' by %s!", newGroup.getName(), promoter.getName());
	}
	
	private static void demote(CommandSender demoter, Player targetPlayer, String reason) throws EssentialsCommandException {
		// get their current group
		PermissionGroup oldGroup = permissionsManager.getUser(targetPlayer).getRankLadders().get("default");
		
		// promote them
		PermissionGroup newGroup = null;
		try {
			PermissionUser playerDemoter = (demoter instanceof Player) ? permissionsManager.getUser((Player)demoter) : null;
			newGroup = permissionsManager.getUser(targetPlayer).demote(playerDemoter, "default");
		}
		catch (RankingException e) {
			throw new EssentialsCommandException("%s isn't demotable by you!", targetPlayer.getName());
		}
		
		// and log it
		logRankChange(targetPlayer, demoter, oldGroup, newGroup, new Timestamp(System.currentTimeMillis()), reason);
		ColourHandler.sendMessage(demoter, "&a%s has been demoted to rank '%s'!", targetPlayer.getName(), newGroup.getName());
		ColourHandler.sendMessage(targetPlayer, "&aYou have been demoted to rank '%s' by %s!", newGroup.getName(), demoter.getName());
	}
	
	private static void hotdog(CommandSender hotdogger, Player targetPlayer, String reason) throws EssentialsCommandException {		
		// get their current group
		PermissionGroup oldGroup = permissionsManager.getUser(targetPlayer).getRankLadders().get("default");
		
		// get the hotdog group
		PermissionGroup newGroup = permissionsManager.getGroup(hotdogGroup);
		
		// make sure it exists
		if(newGroup == null) {
			throw new EssentialsCommandException("Failed to hotdog: invalid group (%s). Please contact an administrator!", hotdogGroup);
		}
		
		// change their group
		PermissionGroup[] newGroups = {newGroup};
		permissionsManager.getUser(targetPlayer).setGroups(newGroups);
		
		// and log it
		logRankChange(targetPlayer, hotdogger, oldGroup, newGroup, new Timestamp(System.currentTimeMillis()), reason);
		ColourHandler.sendMessage(hotdogger, "&a%s has been hotdogged!", newGroup.getName());
		ColourHandler.sendMessage(targetPlayer, "&aYou have been hotdogged by %s!", hotdogger.getName());
	}
	
	@Command(command = "rank",
			arguments = {"target player[s]"},
			description = "tells you the rank of the target player[s]",
			permissions = {"rank"})
	public static boolean rank(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to get the rank of!", targetPlayer);
		}
		
		// loop over all our targets
		for(Player target: targetPlayers) {
			ArrayList<String> groups = PermissionsManager.getGroups(target);
			if(groups != null) {
				String message = String.format("&6%s's ranks: &f", target.getName());
				for(int i = 0; i < groups.size(); i++) {
					if(i != 0) {
						message += ", ";
					}
					message += groups.get(i);
				}
				ColourHandler.sendMessage(sender, message);
			}
		}
		
		return true;
	}
	
	@Command(command = "promote",
			aliases = {"pr"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "promotes the target player[s] for a given reason",
			permissions = {"promote"},
			playerOnly = true)
	public static boolean promote(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// make sure we have a permissions manager
		if(permissionsManager == null) {
			throw new EssentialsCommandException("PermissionsEx isn't installed, can't promote!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to promote!", targetPlayer);
		}
		
		// call our multiline chat handler
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, targetPlayers, RankAction.PROMOTE);
		ConversationManager.startConversation(sender, instance, targetPlayers, RankAction.PROMOTE);
		
		return true;
	}
	
	@Command(command = "promote",
			aliases = {"pr"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "promotes the target player[s]",
			permissions = {"promote"},
			consoleOnly = true)
	public static boolean promoteFromConsole(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// make sure we have a permissions manager
		if(permissionsManager == null) {
			throw new EssentialsCommandException("PermissionsEx isn't installed, can't promote!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to promote!", targetPlayer);
		}
		
		// promote everyone
		String reason = "for no good reason";
		for(Player target: targetPlayers) {
			promote(sender, target, reason);
		}
		
		return true;
	}
	
	@Command(command = "demote",
			aliases = {"de"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "demotes the target player[s] for a given reason",
			permissions = {"demote"},
			playerOnly = true)
	public static boolean demote(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// make sure we have a permissions manager
		if(permissionsManager == null) {
			throw new EssentialsCommandException("PermissionsEx isn't installed, can't demote!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to promote!", targetPlayer);
		}
		
		// call our multiline chat handler
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, targetPlayers, RankAction.DEMOTE);
		ConversationManager.startConversation(sender, instance, targetPlayers, RankAction.DEMOTE);
		
		return true;
	}
	
	@Command(command = "demote",
			aliases = {"de"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "demotes the target player[s]",
			permissions = {"demote"},
			consoleOnly = true)
	public static boolean demoteFromConsole(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// make sure we have a permissions manager
		if(permissionsManager == null) {
			throw new EssentialsCommandException("PermissionsEx isn't installed, can't demote!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to demote!", targetPlayer);
		}
		
		// promote everyone
		String reason = "for no good reason";
		for(Player target: targetPlayers) {
			demote(sender, target, reason);
		}
		
		return true;
	}
	
	@Command(command = "hotdog",
			aliases = {"hd"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "hotdogs the target player[s] for a given reason",
			permissions = {"hotdog"},
			playerOnly = true)
	public static boolean hotdog(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// make sure we have a permissions manager
		if(permissionsManager == null) {
			throw new EssentialsCommandException("PermissionsEx isn't installed, can't hotdog!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to promote!", targetPlayer);
		}
		
		// call our multiline chat handler
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, targetPlayers, RankAction.HOTDOG);
		ConversationManager.startConversation(sender, instance, targetPlayers, RankAction.HOTDOG);
		
		return true;
	}
	
	@Command(command = "hotdog",
			aliases = {"hd"},
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "hotdogs the target player[s]",
			permissions = {"hotdog"},
			consoleOnly = true)
	public static boolean hotdogFromConsole(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// make sure we have a permissions manager
		if(permissionsManager == null) {
			throw new EssentialsCommandException("PermissionsEx isn't installed, can't hotdog!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to hotdog!", targetPlayer);
		}
		
		// promote everyone
		String reason = "for no good reason";
		for(Player target: targetPlayers) {
			demote(sender, target, reason);
		}
		
		return true;
	}

	@Override
	public void onChatComplete(CommandSender sender, String enteredText, Object... args) throws EssentialsCommandException {
		RankAction action = (RankAction)args[1];
		
		@SuppressWarnings("unchecked")
		ArrayList<Player> targetPlayers = (ArrayList<Player>)args[0];
		for(Player target: targetPlayers) {
			// make sure they're still here
			if(target == null) {
				continue;
			}
			
			switch(action) {
			case PROMOTE:
				promote(sender, target, enteredText);
				break;
			case DEMOTE:
				demote(sender, target, enteredText);
				break;
			case HOTDOG:
				hotdog(sender, target, enteredText);
				break;
			}
		}
	}
}
