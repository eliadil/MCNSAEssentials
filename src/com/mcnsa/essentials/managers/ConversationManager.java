package com.mcnsa.essentials.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
import com.mcnsa.essentials.utilities.Logger;

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
		Map<Object, Object> sessionData = new HashMap<Object, Object>();
		sessionData.put("textInputs", new LinkedList<String>());

		// build our conversation factory
		factory = new ConversationFactory(MCNSAEssentials.getInstance())
			.withModality(true)
			.withPrefix(new NullConversationPrefix())
			.withFirstPrompt(instance.new AddTextPrompt())
			.withEscapeSequence("/cancel")
			.withTimeout(conversationTimeoutSeconds)
			.withInitialSessionData(sessionData)
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
			Logger.debug("Conversation ended gracefully");
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
			Logger.debug("Conversation was abandoned by: %s", event.getCanceller().getClass().getName());
			if(event.getCanceller() instanceof InactivityConversationCanceller) {
				event.getContext().getForWhom().sendRawMessage(ColourHandler.processColours("&cYour text entry timed out!"));
			}
			else {
				event.getContext().getForWhom().sendRawMessage(ColourHandler.processColours("&cYour text entry was cancelled!"));
			}
		}
		
		// remove us from the chatter list
		chatters.remove(event.getContext().getForWhom());
		
		// remove our session data
		event.getContext().setSessionData("textInputs", new LinkedList<String>());
	}
	
	private class AddTextPrompt extends StringPrompt {
		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			// check to see if they're done or cancelling
			if(input.equalsIgnoreCase("/done")) {
				Logger.debug("Conversation finished");
				return Prompt.END_OF_CONVERSATION;
			}
			else if(input.equalsIgnoreCase("/cancel")) {
				Logger.debug("Conversation cancelled");
				return Prompt.END_OF_CONVERSATION;
			}
			
			// now just add it to the list
			@SuppressWarnings("unchecked")
			LinkedList<String> textInputs = (LinkedList<String>)context.getSessionData("textInputs");
			textInputs.add(input);
			context.setSessionData("textInputs", textInputs);
			Logger.debug("Added to conversation: %s", input);
			return new AddTextPrompt();
		}

		@Override
		public String getPromptText(ConversationContext context) {
			// default result for first time
			String result = "&aYou are now entering multiline text. All your text will be captured and stored. To finish, type '/done'. Alternatively, type '/cancel' to cancel";
			
			// but if we have at least one line already, use that instead
			@SuppressWarnings("unchecked")
			LinkedList<String> textInputs = (LinkedList<String>)context.getSessionData("textInputs");
			if(textInputs.size() > 0) {
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
