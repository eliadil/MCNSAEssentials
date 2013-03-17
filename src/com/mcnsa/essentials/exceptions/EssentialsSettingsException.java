package com.mcnsa.essentials.exceptions;

public class EssentialsSettingsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5470440109687424801L;

	public EssentialsSettingsException(String message) {
		super(message);
	}

	public EssentialsSettingsException(String format, Object... args) {
		super(String.format(format, args));
	}

}
