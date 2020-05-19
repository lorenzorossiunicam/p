package org.processmining.logabstractions.factories;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;
import org.processmining.logabstractions.models.implementations.LongTermFollowsAbstractionImpl;
import org.processmining.logabstractions.util.XEventClassUtils;

public class LongTermFollowsAbstractionFactory {

	public static final double BOOLEAN_DEFAULT_THRESHOLD = 1.0;

	/**
	 * Taken from: "Mining Process Models with Non-Free-Choice Constructs";
	 * lijie wen et al. a >> b, if not(a > b) and a precedes b and for all k
	 * inbetween a and b: not(k <| a or k |> a)
	 * 
	 * 
	 * @param log
	 * @param classes
	 * @param dfa
	 * @param cpa
	 * @param csa
	 * @return
	 */
	public static strictfp double[][] constructAlphaPlusPlusLongTermFollowsMatrix(XLog log, XEventClasses classes,
			DirectlyFollowsAbstraction<XEventClass> dfa, CausalPrecedenceAbstraction<XEventClass> cpa,
			CausalSuccessionAbstraction<XEventClass> csa) {
		double[][] matrix = new double[classes.size()][classes.size()];
		for (XTrace t : log) {
			for (int i = 0; i < t.size() - 1; i++) {
				XEventClass a = classes.getClassOf(t.get(i));
				for (int j = i + 1; j < t.size(); j++) {
					XEventClass b = classes.getClassOf(t.get(j));
					if (!dfa.holds(a.getIndex(), b.getIndex())) {
						boolean ltf = true;
						for (int k = i + 1; k <= j - 1; k++) {
							XEventClass c = classes.getClassOf(t.get(k));
							if (c.equals(a) || c.equals(b)
									|| cpa.getValue(c.getIndex(), a.getIndex()) >= cpa.getThreshold()
									|| csa.getValue(c.getIndex(), a.getIndex()) >= csa.getThreshold()) {
								ltf = false;
								break;
							}
						}
						if (ltf) {
							matrix[a.getIndex()][b.getIndex()] = BOOLEAN_DEFAULT_THRESHOLD;
						}
					}
				}
			}
		}
		return matrix;
	}

	/**
	 * assumes: dfa, cpa and csa are length one loop free.
	 * 
	 * @param log
	 * @param classes
	 * @param dfa
	 * @param cpa
	 * @param csa
	 * @param lola
	 * @return
	 */
	public static strictfp double[][] constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsMatrix(XLog log,
			XEventClasses classes, DirectlyFollowsAbstraction<XEventClass> dfa,
			CausalPrecedenceAbstraction<XEventClass> cpa, CausalSuccessionAbstraction<XEventClass> csa,
			LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], int[]> l1lClasses = XEventClassUtils.stripLengthOneLoops(lola.getEventClasses(), lola);
		double[][] matrix = new double[l1lClasses.getFirst().length][l1lClasses.getFirst().length];
		if (!(dfa.getEventClasses().length == cpa.getEventClasses().length
				&& cpa.getEventClasses().length == csa.getEventClasses().length
				&& csa.getEventClasses().length == l1lClasses.getFirst().length)) {
			return matrix;
		} else {

			for (XTrace t : log) {
				for (int i = 0; i < t.size() - 1; i++) {
					XEventClass a = classes.getClassOf(t.get(i));
					if (!lola.holds(a.getIndex())) {
						for (int j = i + 1; j < t.size(); j++) {
							XEventClass b = classes.getClassOf(t.get(j));
							if (!lola.holds(b.getIndex())) {
								if (!dfa.holds(l1lClasses.getSecond()[a.getIndex()],
										l1lClasses.getSecond()[b.getIndex()])) {
									boolean ltf = true;
									for (int k = i + 1; k <= j - 1; k++) {
										XEventClass c = classes.getClassOf(t.get(k));
										if (!lola.holds(c.getIndex())) {
											if (c.equals(a) || c.equals(b)
													|| cpa.getValue(l1lClasses.getSecond()[c.getIndex()],
															l1lClasses.getSecond()[a.getIndex()]) >= cpa.getThreshold()
													|| csa.getValue(l1lClasses.getSecond()[c.getIndex()],
															l1lClasses.getSecond()[a.getIndex()]) >= csa
																	.getThreshold()) {
												ltf = false;
												break;
											}
										}
									}
									if (ltf) {
										matrix[l1lClasses.getSecond()[a.getIndex()]][l1lClasses.getSecond()[b
												.getIndex()]] = BOOLEAN_DEFAULT_THRESHOLD;
									}
								}
							}
						}
					}
				}
			}
			return matrix;
		}
	}
	
	/**
	 * assumes: dfa, cpa and csa are length one loop free.
	 * 
	 * @param log
	 * @param classes
	 * @param dfa
	 * @param cpa
	 * @param csa
	 * @param lola
	 * @return
	 */
	public static strictfp LongTermFollowsAbstraction<XEventClass> constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsAbstraction(
			XLog log, XEventClasses classes, DirectlyFollowsAbstraction<XEventClass> dfa,
			CausalPrecedenceAbstraction<XEventClass> cpa, CausalSuccessionAbstraction<XEventClass> csa,
			LengthOneLoopAbstraction<XEventClass> lola) {
		return new LongTermFollowsAbstractionImpl<>(dfa.getEventClasses(),
				constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsMatrix(log, classes, dfa, cpa, csa, lola),
				BOOLEAN_DEFAULT_THRESHOLD);
	}

}
