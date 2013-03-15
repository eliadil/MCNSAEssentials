package com.mcnsa.essentials.containers;

import org.bukkit.entity.Player;

public class EssentialsPlayer {
	private Player player;
	
	public EssentialsPlayer(Player player) {
		this.setPlayer(player);
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
};
