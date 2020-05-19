package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.EventuallyFollowsAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class EventuallyFollowsAbstractionImpl<E> extends AbstractMatrixAbstraction<E>
		implements EventuallyFollowsAbstraction<E> {

	public EventuallyFollowsAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
