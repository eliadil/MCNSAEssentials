package com.mcnsa.essentials.utilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	String[] aliases();
	String usage() default "";
	String shortDescription();
	String help();
}
