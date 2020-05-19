package org.processmining.plugins.transitionsystem.miner.util;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

/**
 * Class that caches (and enforces sequential access to) events in a trace.
 * 
 * @author hverbeek
 * 
 */
public class TSEventCache {

	static private final int MAX_CACHE_SIZE = 1024;

	private XEvent events[];
	private int size;
	private XTrace trace;

	public TSEventCache() {
		trace = null;
	}

	/**
	 * Gets the event at index i in the given trace t.
	 * 
	 * Precondition: This event exists. Side effect: Caches up to MAX_CACHE_SIZE
	 * events in the trace.
	 * 
	 * Somehow, the OpenXES library seems to favor sequential access in traces.
	 * Therefore, we try to access all events in a trace sequentially and cache
	 * them. Later requests can then be answered from the cache.
	 * 
	 * @param t
	 *            The given trace.
	 * @param i
	 *            The given index.
	 * @return The event at index i in trace t.
	 */
	public XEvent get(XTrace t, int i) {
		if (t.isEmpty()) {
			return null;
		}
		if ((trace == null) || (trace != t)) {
			// New trace. Refresh cache.
			trace = t;
			// Determine cache size.
			size = trace.size();
			if (size > MAX_CACHE_SIZE) {
				size = MAX_CACHE_SIZE;
			}
			// Create cache.
			events = new XEvent[size];
			// Access the events in a sequential way, and store them in the cache.
			for (int j = 0; j < size; j++) {
				events[j] = trace.get(j);
			}
		}
		// Return the event, if possible from the cache.
		return (i < size ? events[i] : trace.get(i));
	}
}
