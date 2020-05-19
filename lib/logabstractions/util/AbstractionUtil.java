package org.processmining.logabstractions.util;

import java.util.Collection;

import org.processmining.logabstractions.models.MatrixAbstraction;

public class AbstractionUtil {

	/**
	 * Checks, given some abstraction whether the relation holds amongst the two
	 * given sets.
	 * 
	 * @param abs
	 *            abstraction
	 * @param first
	 *            set of elems
	 * @param second
	 *            set of elems
	 * @param geq
	 *            check for geq or less than threshold
	 * @return
	 */
	public static <T1 extends MatrixAbstraction<T2>, T2> boolean checkAbstractionHolds(T1 abs, Collection<T2> first,
			Collection<T2> second, boolean geq) {
		boolean result = true;
		loop: for (T2 f : first) {
			for (T2 s : second) {
				result &= geq ? abs.getValue(f, s) >= abs.getThreshold() : abs.getValue(f, s) < abs.getThreshold();
				if (!result)
					break loop;
			}
		}
		return result;
	}

	public static <T1 extends MatrixAbstraction<T2>, T2> boolean checkAbstractionsHold(T1[] abs, Collection<T2> first,
			Collection<T2> second, boolean[] geq) {
		boolean result = true;
		if (abs.length != geq.length) {
			result = false;
		} else {
			loop: for (T2 f : first) {
				for (T2 s : second) {
					for (int i = 0; i < abs.length; i++) {
						result &= geq[i] ? abs[i].getValue(f, s) >= abs[i].getThreshold()
								: abs[i].getValue(f, s) < abs[i].getThreshold();
						if (!result)
							break loop;
					}
				}
			}
		}
		return result;
	}

}
