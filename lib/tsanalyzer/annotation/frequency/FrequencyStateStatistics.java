package org.processmining.plugins.tsanalyzer.annotation.frequency;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class FrequencyStateStatistics {

	private final DescriptiveStatistics observations;
	private final DescriptiveStatistics traces;		

	public FrequencyStateStatistics() {
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
