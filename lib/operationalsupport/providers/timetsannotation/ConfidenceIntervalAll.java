package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;

class ConfidenceIntervalAll extends ConfidenceInterval {

	protected double getBorder(StatisticsAnnotationProperty property) {
		return property.getMin();
	}

	public double getLowerBorder(StatisticsAnnotationProperty property) {
		return property.getMin();
	}

	public double getUpperBorder(StatisticsAnnotationProperty property) {
		return property.getMax();
	}
}
