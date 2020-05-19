package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.UnrelatedAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class UnrelatedAbstractionImpl<E> extends AbstractMatrixAbstraction<E> implements UnrelatedAbstraction<E> {

	public UnrelatedAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
