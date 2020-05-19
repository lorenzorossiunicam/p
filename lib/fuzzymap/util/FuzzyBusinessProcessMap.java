package org.processmining.plugins.fuzzymap.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.TransformerUtils;

public class FuzzyBusinessProcessMap extends MutableFuzzyGraph {

	protected Map<String, Set<Set<String>>> selectedPatternNameAlphabetMap;// = new HashMap<String, Set<String>>();
	protected Map<String, Set<String>> selectedPatternNameSequenceMap;// = new HashMap<String, Set<String>>();
	protected Map<String, String> charActivityMap;// = new HashMap<String,String>();
	protected ArrayList<FuzzyMapPatternNode> patternNodes = new ArrayList<FuzzyMapPatternNode>();
	//protected LogRelations originLogRelations; // log relations
	protected XLog originLog;

	public FuzzyBusinessProcessMap(UnaryMetric nodeSignificance, BinaryMetric edgeSignificance,
			BinaryMetric edgeCorrelation, XLog log, Map<String, Set<Set<String>>> selectedPatternNameAlphabetMap1,
			Map<String, Set<String>> selectedPatternNameSequenceMap1, Map<String, String> charActivityMap1,
			XLog originLog) {
		super(nodeSignificance, edgeSignificance, edgeCorrelation, log, true);
		selectedPatternNameAlphabetMap = new HashMap<String, Set<Set<String>>>();
		selectedPatternNameSequenceMap = new HashMap<String, Set<String>>();
		charActivityMap = new HashMap<String, String>();
		selectedPatternNameAlphabetMap.putAll(selectedPatternNameAlphabetMap1);
		selectedPatternNameSequenceMap.putAll(selectedPatternNameSequenceMap1);
		charActivityMap.putAll(charActivityMap1);
		this.originLog = originLog;
		initializeGraph();
	}

	public FuzzyBusinessProcessMap(MetricsRepository metrics,
			Map<String, Set<Set<String>>> selectedPatternNameAlphabetMap,
			Map<String, Set<String>> selectedPatternNameSequenceMap, Map<String, String> charActivityMap, XLog originLog) {
		this(metrics.getAggregateUnaryMetric(), metrics.getAggregateSignificanceBinaryMetric(), metrics
				.getAggregateCorrelationBinaryMetric(), metrics.getLogReader(), selectedPatternNameAlphabetMap,
				selectedPatternNameSequenceMap, charActivityMap, originLog);
	}

	public FuzzyBusinessProcessMap(UnaryMetric nodeSignificance, BinaryMetric edgeSignificance,
			BinaryMetric edgeCorrelation, XLog log) {
		super(nodeSignificance, edgeSignificance, edgeCorrelation, log, true);
	}

	public void initializeGraph() {
		//delete the elements of graph first in order to redraw the grap h
		for (FMEdgeImpl edge : fmEdges) {
			graphElementRemoved(edge);
		}
		for (FMClusterNode node : clusterNodes) {
			graphElementRemoved(node);
		}

		for (FMNode node : primitiveNodes) {
			if (node != null) {
				graphElementRemoved(node);
			}
		}

		//initialize the graph

		fmEdges = new HashSet<FMEdgeImpl>();
		actBinarySignificance = new double[numberOfInitialNodes][numberOfInitialNodes];
		actBinaryCorrelation = new double[numberOfInitialNodes][numberOfInitialNodes];

		/*
		 * if (edgeSignificance instanceof AggregateBinaryMetric) {
		 * System.out.print
		 * ("The edgeSignificance is an AggregateBinaryMetric."); } else {
		 * System.out.print("Not a AggregateBinaryMetric."); }
		 */

		for (int x = 0; x < numberOfInitialNodes; x++) {
			for (int y = 0; y < numberOfInitialNodes; y++) {
				actBinarySignificance[x][y] = edgeSignificance.getMeasure(x, y);
				actBinaryCorrelation[x][y] = edgeCorrelation.getMeasure(x, y);
			}
		}
		patternNodes.clear();

		for (int i = 0; i < numberOfInitialNodes; i++) {
			//create new FMNode
			addNode(i);
		}
	}

	public synchronized void addNode(int index) {
		String eventName;
		FMNode node = new FMNode(this, index, "");
		eventName = node.getElementName();
		String nodeLabel = TransformerUtils.getNodeLabel(node);
		if (selectedPatternNameAlphabetMap.containsKey(eventName)) {
			//create a pattern node
			FuzzyMapPatternNode patternNode = new FuzzyMapPatternNode(this, index, nodeLabel);
			patternNodes.add(patternNode);
			primitiveNodes[index] = patternNode;
			nodeAliasMap[index] = patternNode;
			graphElementAdded(patternNode);
		} else { //create a  normal node

			node.setLabel(nodeLabel);
			primitiveNodes[index] = node;
			nodeAliasMap[index] = node;
			graphElementAdded(node);
		}
	}
}
