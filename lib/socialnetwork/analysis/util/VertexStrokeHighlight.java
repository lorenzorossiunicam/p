package org.processmining.plugins.socialnetwork.analysis.util;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;
import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

public class VertexStrokeHighlight implements Transformer<SNNode, Stroke> {
	protected boolean highlight = false;
	protected Stroke heavy = new BasicStroke(5);
	protected Stroke medium = new BasicStroke(3);
	protected Stroke light = new BasicStroke(1);
	protected PickedInfo<SNNode> pi;
	protected Graph<SNNode, SNEdge> graph;

	public VertexStrokeHighlight(Graph<SNNode, SNEdge> graph, PickedInfo<SNNode> pi) {
		this.graph = graph;
		this.pi = pi;
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	public Stroke transform(SNNode v) {
		if (highlight) {
			if (pi.isPicked(v)) {
				return heavy;
			} else {
				for (SNNode w : graph.getNeighbors(v)) {
					if (pi.isPicked(w)) {
						return medium;
					}
				}
				return light;
			}
		} else {
			return light;
		}
	}

}
