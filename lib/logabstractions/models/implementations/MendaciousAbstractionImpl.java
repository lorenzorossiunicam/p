package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.MendaciousAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

public class MendaciousAbstractionImpl<E> extends AbstractMatrixAbstraction<E> implements MendaciousAbstraction<E> {

	public MendaciousAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}

}
