package com.mcnsa.essentials.components;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.interfaces.MultilineChatHandler;
import com.mcnsa.essentials.managers.ConversationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.IPUtilities;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Ban",
				description = "Commands to ban players",
				permsSettingsPrefix = "bans")
@DatabaseTableInfo(name = "banlogs",
					fields = { "banee TINYTEXT", "banner TINYTEXT", "date TIMESTAMP", "reason TINYTEXT", "expiry TIMESTAMP" })
public class Ban implements Listener, MultilineChatHandler {
	private static Ban instance = null;
	public Ban() {
		Ban.instance = this;
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// utility functions
	private static void ban(Player banee, CommandSender banner, String reason, Timestamp expiry) throws EssentialsCommandException {
		// kick the player
		String message = ColourHandler.processColours("&cYou have been banned until %s by %s: %s",
				expiry.toString(),
				banner.getName(),
				reason);
		ColourHandler.sendMessage(banee, message);
		banee.kickPlayer(message);
		
		// and alert the banner
		ColourHandler.sendMessage(banner, "&6You have banned %s until %s!", banee.getName(), expiry.toString());
		
		// record our ban
		int results = DatabaseManager.updateQuery(
				"insert into banlogs (id, banee, banner, date, reason, expiry) values (NULL, ?, ?, ?, ?, ?);",
				banee.getName(),
				banner.getName(),
				new Timestamp(System.currentTimeMillis()),
				reason,
				expiry);
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to log ban!");
		}
	}
	
	private static void banIP(InetAddress banee, CommandSender banner, String reason, Timestamp expiry) throws EssentialsCommandException {
		// find the player
		ArrayList<Player> matchedPlayers = PlayerSelector.selectPlayersExact("ip:" + banee.toString());
		
		// if they're online, kick the player
		for(Player matchedPlayer: matchedPlayers) {
			String message = ColourHandler.processColours("&cYou have been ip-banned until %s by %s: %s",
					expiry.toString(),
					banner.getName(),
					reason);
			ColourHandler.sendMessage(matchedPlayer, message);
			matchedPlayer.kickPlayer(message);
		}
		
		// and alert the banner
		ColourHandler.sendMessage(banner, "&6You have banned %s until %s!", banee.toString(), expiry.toString());
		
		// record our ban
		int results = DatabaseManager.updateQuery(
				"insert into banlogs (id, banee, banner, date, reason, expiry) values (NULL, ?, ?, ?, ?, ?);",
				banee.toString(),
				banner.getName(),
				new Timestamp(System.currentTimeMillis()),
				reason,
				expiry);
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to log ban!");
		}
		
		// ban in bukkit
		Bukkit.getServer().banIP(banee.toString());
	}
	
	private static void unban(String unbanee, CommandSender unbanner, String reason) throws EssentialsCommandException {
		// and alert the unbanner
		ColourHandler.sendMessage(unbanner, "&6You have unbanned %s!", unbanee);
		
		// record our unban
		int results = DatabaseManager.updateQuery(
				"insert into banlogs (id, banee, banner, date, reason, expiry) values (NULL, ?, ?, ?, ?, ?);",
				unbanee,
				unbanner.getName(),
				new Timestamp(System.currentTimeMillis()),
				reason,
				Timestamp.valueOf("1970-01-01 00:00:01"));
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to log unban!");
		}
	}
	
	private static void unbanIP(InetAddress unbanee, CommandSender unbanner, String reason) throws EssentialsCommandException {
		// and alert the unbanner
		ColourHandler.sendMessage(unbanner, "&6You have unbanned %s!", unbanee);
		
		// record our unban
		int results = DatabaseManager.updateQuery(
				"insert into banlogs (id, banee, banner, date, reason, expiry) values (NULL, ?, ?, ?, ?, ?);",
				unbanee.toString(),
				unbanner.getName(),
				new Timestamp(System.currentTimeMillis()),
				reason,
				Timestamp.valueOf("1970-01-01 00:00:01"));
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to log unban!");
		}
		
		// unban in bukkit
		Bukkit.getServer().unbanIP(unbanee.toString());
	}

	// multiline chat handler
	@Override
	public void onChatComplete(CommandSender sender, String reason, Object... args) throws EssentialsCommandException {
		// determine whether we're banning or unbanning
		if(args.length == 2) {
			// banning
			// determine if its an IP or not
			if(args[0] instanceof ArrayList<?>) {
				// get everyone on our list
				@SuppressWarnings("unchecked")
				ArrayList<Player> targetPlayers = (ArrayList<Player>)args[0];
				
				// get our expiry date
				Timestamp expiry = (Timestamp)args[1];
				
				// loop over our targets and ban them
				for(Player target: targetPlayers) {
					// make sure the target still exists
					if(target == null) {
						continue;
					}
					
					// ok, they're there.
					// ban them.
					ban(target, sender, reason, expiry);
				}
			}
			else if(args[0] instanceof InetAddress) {
				// we have an IP
				InetAddress IP = (InetAddress)args[0];
				
				// get our expiry date
				Timestamp expiry = (Timestamp)args[1];
				
				// ban them
				banIP(IP, sender, reason, expiry);
			}
			else {
				throw new EssentialsCommandException("Something went wrong, please contact an administrator!");
			}
		}
		else if(args.length == 1) {
			// unbanning
			// determine if its an IP or not
			if(args[0] instanceof String) {
				// get our target player
				String targetPlayer = (String)args[0];
				
				// unban them
				unban(targetPlayer, sender, reason);
			}
			else if(args[0] instanceof InetAddress) {
				InetAddress IP = (InetAddress)args[0];
				unban(IP.toString(), sender, reason);
			}
			else {
				throw new EssentialsCommandException("Something went wrong, please contact an administrator!");
			}
		}
		else {
			// ???
			throw new EssentialsCommandException("Something went wrong, please contact an administrator!");
		}
	}
	
	// bukkit events
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		try {
			// see if they're banned or not			
			// query the database
			ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
					"select * from banlogs where banee=? or banee=? order by date desc limit 1;",
					event.getName(),
					event.getAddress().toString());
			
			// check their results
			if(results.size() > 0) {
				// if their latest ban log expiry is after now, they're still banned
				Timestamp expiry = (Timestamp)results.get(0).get("expiry");
				Timestamp now = new Timestamp(System.currentTimeMillis());
				
				/*MCNSAEssentials.debug("expiry: " + expiry);
				MCNSAEssentials.debug("now: " + now);
				MCNSAEssentials.debug("expiry after now: " + expiry.after(now));*/
				
				if(expiry.after(now)) {
					// nope, they're banned
					event.disallow(Result.KICK_BANNED, ColourHandler.processColours(String.format(
							"&cYou are banned until &f%s&c: &f%s",
							expiry.toString(),
							(String)results.get(0).get("reason"))));
				}
			}
		}
		catch(Exception e) {
			event.disallow(Result.KICK_WHITELIST, "Something went wrong: " + e.getMessage());
		}
	}
	
	// commands
	@Command(command = "ban",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "bans the target player[s] for the given reason",
			permissions = {"ban.forever"},
			playerOnly = true)
	public static boolean ban(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		return ban(sender, targetPlayer, "2020-02-02 02:02:02");
	}
	
	@Command(command = "ban",
			arguments = {"target player[s]", "expiry date"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.DATE},
			description = "bans the target player[s] for the given reason until the expiry date",
			permissions = {"ban.expiry"},
			playerOnly = true)
	public static boolean ban(CommandSender sender, String targetPlayer, String expiry) throws EssentialsCommandException {
		// parse our timestamp
		Timestamp expiryTimestamp = null;
		try {
			expiryTimestamp = Timestamp.valueOf(expiry);
		}
		catch(IllegalArgumentException e) {
			throw new EssentialsCommandException("I couldn't understand your date '%s'. Please format it as 'yyyy-mm-dd hh:mm:ss'!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to ban!", targetPlayer);
		}
		
		// call our multiline chat handler
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, targetPlayers, expiryTimestamp);
		ConversationManager.startConversation(sender, instance, targetPlayers, expiryTimestamp);
		
		return true;
	}
	@Command(command = "ban",
			arguments = {"target player[s]"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "bans the target player[s]",
			permissions = {"ban.forever"},
			consoleOnly = true)
	public static boolean banFromConsole(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		return banFromConsole(sender, targetPlayer, "2020-02-02 02:02:02");
	}
	
	@Command(command = "ban",
			arguments = {"target player[s]", "expiry date"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.DATE},
			description = "bans the target player[s] until the expiry date",
			permissions = {"ban.expiry"},
					consoleOnly = true)
	public static boolean banFromConsole(CommandSender sender, String targetPlayer, String expiry) throws EssentialsCommandException {
		// parse our timestamp
		Timestamp expiryTimestamp = null;
		try {
			expiryTimestamp = Timestamp.valueOf(expiry);
		}
		catch(IllegalArgumentException e) {
			throw new EssentialsCommandException("I couldn't understand your date '%s'. Please format it as 'yyyy-mm-dd hh:mm:ss'!");
		}
		
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to ban!", targetPlayer);
		}
		
		String reason = "for no good reason"; 

		// loop over our targets and ban them
		for(Player target: targetPlayers) {
			// ban them.
			ban(target, sender, reason, expiryTimestamp);
			
			// and alert the banner
			ColourHandler.sendMessage(sender, "&6You have banned %s until %s!", target.getName(), expiry.toString());
		}
		
		return true;
	}
	
	 // ban IPS
	@Command(command = "banip",
			arguments = {"target ip"},
			tabCompletions = {TabCompleteType.IP},
			description = "bans the target ip for the given reason",
			permissions = {"ip.forever"},
			playerOnly = true)
	public static boolean banIP(CommandSender sender, String targetIP) throws EssentialsCommandException {
		return banip(sender, targetIP, "2020-02-02 02:02:02");
	}

	@Command(command = "banip",
			arguments = {"target ip", "expiry date"},
			tabCompletions = {TabCompleteType.IP, TabCompleteType.DATE},
			description = "bans the target ip for the given reason until the expiry date",
			permissions = {"ip.expiry"},
			playerOnly = true)
	public static boolean banip(CommandSender sender, String targetIP, String expiry) throws EssentialsCommandException {
		// parse our timestamp
		Timestamp expiryTimestamp = null;
		try {
			expiryTimestamp = Timestamp.valueOf(expiry);
		}
		catch(IllegalArgumentException e) {
			throw new EssentialsCommandException("I couldn't understand your date '%s'. Please format it as 'yyyy-mm-dd hh:mm:ss'!");
		}
		
		// try to parse our IP
		InetAddress IP = null;
		try {
			IP = IPUtilities.parseIpAddress(targetIP);
		}
		catch(Exception e) {
			// nope
			throw new EssentialsCommandException(e.getMessage());
		}
		
		// call our multiline chat handler
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, IP, expiryTimestamp);
		ConversationManager.startConversation(sender, instance, IP, expiryTimestamp);
		
		return true;
	}
	@Command(command = "banip",
			arguments = {"target ip"},
			tabCompletions = {TabCompleteType.IP},
			description = "bans the target ip",
			permissions = {"ip.forever"},
			consoleOnly = true)
	public static boolean banIPFromConsole(CommandSender sender, String targetIP) throws EssentialsCommandException {
		return banIPFromConsole(sender, targetIP, "2020-02-02 02:02:02");
	}
	
	@Command(command = "banip",
			arguments = {"target ip", "expiry date"},
			tabCompletions = {TabCompleteType.IP, TabCompleteType.DATE},
			description = "bans the target ip until the expiry date",
			permissions = {"ip.expiry"},
			consoleOnly = true)
	public static boolean banIPFromConsole(CommandSender sender, String targetIP, String expiry) throws EssentialsCommandException {
		// parse our timestamp
		Timestamp expiryTimestamp = null;
		try {
			expiryTimestamp = Timestamp.valueOf(expiry);
		}
		catch(IllegalArgumentException e) {
			throw new EssentialsCommandException("I couldn't understand your date '%s'. Please format it as 'yyyy-mm-dd hh:mm:ss'!");
		}
		
		// try to parse our IP
		InetAddress IP = null;
		try {
			IP = IPUtilities.parseIpAddress(targetIP);
		}
		catch(Exception e) {
			// nope
			throw new EssentialsCommandException(e.getMessage());
		}
		
		// ban them
		banIP(IP, sender, "for no good reason", expiryTimestamp);
		
		return true;
	}
	
	// unbans
	@Command(command = "unban",
			arguments = {"target player"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "unbans the target player for the given reason",
			permissions = {"unban"},
			playerOnly = true)
	public static boolean unban(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, targetPlayer);
		ConversationManager.startConversation(sender, instance, targetPlayer);
		return true;
	}
	@Command(command = "unban",
			arguments = {"target player"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "unbans the target player",
			permissions = {"unban"},
			consoleOnly = true)
	public static boolean unbanFromConsole(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		unban(targetPlayer, sender, "for no good reason");
		return true;
	}
	
	// unbans
	@Command(command = "unbanip",
			arguments = {"target IP"},
			tabCompletions = {TabCompleteType.IP},
			description = "unbans the target IP for the given reason",
			permissions = {"unip"},
			playerOnly = true)
	public static boolean unbanIP(CommandSender sender, String targetIP) throws EssentialsCommandException {
		// try to parse our IP
		InetAddress IP = null;
		try {
			IP = IPUtilities.parseIpAddress(targetIP);
		}
		catch(Exception e) {
			// nope
			throw new EssentialsCommandException(e.getMessage());
		}
		
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, IP);
		ConversationManager.startConversation(sender, instance, IP);
		return true;
	}
	@Command(command = "unbanip",
			arguments = {"target IP"},
			tabCompletions = {TabCompleteType.IP},
			description = "unbans the target IP",
			permissions = {"unip"},
			consoleOnly = true)
	public static boolean unbanIPFromConsole(CommandSender sender, String targetIP) throws EssentialsCommandException {
		// try to parse our IP
		InetAddress IP = null;
		try {
			IP = IPUtilities.parseIpAddress(targetIP);
		}
		catch(Exception e) {
			// nope
			throw new EssentialsCommandException(e.getMessage());
		}
		
		unbanIP(IP, sender, "for no good reason");
		return true;
	}
}
