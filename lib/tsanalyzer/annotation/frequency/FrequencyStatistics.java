package org.processmining.plugins.tsanalyzer.annotation.frequency;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.tsanalyzer.annotation.Statistics;

public class FrequencyStatistics extends Statistics<FrequencyStateStatistics,FrequencyTransitionStatistics>{
	
	public FrequencyStatistics() {
		super();
	}

	public FrequencyStateStatistics getStatistics(State state) {
		FrequencyStateStatistics statistics = states.get(state);
		if (statistics == null) {
			statistics = new FrequencyStateStatistics();
			states.put(state, statistics);
		}
		return statistics;
	}

	public FrequencyTransitionStatistics getStatistics(Transition transition) {
		FrequencyTransitionStatistics statistics = transitions.get(transition);
		if (statistics == null) {
			statistics = new FrequencyTransitionStatistics();
			transitions.put(transition, statistics);
		}
		return statistics;
	}

}
