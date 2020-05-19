package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class CausalSuccessionAbstractionImpl<E> extends AbstractMatrixAbstraction<E>
		implements CausalSuccessionAbstraction<E> {

	public CausalSuccessionAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
