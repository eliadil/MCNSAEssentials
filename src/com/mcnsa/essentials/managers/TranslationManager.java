package com.mcnsa.essentials.managers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Translation;
import com.mcnsa.essentials.managers.ComponentManager.Component;
import com.mcnsa.essentials.utilities.ClassReflection;
import com.mcnsa.essentials.utilities.Logger;

public class TranslationManager {
	private File translationFile = new File(MCNSAEssentials.getInstance().getDataFolder(), "translation.yml");
	private YamlConfiguration yaml = new YamlConfiguration();
	
	public void load(ComponentManager componentManager) {
		try {
			// make sure our file exists
			if(!translationFile.exists()) {
				translationFile.createNewFile();
			}
			
			// now load the yaml file
			yaml.load(translationFile);
			// automatically write our default strings out
			yaml.options().copyDefaults(true);
			
			// ok, now go through all of our classes
			// to find translation strings

			// get all our loaded classes
			Vector<Class<?>> classes = ClassReflection.getLoadedClasses();
			
			// copy the data
			List<Class<?>> classList = null;
			synchronized(classes) {
				classList = new ArrayList<Class<?>>(classes);
			}
			
			// loop over all our classes
			for(Class<?> clazz: classList) {
				try {
					loadTranslations(clazz, componentManager);
				}
				catch(Exception e) {
					Logger.error("Failed to load translations for class '%s': %s", clazz.getName(), e.getMessage());
					e.printStackTrace();
				}
			}
			
			// and save the translations file!
			yaml.save(translationFile);
		}
		catch(IOException e) {
			Logger.error("Failed to load translations file!");
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e) {
			Logger.error("Invalid translations file!");
			e.printStackTrace();
		}
		catch (Exception e) {
			Logger.error("Something went wrong loading the translations!");
			e.printStackTrace();
		}
	}
	
	private void loadTranslations(Class<?> clazz, ComponentManager componentManager) {
		// determine a prefix if we have one
		String settingsPrefix = "";
		Component component = null;
		// check to see if it's a component
		if(componentManager.getRegisteredComponents().containsKey(clazz.getSimpleName().toLowerCase())) {
			// it is, so we have a settingsPrefix to use
			component = componentManager.getRegisteredComponents().get(clazz.getSimpleName().toLowerCase());
			settingsPrefix = component.componentInfo.permsSettingsPrefix() + ".";
		}
	
		// loop over all the fields in the component
		for(Field field: clazz.getFields()) {
			// make sure it has the "Translation" annotation on it
			if(!field.isAnnotationPresent(Translation.class)) {
				continue;
			}
			
			// and make sure it is accessible
			if(!field.isAccessible()) {
				field.setAccessible(true);
			}
			
			// make sure it is static
			if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				// nope
				Logger.warning("Can't set translation '%s.%s' - only STATIC fields may be translations!",
						clazz.getName(), field.getName());
				continue;
			}
			
			// get the field type
			Class<?> type = field.getType();
			
			// make sure it's a string
			if(!type.equals(String.class) && !type.equals(String[].class)) {
				// nope
				Logger.warning("Can't set translation '%s.%s' - translations MUST be strings (or string arrays)!",
						clazz.getName(), field.getName());
				continue;
			}
			
			// get the annotation
			Translation translation = field.getAnnotation(Translation.class);
			String node = settingsPrefix + translation.node();
			
			// and get the translation
			try {
				if(type.equals(String.class)) {
					// a single string
					yaml.addDefault(node, (String)field.get(null));
					field.set(null, yaml.getString(node, (String)field.get(null)));
				}
				else {
					// an array of strings
					yaml.addDefault(node, (String[])field.get(null));
					List<String> result = yaml.getStringList(node);
					field.set(null, result.toArray(new String[result.size()]));
				}
			} catch (IllegalArgumentException e) {
				Logger.warning("Can't set translation '%s.%s' - invalid arguments!",
						clazz.getName(), field.getName());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Logger.warning("Can't set translation '%s.%s' - can't access translation!",
						clazz.getName(), field.getName());
				e.printStackTrace();
			}
		}
	}
}
