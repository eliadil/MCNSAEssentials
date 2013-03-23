package com.mcnsa.essentials.runnables;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.managers.DatabaseManager;
import com.mcnsa.essentials.utilities.ColourHandler;

public class MailTimerTask implements Runnable {	
	@Override
	public void run() {
		// get all our online players
		Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		
		// start to prepare a statement
		String query = "select recipient, count(recipient) from mail where unread=? and recipient in(";
		for(int i = 0; i < onlinePlayers.length; i++) {
			if(i != 0) {
				query += ",";
			}
			query += "?";
		}
		query += ") group by recipient;";

		PreparedStatement preparedStatement = null;
		try {
			// fill in the actual data
			preparedStatement = DatabaseManager.getConnection().prepareStatement(query);
			preparedStatement.setBoolean(1, true);
			for(int i = 0; i < onlinePlayers.length; i++) {
				preparedStatement.setString(i + 2, onlinePlayers[i].getName());
			}
			
			// now execute the query and return the results
			ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery(preparedStatement);
			for(HashMap<String, Object> result: results) {
				// get our target player
				Player player = Bukkit.getServer().getPlayer((String)result.get("recipient"));
				if(player == null) {
					continue;
				}
				
				// get our count
				long numUnread = (Long)result.get("count(recipient)");
				
				// send them a message
				ColourHandler.sendMessage(player, "&9You have &f%d &9unread messages! Check them with /mail", numUnread);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}