package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.LongTermFollowsAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class LongTermFollowsAbstractionImpl<E> extends AbstractMatrixAbstraction<E>
		implements LongTermFollowsAbstraction<E> {

	public LongTermFollowsAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
