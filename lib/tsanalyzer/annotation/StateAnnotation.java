package org.processmining.plugins.tsanalyzer.annotation;

import org.processmining.models.graphbased.directed.transitionsystem.State;

public class StateAnnotation extends Annotation {

	public StateAnnotation(State owner) {
		super(owner);
	}

	public State getState() {
		return (State) owner;
	}

}
