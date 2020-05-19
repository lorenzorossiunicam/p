package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractColumnAbstraction;

public class LengthOneLoopAbstractionImpl<E> extends AbstractColumnAbstraction<E>
		implements LengthOneLoopAbstraction<E> {

	public LengthOneLoopAbstractionImpl(E[] eventClasses, double[] column, double threshold) {
		super(eventClasses, column, threshold);
	}

}
