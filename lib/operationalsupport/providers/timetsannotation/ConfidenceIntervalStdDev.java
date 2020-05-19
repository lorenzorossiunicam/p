package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;

class ConfidenceIntervalStdDev extends ConfidenceIntervalAverage {

	public ConfidenceIntervalStdDev(double cnst) {
		super(cnst);
	}

	protected double getConstrant(StatisticsAnnotationProperty property) {
		return constant * property.getStandardDeviation();
	}
}
