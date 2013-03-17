package com.mcnsa.essentials.utilities;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerSelector {
	public static OfflinePlayer selectSinglePlayer(String target) {
		return Bukkit.getServer().getOfflinePlayer(target);
	}
	
	public static ArrayList<Player> selectPlayersExact(String target) {
		ArrayList<Player> matchedPlayers = new ArrayList<Player>();
		if(target.equals("*")) {
			matchedPlayers = new ArrayList<Player>(Arrays.asList(Bukkit.getServer().getOnlinePlayers()));
		}
		else {
			String[] playerTargets = target.split(",");
			// now add online players
			for(int i = 0; i < playerTargets.length; i++) {
				Player player = Bukkit.getServer().getPlayer(playerTargets[i]);
				if(player != null) {
					matchedPlayers.add(player);
				}
			}
		}
		
		return matchedPlayers;
	}
}
