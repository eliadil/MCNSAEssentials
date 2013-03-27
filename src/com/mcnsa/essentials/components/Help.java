package com.mcnsa.essentials.components;

import java.util.LinkedList;

import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.managers.InformationManager;
import com.mcnsa.essentials.managers.InformationManager.CommandHelp;
import com.mcnsa.essentials.managers.InformationManager.HelpSection;
import com.mcnsa.essentials.utilities.ColourHandler;

@ComponentInfo(friendlyName = "Help",
				description = "Provides information about commands you can use",
				permsSettingsPrefix = "help")
public class Help {
	@Setting(node = "sections-per-page") public static int sectionsPerPage = 5;
	@Setting(node = "commands-per-page") public static int commandsPerPage = 5;
	
	@Command(command = "help",
			description = "lists help sections you can find more information about",
			permissions = {"sections"})
	public static boolean help(CommandSender sender) throws EssentialsCommandException {
		return help(sender, 1);
	}

	@Translation(node = "section-list-header") public static String sectionListHeader = "&e--- &6Help Sections (Page &f%page%&6/&f%numPages%&6) &e---";
	@Translation(node = "section-list-format") public static String sectionListFormat = "&6%section%&7: &f%description%";
	@Command(command = "help",
			arguments = {"page"},
			description = "lists help sections you can find more information about",
			permissions = {"sections"})
	public static boolean help(CommandSender sender, int page) throws EssentialsCommandException {
		// get all components that the sender can use
		LinkedList<HelpSection> sections = InformationManager.listAvailableSections(sender);
		
		// calculate the number of pages
		int totalPages = sections.size() / sectionsPerPage;
		if(sections.size() % 5 != 0) totalPages++;
		
		// make sure we have an appropriate page
		page -= 1;
		if(page < 0) {
			throw new EssentialsCommandException("Can't list negative pages!");
		}
		else if(page >= totalPages) {
			throw new EssentialsCommandException("There are only %d pages available!", totalPages);
		}
		
		// calculate the start and end warp indices
		int start = page * sectionsPerPage;
		int end = start + sectionsPerPage;
		if(end > sections.size()) {
			end = sections.size();
		}
		
		// send our header
		ColourHandler.sendMessage(sender, sectionListHeader
				.replaceAll("%page%", String.valueOf(page + 1))
				.replaceAll("%numPages%", String.valueOf(totalPages)));
		
		// loop over our selected components
		for(int i = start; i < end; i++) {
			// word wrap that sucker
			String componentInfo = sectionListFormat
					.replaceAll("%section%", sections.get(i).name)
					.replaceAll("%description%", sections.get(i).description);
			String[] lines = ChatPaginator.wordWrap(componentInfo, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH);
			
			// don't indent the first line
			// but indent subsequent lines
			StringBuilder usageBlock = new StringBuilder();
			for(int j = 0; j < lines.length; j++) {
				if(j != 0) {
					usageBlock.append("\t");
				}
				usageBlock.append(lines[j]).append("\n");
			}
			
			// send it
			ColourHandler.sendMessage(sender, usageBlock.toString().trim());
		}
		
		return true;
	}
	
	@Command(command = "help",
			arguments = {"section name"},
			tabCompletions = {TabCompleteType.STRING},
			description = "lists commands in a given section",
			permissions = {"commands"})
	public static boolean help(CommandSender sender, String component) throws EssentialsCommandException {
		// catch an error with argument parsing
		// TODO: fix at the source
		try {
			int page = Integer.parseInt(component);
			return help(sender, page);
		}
		catch(NumberFormatException ignored) {
			
		}
		
		return help(sender, component, 1);
	}
	
	@Translation(node = "command-list-header") public static String commandListHeader = "&e--- &6%section% Commands (Page &f%page%&6/&f%numPages%&6) &e---";
	@Command(command = "help",
			arguments = {"section name", "page"},
			tabCompletions = {TabCompleteType.STRING, TabCompleteType.NUMBER},
			description = "lists commands in a given section",
			permissions = {"commands"})
	public static boolean help(CommandSender sender, String targetSection, int page) throws EssentialsCommandException {
		// try to get a list of our commands
		HelpSection section = InformationManager.findSection(targetSection);
		LinkedList<CommandHelp> commands = InformationManager.listSectionCommands(sender, section);
		
		// calculate the number of pages
		int totalPages = commands.size() / commandsPerPage;
		if(commands.size() % 5 != 0) totalPages++;
		
		// make sure we have an appropriate page
		page -= 1;
		if(page < 0) {
			throw new EssentialsCommandException("Can't list negative pages!");
		}
		else if(page >= totalPages) {
			throw new EssentialsCommandException("There are only %d pages available!", totalPages);
		}
		
		// calculate the start and end warp indices
		int start = page * commandsPerPage;
		int end = start + commandsPerPage;
		if(end > commands.size()) {
			end = commands.size();
		}
		
		// send our header
		ColourHandler.sendMessage(sender, commandListHeader
				.replaceAll("%section%", section.name)
				.replaceAll("%page%", String.valueOf(page + 1))
				.replaceAll("%numPages%", String.valueOf(totalPages)));
		
		// loop over our selected commands
		for(int i = start; i < end; i++) {
			String usage = InformationManager.formatUsage(commands.get(i));
			ColourHandler.sendMessage(sender, usage);
		}
		
		return true;
	}
}
