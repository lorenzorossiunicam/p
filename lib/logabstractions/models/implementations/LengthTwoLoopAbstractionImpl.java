package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.LengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class LengthTwoLoopAbstractionImpl<E> extends AbstractMatrixAbstraction<E>
		implements LengthTwoLoopAbstraction<E> {

	public LengthTwoLoopAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
