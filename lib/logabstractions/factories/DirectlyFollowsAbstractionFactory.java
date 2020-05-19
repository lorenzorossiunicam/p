package org.processmining.logabstractions.factories;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.implementations.DirectlyFollowsAbstractionImpl;
import org.processmining.logabstractions.util.XEventClassUtils;

public class DirectlyFollowsAbstractionFactory {

	public static final double DEFAULT_THRESHOLD_BOOLEAN = 1.0;

	public static strictfp DirectlyFollowsAbstraction<XEventClass> constructAlphaClassicDirectlyFollowsAbstraction(
			final XLog log, final XEventClasses classes) {
		return constructBooleanDirectlyFollowsAbstraction(log, classes);
	}

	public static strictfp double[][] constructAlphaClassicDirectlyFollowsMatrix(XLog log, XEventClasses classes) {
		return constructBooleanDirectlyFollowsMatrix(log, classes);
	}

	public static strictfp DirectlyFollowsAbstraction<XEventClass> constructAlphaPlusDirectlyFollowsAbstraction(
			final XLog log, final XEventClasses classes) {
		return constructAlphaClassicDirectlyFollowsAbstraction(log, classes);
	}

	public static strictfp double[][] constructAlphaPlusDirectlyFollowsMatrix(XLog log, XEventClasses classes) {
		return constructBooleanDirectlyFollowsMatrix(log, classes);
	}

	public static strictfp DirectlyFollowsAbstraction<XEventClass> constructAlphaPlusPlusDirectlyFollowsAbstraction(
			final XLog log, final XEventClasses classes) {
		return constructAlphaClassicDirectlyFollowsAbstraction(log, classes);
	}

	public static strictfp double[][] constructAlphaPlusPlusDirectlyFollowsMatrix(XLog log, XEventClasses classes) {
		return constructBooleanDirectlyFollowsMatrix(log, classes);
	}

	public static strictfp DirectlyFollowsAbstraction<XEventClass> constructBooleanDirectlyFollowsAbstraction(
			final XLog log, final XEventClasses classes) {
		return new DirectlyFollowsAbstractionImpl<>(XEventClassUtils.toArray(classes),
				constructBooleanDirectlyFollowsMatrix(log, classes), DEFAULT_THRESHOLD_BOOLEAN);

	}

	public static strictfp double[][] constructBooleanDirectlyFollowsMatrix(XLog log, XEventClasses classes) {
		double[][] matrix = new double[classes.size()][classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				for (int i = 0; i < trace.size() - 1; i++) {
					XEventClass from = classes.getClassOf(trace.get(i));
					XEventClass to = classes.getClassOf(trace.get(i + 1));
					matrix[from.getIndex()][to.getIndex()] = DEFAULT_THRESHOLD_BOOLEAN;
				}
			}
		}
		return matrix;
	}

	public static strictfp DirectlyFollowsAbstraction<XEventClass> constructBooleanLengthOneLoopFreeDirectlyFollowsAbstraction(
			XLog log, XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], double[][]> pair = constructBooleanLengthOneLoopFreeDirectlyFollowsMatrix(log, classes,
				lola);
		return new DirectlyFollowsAbstractionImpl<>(pair.getFirst(), pair.getSecond(), DEFAULT_THRESHOLD_BOOLEAN);
	}

	/**
	 * construct a directly follows relation based on those event-classes E that
	 * are not in a length-one-loop-relation.
	 * 
	 * @param log
	 * @param classes
	 * @param lola
	 * @return
	 */
	public static strictfp Pair<XEventClass[], double[][]> constructBooleanLengthOneLoopFreeDirectlyFollowsMatrix(
			XLog log, XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], int[]> classesLoLFreePair = XEventClassUtils.stripLengthOneLoops(lola.getEventClasses(),
				lola);
		double[][] matrix = new double[classesLoLFreePair.getFirst().length][classesLoLFreePair.getFirst().length];
		for (XTrace t : log) {
			if (!t.isEmpty()) {
				XEventClass from = null;
				for (int i = 0; i < t.size() - 1; i++) {
					XEventClass newFrom = classes.getClassOf(t.get(i));
					if (lola.holds(newFrom.getIndex()))
						continue;
					else
						from = newFrom;
					XEventClass to = classes.getClassOf(t.get(i + 1));
					if (lola.holds(to.getIndex()))
						continue;
					matrix[classesLoLFreePair.getSecond()[from.getIndex()]][classesLoLFreePair.getSecond()[to
							.getIndex()]] = DEFAULT_THRESHOLD_BOOLEAN;
				}
			}
		}
		return new Pair<XEventClass[], double[][]>(classesLoLFreePair.getFirst(), matrix);
	}

	public static strictfp <E> DirectlyFollowsAbstraction<E> constructDirectlyFollowsAbstraction(E[] classes,
			double[][] matrix, double threshold) {
		return new DirectlyFollowsAbstractionImpl<>(classes, matrix, threshold);
	}
}
