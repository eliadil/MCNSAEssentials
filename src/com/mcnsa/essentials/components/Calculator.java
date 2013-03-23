package com.mcnsa.essentials.components;

import org.bukkit.command.CommandSender;

import com.mcnsa.essentials.annotations.Command;
import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.utilities.ColourHandler;
import com.mcnsa.essentials.utilities.MathEval;
import com.mcnsa.essentials.utilities.StringUtils;

@ComponentInfo(friendlyName = "Calculator",
				description = "A calculator for in-game convenience",
				permsSettingsPrefix = "calculator")
public class Calculator {
	private static String formatResult(double result) {
		return String.format("%.4f", result);
	}
	
	@Command(command = "calc",
			arguments = {"expression"},
			description = "evaluates the expression and returns the result",
			permissions = {"calc"})
	public static boolean calculate(CommandSender sender, String... expressions) throws EssentialsCommandException {
		String expression = StringUtils.implode(" ", expressions);
		
		// evaluate it!
		MathEval math = new MathEval();
		double result = math.evaluate(expression);
		ColourHandler.sendMessage(sender, "&aResult: &f%s", formatResult(result));
		
		return true;
	}
}
