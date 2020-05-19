package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.TwoWayLengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class TwoWayLengthTwoLoopAbstractionImpl<E> extends AbstractMatrixAbstraction<E>
		implements TwoWayLengthTwoLoopAbstraction<E> {

	public TwoWayLengthTwoLoopAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
