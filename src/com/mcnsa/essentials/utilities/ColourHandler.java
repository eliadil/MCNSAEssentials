package com.mcnsa.essentials.utilities;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.AnsiConsole;

public class ColourHandler {
	static Integer nextColour = new Integer(0);
	static boolean colourInitialized = false;
	
	// make it a static class) private constructor, static methods
	private ColourHandler() {}

	public static String translateColour(String name) {
		String colour = new String("???");
		
		// map it!
		if(name.equalsIgnoreCase("&0")) colour = "black";
		else if(name.equalsIgnoreCase("&1")) colour = "dark blue";
		else if(name.equalsIgnoreCase("&2")) colour = "dark green";
		else if(name.equalsIgnoreCase("&3")) colour = "dark teal";
		else if(name.equalsIgnoreCase("&4")) colour = "dark red";
		else if(name.equalsIgnoreCase("&5")) colour = "purple";
		else if(name.equalsIgnoreCase("&6")) colour = "gold";
		else if(name.equalsIgnoreCase("&7")) colour = "grey";
		else if(name.equalsIgnoreCase("&8")) colour = "dark grey";
		else if(name.equalsIgnoreCase("&9")) colour = "blue";
		else if(name.equalsIgnoreCase("&a")) colour = "green";
		else if(name.equalsIgnoreCase("&b")) colour = "teal";
		else if(name.equalsIgnoreCase("&c")) colour = "red";
		else if(name.equalsIgnoreCase("&d")) colour = "pink";
		else if(name.equalsIgnoreCase("&e")) colour = "yellow";
		else if(name.equalsIgnoreCase("&f")) colour = "white";
		else if(name.equalsIgnoreCase("&k")) colour = "magic";
		else if(name.equalsIgnoreCase("&l")) colour = "bold";
		else if(name.equalsIgnoreCase("&m")) colour = "strikethrough";
		else if(name.equalsIgnoreCase("&n")) colour = "underlined";
		else if(name.equalsIgnoreCase("&o")) colour = "italics";
		else if(name.equalsIgnoreCase("&r")) colour = "reset";
		
		return colour;
	}

	public static String translateName(String name) {
		// default colour will be white.
		String colour = new String("");
		
		// map it!
		if(name.equalsIgnoreCase("black")) colour = "&0";
		else if(name.equalsIgnoreCase("dblue")) colour = "&1";
		else if(name.equalsIgnoreCase("dark blue")) colour = "&1";
		else if(name.equalsIgnoreCase("dgreen")) colour = "&2";
		else if(name.equalsIgnoreCase("dark green")) colour = "&2";
		else if(name.equalsIgnoreCase("dteal")) colour = "&3";
		else if(name.equalsIgnoreCase("dark teal")) colour = "&3";
		else if(name.equalsIgnoreCase("daqua")) colour = "&3";
		else if(name.equalsIgnoreCase("dark aqua")) colour = "&3";
		else if(name.equalsIgnoreCase("dred")) colour = "&4";
		else if(name.equalsIgnoreCase("dark red")) colour = "&4";
		else if(name.equalsIgnoreCase("purple")) colour = "&5";
		else if(name.equalsIgnoreCase("dpink")) colour = "&5";
		else if(name.equalsIgnoreCase("dark pink")) colour = "&5";
		else if(name.equalsIgnoreCase("gold")) colour = "&6";
		else if(name.equalsIgnoreCase("orange")) colour = "&6";
		else if(name.equalsIgnoreCase("grey")) colour = "&7";
		else if(name.equalsIgnoreCase("gray")) colour = "&7";
		else if(name.equalsIgnoreCase("dgrey")) colour = "&8";
		else if(name.equalsIgnoreCase("dark grey")) colour = "&8";
		else if(name.equalsIgnoreCase("dgray")) colour = "&8";
		else if(name.equalsIgnoreCase("dark gray")) colour = "&8";
		else if(name.equalsIgnoreCase("blue")) colour = "&9";
		else if(name.equalsIgnoreCase("green")) colour = "&a";
		else if(name.equalsIgnoreCase("bright green")) colour = "&a";
		else if(name.equalsIgnoreCase("teal")) colour = "&b";
		else if(name.equalsIgnoreCase("aqua")) colour = "&b";
		else if(name.equalsIgnoreCase("red")) colour = "&c";
		else if(name.equalsIgnoreCase("pink")) colour = "&d";
		else if(name.equalsIgnoreCase("yellow")) colour = "&e";
		else if(name.equalsIgnoreCase("white")) colour = "&f";
		else if(name.equalsIgnoreCase("random")) colour = "&k";
		else if(name.equalsIgnoreCase("magic")) colour = "&k";
		else if(name.equalsIgnoreCase("bold")) colour = "&l";
		else if(name.equalsIgnoreCase("strike")) colour = "&m";
		else if(name.equalsIgnoreCase("strikethrough")) colour = "&m";
		else if(name.equalsIgnoreCase("underline")) colour = "&n";
		else if(name.equalsIgnoreCase("italics")) colour = "&o";
		else if(name.equalsIgnoreCase("italic")) colour = "&o";
		else if(name.equalsIgnoreCase("reset")) colour = "&r";
		
		return colour;
	}

	// allow for colour tags to be used in strings..
	public static String processColours(String str) {
		return processConsoleColours(str);
	}
	
	public static String processConsoleColours(String str) {		
		str = str.replaceAll("&0", Ansi.ansi().fg(Ansi.Color.BLACK).boldOff().toString());
		str = str.replaceAll("&1", Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString());
		str = str.replaceAll("&2", Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString());
		str = str.replaceAll("&3", Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString());
		str = str.replaceAll("&4", Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString());
		str = str.replaceAll("&5", Ansi.ansi().fg(Ansi.Color.MAGENTA).boldOff().toString());
		str = str.replaceAll("&6", Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString());
		str = str.replaceAll("&7", Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
		str = str.replaceAll("&8", Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString());
		str = str.replaceAll("&9", Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString());
		str = str.replaceAll("&a", Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString());
		str = str.replaceAll("&b", Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString());
		str = str.replaceAll("&c", Ansi.ansi().fg(Ansi.Color.RED).bold().toString());
		str = str.replaceAll("&d", Ansi.ansi().fg(Ansi.Color.MAGENTA).toString());
		str = str.replaceAll("&e", Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
		str = str.replaceAll("&f", Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString());
		str = str.replaceAll("&k", Ansi.ansi().a(Attribute.BLINK_SLOW).toString());
		str = str.replaceAll("&l", Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString());
		str = str.replaceAll("&m", Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString());
		str = str.replaceAll("&n", Ansi.ansi().a(Attribute.UNDERLINE).toString());
		str = str.replaceAll("&o", Ansi.ansi().a(Attribute.ITALIC).toString());
		str = str.replaceAll("&r", Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT).toString());
		return str;
	}

	// strip colour tags from strings..
	public static String stripColours(String str) {
		return str.replaceAll("(&([a-f0-9klmnor]))", "").replaceAll("(\u00A7([a-f0-9klmnor]))", "");
	}
	
	public static void sendMessage(Player player, String message) {
		if(message.length() < 1) {
			return;
		}
		player.sendMessage(processColours(message));
	}
	
	public static void sendMessage(CommandSender sender, String message) {
		if(message.length() < 1) {
			return;
		}
		if(sender instanceof Player) {
			sender.sendMessage(processColours(message));
		}
		else {
			consoleMessage(message);
		}
	}
	
	public static void sendMessage(JavaPlugin plugin, String name, String message) {
		if(message.length() < 1) {
			return;
		}
		Player player = plugin.getServer().getPlayer(name);
		if(player != null) {
			player.sendMessage(processColours(message));
		}
	}
	
	public static void consoleMessage(String message) {
		if(message.length() < 1) {
			return;
		}
		
		if(!colourInitialized) {
			AnsiConsole.systemInstall();
		}
		AnsiConsole.out.println(ColourHandler.processConsoleColours(message));
		//ColouredConsoleSender.getInstance().sendMessage(ColourHandler.processConsoleColours(message));
		//MCNSAEssentials.log(ColourHandler.processConsoleColours(message));
	}
}