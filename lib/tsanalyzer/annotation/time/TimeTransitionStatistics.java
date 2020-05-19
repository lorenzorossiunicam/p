package org.processmining.plugins.tsanalyzer.annotation.time;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class TimeTransitionStatistics {
	
	private final DescriptiveStatistics duration;

	public TimeTransitionStatistics() {
		super();
		duration = new DescriptiveStatistics();
	}

	public DescriptiveStatistics getDuration() {
		return duration;
	}
}
