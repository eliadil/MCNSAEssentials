package com.mcnsa.essentials.components;

import org.bukkit.command.CommandSender;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.enums.TabCompleteType;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.MathEval;
import com.mcnsa.essentials.utilities.StringUtils;

@ComponentInfo(friendlyName = "Calculator",
				description = "A calculator for in-game convenience",
				permsSettingsPrefix = "calculator")
public class Calculator {
	@Translation(node = "result-format") public static String resultFormat = "&aResult: &f%result%";
	
	/*private static String formatResult(double result) {
		return String.format("%.4f", result);
	}*/
	
	@Command(command = "calc",
			arguments = {"expression"},
			tabCompletions = {TabCompleteType.STRING},
			description = "evaluates the expression and returns the result",
			permissions = {"calc"})
	public static boolean calculate(CommandSender sender, String... expressions) throws EssentialsCommandException {
		
		String expression = StringUtils.implode(" ", expressions);
		MathEval math = new MathEval();
		double result = 0;
		
		// evaluate it!
		try {
			result = math.evaluate(expression);
		} catch (ArithmeticException | NumberFormatException e) {
			throw new EssentialsCommandException("Incorrect expression.");
		}
		
		ColourHandler.sendMessage(sender, resultFormat.replaceAll("%result%", String.valueOf(result)));
		
		return true;
	}
}
