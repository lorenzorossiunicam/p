package org.processmining.plugins.tsanalyzer.annotation.time;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.tsanalyzer.annotation.Statistics;

public class TimeStatistics extends Statistics<TimeStateStatistics,TimeTransitionStatistics>{

	public TimeStatistics() {
		super();
	}

	public TimeStateStatistics getStatistics(State state) {
		TimeStateStatistics statistics = states.get(state);
		if (statistics == null) {
			statistics = new TimeStateStatistics();
			states.put(state, statistics);
		}
		return statistics;
	}

	public TimeTransitionStatistics getStatistics(Transition transition) {
		TimeTransitionStatistics statistics = transitions.get(transition);
		if (statistics == null) {
			statistics = new TimeTransitionStatistics();
			transitions.put(transition, statistics);
		}
		return statistics;
	}
}
