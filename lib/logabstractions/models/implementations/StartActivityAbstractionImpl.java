package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractColumnAbstraction;

public class StartActivityAbstractionImpl<E> extends AbstractColumnAbstraction<E>
		implements StartActivityAbstraction<E> {

	public StartActivityAbstractionImpl(E[] eventClasses, double[] column, double threshold) {
		super(eventClasses, column, threshold);
	}

}
