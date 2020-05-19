package org.processmining.logabstractions.models.abstr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.processmining.logabstractions.models.Abstraction;

public class AbstractAbstraction<E> implements Abstraction<E> {

	private final E[] eventClasses;
	private Map<E, Integer> indices = new HashMap<>();

	public AbstractAbstraction(E[] eventClasses) {
		this.eventClasses = eventClasses;
	}

	public E[] getEventClasses() {
		return eventClasses;
	}

	public synchronized int getIndex(E t) {
		int i = -1;
		i = indices.containsKey(t) ? indices.get(t) : -1;
		if (i == -1) {
			i = Arrays.asList(getEventClasses()).indexOf(t);
			indices.put(t, i);
		}
		return i;
	}

	public E getEventClass(int index) {
		return getEventClasses()[index];
	}

}
