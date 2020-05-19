package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class CausalRelationAbstractionImpl<E> extends AbstractMatrixAbstraction<E> implements CausalAbstraction<E> {

	public CausalRelationAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
