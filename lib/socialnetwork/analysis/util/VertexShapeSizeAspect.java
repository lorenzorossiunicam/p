package org.processmining.plugins.socialnetwork.analysis.util;

import java.awt.Shape;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;

public class VertexShapeSizeAspect extends AbstractVertexShapeTransformer<SNNode> {
	protected boolean stretch = false;
	protected boolean scale = false;
	protected boolean funny_shapes = false;
	protected Map<SNNode, Double> voltages;
	protected Graph<SNNode, SNEdge> graph;
	protected double min, max;

	public VertexShapeSizeAspect(Graph<SNNode, SNEdge> graphIn, Map<SNNode, Double> voltagesIn, double minIn,
			double maxIn) {
		graph = graphIn;
		voltages = voltagesIn;
		min = minIn;
		max = maxIn;

		setSizeTransformer(new Transformer<SNNode, Integer>() {
			public Integer transform(SNNode v) {
				if (scale) {
					return (int) (Math.log(voltages.get(v) - min + 1) * 2 + Math.log((voltages.get(v) - min)
							/ (max - min) * 100 + 1)) * 8 + 20;
				} else {
					return 20;
				}

			}
		});

		setAspectRatioTransformer(new Transformer<SNNode, Float>() {

			public Float transform(SNNode v) {
				if (stretch) {
					return (float) (graph.inDegree(v) + 1) / (graph.outDegree(v) + 1);
				} else {
					return 1.0f;
				}
			}
		});
	}

	public void setRanking(Map<SNNode, Double> voltagesIn, double minIn, double maxIn) {
		voltages = voltagesIn;
		min = minIn;
		max = maxIn;
	}

	public void setStretching(boolean stretch) {
		this.stretch = stretch;
	}

	public void setScaling(boolean scale) {
		this.scale = scale;
	}

	public void useFunnyShapes(boolean use) {
		funny_shapes = use;
	}

	public Shape transform(SNNode v) {
		if (funny_shapes) {
			if (graph.degree(v) < 5) {
				int sides = Math.max(graph.degree(v), 3);
				return factory.getRegularPolygon(v, sides);
			} else {
				return factory.getRegularStar(v, graph.degree(v));
			}
		} else {
			return factory.getEllipse(v);
		}
	}
}
