package org.processmining.plugins.etm.model.narytree.replayer;

import gnu.trove.set.TShortSet;
import gnu.trove.set.hash.TShortHashSet;
import nl.tue.astar.AStarThread;
import nl.tue.astar.Tail;
import nl.tue.astar.Trace;
import nl.tue.astar.impl.State;
import nl.tue.storage.CompressedHashSet;
import nl.tue.storage.Deflater;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;
import nl.tue.storage.Inflater;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.boudewijn.treebasedreplay.TreeDelegate;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.StateBuilder;

public abstract class AbstractNAryTreeDelegate<T extends Tail> extends StateBuilder implements
		TreeDelegate<NAryTreeHead, T> {

	protected final AStarAlgorithm algorithm;
	//protected final NAryTree tree;
	protected final int[] node2cost;
	protected final int threads;
	protected final short classes;
	protected final int scaling = 10000;

	protected final NAryTreeHeadCompressor<T> headCompressor;

	public AbstractNAryTreeDelegate(AStarAlgorithm algorithm, NAryTree tree, int configurationNumber, int[] node2cost,
			int threads) {
		super(tree, configurationNumber, true);
		this.algorithm = algorithm;
		//this.tree = tree;
		this.node2cost = node2cost;

		this.threads = threads;

		this.classes = (short) algorithm.getClasses().size();
		this.headCompressor = new NAryTreeHeadCompressor<T>(this, classes);
	}

	public abstract TreeRecord createInitialRecord(NAryTreeHead head, Trace trace);

	public Inflater<NAryTreeHead> getHeadInflater() {
		return headCompressor;
	}

	public Deflater<NAryTreeHead> getHeadDeflater() {
		return headCompressor;
	}

	public void setStateSpace(CompressedHashSet<State<NAryTreeHead, T>> statespace) {
	}

	public XEventClass getClassOf(XEvent e) {
		return algorithm.getClasses().getClassOf(e);
	}

	public short getIndexOf(XEventClass c) {
		return algorithm.getIndexOf(c);
	}

	public short numEventClasses() {
		return (short) algorithm.getClasses().size();
	}

	public int getCostFor(int node, int activity) {
		if (node == AStarThread.NOMOVE) {
			// logMove only
			return getLogMoveCost(activity);
		}
		if (activity == AStarThread.NOMOVE) {
			return getModelMoveCost(node);
		}
		// synchronous move. Don't penalize that.
		return 1;
	}

	private static TShortSet EMPTYSET = new TShortHashSet(1);

	public TShortSet getActivitiesFor(int node) {
		// leafs are mapped to activities.
		if (tree.isLeaf(node) && tree.getType(configurationNumber, node) != NAryTree.TAU) {
			return new TShortHashSet(new short[] { tree.getType(configurationNumber, node) });
		} else {
			return EMPTYSET;
		}
	}

	public XEventClass getEventClass(short act) {
		return algorithm.getEventClass(act);
	}

	public boolean isLeaf(int modelMove) {
		return tree.isLeaf(modelMove);
	}

	public int getLogMoveCost(int i) {
		return 1 + scaling * algorithm.getLogMoveCost(i);
	}

	public int getModelMoveCost(int node) {
		if (node >= node2cost.length) {
			// this is an OR-node which is terminating.
			return 0;
		}
		return 1 + (tree.isHidden(configurationNumber, node) ? 0 : scaling * node2cost[node]);
	}

	public int numNodes() {
		return tree.size();
	}

	public HashOperation<State<NAryTreeHead, T>> getHeadBasedHashOperation() {
		return headCompressor;
	}

	public EqualOperation<State<NAryTreeHead, T>> getHeadBasedEqualOperation() {
		return headCompressor;
	}

	public String toString(short modelMove, short activity) {
		int m = modelMove;
		if (m >= tree.size()) {
			m -= tree.size();
		}

		if (tree.isLeaf(m)) {
			if (tree.getType(configurationNumber, m) == NAryTree.TAU) {
				return "tau (" + modelMove + ")";
			}
			return algorithm.getEventClass(tree.getType(configurationNumber, m)).toString() + " (" + modelMove + ")";
		} else {
			switch (tree.getType(configurationNumber, m)) {
				case NAryTree.OR :
					return "OR (" + modelMove + ")";
				case NAryTree.ILV :
					return "ILV (" + modelMove + ")";
				case NAryTree.XOR :
					return "XOR (" + modelMove + ")";
				case NAryTree.AND :
					return "AND (" + modelMove + ")";
				case NAryTree.LOOP :
					return "LOOP (" + modelMove + ")";
				case NAryTree.SEQ :
					return "SEQ (" + modelMove + ")";
				case NAryTree.REVSEQ :
					return "REVSEQ (" + modelMove + ")";
				default :
					assert false;
					return null;

			}
		}
	}

	public int getScaling() {
		return scaling;
	}

	public boolean isBlocked(int node) {
		return tree.isBlocked(configurationNumber, node);
	}

	public NAryTree getTree() {
		return tree;
	}

	public AStarAlgorithm getAStarAlgorithm() {
		return algorithm;
	}

	public boolean useQueue() {
		return queue != MEMORYLESSQUEUE;
	}
}
