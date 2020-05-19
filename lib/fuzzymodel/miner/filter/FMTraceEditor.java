package org.processmining.plugins.fuzzymodel.miner.filter;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;
import org.processmining.plugins.log.logfilters.XTraceEditor;

public class FMTraceEditor implements XTraceEditor {
	/**
	 * After project every node in a trace into its possible Cluster node, this
	 * method is called for ignore repetitions of the same cluster,i.e. remove
	 * event. If null is returned, or an empty trace is returned, the calling
	 * filter will remove the trace from the log. otherwise, the calling filter
	 * will replace the old trace with the new XTrace objects returned.
	 * 
	 * @param trace
	 *            The trace that is currently being considered by the calling
	 *            filter. Note that it can be assumed that
	 *            trace.isEmpty()==false
	 * @return The edited trace. If null, or an empty trace is returned, then
	 *         the trace is removed. No new trace objects should be returned.
	 */
	
	public XTrace editTrace(XTrace trace) {
		XEvent lastClusterEvent = null;
		XTrace editedTrace = null;
		for (int i = 0; i < trace.size(); i++) {
			XEvent curEvent = trace.get(i);
			String curEventName = FuzzyMinerLog.getEventName(curEvent);
			if (curEventName.startsWith("Cluster")) {
				if (lastClusterEvent == null) {
					lastClusterEvent = curEvent;
				} else {
					String lastCusterEventName = FuzzyMinerLog.getEventName(lastClusterEvent);
					//cluster event repeat,remove later event
					if (curEventName.equals(lastCusterEventName)) {
						trace.remove(i);
						i--;
					}
				}
			} else {
				lastClusterEvent = null;
			}
		}
		editedTrace = trace;
		return editedTrace;
	}

}
