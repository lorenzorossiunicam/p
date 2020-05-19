package org.processmining.plugins.fuzzymodel.anim;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.animation.Animation;
import org.processmining.models.animation.AnimationLog;
import org.processmining.models.animation.EdgeAnimation;
import org.processmining.models.animation.NodeAnimation;
import org.processmining.models.animation.NodeAnimationKeyframe;
import org.processmining.models.animation.TokenAnimation;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphEdge;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;

public class FuzzyAnimation extends Animation {

	private MutableFuzzyGraph mfg;
	private Set<FMEdgeImpl> graphEdges;
	private int maxLookahead;
	private int maxExtraLookahead;

	public FuzzyAnimation(PluginContext context, MutableFuzzyGraph graph, XLog log, int maxLookahead,
			int maxExtraLookahead) {
		super(context, graph, log);
		this.mfg = graph;
		graphEdges = mfg.getEdgeImpls();
		this.maxLookahead = maxLookahead;
		this.maxExtraLookahead = maxExtraLookahead;
	}

	protected void createAnimations(XTrace trace, Progress progress) throws IndexOutOfBoundsException {
		int counter = 0;
		//String caseId = instance.getName();
		// insert case animation
		long start, end;
		start = FuzzyMinerLog.getEventTime(trace.get(0)).getTime();
		end = FuzzyMinerLog.getEventTime(trace.get(trace.size() - 1)).getTime();
		TokenAnimation caseTokenAnim = new TokenAnimation(trace, start, end);
		addTokenAnimation(caseTokenAnim);
		// parse event list
		boolean[] seeded = new boolean[trace.size()];
		Arrays.fill(seeded, false);
		XEvent ref, comp;
		FMNode refNode, compNode;
		boolean swallowed;
		// step through event list
		for (int i = 0; i < trace.size(); i++) {
			counter++;
			if (counter > 299) {
				progress.setValue(progress.getValue() + counter);
				counter = 0;
			}
			swallowed = true; // reset swallowed marker
			ref = trace.get(i);
			refNode = resolveNode(ref);
			if (refNode == null) {
				continue; // invalid event, ignore
			}
			// look ahead for potentially connected events
			int countdown = maxExtraLookahead;
			for (int j = i + 1; (j <= i + maxLookahead) && (j < trace.size()); j++) {
				comp = trace.get(j);
				compNode = resolveNode(comp);
				if (compNode == null) {
					continue; // invalid event, ignore
				}
				//	FMEdge edge = graphEdges.getEdge(refNode, compNode);
				FMEdgeImpl edge = null;
				for (FMEdgeImpl fmEdge : graphEdges) {
					if (fmEdge.getSource().equals(refNode) && fmEdge.getTarget().equals(compNode)) {
						edge = fmEdge;
					}
				}

				if (edge != null) {
					// connected, mark target position
					seeded[j] = true;
					// mark as non-swallowed
					swallowed = false;
					// create and add token animation
					/*
					 * TokenAnimation tokenAnim = new TokenAnimation(caseId,
					 * ref.getTimestamp().getTime(),
					 * comp.getTimestamp().getTime());
					 */
					addTokenAnimation(edge, new TokenAnimation(trace, FuzzyMinerLog.getEventTime(ref).getTime(),
							FuzzyMinerLog.getEventTime(comp).getTime()));
				}
				// stop looking after the extra lookahead after
				// finding the first valid connection.
				if (swallowed == false) {
					countdown--;
					if (countdown < 0) {
						break;
					}
				}
			}
			// create and add task animation
			addKeyframe(refNode, new NodeAnimationKeyframe(trace, FuzzyMinerLog.getEventTime(ref).getTime(),
					!seeded[i], swallowed));
			// update boundaries
			updateBoundaries(FuzzyMinerLog.getEventTime(ref).getTime());
		}
		progress.setValue(progress.getValue() + counter);
	}

	private FMNode resolveNode(XEvent ate) {
		String element = FuzzyMinerLog.getEventName(ate);
		if (element.startsWith("Cluster")) {
			for (FMClusterNode cluster : mfg.getClusterNodes()) {
				if (cluster.getElementName().equals(element)) {
					return cluster;
				}
			}
		}
		int index = mfg.getEventClassIndex(FuzzyMinerLog.getEventName(ate), FuzzyMinerLog.getEventType(ate));
		if (index >= 0) {
			return mfg.getNodeMappedTo(index);
		} else {
			return null;
		}
	}

	public void paintNodeBackground(AbstractDirectedGraphNode node, Graphics2D g2d, double x, double y, double width,
			double height) {
		if (node instanceof FMClusterNode) {
			double offset = height / 3;
			GeneralPath path = new GeneralPath();
			path.moveTo((float)x, (float)(y + offset));
			path.lineTo((float)(x + offset), (float)y);
			path.lineTo((float)(x + width - offset), (float)y);
			path.lineTo((float)(x + width), (float)(y + offset));
			path.lineTo((float)(x + width), (float)(y + height - offset));
			path.lineTo((float)(x + width - offset), (float)(y + height));
			path.lineTo((float)(x + offset), (float)(y + height));
			path.lineTo((float)x, (float)(y + height - offset));
			path.closePath();
			// paint background
			g2d.fill(path);
		} else {
			Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
			g2d.fill(rect);
		}
	}

	public void paintNodeBorder(AbstractDirectedGraphNode node, Graphics2D g2d, double x, double y, double width,
			double height) {
		if (node instanceof FMClusterNode) {
			double offset = height / 3;
			GeneralPath path = new GeneralPath();
			path.moveTo((float)x, (float)(y + offset));
			path.lineTo((float)(x + offset), (float)y);
			path.lineTo((float)(x + width - offset), (float)y);
			path.lineTo((float)(x + width), (float)(y + offset));
			path.lineTo((float)(x + width), (float)(y + height - offset));
			path.lineTo((float)(x + width - offset), (float)(y + height));
			path.lineTo((float)(x + offset), (float)(y + height));
			path.lineTo((float)x, (float)(y + height - offset));
			path.closePath();
			// paint background
			g2d.draw(path);
		} else {
			Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
			g2d.draw(rect);
		}
	}

	public void paintNodeText(AbstractDirectedGraphNode node, Graphics2D g2d, double x, double y, double width,
			double height) {
		FontMetrics fMetrics = g2d.getFontMetrics();
		String name;
		String type;
		if (node instanceof FMClusterNode) {
			name = ((FMClusterNode) node).getElementName();
			type = ((FMClusterNode) node).size() + " primitives";
		} else {
			name = ((FMNode) node).getElementName();
			type = ((FMNode) node).getEventType();
		}
		Rectangle2D nameBounds = fMetrics.getStringBounds(name, g2d);
		Rectangle2D typeBounds = fMetrics.getStringBounds(type, g2d);
		double vSlack = height - nameBounds.getHeight() - typeBounds.getHeight();
		double nameX = x + (width - nameBounds.getWidth()) / 2;
		double nameY = y + (vSlack / 3) + nameBounds.getHeight();
		double typeX = x + (width - typeBounds.getWidth()) / 2;
		double typeY = nameY + (vSlack / 3) + typeBounds.getHeight();
		g2d.drawString(name, (int)nameX, (int)nameY);
		g2d.drawString(type, (int)typeX, (int)typeY);
	}

	public void paintTokenLabel(AbstractDirectedGraphEdge<?, ?> edge, XTrace trace, Graphics2D g2d, double x, double y) {
		String traceName = AnimationLog.getTraceName(trace);
		Rectangle2D bound = g2d.getFontMetrics().getStringBounds(traceName, g2d);
		g2d.drawString(traceName, (int) x, (int) (y - bound.getHeight() / 2));
		//g2d.drawString(edge.getLabel(), (int) x, (int) (y + bound.getHeight() / 2));

	}

	public float getActivity(long modelTime, long maxTaskDelay) {
		float maxActivity = 0;
		float activity = 0;
		for (NodeAnimation anim : getNodeAnimations()) {
			if (anim.getNode() instanceof FMClusterNode) {
				maxActivity++;
				if(anim.getKeyframe(modelTime).getTime() > maxTaskDelay) {
					activity += 4;
				} 
			} else {
				maxActivity++;
				if(anim.getKeyframe(modelTime).getTime() > maxTaskDelay) {
					activity += 2;
				}
			}
		}
		for (EdgeAnimation anim : getEdgeAnimations()) {
			maxActivity++;
			activity += anim.getKeyframe(modelTime).getTokenAnimations().size();
		}
		// amplify activity
		activity *= 2;
		activity = activity / maxActivity;
		if(activity > 1f) {
			activity = 1f;
		}
		return activity;
	}
}
