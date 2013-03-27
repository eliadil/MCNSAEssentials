package com.mcnsa.essentials.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mcnsa.essentials.enums.TabCompleteType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	String command();
	String[] aliases() default {};
	String[] arguments() default {};
	TabCompleteType[] tabCompletions() default{};
	String description();
	String[] permissions() default {};
	boolean playerOnly() default false;
	boolean consoleOnly() default false;
}
