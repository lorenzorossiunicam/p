package org.processmining.logabstractions.models.implementations;

import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractMatrixAbstraction;

/**
 * 
 * @author svzelst
 *
 */
public class DirectlyFollowsAbstractionImpl<E> extends AbstractMatrixAbstraction<E>
		implements DirectlyFollowsAbstraction<E> {

	public DirectlyFollowsAbstractionImpl(E[] eventClasses, double[][] matrix, double threshold) {
		super(eventClasses, matrix, threshold);
	}
}
