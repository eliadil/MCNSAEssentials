package com.mcnsa.essentials.components;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import com.mcnsa.essentials.annotations.Translation;
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
	
	@Translation(node = "you-have-been-banned") public static String youHaveBeenBanned =
			"&cYou have been banned until %expiry% by %banner%: %reason%";
	@Translation(node = "you-banned") public static String youBanned =
			"&6You have banned %player% until %expiry%!";
	private static void ban(String banee, CommandSender banner, String reason, Timestamp expiry, boolean doKick) throws EssentialsCommandException {
		// create a pretty expiry string
		String expiryString = DateUtils.formatTimestamp(expiry, false);
		
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
						String message = ColourHandler.processColours(youHaveBeenBanned
								.replaceAll("%expiry%", expiryString)
								.replaceAll("%banner%", banner.getName())
								.replaceAll("%reason%", reason));
						ColourHandler.sendMessage(online, message);
						online.kickPlayer(message);
					}
				}
				
				// ban the IP in bukkit
				Bukkit.getServer().banIP(IPUtils.stripIP(ip));
			}
			catch(ParseException e) {
				// nope, it must be a player
				// get them
				Player player = Bukkit.getServer().getPlayer(banee);
				if(player != null) {
					// yup, we found em!
					String message = ColourHandler.processColours(youHaveBeenBanned
							.replaceAll("%expiry%", expiryString)
							.replaceAll("%banner%", banner.getName())
							.replaceAll("%reason%", reason));
					ColourHandler.sendMessage(player, message);
					player.kickPlayer(message);
				}
				
				// "Smart" ban them (ban their IP as well, for the next smartBanHours hours)
				ban(IPUtils.stripIP(player.getAddress()), banner, reason, new Timestamp(System.currentTimeMillis() + (smartBanHours * 60 * 60 * 1000)), false);
				
				// and ban them in bukkit
				player.setBanned(true);
			}
		}
		
		// and alert the banner
		ColourHandler.sendMessage(banner, youBanned
				.replaceAll("%player%", banee)
				.replaceAll("%expiry%", expiryString));
		
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
	
	@Translation(node = "you-unbanned") public static String youUnbanned = "&6You have unbanned %player%!";
	private static void unban(String unbanee, CommandSender unbanner, String reason) throws EssentialsCommandException {
		// and alert the unbanner
		ColourHandler.sendMessage(unbanner, youUnbanned.replaceAll("%player%", unbanee));
		
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
		
		// determine if it is an ip or not
		try {
			// try to parse the ip
			InetAddress ip = IPUtils.parseIpAddress(unbanee);
			
			// unban them in bukkit
			Bukkit.getServer().unbanIP(IPUtils.stripIP(ip));
		}
		catch(ParseException e) {
			// nope
			// try to get the offline player
			OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(unbanee);
			// and unban them in bukkit
			if(player.isBanned()) {
				player.setBanned(false);
			}
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
	@Translation(node = "login-banned") public static String loginBanned = "&cYou are banned until &f%expiry%&c: &f%reason%";
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		try {
			// see if they're banned or not			
			// query the database
			ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
					"select * from banlogs where banee=? or banee=? order by date desc limit 1;",
					event.getName(),
					IPUtils.stripIP(event.getAddress()));
			
			// check their results
			if(results.size() > 0) {
				// if their latest ban log expiry is after now, they're still banned
				Timestamp expiry = (Timestamp)results.get(0).get("expiry");
				Timestamp now = new Timestamp(System.currentTimeMillis());
				
				// create a pretty expiry string
				String expiryString = DateUtils.formatTimestamp(expiry, false);
				
				if(expiry.after(now)) {
					// nope, they're banned
					event.disallow(Result.KICK_BANNED, ColourHandler.processColours(String.format(
							loginBanned
								.replaceAll("%expiry%", expiryString)
								.replaceAll("%reason%", (String)results.get(0).get("reason"))
								.replaceAll("%banner%", (String)results.get(0).get("banner")))));
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
	
	@Translation(node = "provide-reason") public static String provideReason = "&cPlease enter a reason for why you're banning %player%:";
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
			String ipString = IPUtils.stripIP(ip);
			
			// call our multiline chat handler
			ColourHandler.sendMessage(sender, provideReason.replaceAll("%player%", ipString));
			ConversationManager.startConversation(sender, instance, ipString, expiryTimestamp);
		}
		catch(ParseException e) {
			// get a list of all target players
			ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
			
			// make sure we have at least one target player
			if(targetPlayers.size() == 0) {
				throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to ban!", targetPlayer);
			}
			
			// call our multiline chat handler
			ColourHandler.sendMessage(sender, provideReason.replaceAll("%player%", targetPlayer));
			ConversationManager.startConversation(sender, instance, targetPlayers, expiryTimestamp);
		}
		
		return true;
	}
	
	// unbans
	@Translation(node = "isnt-banned") public static String isntBanned = "%player% isn't banned!";
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
			throw new EssentialsCommandException(isntBanned.replaceAll("%player", targetPlayer));
		}
		
		ColourHandler.sendMessage(sender, "&cPlease enter a reason for why you're unbanning %s:", targetPlayer);
		ConversationManager.startConversation(sender, instance, targetPlayer);
		return true;
	}
	
	// determine whether someone is banned or not
	@Translation(node = "ban-info") public static String banInfo =
			"%player% &6was banned on &8%date%&6 by &a%banner% &6 until &8%expiry%&6: &7%reason%";
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
			ColourHandler.sendMessage(sender, isntBanned.replaceAll("%player", targetPlayer));
			return true;
		}
		
		// yup, they are
		ColourHandler.sendMessage(sender, banInfo
				.replaceAll("%player%", (String)results.get(0).get("banee"))
				.replaceAll("%date%", DateUtils.formatTimestamp(((Timestamp)results.get(0).get("date")), false))
				.replaceAll("%banner%", (String)results.get(0).get("banner"))
				.replaceAll("%expiry%", DateUtils.formatTimestamp(((Timestamp)results.get(0).get("expiry")), false))
				.replaceAll("%reason%", (String)results.get(0).get("reason"))
				);
		
		return true;
	}
}
