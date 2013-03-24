package com.mcnsa.essentials.managers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.bukkit.configuration.file.FileConfiguration;

import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.exceptions.EssentialsSettingsException;
import com.mcnsa.essentials.managers.ComponentManager.Component;
import com.mcnsa.essentials.utilities.ClassReflection;
import com.mcnsa.essentials.utilities.Logger;

public class ConfigurationManager {
	// store our config
	private FileConfiguration fileConfiguation = null;
	
	public ConfigurationManager(FileConfiguration fileConfiguration) {
		// store our config
		this.fileConfiguation = fileConfiguration;
		
		// deal with defaults
		fileConfiguration.options().copyDefaults(true);
	}
	
	public void loadDisabledComponents(ComponentManager componentManager) {
		// get our components
		HashMap<String, Component> registeredComponents = componentManager.getRegisteredComponents();
		
		// deal with disabling components
		this.fileConfiguation.addDefault("disabled-components", new ArrayList<String>());
		List<String> disabledComponents = this.fileConfiguation.getStringList("disabled-components");
		// try to disable all our components
		for(String disabledComponent: disabledComponents) {
			if(!registeredComponents.containsKey(disabledComponent.toLowerCase())) {
				Logger.warning("Component '%s' does not exist and so cannot be disabled!", disabledComponent.toLowerCase());
				continue;
			}
			
			// ok, disable it
			registeredComponents.get(disabledComponent).disabled = true;
		}
	}
	
	public void loadSettings(ComponentManager componentManager) {
		// get all our loaded classes
		Vector<Class<?>> classes = null;
		try {
			classes = ClassReflection.getLoadedClasses();
		}
		catch(Exception e) {
			Logger.error("Unhandled exception (%s): %s!", e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
		
		// copy the data
		List<Class<?>> classList = null;
		synchronized(classes) {
			classList = new ArrayList<Class<?>>(classes);
		}
		
		// loop over all our classes
		for(Class<?> clazz: classList) {
			// and register their settings
			try {
				if(clazz.getPackage() != null) {
					registerSettings(clazz, componentManager);
				}
			}
			catch(Exception e) {
				Logger.error("Failed to load settings for class '%s': %s", clazz.getName(), e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public void registerSettings(Class<?> clazz, ComponentManager componentManager) throws IllegalArgumentException, IllegalAccessException {
		String settingsPrefix = "";
		Component component = null;
		// check to see if it's a component
		if(componentManager.getRegisteredComponents().containsKey(clazz.getSimpleName().toLowerCase())) {
			component = componentManager.getRegisteredComponents().get(clazz.getSimpleName().toLowerCase());
			settingsPrefix = component.componentInfo.permsSettingsPrefix() + ".";
		}
		
		// if we have a component, get any disabled commands in it
		if(component != null) {
			String node = settingsPrefix + "disabled-commands";
			this.fileConfiguation.addDefault(node, new String[]{});
			component.disabledCommands = new ArrayList<String>(this.fileConfiguation.getStringList(node));
		}
		
		// loop over all the fields in the component
		for(Field field: clazz.getFields()) {
			// make sure it has the "Setting" annotation on it
			if(!field.isAnnotationPresent(Setting.class)) {
				continue;
			}
			
			// and make sure it is accessible
			if(!field.isAccessible()) {
				field.setAccessible(true);
			}
			
			// make sure it is static
			if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				// nope
				Logger.warning("Can't configure setting '%s.%s' - only STATIC fields may be configurable settings!",
						clazz.getName(), field.getName());
				continue;
			}
			
			// get the annotation
			Setting setting = field.getAnnotation(Setting.class);
			String node = settingsPrefix + setting.node();
			
			// get the field type
			Class<?> type = field.getType();
			
			try {
				// now determine what to do based on what type of variable it is
				if(type.equals(String.class)) {
					// single string
					this.fileConfiguation.addDefault(node, (String)field.get(null));
					field.set(null, this.fileConfiguation.getString(node, (String)field.get(null)));
				}
				else if(type.equals(String[].class)) {
					// list of strings
					this.fileConfiguation.addDefault(node, (String[])field.get(null));
					List<String> result = this.fileConfiguation.getStringList(node);
					field.set(null, result.toArray(new String[result.size()]));
				}
				else if(type.equals(int.class)) {
					// single int
					this.fileConfiguation.addDefault(node, field.getInt(null));
					field.setInt(null, this.fileConfiguation.getInt(node, field.getInt(null)));
				}
				else if(type.equals(Integer[].class)) {
					// list of ints
					this.fileConfiguation.addDefault(node, (Integer[])field.get(null));
					List<Integer> result = this.fileConfiguation.getIntegerList(node);
					field.set(null, result.toArray(new Integer[result.size()]));
				}
				else if(type.equals(boolean.class)) {
					// single boolean
					this.fileConfiguation.addDefault(node, field.getBoolean(null));
					field.setBoolean(null, this.fileConfiguation.getBoolean(node, field.getBoolean(null)));
				}
				else if(type.equals(Boolean[].class)) {
					// list of booleans
					this.fileConfiguation.addDefault(node, (Boolean[])field.get(null));
					List<Boolean> result = this.fileConfiguation.getBooleanList(node);
					field.set(null, result.toArray(new Boolean[result.size()]));
				}
				else if(type.equals(float.class)) {
					// single float
					this.fileConfiguation.addDefault(node, field.getFloat(null));
					field.setFloat(null, (float)this.fileConfiguation.getDouble(node, field.getFloat(null)));
				}
				else if(type.equals(Float[].class)) {
					// list of floats
					this.fileConfiguation.addDefault(node, (Float[])field.get(null));
					List<Float> result = this.fileConfiguation.getFloatList(node);
					field.set(null, result.toArray(new Float[result.size()]));
				}
				else if(type.equals(long.class)) {
					// single boolean
					this.fileConfiguation.addDefault(node, field.getLong(null));
					field.setLong(null, this.fileConfiguation.getLong(node, field.getLong(null)));
				}
				else if(type.equals(Long[].class)) {
					// list of booleans
					this.fileConfiguation.addDefault(node, (Long[])field.get(null));
					List<Long> result = this.fileConfiguation.getLongList(node);
					field.set(null, result.toArray(new Long[result.size()]));
				}
				else {
					throw new EssentialsSettingsException("Unrecognized setting type '%s' for field %s.%s!", type.toString(), clazz.getSimpleName(), field.getName());
				}
			}
			catch(EssentialsSettingsException e) {
				Logger.warning(e.getMessage() + " &c(ignoring setting)");
			}
		}
	}
}
