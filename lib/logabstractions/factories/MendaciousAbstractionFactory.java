package org.processmining.logabstractions.factories;

import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.MendaciousAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.implementations.MendaciousAbstractionImpl;

public class MendaciousAbstractionFactory {

	public static double DEFAULT_THRESHOLD_BOOLEAN = 1.0d;

	/**
	 * precondition: cra,dfa,pa share the same eventclasses
	 * 
	 * @param cra
	 * @param dfa
	 * @param pa
	 * @return
	 */
	public static strictfp <E> double[][] constructBooleanMendaciousAbstractionMatrix(CausalAbstraction<E> cra,
			DirectlyFollowsAbstraction<E> dfa, ParallelAbstraction<E> pa) {
		double[][] matrix = new double[cra.getMatrix().length][cra.getMatrix().length];
		for (int a = 0; a < cra.getEventClasses().length; a++) {
			for (int b = 0; b < cra.getEventClasses().length; b++) {
				if (cra.holds(a, b) || (a == b && dfa.holds(a, b))) {
					for (int x : cra.getAllIndicesGeqForRow(a)) {
						for (int y : cra.getAllIndicesGeqForColumn(b)) {
							if (!dfa.holds(y, x) && !pa.holds(x, b) && !pa.holds(a, y)) {
								matrix[a][b] = DEFAULT_THRESHOLD_BOOLEAN;
							}
						}
					}
				}
			}
		}
		return matrix;
	}

	public static strictfp <E> MendaciousAbstraction<E> constructAlphaSharpMendaciousAbstraction(
			CausalAbstraction<E> cra, DirectlyFollowsAbstraction<E> dfa, ParallelAbstraction<E> pa) {
		return new MendaciousAbstractionImpl<>(cra.getEventClasses(),
				constructBooleanMendaciousAbstractionMatrix(cra, dfa, pa), DEFAULT_THRESHOLD_BOOLEAN);
	}

	/**
	 * Please refer to Page 109, Equation 1 of
	 * "Mining Process Models with Prime Invisible Tasks"; L. Wen et al.
	 * 
	 * @param ma
	 * @return
	 */
	public static strictfp <E> Pair<MendaciousAbstraction<E>, MendaciousAbstraction<E>> splitByRedundancyRuleAlphaSharp(
			CausalAbstraction<E> ca, MendaciousAbstraction<E> ma) {
		double[][] nonRed = new double[ma.getEventClasses().length][ma.getEventClasses().length];
		double[][] redundant = new double[ma.getEventClasses().length][ma.getEventClasses().length];
		for (int a = 0; a < ma.getEventClasses().length; a++) {
			// a ~> b
			for (int b : ma.getAllIndicesGeqForRow(a)) {
				boolean cdFound = false;
				// a ~> d 
				cdLoop: for (int d : ma.getAllIndicesGeqForRow(a)) {
					if (d != b) { // this condition is not specified in the paper
						for (int c : ca.getAllIndicesGeqForColumn(d)) {
							if (c != a) { // this condition is not specified in the paper
								if (ma.holds(c, b)) {
									cdFound = true;
									break cdLoop;
								}
							}

						}
					}
				}
				if (cdFound) {
					redundant[a][b] = DEFAULT_THRESHOLD_BOOLEAN;
				} else {
					nonRed[a][b] = DEFAULT_THRESHOLD_BOOLEAN;
				}
			}
		}
		return new Pair<MendaciousAbstraction<E>, MendaciousAbstraction<E>>(
				new MendaciousAbstractionImpl<>(ma.getEventClasses(), nonRed, DEFAULT_THRESHOLD_BOOLEAN),
				new MendaciousAbstractionImpl<>(ma.getEventClasses(), redundant, DEFAULT_THRESHOLD_BOOLEAN));
	}

}
