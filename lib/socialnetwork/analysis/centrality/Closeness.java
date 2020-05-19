package org.processmining.plugins.socialnetwork.analysis.centrality;

import org.apache.commons.collections15.Transformer;
import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.graph.Graph;

public class Closeness extends AbstractCentrality {

	/**
	 * Calculates the score for the specified vertex.
	 */
	protected double getVertexScore(Graph<SNNode, SNEdge> graph, SNNode v) {
		Transformer<SNEdge, Double> wtTransformer = new Transformer<SNEdge, Double>() {
			public Double transform(SNEdge link) {
				return link.getWeight();
			}
		};
		Distance<SNNode> distance = new DijkstraDistance<SNNode, SNEdge>(graph, wtTransformer);

		double sum = 0.0;
		for (SNNode w : graph.getVertices()) {
			if (w.equals(v)) {
				continue;
			}
			Number w_distance = distance.getDistance(v, w);
			if (w_distance == null) {
				continue;
			} else {
				sum += w_distance.doubleValue();
			}
		}

		double value = sum / distance.getDistanceMap(v).size();

		return value;
	}

}
