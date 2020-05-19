package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;

class ConfidenceIntervalAverage extends ConfidenceInterval {

	protected double constant;

	public ConfidenceIntervalAverage(double cnst) {
		super();
		constant = cnst;
	}

	public double getLowerBorder(StatisticsAnnotationProperty property) {
		return property.getValue().doubleValue() - getConstrant(property);
	}

	public double getUpperBorder(StatisticsAnnotationProperty property) {
		return property.getValue().doubleValue() + getConstrant(property);
	}

	protected double getConstrant(StatisticsAnnotationProperty property) {
		return constant;
	}
}
