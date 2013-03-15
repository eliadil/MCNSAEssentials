package com.mcnsa.essentials.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	String command();
	String[] aliases() default {};
	String[] arguments() default {};
	String description();
	String[] permissions() default {};
	boolean playerOnly() default false;
	boolean consoleOnly() default false;
}
