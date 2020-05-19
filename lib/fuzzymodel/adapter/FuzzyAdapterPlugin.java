package org.processmining.plugins.fuzzymodel.adapter;

import java.util.Arrays;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.connections.fuzzymodel.FuzzyModelConnection;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.AggregateBinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.BestEdgeTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FastTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FuzzyEdgeTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;

@Plugin(name = "Select Best Fuzzy Instance", level = PluginLevel.PeerReviewed, parameterLabels = { "Fuzzy Model" }, returnLabels = { "Fuzzy Instance" }, returnTypes = { MutableFuzzyGraph.class })
public class FuzzyAdapterPlugin {

	@UITopiaVariant(uiLabel = "Select Best Fuzzy Instance", affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "Fuzzy")
	@PluginVariant(variantLabel = "Generic Select Best Fuzzy Instance", requiredParameterLabels = { 0 })
	public MutableFuzzyGraph mineGeneric(final PluginContext context, MetricsRepository repository) {
		long time = System.currentTimeMillis();
		MutableFuzzyGraph mfg = adapt(context, repository);
		context.addConnection(new FuzzyModelConnection(mfg));
		time = System.currentTimeMillis() - time;
		String logStr = new String("Select Best Fuzzy Instance: Took " + time + " ms.");
		context.log(logStr, MessageLevel.NORMAL);
		return mfg;
	}

	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String EDGE_TRANSFORMER_SELECTION = "EdgeTransformerSelection";
	private static final String EDGE_TRANSFORMER_SELECTION_BEST_EDGES = "EdgeTransformerSelectionBestEdges";
	private static final String EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES = "EdgeTransformerSelectionFuzzyEdges";
	private static final String CONCURRENCY_EDGE_TRANSFORMER_ACTIVE = "ConcurrencyEdgeTransformerActive";
	private static final String NODE_CUTOFF = "NodeCutoff";
	private static final String FUZZY_EDGE_RATIO = "FuzzyEdgeRatio";
	private static final String FUZZY_EDGE_CUTOFF = "FuzzyEdgeCutoff";
	private static final String CONCURRENCY_THRESHOLD = "ConcurrencyThreshold";
	private static final String CONCURRENCY_RATIO = "ConcurrencyRatio";
	private static final String EDGES_FUZZY_IGNORE_LOOPS = "EdgesFuzzyIgnoreLoops";
	private static final String EDGES_FUZZY_INTERPRET_ABSOLUTE = "EdgesFuzzyInterpretAbsolute";

	private MutableFuzzyGraph adapt(PluginContext context, MetricsRepository repository) {
		MutableFuzzyGraph mfg = new MutableFuzzyGraph(repository);
		XLog log = repository.getLogReader();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);

		TraceSimilarityMetric tsm = new TraceSimilarityMetric(logInfo, repository);
		double logComplexity = (logInfo.getNumberOfEvents() > 50000 ? 1600 : tsm.measure());

		mfg.initializeGraph();
		mfg.setBinaryRespectiveSignificance();
		AggregateBinaryMetric edgeLogSignificance = repository.getAggregateSignificanceBinaryLogMetric();
		int numberofEventClasses = logInfo.getEventClasses().size();
		int numTr = FuzzyMinerLog.getTraces(log).size();
		int numberofEC = logInfo.getEventClasses().size();
		int numberofEvents = logInfo.getNumberOfEvents();
		double avgEvents = numberofEvents / (numTr * 1.0);
		double weight = avgEvents / numberofEC;
		double timeCom = numberofEventClasses * numTr * (weight > 1.0 ? Math.pow(1.5, weight) : 1.0);

		double originalEdgesDetail = 0.0;
		int tEdges = 0;
		for (int x = 0; x < numberofEventClasses; x++) {
			for (int y = 0; y < numberofEventClasses; y++) {
				if (edgeLogSignificance.getMeasure(x, y) > 0) {
					tEdges++;
				}
				originalEdgesDetail += mfg.getBinarySignificance(x, y);
			}
		}

		int nofNodes = mfg.getNumberOfInitialNodes();
		int total = nofNodes * nofNodes;
		int count = 0;
		double[] preserveall = new double[total];
		double[] ratioall = new double[total];
		double sumOfPreserve = 0;
		double sumOfRatio = 0;
		for (int x = 0; x < nofNodes; x++) {
			for (int y = 0; y < x; y++) {
				double relImpAB = mfg.getBinaryRespectiveSignificance(x, y);
				double relImpBA = mfg.getBinaryRespectiveSignificance(y, x);

				if (relImpAB > 0.0 && relImpBA > 0.0) {
					// conflict situation
					if (relImpAB > relImpBA) {
						preserveall[count] = relImpBA;
						ratioall[count] = Math.min(relImpAB, relImpBA) / Math.max(relImpAB, relImpBA);
						sumOfPreserve = sumOfPreserve + relImpBA;
						sumOfRatio = sumOfRatio + Math.min(relImpAB, relImpBA) / Math.max(relImpAB, relImpBA);
					} else {
						preserveall[count] = relImpAB;
						ratioall[count] = Math.min(relImpAB, relImpBA) / Math.max(relImpAB, relImpBA);
						sumOfPreserve = sumOfPreserve + relImpAB;
						sumOfRatio = sumOfRatio + Math.min(relImpAB, relImpBA) / Math.max(relImpAB, relImpBA);
					}
					count++;

				}
			}
		}
		Arrays.sort(preserveall);
		Arrays.sort(ratioall);
		double avgOfPreserve;
		double avgOfRatio;
		if (count > 0) {
			avgOfPreserve = sumOfPreserve / (count * 1.0);
			avgOfRatio = sumOfRatio / (count * 1.0);
		} else {
			avgOfPreserve = 0.0;
			avgOfRatio = 0.0;
			preserveall = null;
			ratioall = null;
		}
		int n = mfg.getNumberOfInitialNodes();
		double[] nodesigall = new double[n];
		UnaryMetric nodeSignificance = repository.getAggregateUnaryLogMetric();
		for (int i = 0; i < n; i++) {
			int j = 0;
			for (j = 0; j < nodesigall.length; j++) {
				if (nodesigall[j] == nodeSignificance.getMeasure(i)) {
					break;
				}
			}
			if (j >= nodesigall.length) {
				nodesigall[i] = nodeSignificance.getMeasure(i);
			}
		}
		Arrays.sort(nodesigall);

		FastTransformer fastTransformer = new FastTransformer(context);
		BestEdgeTransformer bestEdgeTransformer = new BestEdgeTransformer(context);
		FuzzyEdgeTransformer fuzzyEdgeTransformer = new FuzzyEdgeTransformer(context);
		NewConcurrencyEdgeTransformer concurrencyEdgeTransformer = new NewConcurrencyEdgeTransformer(context);

		double nodeThreshold;
		double conformance = 0.8;
		if (nofNodes > 0) {
			nodeThreshold = mfg.getThresholdShowingPrimitives(nofNodes) - mfg.getMinimalNodeSignificance();
			nodeThreshold = nodeThreshold / (1.0 - mfg.getMinimalNodeSignificance());
			fastTransformer.setThreshold(nodeThreshold);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(0.75);
			fuzzyEdgeTransformer.setPreservePercentage(0.2);
			fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			concurrencyEdgeTransformer.setPreserveThreshold(0.6);
			concurrencyEdgeTransformer.setRatioThreshold(0.7);
		} else {
			fastTransformer.setThreshold(0.0);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(0.75);
			fuzzyEdgeTransformer.setPreservePercentage(1.0);
			fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			concurrencyEdgeTransformer.setPreserveThreshold(1.0);
			concurrencyEdgeTransformer.setRatioThreshold(0.7);
		}

		String edgeTransformerSelection = mfg.getAttribute(EDGE_TRANSFORMER_SELECTION);
		if (edgeTransformerSelection != null) {
			if (edgeTransformerSelection.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_BEST_EDGES)) {
				fastTransformer.addInterimTransformer(bestEdgeTransformer);
			} else if (edgeTransformerSelection.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES)) {
				fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			}
		}
		String concurrencyTransformerActive = mfg.getAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE);
		if (concurrencyTransformerActive != null) {
			if (concurrencyTransformerActive.equals(TRUE)) {
				fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			} else if (concurrencyTransformerActive.equals(FALSE)) {
				fastTransformer.removePreTransformer(concurrencyEdgeTransformer);
			}
		}
		String nodeCutoff = mfg.getAttribute(NODE_CUTOFF);
		if (nodeCutoff != null) {
			fastTransformer.setThreshold(Double.parseDouble(nodeCutoff));
		}
		String fuzzyEdgeRatio = mfg.getAttribute(FUZZY_EDGE_RATIO);
		if (fuzzyEdgeRatio != null) {
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(Double.parseDouble(fuzzyEdgeRatio));
		}
		String fuzzyEdgeCutoff = mfg.getAttribute(FUZZY_EDGE_CUTOFF);
		if (fuzzyEdgeCutoff != null) {
			fuzzyEdgeTransformer.setPreservePercentage(Double.parseDouble(fuzzyEdgeCutoff));
		}
		String concurrencyThreshold = mfg.getAttribute(CONCURRENCY_THRESHOLD);
		if (concurrencyThreshold != null) {
			concurrencyEdgeTransformer.setPreserveThreshold(Double.parseDouble(concurrencyThreshold));
		}
		String concurrencyRatio = mfg.getAttribute(CONCURRENCY_RATIO);
		if (concurrencyRatio != null) {
			concurrencyEdgeTransformer.setRatioThreshold(Double.parseDouble(concurrencyRatio));
		}
		String ignoreLoops = mfg.getAttribute(EDGES_FUZZY_IGNORE_LOOPS);
		if (ignoreLoops != null) {
			if (ignoreLoops.equals(TRUE)) {
				fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			} else if (ignoreLoops.equals(FALSE)) {
				fuzzyEdgeTransformer.setIgnoreSelfLoops(false);
			}
		}
		String interpretAbsolute = mfg.getAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE);
		if (interpretAbsolute != null) {
			if (interpretAbsolute.equals(TRUE)) {
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(true);
			} else if (interpretAbsolute.equals(FALSE)) {
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			}
		}

		FastFuzzyMinerAdapted fastfuzzymineradapted = new FastFuzzyMinerAdapted(context, mfg, log, conformance,
				repository, logComplexity, count, avgOfPreserve, avgOfRatio, timeCom, tEdges, originalEdgesDetail,
				preserveall, ratioall, 1, logInfo, nodesigall);
		fastfuzzymineradapted.calculation();

		FuzzyOptimalResult fuzzyoptimalresult = fastfuzzymineradapted.getMaxOptimalvalueFuzzyResult(fastfuzzymineradapted
				.getfuzzyoptimalresults());

		mfg.setAttribute(EDGES_FUZZY_IGNORE_LOOPS, TRUE);
		mfg.setAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE, FALSE);
		mfg.setAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE, TRUE);
		mfg.setAttribute(CONCURRENCY_RATIO, Double.toString(fuzzyoptimalresult.ratio));
		mfg.setAttribute(CONCURRENCY_THRESHOLD, Double.toString(fuzzyoptimalresult.preserve));
		mfg.setAttribute(FUZZY_EDGE_CUTOFF, Double.toString(fuzzyoptimalresult.cutoff));
		mfg.setAttribute(FUZZY_EDGE_RATIO, Double.toString(fuzzyoptimalresult.utility));
		mfg.setAttribute(NODE_CUTOFF, Double.toString(fuzzyoptimalresult.nodesig));
		mfg.setAttribute(EDGE_TRANSFORMER_SELECTION, EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES);
		fastTransformer.setThreshold(fuzzyoptimalresult.nodesig);
		fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
		fuzzyEdgeTransformer.setSignificanceCorrelationRatio(fuzzyoptimalresult.utility);
		fuzzyEdgeTransformer.setPreservePercentage(fuzzyoptimalresult.cutoff);
		fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
		fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
		fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
		concurrencyEdgeTransformer.setPreserveThreshold(fuzzyoptimalresult.preserve);
		concurrencyEdgeTransformer.setRatioThreshold(fuzzyoptimalresult.ratio);

		return mfg;
	}
}
