package org.processmining.plugins.tsanalyzer.annotation.time;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class TimeStateStatistics {

	private final DescriptiveStatistics soujourn;
	private final DescriptiveStatistics remaining;
	private final DescriptiveStatistics elapsed;

	public TimeStateStatistics() {
		super();
		soujourn = new DescriptiveStatistics();
		remaining = new DescriptiveStatistics();
		elapsed = new DescriptiveStatistics();
	}

	public DescriptiveStatistics getSoujourn() {
		return soujourn;
	}

	public DescriptiveStatistics getRemaining() {
		return remaining;
	}

	public DescriptiveStatistics getElapsed() {
		return elapsed;
	}
}
