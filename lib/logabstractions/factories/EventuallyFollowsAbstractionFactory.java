package org.processmining.logabstractions.factories;

import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.EventuallyFollowsAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;
import org.processmining.logabstractions.models.implementations.EventuallyFollowsAbstractionImpl;

public class EventuallyFollowsAbstractionFactory {

	public static final double BOOLEAN_DEFAULT_THRESHOLD = 1.0;

	public static strictfp <E> double[][] constructAlphaPlusPlusEventuallyFollowsMatrix(
			CausalAbstraction<E> cra, LongTermFollowsAbstraction<E> ltfa) {
		double[][] matrix = new double[cra.getNumberOfRows()][cra.getNumberOfColumns()];
		for (int r = 0; r < matrix.length; r++) {
			for (int c = 0; c < matrix.length; c++) {
				if (cra.getValue(r, c) >= cra.getThreshold() || ltfa.getValue(r, c) >= ltfa.getThreshold()) {
					matrix[r][c] = BOOLEAN_DEFAULT_THRESHOLD;
				}
			}
		}
		return matrix;
	}

	public static strictfp <E> EventuallyFollowsAbstraction<E> constructAlphaPlusPlusEventuallyFollowsAbstraction(
			CausalAbstraction<E> cra, LongTermFollowsAbstraction<E> ltfa) {
		return new EventuallyFollowsAbstractionImpl<>(cra.getEventClasses(),
				constructAlphaPlusPlusEventuallyFollowsMatrix(cra, ltfa), BOOLEAN_DEFAULT_THRESHOLD);
	}
}
