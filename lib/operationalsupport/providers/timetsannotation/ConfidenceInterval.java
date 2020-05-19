package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;

abstract class ConfidenceInterval {

	public static final int UNDER = 0;
	public static final int WITHIN = 1;
	public static final int ABOVE = 2;

	public ConfidenceInterval() {
		super();
	}

	public int inInterval(double value, StatisticsAnnotationProperty property) {

		if (value < getLowerBorder(property)) {
			return UNDER;
		}
		if (getUpperBorder(property) < value) {
			return ABOVE;
		}
		return WITHIN;
	}

	public boolean underInterval(double value, StatisticsAnnotationProperty property) {
		return (getLowerBorder(property) <= value) && (value <= getUpperBorder(property));
	}

	public abstract double getLowerBorder(StatisticsAnnotationProperty property);

	public abstract double getUpperBorder(StatisticsAnnotationProperty property);
}
