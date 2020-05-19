package org.processmining.plugins.socialnetwork.analysis.util;

import org.apache.commons.collections15.Predicate;
import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;

public class DirectionDisplayPredicate implements Predicate<Context<Graph<SNNode, SNEdge>, SNEdge>> {
	protected boolean show_d;

	public DirectionDisplayPredicate(boolean show_d, boolean show_u) {
		this.show_d = show_d;
	}

	public void showDirected(boolean b) {
		show_d = b;
	}

	public boolean evaluate(Context<Graph<SNNode, SNEdge>, SNEdge> context) {
		Graph<SNNode, SNEdge> graph = context.graph;
		SNEdge e = context.element;
		if ((graph.getEdgeType(e) == EdgeType.DIRECTED) && show_d) {
			return true;
		}

		return false;
	}
}
