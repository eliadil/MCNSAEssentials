package com.mcnsa.essentials.exceptions;

public class EssentialsCommandException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2794584664600435475L;

	public EssentialsCommandException(String message) {
		super(message);
	}

	public EssentialsCommandException(String format, Object... args) {
		super(String.format(format, args));
	}
}
