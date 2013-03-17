package com.mcnsa.essentials.managers;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.ComponentInfo;

public class ComponentManager {
	public class Component {
		public Class<?> clazz = null;
		public Object instance = null;
		public boolean disabled = false;
		public ArrayList<String> disabledCommands = new ArrayList<String>();
		ComponentInfo componentInfo = null;
	}
	
	private HashMap<String, Component> registeredComponents = new HashMap<String, Component>();
	
	public ComponentManager() {
		// load all the classes in our desired package (the components package)
		
		try {
			// get the code source that we're in
			CodeSource src = CommandsManager.class.getProtectionDomain().getCodeSource();
			if(src != null) {
				URL jar = src.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				
				// get our class loader
				File myFile = new File("plugins/MCNSAEssentials.jar");
				URL myJarFileURL = new URL("jar", "", "file:" + myFile.getAbsolutePath() + "!/");
				URL[] classes = {myJarFileURL};
				URLClassLoader classLoader = new URLClassLoader(classes, this.getClass().getClassLoader());
				
				// now loop over our files
				ZipEntry ze = null;
				assert(classLoader != null);
				while((ze = zip.getNextEntry()) != null) {
					String entryName = ze.getName();
					if(entryName.endsWith(".class") && entryName.startsWith("com/mcnsa/essentials/components/")) {						
						// get it's class
						Class<?> clazz = Class.forName(entryName.replaceAll("/", ".").substring(0, entryName.length() - 6), true, classLoader);
						
						// get its information
						if(!clazz.isAnnotationPresent(ComponentInfo.class)) {
							// no component info!
							// skip it!
							MCNSAEssentials.warning("no component info for class '%s'! Skipping...", clazz.getSimpleName());
							continue;
						}
						ComponentInfo ci = clazz.getAnnotation(ComponentInfo.class); 
						
						// create a component object
						Component component = new Component();
						component.clazz = clazz;
						component.instance = clazz.newInstance();
						component.componentInfo = ci;
						
						// register an instance of it
						registeredComponents.put(clazz.getSimpleName().toLowerCase(), component);
						MCNSAEssentials.debug("Added component '%s'!", clazz.getSimpleName());
					}
				}
			}
			else {
				MCNSAEssentials.error("code source was null!");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			MCNSAEssentials.error("Failed to load component classes!");
		}
	}
	
	public HashMap<String, Component> getRegisteredComponents() {
		return registeredComponents;
	}
}
