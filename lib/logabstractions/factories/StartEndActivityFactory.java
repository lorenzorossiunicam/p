package org.processmining.logabstractions.factories;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.logabstractions.models.implementations.EndActivityAbstractionImpl;
import org.processmining.logabstractions.models.implementations.StartActivityAbstractionImpl;
import org.processmining.logabstractions.util.XEventClassUtils;

public class StartEndActivityFactory {

	public static final double DEFAULT_THRESHOLD_ABSOLUTE = 1.0d;
	public static final double DEFAULT_THRESHOLD_BOOLEAN = 1.0d;

	public static strictfp double[] constructAbsoluteEndActivityColumn(XLog log, XEventClasses classes) {
		double[] ends = new double[classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				ends[classes.getClassOf(trace.get(trace.size() - 1)).getIndex()] += DEFAULT_THRESHOLD_ABSOLUTE;
			}
		}
		return ends;
	}

	public static strictfp double[] constructAbsoluteStartActivityColumn(XLog log, XEventClasses classes) {
		double[] starts = new double[classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0)).getIndex()] += DEFAULT_THRESHOLD_ABSOLUTE;
			}
		}
		return starts;
	}

	public static Pair<double[], double[]> constructAbsoluteStartEndActivityColumn(XLog log, XEventClasses classes) {
		double[] starts = new double[classes.size()];
		double[] ends = new double[classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0)).getIndex()] += DEFAULT_THRESHOLD_ABSOLUTE;
				ends[classes.getClassOf(trace.get(trace.size() - 1)).getIndex()] += DEFAULT_THRESHOLD_ABSOLUTE;
			}
		}
		return new Pair<double[], double[]>(starts, ends);
	}

	public static strictfp double[] constructBooleanEndActivityColumn(XLog log, XEventClasses classes) {
		double[] ends = new double[classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				ends[classes.getClassOf(trace.get(trace.size() - 1)).getIndex()] = DEFAULT_THRESHOLD_BOOLEAN;
			}
		}
		return ends;
	}

	public static strictfp EndActivityAbstraction<XEventClass> constructBooleanLengthOneLoopFreeEndActivityAbstraction(
			XLog log, XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], double[]> abstr = constructBooleanLengthOneLoopFreeEndActivityColumn(log, classes, lola);
		return new EndActivityAbstractionImpl<>(abstr.getFirst(), abstr.getSecond(), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp Pair<XEventClass[], double[]> constructBooleanLengthOneLoopFreeEndActivityColumn(XLog log,
			XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], int[]> classesLoLFreePair = XEventClassUtils.stripLengthOneLoops(lola.getEventClasses(),
				lola);
		double[] column = new double[classesLoLFreePair.getFirst().length];
		for (XTrace t : log) {
			if (!t.isEmpty()) {
				for (int i = t.size() - 1; i >= 0; i--) {
					XEventClass e = classes.getClassOf(t.get(i));
					if (lola.holds(e.getIndex()))
						continue;
					else {
						column[classesLoLFreePair.getSecond()[e.getIndex()]] = DEFAULT_THRESHOLD_BOOLEAN;
						break;
					}
				}
			}
		}
		return new Pair<XEventClass[], double[]>(classesLoLFreePair.getFirst(), column);
	}

	public static strictfp StartActivityAbstraction<XEventClass> constructBooleanLengthOneLoopFreeStartActivityAbstraction(
			XLog log, XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], double[]> abstr = constructBooleanLengthOneLoopFreeStartActivityColumn(log, classes, lola);
		return new StartActivityAbstractionImpl<>(abstr.getFirst(), abstr.getSecond(), DEFAULT_THRESHOLD_BOOLEAN);
	}

	public static strictfp Pair<XEventClass[], double[]> constructBooleanLengthOneLoopFreeStartActivityColumn(XLog log,
			XEventClasses classes, LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], int[]> classesLoLFreePair = XEventClassUtils.stripLengthOneLoops(lola.getEventClasses(),
				lola);
		double[] column = new double[classesLoLFreePair.getFirst().length];
		for (XTrace t : log) {
			if (!t.isEmpty()) {
				for (int i = 0; i < t.size(); i++) {
					XEventClass e = classes.getClassOf(t.get(i));
					if (lola.holds(e.getIndex()))
						continue;
					else {
						column[classesLoLFreePair.getSecond()[e.getIndex()]] = DEFAULT_THRESHOLD_BOOLEAN;
						break;
					}
				}
			}
		}
		return new Pair<XEventClass[], double[]>(classesLoLFreePair.getFirst(), column);
	}

	public static strictfp double[] constructBooleanStartActivityColumn(XLog log, XEventClasses classes) {
		double[] starts = new double[classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0)).getIndex()] = DEFAULT_THRESHOLD_BOOLEAN;
			}
		}
		return starts;
	}

	public static Pair<StartActivityAbstraction<XEventClass>, EndActivityAbstraction<XEventClass>> constructBooleanStartEndActivityAbstraction(
			XLog log, XEventClasses classes) {
		Pair<double[], double[]> startEnds = constructBooleanStartEndActivityColumn(log, classes);
		XEventClass[] eventClasses = XEventClassUtils.toArray(classes);
		StartActivityAbstraction<XEventClass> start = new StartActivityAbstractionImpl<>(eventClasses,
				startEnds.getFirst(), DEFAULT_THRESHOLD_BOOLEAN);
		EndActivityAbstraction<XEventClass> end = new EndActivityAbstractionImpl<>(eventClasses, startEnds.getSecond(),
				DEFAULT_THRESHOLD_BOOLEAN);
		return new Pair<StartActivityAbstraction<XEventClass>, EndActivityAbstraction<XEventClass>>(start, end);
	}

	public static Pair<double[], double[]> constructBooleanStartEndActivityColumn(XLog log, XEventClasses classes) {
		double[] starts = new double[classes.size()];
		double[] ends = new double[classes.size()];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0)).getIndex()] = DEFAULT_THRESHOLD_BOOLEAN;
				ends[classes.getClassOf(trace.get(trace.size() - 1)).getIndex()] = DEFAULT_THRESHOLD_BOOLEAN;
			}
		}
		return new Pair<double[], double[]>(starts, ends);
	}

	public static strictfp <E> EndActivityAbstraction<E> constructEndActivityAbstraction(E[] eventClasses,
			double[] column, double threshold) {
		return new EndActivityAbstractionImpl<>(eventClasses, column, threshold);
	}

	public static strictfp <E> StartActivityAbstraction<E> constructStartActivityAbstraction(E[] eventClasses,
			double[] column, double threshold) {
		return new StartActivityAbstractionImpl<>(eventClasses, column, threshold);
	}

}
