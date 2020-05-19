package org.processmining.plugins.socialnetwork.analysis.centrality;

import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;

public class PageRanker extends AbstractCentrality {

	public Map<SNNode, Double> getRankings(Graph<SNNode, SNEdge> graph) {
		Transformer<SNEdge, Double> wtTransformer = new Transformer<SNEdge, Double>() {
			public Double transform(SNEdge link) {
				return link.getWeight();
			}
		};
		PageRank<SNNode, SNEdge> vs = new PageRank<SNNode, SNEdge>(graph, 0.5);
		vs.initialize();
		vs.setEdgeWeights(wtTransformer);
		vs.evaluate();
		for (SNNode v : graph.getVertices()) {
			double temp = vs.getVertexScore(v);
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
