package com.mcnsa.essentials.components;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.ParseException;
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
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.interfaces.MultilineChatHandler;
import com.mcnsa.essentials.managers.ConversationManager;
import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.DateUtils;
import com.mcnsa.essentials.utilities.IPUtils;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Ban",
				description = "Commands to ban players",
				permsSettingsPrefix = "bans")
@DatabaseTableInfo(name = "banlogs",
					fields = { "banee TINYTEXT", "banner TINYTEXT", "date TIMESTAMP", "reason TINYTEXT", "expiry TIMESTAMP" })
public class Ban implements Listener, MultilineChatHandler {
	@Setting(node = "smart-ban-hours") public static int smartBanHours = 24;
	
	private static Ban instance = null;
	public Ban() {
		Ban.instance = this;
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
	}
	
	// utility functions
	private static void ban(String banee, CommandSender banner, String reason, Timestamp expiry) throws EssentialsCommandException {
		ban(banee, banner, reason, expiry, true);
	}
	
	private static void ban(String banee, CommandSender banner, String reason, Timestamp expiry, boolean doKick) throws EssentialsCommandException {
		// create a pretty expiry string
		String expiryString = expiry.toString();
		if(expiry.toString().equals(Timestamp.valueOf("2020-02-02 02:02:02").toString())) {
			expiryString = "forever";
		}
		else {
			expiryString = DateUtils.formatTimestamp(expiry);
		}
		
		// determine if the banee is a player name or an IP
		if(doKick) {
			try {
				// try to parse the ip
				InetAddress ip = IPUtils.parseIpAddress(banee);
				
				// we have the IP
				// kick any players that match this ip
				Player[] onlinePlayers = Bukkit.getOnlinePlayers();
				for(Player online: onlinePlayers) {
					if(online.getAddress().getAddress().toString().equalsIgnoreCase(ip.toString())) {
						// yup, we found em!
						String message = ColourHandler.processColours("&cYou have been banned until %s by %s: %s",
								expiryString,
								banner.getName(),
								reason);
						ColourHandler.sendMessage(online, message);
						online.kickPlayer(message);
					}
				}
			}
			catch(ParseException e) {
				// nope, it must be a player
				// get them
				Player player = Bukkit.getServer().getPlayer(banee);
				if(player != null) {
					// yup, we found em!
					String message = ColourHandler.processColours("&cYou have been banned until %s by %s: %s",
							expiryString,
							banner.getName(),
							reason);
					ColourHandler.sendMessage(player, message);
					player.kickPlayer(message);
				}
				
				// "Smart" ban them (ban their IP as well, for the next smartBanHours hours)
				String ipString = player.getAddress().toString();
				// strip off the starting '/'
				if(ipString.startsWith("/")) {
					ipString = ipString.substring(1);
				}
				// strip off the back port number
				if(ipString.contains(":")) {
					ipString = ipString.split(":", 2)[0];
				}
				ban(ipString, banner, reason, new Timestamp(System.currentTimeMillis() + (smartBanHours * 60 * 60 * 1000)), false);
			}
		}
		
		// and alert the banner
		ColourHandler.sendMessage(banner, "&6You have banned %s until %s!", banee, expiryString);
		
		// record our ban
		int results = DatabaseManager.updateQuery(
				"insert into banlogs (id, banee, banner, date, reason, expiry) values (NULL, ?, ?, ?, ?, ?);",
				banee,
				banner.getName(),
				new Timestamp(System.currentTimeMillis()),
				reason,
				expiry);
		
		// make sure it worked!
		if(results == 0) {
			throw new EssentialsCommandException("Failed to log ban!");
		}
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

	// multiline chat handler
	@Override
	public void onChatComplete(CommandSender sender, String reason, Object... args) throws EssentialsCommandException {
		// determine whether we're banning or unbanning
		if(args.length == 2) {
			// get our expiry date
			Timestamp expiry = (Timestamp)args[1];
			
			// banning
			// determine if its an IP or not
			if(args[0] instanceof ArrayList<?>) {				
				// get everyone on our list
				@SuppressWarnings("unchecked")
				ArrayList<Player> targetPlayers = (ArrayList<Player>)args[0];
				
				// loop over our targets and ban them
				for(Player target: targetPlayers) {
					// make sure the target still exists
					if(target == null) {
						continue;
					}
					
					// ok, they're there.
					// ban them.
					ban(target.getName(), sender, reason, expiry);
				}
			}
			else if(args[0] instanceof String) {
				// banning an IP
				ban((String)args[0], sender, reason, expiry);
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
			String ipString = event.getAddress().toString();
			// strip off the starting '/'
			if(ipString.startsWith("/")) {
				ipString = ipString.substring(1);
			}
			// strip off the back port number
			if(ipString.contains(":")) {
				ipString = ipString.split(":", 2)[0];
			}
			ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
					"select * from banlogs where banee=? or banee=? order by date desc limit 1;",
					event.getName(),
					ipString);
			
			// check their results
			if(results.size() > 0) {
				// if their latest ban log expiry is after now, they're still banned
				Timestamp expiry = (Timestamp)results.get(0).get("expiry");
				Timestamp now = new Timestamp(System.currentTimeMillis());
				
				// create a pretty expiry string
				String expiryString = expiry.toString();
				if(expiry.toString().equals(Timestamp.valueOf("2020-02-02 02:02:02").toString())) {
					expiryString = "forever";
				}
				else {
					expiryString = DateUtils.formatTimestamp(expiry);
				}
				
				if(expiry.after(now)) {
					// nope, they're banned
					event.disallow(Result.KICK_BANNED, ColourHandler.processColours(String.format(
							"&cYou are banned until &f%s&c: &f%s",
							expiryString,
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
			arguments = {"target player[s] / IP address"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "bans the target player[s] / IP address for the given reason",
			permissions = {"ban.forever"})
	public static boolean ban(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		return ban(sender, targetPlayer, "2020-02-02 02:02:02");
	}
	
	@Command(command = "ban",
			arguments = {"target player[s] / IP address", "expiry date"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.DATE},
			description = "bans the target player[s] / IP address for the given reason until the expiry date",
			permissions = {"ban.expiry"})
	public static boolean ban(CommandSender sender, String targetPlayer, String expiry) throws EssentialsCommandException {
		// parse our timestamp
		Timestamp expiryTimestamp = null;
		expiryTimestamp = DateUtils.parseTimestamp(expiry);
		
		// determine if it's an IP or not
		try {
			// try to parse the ip
			InetAddress ip = IPUtils.parseIpAddress(targetPlayer);
			String ipString = ip.toString();
			if(ipString.startsWith("/")) {
				ipString = ipString.substring(1);
			}
			
			// call our multiline chat handler
			ColourHandler.sendMessage(sender, "&cPlease enter a reason for why you're banning this %s:", ip.toString());
			ConversationManager.startConversation(sender, instance, ipString, expiryTimestamp);
		}
		catch(ParseException e) {
			// get a list of all target players
			ArrayList<Player> targetPlayers = PlayerSelector.selectPlayersExact(targetPlayer);
			
			// make sure we have at least one target player
			if(targetPlayers.size() == 0) {
				throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to ban!", targetPlayer);
			}
			
			// call our multiline chat handler
			ColourHandler.sendMessage(sender, "&cPlease enter a reason for why you're banning %s:", targetPlayer);
			ConversationManager.startConversation(sender, instance, targetPlayers, expiryTimestamp);
		}
		
		return true;
	}
	
	// unbans
	@Command(command = "unban",
			arguments = {"target player / IP address"},
			tabCompletions = {TabCompleteType.PLAYER},
			description = "unbans the target player / IP address for the given reason",
			permissions = {"unban"})
	public static boolean unban(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// make sure that player is banned
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from banlogs where banee=? order by date desc limit 1;",
				targetPlayer);
		if(results.size() != 1) {
			throw new EssentialsCommandException("Player / ip '%s' isn't banned!", targetPlayer);
		}
		
		ColourHandler.sendMessage(sender, "&cPlease enter a reason for why you're unbanning %s:", targetPlayer);
		ConversationManager.startConversation(sender, instance, targetPlayer);
		return true;
	}
	
	// determine whether someone is banned or not
	@Command(command = "isbanned",
			arguments = {"target player / IP address"},
			tabCompletions = {TabCompleteType.STRING},
			description = "determines if a player is banned or not",
			permissions = {"isbanned"})
	public static boolean isBanned(CommandSender sender, String targetPlayer) throws EssentialsCommandException {
		// try to get the player
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from banlogs where banee=? order by date desc limit 1;",
				targetPlayer);
		if(results.size() != 1) {
			ColourHandler.sendMessage(sender, "&a%s is &lNOT&r&a banned!", targetPlayer);
			return true;
		}
		
		// yup, they are
		ColourHandler.sendMessage(sender, "%s &6was banned on &8%s&6 by &a%s &6 until &8%s&6: &7%s",
				(String)results.get(0).get("banee"),
				DateUtils.formatTimestamp(((Timestamp)results.get(0).get("date"))),
				(String)results.get(0).get("banner"),
				DateUtils.formatTimestamp(((Timestamp)results.get(0).get("expiry"))),
				(String)results.get(0).get("reason")
				);
		
		return true;
	}
}
