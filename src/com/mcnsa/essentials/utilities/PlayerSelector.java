package com.mcnsa.essentials.utilities;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.mcnsa.essentials.exceptions.EssentialsCommandException;

public class PlayerSelector {
	public static OfflinePlayer selectSinglePlayer(String target) {
		return Bukkit.getServer().getOfflinePlayer(target);
	}
	
	public static ArrayList<Player> selectPlayers(String target) throws EssentialsCommandException {
		ArrayList<Player> matchedPlayers = new ArrayList<Player>();
		if(target.equals("*")) {
			matchedPlayers = new ArrayList<Player>(Arrays.asList(Bukkit.getServer().getOnlinePlayers()));
		}
		else if(target.startsWith("world:")) {
			// split it into half
			String[] parts = target.split(";", 2);
			if(parts.length != 2) {
				throw new EssentialsCommandException("Player-in-world selection format: world:<worldname>;<player[s]>");
			}
			
			// ok, get the world name
			String[] worldParts = parts[0].split(":", 2);
			if(worldParts.length != 2) {
				throw new EssentialsCommandException("Player-in-world selection format: world:<worldname>;<player[s]>");
			}
			// try to get the world
			World targetWorld = Bukkit.getServer().getWorld(worldParts[1]);
			if(targetWorld == null) {
				throw new EssentialsCommandException("I couldn't find the world '%s' to select from!", worldParts[1]);
			}
			
			// ok, world exists
			// get all players that we're targeting
			ArrayList<Player> players = selectPlayers(parts[1]);
			
			// go through and make sure that the players are in the correct world
			for(Player player: players) {
				if(player.getWorld().equals(targetWorld)) {
					matchedPlayers.add(player);
				}
			}
		}
		else {
			String[] playerTargets = target.split(",");
			// now add online players
			
			for(int i = 0; i < playerTargets.length; i++) {
				ArrayList<Player> players = getOnlinePlayersWithNameLike(playerTargets[i]);
				if(players.size() == 1)
				{
					matchedPlayers.add(players.get(0));
				}
				else if(players.size() > 1)
				{
					ArrayList<String> listOfNames = new ArrayList<String>();
					for(Player p : players)
					{
						listOfNames.add(p.getName());
					}
					String implodedNames = StringUtils.implode(",", listOfNames.toArray(new String[listOfNames.size()]));
					throw new EssentialsCommandException("There is more than one player with name like '%s'!\n '%s'", playerTargets[i], implodedNames);
				}
			}
		}
		
		/*if(matchedPlayers.isEmpty()) {
			throw new EssentialsCommandException("I couldn't find the player[s] '%s'! For information on player selecting, type /helpess playerselecting", target);
		}*/
		
		return matchedPlayers;
	}
	
	
	private static ArrayList<Player> getOnlinePlayersWithNameLike(String name)
	{
		ArrayList<Player> playerList = new ArrayList<Player>();
		Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		for(Player player: onlinePlayers) {
			if(player.getName().toLowerCase().startsWith(name.toLowerCase())) {
				playerList.add(player);
			}
		}
		return playerList;
	}
}
