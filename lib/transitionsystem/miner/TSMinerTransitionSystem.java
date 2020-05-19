package org.processmining.plugins.transitionsystem.miner;

import org.processmining.models.graphbased.directed.transitionsystem.payload.event.EventPayloadTransitionSystem;

public class TSMinerTransitionSystem extends EventPayloadTransitionSystem {

	public TSMinerTransitionSystem(String label, TSMinerPayloadHandler handler) {
		super(label, handler);
	}
}
