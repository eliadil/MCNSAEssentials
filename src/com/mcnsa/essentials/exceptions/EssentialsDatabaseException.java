package com.mcnsa.essentials.exceptions;

public class EssentialsDatabaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7929858544878185984L;

	public EssentialsDatabaseException(String message) {
		super(message);
	}

	public EssentialsDatabaseException(String format, Object... args) {
		super(String.format(format, args));
	}
}
