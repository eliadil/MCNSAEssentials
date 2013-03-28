package com.mcnsa.essentials.utilities;

public class StringUtils {
	public static String implode(String joiner, String... parts) {
		// join our expression
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String part: parts) {
			if(first) {
				sb.append(part);
				first = false;
			}
			else {
				sb.append(joiner).append(part);
			}
		}
		return sb.toString();
	}
}
