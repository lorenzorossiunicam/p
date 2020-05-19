package org.processmining.plugins.transitionsystem.miner;

public class TSMinerHelp {

	public final static String TEXT = ""
			+ "Discovers a transition system from the given log. "
			+ "A state in the resulting transition system is a combination of "
			+ "(1) a collection of activities seen so far, "
			+ "(2) a collection of activities yet to be seen, and "
			+ "(3) a mapping from some attributes to their actual values. "
			+ "The collections include sets, bags, and lists, and a threshold for the distance to the current event can be set. "
			+ "The discovered transition system can be mollified in some ways before it is returned.";
}
