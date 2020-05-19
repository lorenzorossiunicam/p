package org.processmining.plugins.socialnetwork.analysis.centrality;

import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.graph.Graph;

public class OutDegreeCentrality extends AbstractCentrality {

	protected double getVertexScore(Graph<SNNode, SNEdge> graph, SNNode v) {
		return getVertexScore(v);
	}

	protected double getVertexScore(SNNode v) {
		return v.getOutDegree();
	}

}
