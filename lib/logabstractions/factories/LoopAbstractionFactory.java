package org.processmining.logabstractions.factories;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.LengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.TwoWayLengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.implementations.LengthOneLoopAbstractionImpl;
import org.processmining.logabstractions.models.implementations.LengthTwoLoopAbstractionImpl;
import org.processmining.logabstractions.models.implementations.TwoWayLengthTwoLoopAbstractionImpl;
import org.processmining.logabstractions.util.XEventClassUtils;

public class LoopAbstractionFactory {

	public static final double DEFAULT_THRESHOLD_BOOLEAN = 1.0;

	public static strictfp <E> LengthOneLoopAbstraction<E> constructBooleanLengthOneLoopAbstraction(
			DirectlyFollowsAbstraction<E> dfa) {
		return new LengthOneLoopAbstractionImpl<>(dfa.getEventClasses(), constructBooleanLengthOneLoopColumn(dfa),
				DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <E> double[] constructBooleanLengthOneLoopColumn(DirectlyFollowsAbstraction<E> dfa) {
		double[] column = new double[dfa.getEventClasses().length];
		for (int i = 0; i < column.length; i++) {
			if (dfa.getValue(i, i) >= dfa.getThreshold()) {
				column[i] = DEFAULT_THRESHOLD_BOOLEAN;
			}
		}
		return column;
	}

	public static strictfp LengthTwoLoopAbstraction<XEventClass> constructBooleanLengthTwoLoopAbstraction(XLog log,
			XEventClasses classes) {
		return new LengthTwoLoopAbstractionImpl<>(XEventClassUtils.toArray(classes),
				constructBooleanLengthTwoLoopMatrix(log, classes), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp double[][] constructBooleanLengthTwoLoopMatrix(XLog log, XEventClasses classes) {
		double[][] l2l = new double[classes.size()][classes.size()];
		for (XTrace trace : log) {
			if (trace.size() > 2) {
				for (int i = 0; i < trace.size() - 2; i++) {
					XEventClass from = classes.getClassOf(trace.get(i));
					XEventClass to = classes.getClassOf(trace.get(i + 1));
					if (classes.getClassOf(trace.get(i + 2)).equals(from)) {
						l2l[from.getIndex()][to.getIndex()] = 1.0;
					}
				}
			}
		}
		return l2l;
	}

	public static strictfp <E> double[][] constructBooleanTwoWayLengthTwoLoopMatrix(LengthTwoLoopAbstraction<E> l2la) {
		double[][] twl2l = new double[l2la.getNumberOfRows()][l2la.getNumberOfColumns()];
		for (int r = 0; r < twl2l.length; r++) {
			for (int c = 0; c < twl2l[r].length; c++) {
				if (twl2l[r][c] >= l2la.getThreshold()) {
					twl2l[c][r] = DEFAULT_THRESHOLD_BOOLEAN;
				} else {
					if (l2la.getValue(r, c) >= l2la.getThreshold()) {
						if (l2la.getValue(c, r) >= l2la.getThreshold()) {
							twl2l[c][r] = DEFAULT_THRESHOLD_BOOLEAN;
						}
					}
				}
			}
		}
		return twl2l;
	}

	public static strictfp Pair<XEventClass[], double[][]> constructBooleanLengthOneLoopFreeLengthTwoLoopMatrix(
			XLog log, XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], int[]> lolaClasses = XEventClassUtils.stripLengthOneLoops(lola.getEventClasses(), lola);
		double[][] matrix = new double[lolaClasses.getFirst().length][lolaClasses.getFirst().length];
		for (XTrace t : log) {
			if (t.size() > 2) {
				XEventClass first, second, third;
				first = second = third = null;
				for (int i = 0; i < t.size() - 2; i++) {
					XEventClass current = classes.getClassOf(t.get(i));
					if (lola.holds(current))
						continue;
					if (first == null) {
						first = current;
						continue;
					}
					if (second == null) {
						second = current;
						continue;
					}
					if (third == null) {
						third = current;
					} else {
						first = second;
						second = third;
						third = current;
					}
					if (first.equals(third))
						matrix[lolaClasses.getSecond()[first.getIndex()]][lolaClasses.getSecond()[second
								.getIndex()]] = DEFAULT_THRESHOLD_BOOLEAN;
				}
			}
		}
		return new Pair<XEventClass[], double[][]>(lolaClasses.getFirst(), matrix);
	}

	public static strictfp LengthTwoLoopAbstraction<XEventClass> constructBooleanLengthOneLoopFreeLengthTwoLoopAbstraction(
			XLog log, XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], double[][]> matrix = constructBooleanLengthOneLoopFreeLengthTwoLoopMatrix(log, classes,
				lola);
		return new LengthTwoLoopAbstractionImpl<>(matrix.getFirst(), matrix.getSecond(), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <L extends LengthTwoLoopAbstraction<E>, E> TwoWayLengthTwoLoopAbstraction<E> constructBooleanTwoWayLengthTwoLoopAbstraction(
			L l2la) {
		return new TwoWayLengthTwoLoopAbstractionImpl<>(l2la.getEventClasses(),
				constructBooleanTwoWayLengthTwoLoopMatrix(l2la), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp <E> LengthTwoLoopAbstraction<E> constructLengthTwoLoopAbstraction(E[] eventClasses,
			double[][] matrix, double threshold) {
		return new LengthTwoLoopAbstractionImpl<>(eventClasses, matrix, threshold);
	}

	public static strictfp <E> LengthOneLoopAbstraction<E> constructLengthOneLoopAbstraction(E[] eventClasses,
			double[] column, double threshold) {
		return new LengthOneLoopAbstractionImpl<>(eventClasses, column, threshold);
	}

}
