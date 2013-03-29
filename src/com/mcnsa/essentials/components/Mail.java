package com.mcnsa.essentials.components;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.ChatPaginator;

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
import com.mcnsa.essentials.runnables.MailTimerTask;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.PlayerSelector;

@ComponentInfo(friendlyName = "Mail",
				description = "Provides inter-player mail",
				permsSettingsPrefix = "mail")
@DatabaseTableInfo(name = "mail",
					fields = { "recipient TINYTEXT", "sender TINYTEXT", "date TIMESTAMP", "subject TINYTEXT", "contents TEXT", "unread BOOLEAN" })
public class Mail implements Listener, MultilineChatHandler {
	@Setting(node = "messages-per-page") public static int MESSAGES_PER_PAGE = 5;
	@Setting(node = "update-interval-minutes") public static float UPDATE_INTERVAL = 5f;
	
	private static Mail instance;
	
	public Mail() {
		instance = this;
		
		// register our events
		Bukkit.getServer().getPluginManager().registerEvents(this, MCNSAEssentials.getInstance());
		
		// set up a timer task to alert people of new mail
		MailTimerTask task = new MailTimerTask();
		long numTicks = (long)(UPDATE_INTERVAL * 60 * 20);
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(
				MCNSAEssentials.getInstance(), task, 0, numTicks);
	}
	
	public static int countNumberUnread(Player player) throws EssentialsCommandException {
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select id from mail where recipient=? and unread=?;",
				player.getName(), 1);
		return results.size();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) throws EssentialsCommandException {
		ColourHandler.sendMessage(event.getPlayer(), "&6You have %d unread mail messages!", countNumberUnread(event.getPlayer()));
	}
	
	@Command(command = "mail",
			description = "lists your mail messages",
			permissions = {"read"},
			playerOnly = true)
	public static boolean checkMail(CommandSender sender) throws EssentialsCommandException {
		return checkMail(sender, 1);
	}
	
	@Command(command = "mail",
			arguments = {"page #"},
			tabCompletions = {TabCompleteType.NUMBER},
			description = "lists your mail messages",
			permissions = {"read"},
			playerOnly = true)
	public static boolean checkMail(CommandSender sender, int page) throws EssentialsCommandException {
		// get our mail
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from mail where recipient=? order by date desc;",
				sender.getName());
		
		// calculate the number of pages
		int totalPages = results.size() / MESSAGES_PER_PAGE;
		if(results.size() % 5 != 0) totalPages++;
		
		// make sure we have an appropriate page
		page -= 1;
		if(page < 0) {
			throw new EssentialsCommandException("Can't list negative pages!");
		}
		else if(page >= totalPages) {
			throw new EssentialsCommandException("There are only %d pages available!", totalPages);
		}
		
		// calculate the start and end warp indices
		int start = page * MESSAGES_PER_PAGE;
		int end = start + MESSAGES_PER_PAGE;
		if(end > results.size()) {
			end = results.size();
		}
		
		// show our mail
		ColourHandler.sendMessage(sender, "&6%s's Inbox (page %d/%d):", sender.getName(), (page+1), totalPages);
		for(int i = start; i < end; i++) {
			ColourHandler.sendMessage(sender,
					"&7[%d]&f%s &9%s&7: &f%s &7(%s)",
					(Integer)results.get(i).get("id"),
					(Boolean)results.get(i).get("unread") ? "*" : "",
					(String)results.get(i).get("sender"),
					(String)results.get(i).get("subject"),
					((Timestamp)results.get(i).get("date")).toString());
		}
		
		return true;
	}
	
	@Command(command = "readmail",
			arguments = {"mail ID"},
			tabCompletions = {TabCompleteType.NUMBER},
			description = "reads the mail message with the given ID",
			permissions = {"read"},
			playerOnly = true)
	public static boolean readMail(CommandSender sender, int mailID) throws EssentialsCommandException {
		// get our mail
		ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(
				"select * from mail where recipient=? and id=?;",
				sender.getName(), mailID);
		
		// make sure we got something
		if(results.size() == 0) {
			throw new EssentialsCommandException("'%d' isn't a valid mail ID (or does not belong to you!", mailID);
		}
		
		// ok, read it
		String mailSender = (String)results.get(0).get("sender");
		String subject = (String)results.get(0).get("subject");
		String contents = ColourHandler.processColours((String)results.get(0).get("contents"));
		Timestamp date = (Timestamp)results.get(0).get("date");
		ColourHandler.sendMessage(sender, "&9Mail from %s: %s (on %s):", mailSender, subject, date.toString());
		String[] lines = ChatPaginator.wordWrap(contents, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH);
		for(String line: lines) {
			ColourHandler.sendMessage(sender, line);
		}
		
		// now mark it as read
		int updateResults = DatabaseManager.updateQuery(
				"update mail set unread=? where id=?;",
				false, (Integer)results.get(0).get("id"));
		
		// make sure it worked!
		if(updateResults == 0) {
			throw new EssentialsCommandException("Failed to mark the message as read!");
		}
		
		return true;
	}
	
	@Command(command = "sendmail",
			arguments = {"target player[s]", "subject"},
			tabCompletions = {TabCompleteType.PLAYER, TabCompleteType.STRING},
			description = "Starts writing mail to the target player[s] with the given subject",
			permissions = {"send"},
			playerOnly = true)
	public static boolean sendMail(CommandSender sender, String targetPlayer, String subject) throws EssentialsCommandException {
		// get a list of all target players
		ArrayList<Player> targetPlayers = PlayerSelector.selectPlayers(targetPlayer);
		
		// make sure we have at least one target player
		if(targetPlayers.size() == 0) {
			throw new EssentialsCommandException("I couldn't find / parse target player[s] '%s' to send mail to!", targetPlayer);
		}
		
		// call our multiline chat handler
		//MultilineChatEntry.scheduleMultilineTextEntry((Player)sender, instance, targetPlayers, subject);
		ConversationManager.startConversation(sender, instance, targetPlayers, subject);
		
		return true;
	}

	@Override
	public void onChatComplete(CommandSender sender, String contents, Object... args) throws EssentialsCommandException {
		// get our arguments back
		@SuppressWarnings("unchecked")
		ArrayList<Player> targetPlayers = (ArrayList<Player>)args[0];
		String subject = (String)args[1];
		
		// loop over all our recipients
		for(Player target: targetPlayers) {
			// add our mail message
			int results = DatabaseManager.updateQuery(
					"insert into mail (id, recipient, sender, date, subject, contents, unread) values (NULL, ?, ?, ?, ?, ?, ?);",
					target.getName(),
					sender.getName(),
					new Timestamp(System.currentTimeMillis()),
					subject,
					contents,
					1);
			
			// make sure it worked!
			if(results == 0) {
				throw new EssentialsCommandException("Failed to send mail!");
			}
			
			// alert the people
			ColourHandler.sendMessage(sender, "&aYour message has been delivered to %s!", target.getName());
			ColourHandler.sendMessage(target, "&aYou have new mail from %s!", sender.getName());
		}
	}
}
