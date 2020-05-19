package org.processmining.plugins.transitionsystem.miner;

import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;

/**
 * Transition System Miner Output.
 * 
 * @author Eric Verbeek
 * @version 0.1
 */

public class TSMinerOutput {
	/**
	 * The transition system.
	 */
	private TSMinerTransitionSystem ts;
	/**
	 * The identifiers that started some trace.
	 */
	private StartStateSet starts;
	/**
	 * The identifiers that ended some trace.
	 */
	private AcceptStateSet accepts;
	/**
	 * The weights of all elements in the TS
	 */
	private DirectedGraphElementWeights weights;

	/**
	 * Create default output.
	 */
	public TSMinerOutput() {
		ts = null;
		starts = new StartStateSet();
		accepts = new AcceptStateSet();
		weights = new DirectedGraphElementWeights();
	}

	/**
	 * Returns the transition system.
	 * 
	 * @return The transition system.
	 */
	public TSMinerTransitionSystem getTransitionSystem() {
		return ts;
	}

	public TSMinerTransitionSystem setTransitionSystem(TSMinerTransitionSystem newTS) {
		TSMinerTransitionSystem oldTS = ts;
		ts = newTS;
		return oldTS;
	}

	/**
	 * Returns the start Ids.
	 * 
	 * @return The start Ids.
	 */
	public StartStateSet getStarts() {
		return starts;
	}

	public StartStateSet setStarts(StartStateSet newIds) {
		StartStateSet oldIds = starts;
		starts = newIds;
		return oldIds;
	}

	/**
	 * Returns the accept Ids.
	 * 
	 * @return The accept Ids.
	 */
	public AcceptStateSet getAccepts() {
		return accepts;
	}

	public AcceptStateSet setAccepts(AcceptStateSet newIds) {
		AcceptStateSet oldIds = accepts;
		accepts = newIds;
		return oldIds;
	}

	public DirectedGraphElementWeights getWeights() {
		return weights;
	}

	public DirectedGraphElementWeights setWeights(DirectedGraphElementWeights newWeights) {
		DirectedGraphElementWeights oldWeights = weights;
		weights = newWeights;
		return oldWeights;
	}
}
