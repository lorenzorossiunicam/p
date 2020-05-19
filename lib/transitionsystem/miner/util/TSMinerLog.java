package org.processmining.plugins.transitionsystem.miner.util;

/**
 * Transition Miner Log
 * 
 * Log access for the transition system miner.
 * 
 * @author Eric Verbeek
 * @version 0.1
 * 
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class TSMinerLog {

	/**
	 * Get all traces from a log.
	 * 
	 * @param log
	 *            The log to get the traces from.
	 * @return All traces from the log.
	 */
	static public Collection<XTrace> getTraces(XLog log) {
		return log;
	}

	/**
	 * Get all events from a trace.
	 * 
	 * @param trace
	 *            The trace to get the events from.
	 * @return All events from the trace.
	 */
	static public Collection<XEvent> getEvents(XTrace trace) {
		return trace;
	}

	/**
	 * Get the data attributes of an event.
	 * 
	 * @param event
	 *            The event to get the data attribute sof.
	 * @return The data attributes of the event.
	 */
	static public Map<String, String> getDataAttributes(XEvent event) {
		XAttributeMap attributeMap = event.getAttributes();
		HashMap<String, String> map = new HashMap<String, String>();
		for (String label : attributeMap.keySet()) {
			map.put(label, attributeMap.get(label).toString());
		}
		return map;
	}
}
