package org.processmining.plugins.fuzzymap.util;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMEdges;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMColors;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.models.shapes.Octagon;
import org.processmining.plugins.log.logabstraction.BasicLogRelations;
import org.processmining.plugins.log.logabstraction.LogRelations;

public class FuzzyMapPatternNode extends FMNode {
	protected HashSet<FMNode> primitives;
	protected FuzzyBusinessProcessMap wholeMap;

	//protected HashSet<String> patternSequences;
	public FuzzyMapPatternNode(FuzzyBusinessProcessMap map, int index, String label) {
		super(map, index, label);
		primitives = new HashSet<FMNode>();
		wholeMap = map;
		getAttributeMap().put(AttributeMap.LABEL, label);
		getAttributeMap().put(AttributeMap.SHAPE, new Octagon(0.2));
		getAttributeMap().put(AttributeMap.RESIZABLE, true);
		getAttributeMap().put(AttributeMap.SIZE, new Dimension(120, 70));
		getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getAbstractBackgroundColor());
		getAttributeMap().put(AttributeMap.SHOWLABEL, true);
	}

	public void add(FMNode node) {
		primitives.add(node);
	}

	public boolean remove(FMNode node) {
		return primitives.remove(node);
	}

	public Set<FMNode> getPrimitives() {
		return primitives;
	}

	public int size() {
		return primitives.size();
	}

	public boolean contains(FMNode node) {
		return primitives.contains(node);
	}

	public String id() {
		return "pattern_" + index;
	}

	public ProMJGraphPanel getPatternInnerGraphPanel(PluginContext context, XLog log) throws Exception {

		ProMJGraphPanel graphPanel;
		//make a new fuzzy business process map according to the old one
		/*
		 * FuzzyBusinessProcessMap patternMap = new
		 * FuzzyBusinessProcessMap(graph.nodeSignificance,
		 * graph.edgeSignificance, graph.edgeCorrelation, log);
		 */
		FuzzyBusinessProcessMap patternMap = new FuzzyBusinessProcessMap(null, null, null, log);

		//remove all the primitiveNodes
		for (int i = 0; i < patternMap.getNumberOfInitialNodes(); i++) {
			FMNode node = patternMap.getPrimitiveNode(i);
			if (node != null) {
				patternMap.setPrimitiveNode(i, null);
				patternMap.setNodeAliasMapping(i, null);
				patternMap.graphElementRemoved(node);
			}
		}

		//get the primitive nodes that compose the pattern node
		primitives.clear();
		String eventName = getElementName();
		int idx = 0;
		Set<Set<String>> patternAlphabet = wholeMap.selectedPatternNameAlphabetMap.get(eventName);
		Set<String> longestLabel = FuzzyMapUtil.findLongestLabel(patternAlphabet);
		for (String primitiveNodeName : longestLabel) {
			String decodedActivityName = wholeMap.charActivityMap.get(primitiveNodeName);
			String innerNodeLabel;
			innerNodeLabel = FuzzyMapUtil.getSingleActivityNodeLabel(decodedActivityName, "-");
			FMNode innerNode = new FMNode(patternMap, idx, innerNodeLabel, true);
			System.out.println("The toString of innerNode is " + innerNode.toString());
			add(innerNode);
			idx++;
		}

		//Set the  primitive Nodes in the cluster to the clusterGraph
		int indexofPrimitives = 0;
		for (FMNode node : primitives) {
			patternMap.setPrimitiveNode(indexofPrimitives, node);
			patternMap.setNodeAliasMapping(indexofPrimitives, node);
			patternMap.graphElementAdded(node);
			indexofPrimitives++;
		}

		// write adjacent predecessor and successor nodes
		Set<FMNode> predecessors = getPredecessors();
		Set<FMNode> successors = getSuccessors();
		// unified set, to prevent duplicate nodes (both predecessor and successor)

		Set<FMNode> newPredecessors = makeNewNodesInPatternMap(predecessors, patternMap, indexofPrimitives, false,
				null, null);
		Set<FMNode> newSuccessors = makeNewNodesInPatternMap(successors, patternMap, indexofPrimitives
				+ predecessors.size() + 1, true, predecessors, newPredecessors);
		// unified set, to prevent duplicate nodes (both predecessor and successor)

		HashSet<FMNode> adjacentNodes = new HashSet<FMNode>();
		adjacentNodes.addAll(newPredecessors);
		adjacentNodes.addAll(newSuccessors);
		//keep the adjacent nodes and remove those which are not adjacent to  the cluster node 
		for (FMNode node : adjacentNodes) {
			if (node instanceof FMClusterNode) {
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getClusterBackgroundColor());
			} else if (node instanceof FuzzyMapPatternNode) {
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getAbstractBackgroundColor());
			} else {
				node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getPrimitiveBackgroundColor());
			}
			patternMap.setPrimitiveNode(indexofPrimitives, node);
			patternMap.setNodeAliasMapping(indexofPrimitives, node);
			patternMap.graphElementAdded(node);
			indexofPrimitives++;
		}

		//reset the edges of the detail graph of this Cluster node
		//patternMap.fmEdges = new HashSet<FMEdgeImpl>();
		//	patternMap.fmEdges = patternMap.getEdges();
		// asssemble edges
		FMEdges clusterEdges = new FMEdges(patternMap);
		// write edges within clusters
		addInnerEdgesBetweenSets(primitives, primitives, clusterEdges);
		for (FMEdgeImpl edge : clusterEdges.getEdges()) {
			patternMap.addEdge(edge);
		}
		// create external edges
		FMEdges externalEdges = new FMEdges(patternMap);
		// write edges from predecessors to cluster nodes
		addExternalInEdgesBetweenSets(newPredecessors, primitives, externalEdges);
		// write edges from cluster nodes to successors
		addExternalOutEdgesBetweenSets(primitives, newSuccessors, externalEdges);
		// write edges
		for (FMEdgeImpl edge : externalEdges.getEdges()) {
			edge.getAttributeMap().put(AttributeMap.EDGECOLOR, FMColors.getEdgeColor());
			edge.getAttributeMap().put(AttributeMap.LABELCOLOR, FMColors.getLabelColor());
			patternMap.addEdge(edge);
		}

		graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, patternMap);

		return graphPanel;
	}

	/*
	 * make new nodes in the new patter graph
	 */
	private Set<FMNode> makeNewNodesInPatternMap(Set<FMNode> nodesInWholeMap, FuzzyBusinessProcessMap patternMap,
			int indexofPrimitives, boolean isSuccessors, Set<FMNode> oldPredecessors, Set<FMNode> newPredecessors) {
		Set<FMNode> newNodesInPatternGraph = new HashSet<FMNode>();
		for (FMNode oldNode : nodesInWholeMap) {
			FMNode newNode;
			if (isSuccessors && oldPredecessors.contains(oldNode)) {
				//if the successor node is also a predecessor node
				//add the existing new predecessor node to pattern Graph as successors
				newNode = findNewNodeInPatternGraph(oldNode, newPredecessors);
			} else {
				if (oldNode instanceof FMClusterNode) {
					//FMClusterNode newClusterNode = new FMClusterNode(patternMap,indexofPrimitives,oldNode.getLabel());
					newNode = new FMClusterNode(patternMap, indexofPrimitives, oldNode.getLabel());
					//newNode.getAttributeMap().put(AttributeMap.FILLCOLOR, new Color(180, 220, 180));
				} else if (oldNode instanceof FuzzyMapPatternNode) {
					newNode = new FuzzyMapPatternNode(patternMap, indexofPrimitives, oldNode.getLabel());
					newNode.setEventType(oldNode.getEventType());
					//newNode.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.blue);
				} else {
					newNode = new FMNode(patternMap, indexofPrimitives, oldNode.getLabel());
					newNode.setEventType(oldNode.getEventType());

				}
				newNode.setElementName(oldNode.getElementName());

			}
			newNodesInPatternGraph.add(newNode);
			//patternMap.setPrimitiveNode(indexofPrimitives, newNode);
			//patternMap.setNodeAliasMapping(indexofPrimitives, newNode);
			//patternMap.graphElementAdded(newNode);
		}
		return newNodesInPatternGraph;
	}

	private FMNode findNewNodeInPatternGraph(FMNode oldNode, Set<FMNode> newPredecessors) {
		FMNode newNode = null;
		String oldNodeLabel = oldNode.getLabel();
		for (FMNode node : newPredecessors) {
			String currentLabel = node.getLabel();
			if (currentLabel.equals(oldNodeLabel)) {
				newNode = node;
				break;
			}
		}
		return newNode;
	}

	/*
	 * add edges between primitive nodes inside the pattern node
	 */
	protected void addInnerEdgesBetweenSets(Set<FMNode> sources, Set<FMNode> targets, FMEdges edges) {
		XLog logToUse;
		//The inner patter node edges should be get by using the event relations in the original log
		logToUse = wholeMap.originLog;
		//get the relations between events in the original log,they are used to construct the inner structure of the pattern node
		LogRelations originalLogRelations = new BasicLogRelations(logToUse);
		Map<Pair<XEventClass, XEventClass>, Integer> directFollowRelations = originalLogRelations
				.getDirectFollowsDependencies();
		Map<Pair<String, String>, Integer> directFollowRelationsBasedOnEventId = new HashMap<Pair<String, String>, Integer>();
		for (Pair<XEventClass, XEventClass> pair : directFollowRelations.keySet()) {
			XEventClass firstEvtClass = pair.getFirst();
			String firstEvtId = firstEvtClass.getId();
			XEventClass secondEvtClass = pair.getSecond();
			String secondEvtId = secondEvtClass.getId();
			Integer count = directFollowRelations.get(pair);
			directFollowRelationsBasedOnEventId.put(new Pair<String, String>(firstEvtId, secondEvtId), count);
		}

		String sourceEventId, targetEventId;
		for (FMNode source : sources) {
			sourceEventId = source.getElementName() + "+" + source.getEventType();
			for (FMNode target : targets) {
				targetEventId = target.getElementName() + "+" + target.getEventType();
				Pair<String, String> eventPair = new Pair<String, String>(sourceEventId, targetEventId);
				Integer relationCount = new Integer(0);
				relationCount = directFollowRelationsBasedOnEventId.get(eventPair);
				if ((relationCount != null) && (relationCount > 0.0)) {
					edges.addEdge(source, target, 0.5, 0.5);
				}
			}
		}
	}

	/*
	 * add external edges from the predecessors and the pattern primitive nodes
	 */
	protected void addExternalInEdgesBetweenSets(Set<FMNode> predecessors, Set<FMNode> primitives, FMEdges edges) {
		Set<String> sequencesOfPattern = wholeMap.selectedPatternNameSequenceMap.get(getElementName());
		Set<String> startSymbols = new HashSet<String>();
		for (String sequence : sequencesOfPattern) {
			String encodedStartSymbol = sequence.substring(0, 1);
			String startSymbol = wholeMap.charActivityMap.get(encodedStartSymbol);
			startSymbols.add(startSymbol);
		}

		String targetEventId;
		for (FMNode source : predecessors) {
			//sourceEventId = source.getElementName() + "-" + source.getEventType();
			for (FMNode target : primitives) {
				targetEventId = target.getElementName() + "-" + target.getEventType();
				if (startSymbols.contains(targetEventId)) {
					edges.addEdge(source, target, 0.5, 0.5);
				}
			}
		}
	}

	/*
	 * add external edges from the pattern primitive nodes and its successors
	 */
	protected void addExternalOutEdgesBetweenSets(Set<FMNode> primitives, Set<FMNode> successors, FMEdges edges) {
		Set<String> sequencesOfPattern = wholeMap.selectedPatternNameSequenceMap.get(getElementName());
		Set<String> endSymbols = new HashSet<String>();
		for (String sequence : sequencesOfPattern) {
			int length = sequence.length();
			String encodedEndSymbol = sequence.substring(length - 1, length);
			String endSymbol = wholeMap.charActivityMap.get(encodedEndSymbol);
			endSymbols.add(endSymbol);
		}

		String sourceEventId;
		for (FMNode source : primitives) {
			sourceEventId = source.getElementName() + "-" + source.getEventType();
			if (endSymbols.contains(sourceEventId)) {
				for (FMNode target : successors) {
					edges.addEdge(source, target, 0.5, 0.5);
				}
			}
		}
	}

}
