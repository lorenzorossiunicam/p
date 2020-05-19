package org.processmining.logabstractions.factories;

import java.util.Collection;
import java.util.Map;

import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.LengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.MendaciousAbstraction;
import org.processmining.logabstractions.models.TwoWayLengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;
import org.processmining.logabstractions.models.implementations.CausalPrecedenceAbstractionImpl;
import org.processmining.logabstractions.models.implementations.CausalRelationAbstractionImpl;
import org.processmining.logabstractions.models.implementations.CausalSuccessionAbstractionImpl;

/**
 * Factory method for causal abstractions: causal relation abstraction, causal
 * precedence abstraction, causal succession abstraction
 * 
 * @author svzelst
 *
 */
public class CausalAbstractionFactory {

	public static double DEFAULT_THRESHOLD_BOOLEAN = 1.0;

	public static <D extends DirectlyFollowsAbstraction<E>, E> CausalAbstraction<E> constructAlphaClassicCausalAbstraction(
			D dfa) {
		return constructBooleanCausalAbstraction(dfa);
	}

	public static strictfp double[][] constructAlphaClassicCausalAbstractionMatrix(DirectlyFollowsAbstraction<?> dfa) {
		return constructBooleanCausalAbstractionMatrix(dfa);
	}

	public static strictfp <E> CausalAbstraction<E> constructAlphaPlusCausalAbstraction(
			DirectlyFollowsAbstraction<E> dfa, TwoWayLengthTwoLoopAbstraction<E> twltla, double threshold) {
		return new CausalRelationAbstractionImpl<>(dfa.getEventClasses(), constructAlphaPlusCausalMatrix(dfa, twltla),
				threshold);
	}

	public static strictfp <T> double[][] constructAlphaPlusCausalMatrix(DirectlyFollowsAbstraction<T> dfa,
			TwoWayLengthTwoLoopAbstraction<T> twltla) {
		double[][] cra = new double[dfa.getNumberOfRows()][dfa.getNumberOfColumns()];
		for (int r = 0; r < cra.length; r++) {
			for (int c = 0; c < cra[r].length; c++) {
				if (dfa.getValue(r, c) >= dfa.getThreshold()) {
					if (dfa.getValue(c, r) >= dfa.getThreshold()) {
						if (twltla.getValue(r, c) >= twltla.getThreshold()) {
							cra[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
							cra[c][r] = DEFAULT_THRESHOLD_BOOLEAN;
						}
					} else {
						cra[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
			}
		}
		return cra;
	}

	public static strictfp <T> double[][] constructAlphaPlusPlusCausalAbstractionMatrix(
			DirectlyFollowsAbstraction<T> dfa, LengthTwoLoopAbstraction<T> ltla) {
		double[][] cra = new double[dfa.getNumberOfRows()][dfa.getNumberOfColumns()];
		for (int r = 0; r < cra.length; r++) {
			for (int c = 0; c < cra[r].length; c++) {
				if (dfa.getValue(r, c) >= dfa.getThreshold()) {
					if (dfa.getValue(c, r) >= dfa.getThreshold()) {
						if (ltla.getValue(r, c) >= ltla.getThreshold() || ltla.getValue(c, r) >= ltla.getThreshold()) {
							cra[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
							cra[c][r] = DEFAULT_THRESHOLD_BOOLEAN;
						}
					} else {
						cra[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
			}
		}
		return cra;
	}

	public static strictfp <E> CausalPrecedenceAbstraction<E> constructAlphaPlusPlusCausalPrecedenceAbstraction(
			CausalAbstraction<E> cra, UnrelatedAbstraction<E> ua) {
		return new CausalPrecedenceAbstractionImpl<>(cra.getEventClasses(),
				constructAlphaPlusPlusCausalPrecedenceAbstractionMatrix(cra, ua), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <T> double[][] constructAlphaPlusPlusCausalPrecedenceAbstractionMatrix(
			CausalAbstraction<T> cra, UnrelatedAbstraction<T> ua) {
		double[][] cpa = new double[cra.getNumberOfRows()][cra.getNumberOfColumns()];
		for (int r = 0; r < cra.getMatrix().length; r++) {
			for (int c = 0; c < cra.getMatrix()[r].length; c++) {
				if (ua.getValue(r, c) >= ua.getThreshold()) {
					for (int other = 0; other < cra.getMatrix()[r].length; other++) {
						if (cra.getValue(other, r) >= cra.getThreshold()
								&& cra.getValue(other, c) >= cra.getThreshold()) {
							cpa[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
						}
					}
				}
			}
		}
		return cpa;
	}

	public static strictfp <E> CausalPrecedenceAbstraction<E> constructAlphaPlusPlusCausalPrecedenceAbstraction(
			CausalAbstraction<E> cra, UnrelatedAbstraction<E> ua, double threshold) {
		return new CausalPrecedenceAbstractionImpl<E>(cra.getEventClasses(),
				CausalAbstractionFactory.constructAlphaPlusPlusCausalPrecedenceAbstractionMatrix(cra, ua), threshold);
	}

	public static strictfp <T> Pair<double[][], double[][]> constructAlphaPlusPlusCausalPrecedenceSuccessionAbstractionMatrix(
			CausalAbstraction<T> cra, UnrelatedAbstraction<T> ua) {
		double[][] cpa = new double[cra.getNumberOfRows()][cra.getNumberOfColumns()];
		double[][] csa = new double[cra.getNumberOfRows()][cra.getNumberOfColumns()];
		for (int r = 0; r < cra.getMatrix().length; r++) {
			for (int c = 0; c < cra.getMatrix()[r].length; c++) {
				if (ua.getValue(r, c) >= ua.getThreshold()) {
					for (int other = 0; other < cra.getMatrix()[r].length; other++) {
						if (cra.getValue(other, r) >= cra.getThreshold()
								&& cra.getValue(other, c) >= cra.getThreshold()) {
							cpa[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
						}
						if (cra.getValue(r, other) >= cra.getThreshold()
								&& cra.getValue(c, other) >= cra.getThreshold()) {
							csa[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
						}
					}
				}
			}
		}
		return new Pair<double[][], double[][]>(cpa, csa);
	}

	public static strictfp <E> CausalAbstraction<E> constructAlphaPlusPlusCausalAbstraction(
			DirectlyFollowsAbstraction<E> dfa, LengthTwoLoopAbstraction<E> ltla, double threshold) {
		return new CausalRelationAbstractionImpl<>(dfa.getEventClasses(),
				constructAlphaPlusPlusCausalAbstractionMatrix(dfa, ltla), threshold);
	}

	public static strictfp <T> CausalSuccessionAbstraction<T> constructAlphaPlusPlusCausalSuccessionAbstraction(
			CausalAbstraction<T> cra, UnrelatedAbstraction<T> ua) {
		return new CausalSuccessionAbstractionImpl<>(cra.getEventClasses(),
				constructAlphaPlusPlusCausalSuccessionAbstractionMatrix(cra, ua), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <T> double[][] constructAlphaPlusPlusCausalSuccessionAbstractionMatrix(
			CausalAbstraction<T> cra, UnrelatedAbstraction<T> ua) {
		double[][] csa = new double[cra.getNumberOfRows()][cra.getNumberOfColumns()];
		for (int r = 0; r < cra.getMatrix().length; r++) {
			for (int c = 0; c < cra.getMatrix()[r].length; c++) {
				if (ua.getValue(r, c) >= ua.getThreshold()) {
					for (int other = 0; other < cra.getMatrix()[r].length; other++) {
						if (cra.getValue(r, other) >= cra.getThreshold()
								&& cra.getValue(c, other) >= cra.getThreshold()) {
							csa[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
						}
					}
				}
			}
		}
		return csa;
	}

	private static strictfp <E> double[][] constructAlphaSharpCausalMatrix(DirectlyFollowsAbstraction<E> dfa,
			TwoWayLengthTwoLoopAbstraction<E> twltla) {
		double[][] ca = new double[dfa.getEventClasses().length][dfa.getEventClasses().length];
		for (int r = 0; r < ca.length; r++) {
			for (int c = 0; c < ca[r].length; c++) {
				if (r == c) {
					if (dfa.holds(r, c)) {
						ca[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
					}
				} else {
					if (dfa.holds(r, c)) {
						if (!dfa.holds(c, r) || twltla.holds(r, c)) {
							ca[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
						}
					}
				}
			}
		}
		return ca;
	}

	public static strictfp <E> CausalAbstraction<E> constructAlphaSharpCausalAbstraction(
			DirectlyFollowsAbstraction<E> dfa, TwoWayLengthTwoLoopAbstraction<E> twltla) {
		return new CausalRelationAbstractionImpl<E>(dfa.getEventClasses(), constructAlphaSharpCausalMatrix(dfa, twltla),
				DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <E> CausalAbstraction<E> constructAlphaSharpRealCausalAbstraction(
			CausalAbstraction<E> cpaOriginal, MendaciousAbstraction<E> ma) {
		return new CausalRelationAbstractionImpl<>(cpaOriginal.getEventClasses(),
				constructAlphaSharpRealCausalAbstractionMatrix(cpaOriginal, ma), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <E> double[][] constructAlphaSharpRealCausalAbstractionMatrix(
			CausalAbstraction<E> cpaOriginal, MendaciousAbstraction<E> ma) {
		double[][] matrix = new double[cpaOriginal.getEventClasses().length][cpaOriginal.getEventClasses().length];
		for (int r = 0; r < matrix.length; r++) {
			for (int c = 0; c < matrix.length; c++) {
				if (cpaOriginal.holds(r, c) && !ma.holds(r, c)) {
					matrix[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
				}
			}
		}
		return matrix;
	}

	public static <E> CausalAbstraction<E> constructBooleanCausalAbstraction(DirectlyFollowsAbstraction<E> dfa) {
		return new CausalRelationAbstractionImpl<E>(dfa.getEventClasses(), constructBooleanCausalAbstractionMatrix(dfa),
				DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp double[][] constructBooleanCausalAbstractionMatrix(DirectlyFollowsAbstraction<?> dfa) {
		double[][] cra = new double[dfa.getNumberOfRows()][dfa.getNumberOfColumns()];
		for (int r = 0; r < cra.length; r++) {
			for (int c = 0; c < cra[r].length; c++) {
				if (dfa.getValue(r, c) >= dfa.getThreshold()) {
					if (dfa.getValue(c, r) < dfa.getThreshold()) {
						cra[r][c] = DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
			}
		}
		return cra;
	}

	public static strictfp <E> CausalAbstraction<E> constructCausalAbstraction(Collection<Pair<E, E>> relations,
			Map<E, Integer> indices, E[] allEventClasses) {
		double[][] matrix = new double[allEventClasses.length][allEventClasses.length];
		for (Pair<E, E> pair : relations) {
			matrix[indices.get(pair.getFirst())][indices.get(pair.getSecond())] = DEFAULT_THRESHOLD_BOOLEAN;
		}
		return new CausalRelationAbstractionImpl<>(allEventClasses, matrix, DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <E> CausalAbstraction<E> constructCausalAbstraction(E[] classes, double[][] matrix,
			double threshold) {
		return new CausalRelationAbstractionImpl<>(classes, matrix, threshold);
	}
}
