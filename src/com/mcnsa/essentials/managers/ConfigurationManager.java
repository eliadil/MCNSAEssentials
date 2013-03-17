package com.mcnsa.essentials.managers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.Setting;
import com.mcnsa.essentials.exceptions.EssentialsSettingsException;
import com.mcnsa.essentials.managers.ComponentManager.Component;

public class ConfigurationManager {
	// store our config
	private FileConfiguration fileConfiguation = null;
	
	public ConfigurationManager(FileConfiguration fileConfiguration, ComponentManager componentManager) {
		// store our config
		this.fileConfiguation = fileConfiguration;
		
		// deal with defaults
		fileConfiguration.options().copyDefaults(true);
		
		// get our components
		HashMap<String, Component> registeredComponents = componentManager.getRegisteredComponents();
		
		// deal with disabling components
		this.fileConfiguation.addDefault("disabled-components", new ArrayList<String>());
		List<String> disabledComponents = this.fileConfiguation.getStringList("disabled-components");
		// try to disable all our components
		for(String disabledComponent: disabledComponents) {
			if(!registeredComponents.containsKey(disabledComponent.toLowerCase())) {
				MCNSAEssentials.warning("Component '%s' does not exist and so cannot be disabled!", disabledComponent.toLowerCase());
				continue;
			}
			
			// ok, disable it
			registeredComponents.get(disabledComponent).disabled = true;
		}
		
		for(String component: registeredComponents.keySet()) {
			// now deal with disabling commands
			String disabledKeyString = registeredComponents.get(component).componentInfo.permsSettingsPrefix() + ".disabled-commands";
			this.fileConfiguation.addDefault(disabledKeyString, new ArrayList<String>());
			List<String> disabledCommands = this.fileConfiguation.getStringList(disabledKeyString);
			registeredComponents.get(component).disabledCommands.addAll(disabledCommands);

			// register configuration keys from the component manager
			try {
				// and register it's config keys
				// but only if its not disabled
				if(!registeredComponents.get(component).disabled) {
					registerComponentSettings(registeredComponents.get(component));
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				MCNSAEssentials.error("Failed to load component settings!");
				MCNSAEssentials.error("Contact and administrator for help!");
				break;
			}
		}
	}
	
	public void registerComponentSettings(Component component) throws IllegalArgumentException, IllegalAccessException {
		Object instance = component.instance;
		
		// loop over all the fields in the component
		for(Field field: instance.getClass().getFields()) {
			// make sure it has the "Setting" annotation on it
			if(!field.isAnnotationPresent(Setting.class)) {
				continue;
			}
			
			// and make sure it is accessible
			if(!field.isAccessible()) {
				field.setAccessible(true);
			}
			
			// get the annotation
			Setting setting = field.getAnnotation(Setting.class);
			String node = component.componentInfo.permsSettingsPrefix() + "." + setting.node();
			
			// get the field type
			Class<?> type = field.getType();
			
			try {
				// now determine what to do based on what type of variable it is
				if(type.equals(String.class)) {
					// single string
					this.fileConfiguation.addDefault(node, (String)field.get(instance));
					field.set(instance, this.fileConfiguation.getString(node, (String)field.get(instance)));
				}
				else if(type.equals(String[].class)) {
					// list of strings
					this.fileConfiguation.addDefault(node, (String[])field.get(instance));
					List<String> result = this.fileConfiguation.getStringList(node);
					field.set(instance, result.toArray(new String[result.size()]));
				}
				else if(type.equals(int.class)) {
					// single int
					this.fileConfiguation.addDefault(node, field.getInt(instance));
					field.setInt(instance, this.fileConfiguation.getInt(node, field.getInt(instance)));
				}
				else if(type.equals(Integer[].class)) {
					// list of ints
					this.fileConfiguation.addDefault(node, (Integer[])field.get(instance));
					List<Integer> result = this.fileConfiguation.getIntegerList(node);
					field.set(instance, result.toArray(new Integer[result.size()]));
				}
				else if(type.equals(boolean.class)) {
					// single boolean
					this.fileConfiguation.addDefault(node, field.getBoolean(instance));
					field.setBoolean(instance, this.fileConfiguation.getBoolean(node, field.getBoolean(instance)));
				}
				else if(type.equals(Boolean[].class)) {
					// list of booleans
					this.fileConfiguation.addDefault(node, (Boolean[])field.get(instance));
					List<Boolean> result = this.fileConfiguation.getBooleanList(node);
					field.set(instance, result.toArray(new Boolean[result.size()]));
				}
				else if(type.equals(float.class)) {
					// single float
					this.fileConfiguation.addDefault(node, field.getFloat(instance));
					field.setFloat(instance, (float)this.fileConfiguation.getDouble(node, field.getFloat(instance)));
				}
				else if(type.equals(Float[].class)) {
					// list of floats
					this.fileConfiguation.addDefault(node, (Float[])field.get(instance));
					List<Float> result = this.fileConfiguation.getFloatList(node);
					field.set(instance, result.toArray(new Float[result.size()]));
				}
				else if(type.equals(long.class)) {
					// single boolean
					this.fileConfiguation.addDefault(node, field.getLong(instance));
					field.setLong(instance, this.fileConfiguation.getLong(node, field.getLong(instance)));
				}
				else if(type.equals(Long[].class)) {
					// list of booleans
					this.fileConfiguation.addDefault(node, (Long[])field.get(instance));
					List<Long> result = this.fileConfiguation.getLongList(node);
					field.set(instance, result.toArray(new Long[result.size()]));
				}
				else {
					throw new EssentialsSettingsException("Unrecognized setting type '%s' for field %s.%s!", type.toString(), component.clazz.getSimpleName(), field.getName());
				}
			}
			catch(EssentialsSettingsException e) {
				MCNSAEssentials.warning(e.getMessage() + " &c(ignoring setting)");
			}
		}
	}
}
