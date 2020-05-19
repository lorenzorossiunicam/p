package org.processmining.logabstractions.models;

import java.util.Collection;

public interface ColumnAbstraction<E> extends Abstraction<E> {

	Collection<E> getAllGEQThreshold();

	Collection<E> getAllLessThanThreshold();

	double[] getColumn();

	double getThreshold();

	double getValue(E index);

	double getValue(int index);

	boolean holds(E index);

	boolean holds(int index);
}
