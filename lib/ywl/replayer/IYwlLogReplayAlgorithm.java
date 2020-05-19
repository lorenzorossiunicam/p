package org.processmining.plugins.ywl.replayer;

import java.util.Collection;
import java.util.HashSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public interface IYwlLogReplayAlgorithm {
	public String toString();
	
	/**
	 * Array of objects consists of: 
	 * 0 - YPD (FPD)
	 * 1 - YawlConformanceResult
	 * 2 - GraphAnimation
	 */
	public Object[] replayLog(PluginContext context, NetGraph yawl, XLog log, Object[] vertices, HashSet<YAWLFlowRelation> flows, Collection<Pair<YAWLVertex, XEventClass>> mapping,
			int misTokenWeight, int remTokenWeight, int heurDistanceWeight, boolean cancellationOption);
	
}
