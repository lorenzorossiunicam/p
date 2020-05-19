package org.processmining.plugins.socialnetwork.analysis.centrality;

import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.graph.Graph;

public abstract class AbstractCentrality {

	protected Map<SNNode, Double> rankings = new HashMap<SNNode, Double>();
	protected double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

	protected double getVertexScore(Graph<SNNode, SNEdge> graph, SNNode v) {
		return 0.0;
	}

	public Map<SNNode, Double> getRankings(Graph<SNNode, SNEdge> graph) {
		for (SNNode v : graph.getVertices()) {
			double temp = getVertexScore(graph, v);
			rankings.put(v, temp);
			if (temp < min) {
				min = temp;
			}
			if (temp > max) {
				max = temp;
			}
		}
		return rankings;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

}
