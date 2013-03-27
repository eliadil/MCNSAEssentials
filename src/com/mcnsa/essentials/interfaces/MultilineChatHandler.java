package com.mcnsa.essentials.interfaces;

import org.bukkit.command.CommandSender;

import com.mcnsa.essentials.exceptions.EssentialsCommandException;

public interface MultilineChatHandler {
	public void onChatComplete(CommandSender sender, String enteredText, Object... args) throws EssentialsCommandException;
}
