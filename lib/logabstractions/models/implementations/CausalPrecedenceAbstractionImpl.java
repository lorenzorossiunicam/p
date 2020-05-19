package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class CausalPrecedenceAbstractionImpl<E> extends AbstractMatrixAbstraction<E>
		implements CausalPrecedenceAbstraction<E> {

	public CausalPrecedenceAbstractionImpl(final E[] eventClasses, final double[][] matrix, final double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
