package com.mcnsa.essentials.utilities;

import java.lang.reflect.Field;
import java.util.Vector;

import com.mcnsa.essentials.managers.ConfigurationManager;

public class ClassReflection {
	public static Vector<Class<?>> getLoadedClasses() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		// get a list of all our classes
		//Logger.debug("Getting classes...");
		Field f = ClassLoader.class.getDeclaredField("classes");
		//Logger.debug("Type: %s", f.getType().getName());
		f.setAccessible(true);
		@SuppressWarnings("unchecked")
		Vector<Class<?>> classes = (Vector<Class<?>>)f.get(ConfigurationManager.class.getClassLoader());
		/*for(Class<?> clazz: classes) {
			if(clazz != null && clazz.getPackage() != null) {
				Logger.debug("Found class '%s' (%s)!", clazz.getName(), clazz.getPackage().getName());
			}
		}
		Logger.debug("done!");*/
		
		return classes;
	}
}
