package org.processmining.plugins.tsanalyzer.annotation;

/**
 * This class represents one annotation, which is generated for one system node
 * (state or transition). One annotation can contain multiple annotation
 * properties (e.g., soujourn time, remaining time, elapsed time...)
 */

import java.util.HashMap;
import java.util.Map.Entry;

public class Annotation {

	private final HashMap<String, AnnotationProperty<?>> properties;
	protected Object owner;

	public Annotation(Object owner) {
		super();
		properties = new HashMap<String, AnnotationProperty<?>>();
		this.owner = owner;
	}

	protected void addProperty(String name, AnnotationProperty<?> property) {
		properties.put(name, property);
	}

	public AnnotationProperty<?> getProperty(String name) {
		return properties.get(name);
	}

	public Iterable<Entry<String, AnnotationProperty<?>>> getProperties() {
		return properties.entrySet();
	}

	/*
	 * public Object getOwner() { return owner; }
	 */
}
