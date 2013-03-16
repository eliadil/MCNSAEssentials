package com.mcnsa.essentials.exceptions;

public class CommandException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2794584664600435475L;

	public CommandException(String message) {
		super(message);
	}

	public CommandException(String format, Object... args) {
		super(String.format(format, args));
	}
}
