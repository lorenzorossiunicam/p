package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class ParallelAbstractionImpl<E> extends AbstractMatrixAbstraction<E> implements ParallelAbstraction<E> {

	public ParallelAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
