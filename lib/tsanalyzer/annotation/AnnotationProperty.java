package org.processmining.plugins.tsanalyzer.annotation;

/**
 * This class represents one annotation property. For example, it could
 * represent the soujourn time in the node, a business rule, etc. It contains a
 * map of measurements: e.g., average, standard deviation, etc. Each measurement
 * has a name (String) and a value ().
 */
import java.util.HashMap;
import java.util.Map.Entry;

public class AnnotationProperty<T> {

	private final HashMap<String, Comparable<?>> measurements;
	protected T value;

	public AnnotationProperty() {
		super();
		measurements = new HashMap<String, Comparable<?>>();
	}

	protected void setMeasurement(String name, Comparable<?> value) {
		measurements.put(name, value);
	}

	public Comparable<?> getMeasurement(String name) {
		return measurements.get(name);
	}

	public Iterable<Entry<String, Comparable<?>>> getMeasurements() {
		return measurements.entrySet();
	}

	/*
	 * public int compareTo(AnnotationProperty<T> o) { return
	 * getValue().compareTo((o.getValue())); }
	 */

	public T getValue() {
		return value;
	}

	public void setValue(T v) {
		value = v;
	}
}
