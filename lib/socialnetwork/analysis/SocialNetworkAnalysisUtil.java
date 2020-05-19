package org.processmining.plugins.socialnetwork.analysis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;
import org.processmining.models.graphbased.directed.socialnetwork.SNEdge;
import org.processmining.models.graphbased.directed.socialnetwork.SNNode;
import org.processmining.plugins.socialnetwork.analysis.centrality.AbstractCentrality;
import org.processmining.plugins.socialnetwork.analysis.centrality.BaryRanker;
import org.processmining.plugins.socialnetwork.analysis.centrality.Betweenness;
import org.processmining.plugins.socialnetwork.analysis.centrality.Closeness;
import org.processmining.plugins.socialnetwork.analysis.centrality.DegreeCentrality;
import org.processmining.plugins.socialnetwork.analysis.centrality.InDegreeCentrality;
import org.processmining.plugins.socialnetwork.analysis.centrality.OutDegreeCentrality;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * 
 * @author Minseok Song
 * 
 */
public class SocialNetworkAnalysisUtil<V, E> implements Layout<V, E> {

	private Dimension size;
	private Graph<SNNode, SNEdge> graph;
	private VisualizationViewer<String, SNEdge> vv;
	private final String ranking;
	protected Map<SNNode, Point2D> locations = LazyMap.decorate(new HashMap<SNNode, Point2D>(),
			new Transformer<SNNode, Point2D>() {
				public Point2D transform(SNNode arg0) {
					return new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
				}
			});

	protected Map<SNNode, PolarPoint> polarLocations = LazyMap.decorate(new HashMap<SNNode, PolarPoint>(),
			new Transformer<SNNode, PolarPoint>() {
				public PolarPoint transform(SNNode arg0) {
					return new PolarPoint();
				}
			});

	protected Map<SNNode, Double> radii = new HashMap<SNNode, Double>();
	protected Map<SNNode, Double> rankings = new HashMap<SNNode, Double>();
	protected boolean bFirst = true;

	public SocialNetworkAnalysisUtil(Graph<SNNode, SNEdge> g, String ranking) {
		this.graph = g;
		this.ranking = ranking;
	}

	protected void setPolars() {
		AbstractCentrality absCen = null;
		double max = Double.MIN_VALUE;

		if (ranking.equals(SocialNetworkAnalysisUI.ST_DEGREE)) {
			absCen = new DegreeCentrality();
		} else if (ranking.equals(SocialNetworkAnalysisUI.INDEGREE)) {
			absCen = new InDegreeCentrality();
		} else if (ranking.equals(SocialNetworkAnalysisUI.OUTDEGREE)) {
			absCen = new OutDegreeCentrality();
		} else if (ranking.equals(SocialNetworkAnalysisUI.ST_BARYRANKER)) {
			absCen = new BaryRanker();
		} else if (ranking.equals(SocialNetworkAnalysisUI.ST_CLOSENESS)) {
			absCen = new Closeness();
		} else if (ranking.equals(SocialNetworkAnalysisUI.ST_BETWEENNESS)) {
			absCen = new Betweenness();
		}

		rankings = absCen.getRankings(graph);
		max = absCen.getMax();

		double angle = Math.max(0, Math.PI * 2 / graph.getVertexCount());
		double radius = Math.min(size.getWidth() / 2, size.getHeight() / 2) * 0.8;

		double rand = Math.random();
		double theta = 0;
		for (SNNode v : graph.getVertices()) {
			theta += (angle + rand);
			double tempRadius;
			//if(false) {
			//	tempRadius = radius/max*rankings.get(v);
			//} else {
			tempRadius = radius / max * (max - rankings.get(v));
			//}
			radii.put(v, tempRadius);
			PolarPoint pp = new PolarPoint(theta, tempRadius);
			polarLocations.put(v, pp);

			Point2D p = new Point(0, 0);
			p
					.setLocation(Math.cos(theta) * tempRadius + size.width / 2, Math.sin(theta) * tempRadius
							+ size.height / 2);
			locations.put(v, p);
		}
	}

	public void setSize(Dimension size) {
		this.size = size;
		if (bFirst) {
			setPolars();
			bFirst = false;
		}
	}

	@SuppressWarnings("unchecked")
	public Graph<V, E> getGraph() {
		return (Graph<V, E>) graph;
	}

	public Dimension getSize() {
		return size;
	}

	public void initialize() {

	}

	public boolean isLocked(V v) {
		return false;
	}

	public void lock(V v, boolean state) {
	}

	public void reset() {
	}

	@SuppressWarnings("unchecked")
	public void setGraph(Graph<V, E> graph) {
		this.graph = (Graph<SNNode, SNEdge>) graph;
	}

	public void setInitializer(Transformer<V, Point2D> initializer) {
	}

	public Point2D getCenter() {
		return new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
	}

	public void setLocation(V v, Point2D location) {
		SNNode node = (SNNode) v;
		Point2D c = getCenter();
		Point2D pv = new Point2D.Double(location.getX() - c.getX(), location.getY() - c.getY());
		PolarPoint newLocation = PolarPoint.cartesianToPolar(pv);
		polarLocations.get(node).setLocation(newLocation);

		Point2D center = getCenter();
		pv.setLocation(pv.getX() + center.getX(), pv.getY() + center.getY());
		locations.put(node, pv);
	}

	public Point2D transform(V v) {
		return locations.get(v);
	}

	/**
	 * @return the radii
	 */
	public Map<SNNode, Double> getRadii() {
		return radii;
	}

	public Map<SNNode, PolarPoint> getPolarLocations() {
		return polarLocations;
	}

	public VisualizationServer.Paintable getBackgroundTemp(VisualizationViewer<String, SNEdge> v) {
		this.vv = v;
		return new VisualizationServer.Paintable() {
			public void paint(Graphics g) {
				g.setColor(Color.gray);
				Graphics2D g2d = (Graphics2D) g;
				Point2D center = getCenter();

				Ellipse2D ellipse = new Ellipse2D.Double();
				for (SNNode v : graph.getVertices()) {
					double d = polarLocations.get(v).getRadius();
					ellipse.setFrameFromDiagonal(center.getX() - d, center.getY() - d, center.getX() + d, center.getY()
							+ d);
					Shape shape = vv.getRenderContext().getMultiLayerTransformer().transform(ellipse);
					g2d.draw(shape);
					g2d.drawString(String.valueOf(rankings.get(v)), (int) center.getX(), (int) (center.getY() + d));
				}
			}

			public boolean useTransform() {
				return true;
			}
		};
	}

	public VisualizationServer.Paintable getBackground(VisualizationViewer<String, SNEdge> v) {
		this.vv = v;
		return new VisualizationServer.Paintable() {
			public void paint(Graphics g) {
				g.setColor(Color.gray);
				Graphics2D g2d = (Graphics2D) g;
				Point2D center = getCenter();

				Ellipse2D ellipse = new Ellipse2D.Double();
				double max2 = Double.MIN_VALUE, max1 = Double.MIN_VALUE;
				for (SNNode v : graph.getVertices()) {
					double d = polarLocations.get(v).getRadius();
					if (d > max2) {
						max2 = d;
					}
					d = rankings.get(v);
					if (d > max1) {
						max1 = d;
					}
				}
				double index = max2 / 5;
				double index2 = max1 / 5;
				for (int i = 1; i < 6; i++) {
					double d = i * index;
					ellipse.setFrameFromDiagonal(center.getX() - d, center.getY() - d, center.getX() + d, center.getY()
							+ d);

					Shape shape = vv.getRenderContext().getMultiLayerTransformer().transform(ellipse);
					g2d.draw(shape);
					g2d.drawString(String.valueOf((5 - i) * index2), (int) center.getX(), (int) (center.getY() + d));
				}
			}

			public boolean useTransform() {
				return true;
			}
		};
	}
}
