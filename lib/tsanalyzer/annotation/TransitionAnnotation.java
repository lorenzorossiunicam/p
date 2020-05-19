package org.processmining.plugins.tsanalyzer.annotation;

import org.processmining.models.graphbased.directed.transitionsystem.Transition;

public class TransitionAnnotation extends Annotation {

	public TransitionAnnotation(Transition owner) {
		super(owner);
	}

	public Transition getTransition() {
		return (Transition) owner;
	}

}
