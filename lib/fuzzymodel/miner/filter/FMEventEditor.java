package org.processmining.plugins.fuzzymodel.miner.filter;

import org.deckfour.xes.model.XEvent;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLog;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLogEvents;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;
import org.processmining.plugins.log.logfilters.XEventEditor;

public class FMEventEditor implements XEventEditor {

	protected FuzzyGraph graph;
	protected FMLogEvents events;
	protected PluginContext context;

	public FMEventEditor(PluginContext context, FuzzyGraph aGraph) {
		//	super(LogFilter.FAST, "Fuzzy Graph Projection Filter");
		graph = aGraph;
		events = graph.getLogEvents();
		this.context = context;
	}

	/**
	 * When filtering, this method is called for each XEvent in the log. The
	 * event can be edited, or a new one can be returned. If null is returned,
	 * the calling filter will remove the event from the log. If a new XEvent
	 * object is returned, the called filter will replace the old event with the
	 * new event.
	 * 
	 * @param event
	 *            The event that is currently being considered by the calling
	 *            filter.
	 * @return The edited event. If null is returned, then the event is removed.
	 *         If a new XEvent object is returned, the event is replaced.
	 * 
	 */
	public XEvent editEvent(XEvent event) {
		//FMClusterNode lastCluster = null;
		FMNode node = null;
		XEvent editedEvent = null;
		int logEventIndex = events.findLogEventNumber(event);
		if (logEventIndex < 0) {
			// no valid log event found, look for cluster match

			if (FuzzyMinerLog.getEventName(event).startsWith("Cluster")) {
				// log already filtered
				for (FMClusterNode cluster : graph.getClusterNodes()) {
					if (cluster.getElementName().equals(FuzzyMinerLog.getEventName(event))) {
						editedEvent = (XEvent)event.clone();
						break;
					}
				}
			}
			if (editedEvent == null) {
				// not found
				context.log("Fuzzy Graph projection filter: Could not find log event for "
						+ FuzzyMinerLog.getEventName(event) + "/" + FuzzyMinerLog.getEventType(event) + "!",
						MessageLevel.WARNING);
			}
		} else {
			node = graph.getNodeMappedTo(logEventIndex);
		}
		if (node == null) {
			// event class has been removed
			editedEvent = null;
		} else if (node instanceof FMClusterNode) {
			// event class has been clustered
			editedEvent = (XEvent)event.clone();
			FMClusterNode cluster = (FMClusterNode) node;
			/*
			 * if (lastCluster != null && lastCluster.equals(cluster)) { //
			 * ignore repetitions of the same cluster, i.e. remove event
			 * editedEvent = null; } else {
			 */
			// replace event with reference to cluster
			/*
			 * current.setAttribute("FMFILTER_OriginalElement",
			 * current.getElement());
			 * /current.setAttribute("FMFILTER_OriginalEventType",
			 * current.getType()); current.setElement(cluster.id());
			 * ateList.replace(current, i);
			 */
			FMLog.setConceptName(editedEvent, cluster.getElementName());
			FMLog.setLifecycleTransition(editedEvent, cluster.getEventType());
			// remember last cluster which we mapped to
			/*
			 * lastCluster = cluster; }
			 */
		} else {
			// else: normal, unclustered event, leave as is.
			// reset last cluster
			editedEvent = event;
		}

		return editedEvent;
	}
	/*
	 * protected boolean doFiltering( XTrace instance) { //XTrace ateList =
	 * instance.getAuditTrailEntryList(); FMClusterNode lastCluster = null;
	 * XEvent current = null; FMNode node = null; for (int i = 0; i <
	 * instance.size(); i++) { current = instance.get(i); int logEventIndex =
	 * events.findLogEventNumber(current); if (logEventIndex < 0) { // no valid
	 * log event found, look for cluster match node = null;
	 * 
	 * if (FuzzyMinerLog.getEventName(current).startsWith("cluster")) { // log
	 * already filtered for (FMClusterNode cluster : graph.getClusterNodes()) {
	 * if (cluster.id().equals(FuzzyMinerLog.getEventName(current))) { node =
	 * cluster; break; } } } if (node == null) { // not found
	 * context.log("Fuzzy Graph projection filter: Could not find log event for "
	 * + FuzzyMinerLog.getEventName(current) + "/" +
	 * FuzzyMinerLog.getEventType(current) + "!", MessageLevel.WARNING); } }
	 * else { node = graph.getNodeMappedTo(logEventIndex); } if (node == null) {
	 * // event class has been removed instance.remove(i); i--; // correct index
	 * } else if (node instanceof FMClusterNode) { // event class has been
	 * clustered FMClusterNode cluster = (FMClusterNode) node; if (lastCluster
	 * != null && lastCluster.equals(cluster)) { // ignore repetitions of the
	 * same cluster, i.e. remove event instance.remove(i); i--; } else { //
	 * replace event with reference to cluster
	 * 
	 * ((XAttributeLiteralImpl)
	 * current.getAttributes().get(EVENT_NAME_KEY)).setValue(cluster
	 * .getElementName()); ((XAttributeLiteralImpl)
	 * current.getAttributes().get(EVENT_TYPE_KEY)).setValue(cluster
	 * .getEventType()); // remember last cluster which we mapped to lastCluster
	 * = cluster; } } else { // else: normal, unclustered event, leave as is. //
	 * reset last cluster lastCluster = null; }
	 * 
	 * } if (instance.size() == 0) { return false; } else { return true; } }
	 */

}
