package org.processmining.plugins.tsanalyzer.annotation;

import java.util.HashMap;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;

public class TransitionSystemAnnotation<S extends StateAnnotation, T extends TransitionAnnotation> {

	private final HashMap<State, S> states;
	private final HashMap<Transition, T> transitions;

	public TransitionSystemAnnotation() {
		super();
		states = new HashMap<State, S>();
		transitions = new HashMap<Transition, T>();
	}

	public void addStateAnnotation(S annotation) {
		states.put(annotation.getState(), annotation);
	}

	public S getStateAnnotation(State state) {
		return states.get(state);
	}

	public Iterable<S> getAllStateAnnotations() {
		return states.values();
	}

	public void addTransitionAnnotation(T annotation) {
		transitions.put(annotation.getTransition(), annotation);
	}

	public T getTransitionAnnotation(Transition transition) {
		return transitions.get(transition);
	}

	public Iterable<T> getAllTransitionAnnotations() {
		return transitions.values();
	}
}
