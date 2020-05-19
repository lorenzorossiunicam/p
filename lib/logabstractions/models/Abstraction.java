package org.processmining.logabstractions.models;

/**
 * Process Mining "Abstraction"
 * 
 * @author svzelst
 *
 * @param <E>
 *            event class
 */
public interface Abstraction<E> {
	
	int getIndex(E t);

	E getEventClass(int index);

	E[] getEventClasses();

}
