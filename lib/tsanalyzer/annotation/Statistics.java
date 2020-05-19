package org.processmining.plugins.tsanalyzer.annotation;

import java.util.HashMap;
import java.util.Map.Entry;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;


/**
 * @author abolt
 *
 * Generic statistics handling for extension
 *
 * @param <S> abstraction for state statistics
 * @param <T> abstraction for transition statistics
 */
public abstract class Statistics<S,T> {

	protected HashMap<State, S> states;
	protected HashMap<Transition, T> transitions;
	
	public Statistics()
	{		
		states = new HashMap<State,S>();
		transitions = new HashMap<Transition,T>();
	}
	
	public Iterable<Entry<State, S>> getStates() {
		return states.entrySet();
	}

	public Iterable<Entry<Transition, T>> getTransitions() {
		return transitions.entrySet();
	}

	public abstract S getStatistics(State state);

	public abstract T getStatistics(Transition transition);
}
