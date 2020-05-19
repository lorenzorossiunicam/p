package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractColumnAbstraction;

public class EndActivityAbstractionImpl<E> extends AbstractColumnAbstraction<E>
		implements EndActivityAbstraction<E> {

	public EndActivityAbstractionImpl(E[] eventClasses, double[] column, double threshold) {
		super(eventClasses, column, threshold);
	}

}
