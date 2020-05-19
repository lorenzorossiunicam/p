package org.processmining.plugins.socialnetwork.analysis.centrality;

import java.util.Map;

import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;

public class Betweenness extends AbstractCentrality {

	public Map<SNNode, Double> getRankings(Graph<SNNode, SNEdge> graph) {
		BetweennessCentrality<SNNode, SNEdge> bc = new BetweennessCentrality<SNNode, SNEdge>(graph);
		bc.setRemoveRankScoresOnFinalize(false);
		bc.evaluate();
		for (SNNode v : graph.getVertices()) {
			double temp = bc.getVertexRankScore(v);
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
}
