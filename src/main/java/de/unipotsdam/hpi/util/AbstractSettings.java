package de.unipotsdam.hpi.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


/**
 * Allows for directly initializing settings from a {@link Properties} object.<br>
 * Subclasses should annotate a {@link Property} to their members, so that these
 * fields will be automatically loaded with the right property.
 */
abstract public class AbstractSettings {

	public void load(String propertiesPath) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesPath));
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Could not load properties from " + propertiesPath, e);
		}
		load(properties);
	}

	/**
	 * Loads all matching fields from the properties. If a field is not included
	 * in the given properties, it remains as it is.
	 */
	public void load(Properties properties) {
		System.out.println("Reading settings from properties:");
		System.out.println(properties);

		Set<String> usedProperties = new HashSet<String>();
		List<Class<?>> hierarchy = new ArrayList<Class<?>>();
		for (Class<?> cls = getClass(); AbstractSettings.class
				.isAssignableFrom(cls); cls = cls.getSuperclass()) {
			hierarchy.add(cls);
		}
		Collections.reverse(hierarchy);

		for (Class<?> cls : hierarchy)
			fillFieldsForClass(properties, usedProperties, cls);

		Set<String> unusedProperties = properties.stringPropertyNames();
		unusedProperties.removeAll(usedProperties);
		for (String unusableProperty : unusedProperties) {
			System.err.println("Warning: Non-read property in property file.");
			System.err.println("\t" + unusableProperty);
		}
	}

	private void fillFieldsForClass(Properties properties,
			Set<String> usedProperties, Class<?> cls) {
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			Property propertyAnnotation = field.getAnnotation(Property.class);
			if (propertyAnnotation == null)
				continue;

			String propertyKey = propertyAnnotation.value();
			readProperty(propertyKey, field, properties);
			usedProperties.add(propertyKey);
		}
	}

	public Properties toProperties() {
		Properties properties = new Properties();

		Field[] fields = getClass().getDeclaredFields();
		for (Field field : fields) {
			Property propertyAnnotation = field.getAnnotation(Property.class);
			if (propertyAnnotation == null)
				continue;

			String propertyKey = propertyAnnotation.value();
			String value = readField(field);
			if (value != null)
				properties.setProperty(propertyKey, value);
		}

		return properties;
	}

	private String readField(Field field) {
		try {
			Class<?> type = field.getType();
			if (type.equals(int.class))
				return String.valueOf(field.getInt(this));

			else if (type.equals(boolean.class))
				return String.valueOf(field.getBoolean(this));

			else if (type.equals(double.class))
				return String.valueOf(field.getDouble(this));

			else if (type.equals(String.class))
				return (String) field.get(this);

			else
				throw new Exception("Unsupported field class: " + type);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void readProperty(String propertyKey, Field field,
			Properties properties) {
		String value = properties.getProperty(propertyKey);
		if (value == null)
			return;

		try {
			Class<?> type = field.getType();
			if (type.equals(int.class))
				field.setInt(this, Integer.parseInt(value));

			else if (type.equals(boolean.class))
				field.setBoolean(this, Boolean.parseBoolean(value));

			else if (type.equals(double.class))
				field.setDouble(this, Double.parseDouble(value));

			else if (type.equals(String.class))
				field.set(this, value);

			else
				throw new Exception("Unsupported field class: " + type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
