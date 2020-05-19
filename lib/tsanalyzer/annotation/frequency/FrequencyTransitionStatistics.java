package org.processmining.plugins.tsanalyzer.annotation.frequency;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class FrequencyTransitionStatistics {

	private final DescriptiveStatistics observations;
	private final DescriptiveStatistics traces;

	public FrequencyTransitionStatistics() {
		super();
		observations = new DescriptiveStatistics();
		traces = new DescriptiveStatistics();
	}

	public DescriptiveStatistics getObservations() {
		return observations;
	}

	public DescriptiveStatistics getTraces() {
		return traces;
	}
}
