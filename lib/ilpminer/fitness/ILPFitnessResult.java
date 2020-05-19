package org.processmining.plugins.ilpminer.fitness;

import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class ILPFitnessResult {
	private Map<Transition, Integer> missing, consumed, totalEvents, failedEvents;

	public ILPFitnessResult(Map<Transition, Integer> missing,
			Map<Transition, Integer> consumed,
			Map<Transition, Integer> totalEvents,
			Map<Transition, Integer> failedEvents) {
		this.missing = missing;
		this.consumed = consumed;
		this.totalEvents = totalEvents;
		this.failedEvents = failedEvents;
	}

	public void setMissing(Map<Transition, Integer> missing) {
		this.missing = missing;
	}

	public Map<Transition, Integer> getMissing() {
		return missing;
	}

	public void setConsumed(Map<Transition, Integer> consumed) {
		this.consumed = consumed;
	}

	public Map<Transition, Integer> getConsumed() {
		return consumed;
	}

	public void setTotalEvents(Map<Transition, Integer> totalEvents) {
		this.totalEvents = totalEvents;
	}

	public Map<Transition, Integer> getTotalEvents() {
		return totalEvents;
	}

	public void setFailedEvents(Map<Transition, Integer> failedEvents) {
		this.failedEvents = failedEvents;
	}

	public Map<Transition, Integer> getFailedEvents() {
		return failedEvents;
	}
}
