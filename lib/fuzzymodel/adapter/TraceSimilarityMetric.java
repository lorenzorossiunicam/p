package org.processmining.plugins.fuzzymodel.adapter;

import java.util.List;
import java.util.Random;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.AggregateBinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLogEvents;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class TraceSimilarityMetric {
	protected List<XTrace> logTraces;
	protected FMLogEvents logEvents;
	protected int numberofEvents;
	protected int Tedges;
	protected BinaryMetric edgeSignificance;
	protected double LogComplexity;
	protected XLogInfo logsummary = null;
	protected MetricsRepository aRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.TraceMetric#measure(org.
	 * processmining.framework.log.LogReader)
	 */
	public TraceSimilarityMetric(XLogInfo logsummary, MetricsRepository aRepository) {
		this.logsummary = logsummary;
		this.aRepository = aRepository;

		/*
		 * super("Trace similarity",
		 * "Measures two trace by the number of intersect contained event class relations compared to the total relation they have."
		 * , aRepository);
		 */

	}

	protected double getFollowingRelation(XTrace pi1, XTrace pi2, int size) {
		DoubleMatrix2D followRelations, followRelations1, followRelations2;
		int AteIndex, followerAteIndex;
		if (size < 512) {
			followRelations = DoubleFactory2D.dense.make(size, size, 0.0);
			followRelations1 = DoubleFactory2D.dense.make(size, size, 0.0);
			followRelations2 = DoubleFactory2D.dense.make(size, size, 0.0);
		} else {
			followRelations = DoubleFactory2D.sparse.make(size, size, 0.0);
			followRelations1 = DoubleFactory2D.sparse.make(size, size, 0.0);
			followRelations2 = DoubleFactory2D.sparse.make(size, size, 0.0);
		}
		for (int k = 0; k < pi1.size(); k++) {
			XEvent Ate = pi1.get(k);
			AteIndex = logEvents.findLogEventNumber(Ate);
			for (int n = k + 1; n < pi1.size(); n++) {
				XEvent followerAte = pi1.get(n);
				followerAteIndex = logEvents.findLogEventNumber(followerAte);
				followRelations.set(AteIndex, followerAteIndex, 1.0);
				followRelations1.set(AteIndex, followerAteIndex, 1.0);
			}
		}
		for (int k = 0; k < pi2.size(); k++) {
			XEvent Ate = pi2.get(k);
			AteIndex = logEvents.findLogEventNumber(Ate);
			for (int n = k + 1; n < pi2.size(); n++) {
				XEvent followerAte = pi2.get(n);
				followerAteIndex = logEvents.findLogEventNumber(followerAte);
				followRelations.set(AteIndex, followerAteIndex, 1.0);
				followRelations2.set(AteIndex, followerAteIndex, 1.0);
			}
		}
		int followRelationSizeM = 0;
		int followRelationSizeInt = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (followRelations.get(i, j) > 0.0) {
					followRelationSizeM++;
				}
				if (followRelations1.get(i, j) > 0.0 && followRelations2.get(i, j) > 0.0) {
					followRelationSizeInt++;
				}
			}
		}
		double ratio;
		if (followRelationSizeM == 0) {
			ratio = 0;
		} else {
			ratio = followRelationSizeInt / (followRelationSizeM * 1.0);
		}
		return ratio;
	}

	protected double getEventIntersectionRatio(XTrace pi1, XTrace pi2, int size) {
		int totalEvents = 0;
		int Ate1Index, Ate2Index;
		int intersectNum = 0;

		boolean pi1T = false;
		boolean pi2T = false;

		for (int k = 0; k < size; k++) {
			for (XEvent Ate1 : pi1) {
				Ate1Index = logEvents.findLogEventNumber(Ate1);
				if (k == Ate1Index) {
					pi1T = true;
					break;
				}
			}
			for (XEvent Ate2 : pi2) {
				Ate2Index = logEvents.findLogEventNumber(Ate2);
				if (k == Ate2Index) {
					pi2T = true;
					break;
				}
			}
			if (pi1T && pi2T) {
				intersectNum++;
			}
			if ((pi1T == false && pi2T == false) == false) {
				totalEvents++;
			}
			pi1T = false;
			pi2T = false;
		}
		return intersectNum / (totalEvents * 1.0);
	}

	public double measure() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub        
		//---determine the log complexity
		AggregateBinaryMetric edgeLogSignificance = aRepository.getAggregateSignificanceBinaryLogMetric();

		double tracesimilarity = 0.0;
		double eventsSimilarity = 0.0;
		int traceRelativeNumber = 0;

		numberofEvents = logsummary.getEventClasses().size();
		XLog log = logsummary.getLog();
		logTraces = log; //FuzzyMinerLog.getTraces(log);
		logEvents = FuzzyMinerLog.getLogEvents(log);
		int numberofTr = logsummary.getNumberOfTraces();

		Tedges = 0;
		for (int x = 0; x < numberofEvents; x++) {
			for (int y = 0; y < numberofEvents; y++) {
				if (edgeLogSignificance.getMeasure(x, y) > 0) {
					Tedges++;
				}
			}
		}
		//---determine the log complexity
		Random r = new Random();
		int p = 0;
		int numberOfEs = logsummary.getNumberOfEvents();
		int loopsize;
		//long logsize=numberOfEs*numberofTr;
		int AvgEvents = numberOfEs / numberofTr;
		//logsize=logsummary.getNumberOfEvents();

		if (AvgEvents > 20) {
			loopsize = 1;
		} else {
			loopsize = 20;
		}
		while (p < loopsize) {
			XTrace pi1, pi2;
			int k = r.nextInt(numberofTr);
			int n = r.nextInt(numberofTr);
			pi1 = logTraces.get(k);
			pi2 = logTraces.get(n);
			if (k != n) {
				tracesimilarity = tracesimilarity + getFollowingRelation(pi1, pi2, numberofEvents);
				eventsSimilarity = eventsSimilarity + getEventIntersectionRatio(pi1, pi2, numberofEvents);
				traceRelativeNumber++;
				p++;
			}
		}

		double traceS = tracesimilarity / (traceRelativeNumber * 1.0);
		double traceE = eventsSimilarity / (traceRelativeNumber * 1.0);
		double AggregetedTS = 0.2 * traceE + 0.8 * traceS;
		double u = Math.pow(2, 1 - AggregetedTS);
		LogComplexity = Math.pow(numberofEvents, u);
		if (LogComplexity == 0) {
			LogComplexity = Math.pow(numberofEvents, (Tedges * 1.0) / (numberofEvents ^ 2));
		}
		return LogComplexity;
	}

	public double getLogComplexity() {
		return LogComplexity;
	}
}
