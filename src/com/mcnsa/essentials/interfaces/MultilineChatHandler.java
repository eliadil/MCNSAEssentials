package com.mcnsa.essentials.interfaces;

import org.bukkit.entity.Player;

import com.mcnsa.essentials.exceptions.EssentialsCommandException;

public interface MultilineChatHandler {
	public void onChatComplete(Player player, String enteredText, Object... args) throws EssentialsCommandException;
}
