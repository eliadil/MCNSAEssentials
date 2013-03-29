package com.mcnsa.essentials.managers;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.NullConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.interfaces.MultilineChatHandler;
import com.mcnsa.essentials.utilities.ColourHandler;

public class ConversationManager implements ConversationAbandonedListener {
	@Setting(node = "conversation-timeout-seconds") public static int conversationTimeoutSeconds = 30;
	
	// store our conversation builder
	private static ConversationFactory factory = null;
	
	// store our instance
	private static ConversationManager instance = null;
	
	// store a map of all commandsenders undergoing conversations
	// (since we can't store metadata on CommandSenders)
	private static HashMap<Conversable, MultilineChatData> chatters = new HashMap<Conversable, MultilineChatData>();
	
	public ConversationManager() {
		// store our instance
		instance = this;
	}
	
	public void load() {
		// build our conversation factory
		factory = new ConversationFactory(MCNSAEssentials.getInstance())
			.withModality(true)
			.withPrefix(new NullConversationPrefix())
			.withFirstPrompt(instance.new AddTextPrompt())
			.withEscapeSequence("/cancel")
			.withTimeout(conversationTimeoutSeconds)
			.withLocalEcho(false)
			.addConversationAbandonedListener(instance);
	}
	
	public static void startConversation(CommandSender sender, MultilineChatHandler handler, Object... args) throws EssentialsCommandException {
		if(chatters.containsKey(sender)) {
			throw new EssentialsCommandException("You are already conversing!");
		}
		
		if(sender instanceof Conversable) {
			factory.buildConversation((Conversable)sender).begin();
			chatters.put((Conversable)sender, (instance.new MultilineChatData()).setHandler(handler).setArgs(args));
		}
		else {
			throw new EssentialsCommandException("You can't converse!");
		}
	}

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent event) {
		if(event.gracefulExit()) {
			// get our chat data
			MultilineChatData chatData = chatters.get(event.getContext().getForWhom());
			try {
				// get our text inputs
				@SuppressWarnings("unchecked")
				LinkedList<String> textInputs = (LinkedList<String>)event.getContext().getSessionData("textInputs");
				
				// compose our string
				StringBuilder lines = new StringBuilder();
				for(String line: textInputs) {
					lines.append(line).append("\n");
				}
					
				// call the event handler
				CommandSender sender = (CommandSender)event.getContext().getForWhom();
				chatData.handler.onChatComplete(sender,
						lines.toString().trim(),
						chatData.args);
			}
			catch (EssentialsCommandException e) {
				// catch any errors
				ColourHandler.sendMessage((CommandSender)event.getContext().getForWhom(), "&c" + e.getMessage());
			}
		}
		else {
			if(event.getCanceller() instanceof InactivityConversationCanceller) {
				event.getContext().getForWhom().sendRawMessage(ColourHandler.processColours("&cYour text entry timed out!"));
			}
			else {
				event.getContext().getForWhom().sendRawMessage(ColourHandler.processColours("&cYour text entry was cancelled!"));
			}
		}
		
		// remove us from the chatter list
		chatters.remove(event.getContext().getForWhom());
	}
	
	private class AddTextPrompt extends StringPrompt {
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			// check to see if they're done or cancelling
			if(input.equalsIgnoreCase("/done")) {
				return Prompt.END_OF_CONVERSATION;
			}
			
			// now just add it to the list
			@SuppressWarnings("unchecked")
			LinkedList<String> textInputs = (LinkedList<String>)context.getSessionData("textInputs");
			if(textInputs == null) {
				textInputs = new LinkedList<String>();
			}
			textInputs.add(input);
			context.setSessionData("textInputs", textInputs);
			return new AddTextPrompt();
		}

		@Override
		public String getPromptText(ConversationContext context) {
			// default result for first time
			String result = "&aYou are now entering multiline text. All your text will be captured and stored. To finish, type '/done'. Alternatively, type '/cancel' to cancel";
			
			// but if we have at least one line already, use that instead
			@SuppressWarnings("unchecked")
			LinkedList<String> textInputs = (LinkedList<String>)context.getSessionData("textInputs");
			if(textInputs != null && textInputs.size() > 0) {
				result = "&9Added text: &f" + textInputs.getLast();
			}
			
			return ColourHandler.processColours(result);
		}
	}
	
	private class MultilineChatData {
		public MultilineChatHandler handler = null;
		public Object[] args = null;
		
		MultilineChatData setHandler(MultilineChatHandler handler) {
			this.handler = handler;
			return this;
		}
		
		MultilineChatData setArgs(Object[] args) {
			this.args = args;
			return this;
		}
	}
}
