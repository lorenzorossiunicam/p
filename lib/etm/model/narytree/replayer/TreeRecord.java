package org.processmining.plugins.etm.model.narytree.replayer;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.tue.astar.AStarThread;
import nl.tue.astar.Delegate;
import nl.tue.astar.Head;
import nl.tue.astar.Record;
import nl.tue.astar.Tail;
import nl.tue.astar.Trace;
import nl.tue.astar.impl.State;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.StorageException;
import nl.tue.storage.compressor.BitMask;

import org.processmining.plugins.boudewijn.treebasedreplay.TreeDelegate;
import org.processmining.plugins.boudewijn.treebasedreplay.TreeHead;

public class TreeRecord implements Record {
	//                              header: 16 bytes 
	private long state; //                   8 bytes
	private final int cost; //               4 bytes
	private final TreeRecord predecessor; // 8 bytes
	private final int modelMove; //          4 bytes 
	private final int movedEvent; //         4 bytes 
	private final int modelMoveCount; //          4 bytes
	private double totalCost; //                4 bytes
	private final BitMask executed; //       8 bytes (plus size of object if movedEvent!= AStarThread.NOMOVE
	private final int[] internalMoves; //   >
	private final int internalMoveCost;
	private boolean exact;
	private final int backtraceSize;

	private TreeRecord(long state, int cost, TreeRecord predecessor, int modelMove, int movedEvent, int backtrace,
			int[] internalMoves, int internalMoveCost, BitMask executed, int backTraceSize) {
		this.state = state;
		this.cost = cost;
		this.predecessor = predecessor;
		this.modelMove = modelMove;
		this.movedEvent = movedEvent;
		this.modelMoveCount = backtrace;
		this.internalMoves = internalMoves;
		this.internalMoveCost = internalMoveCost;
		this.executed = executed;
		this.backtraceSize = backTraceSize;
	}

	public TreeRecord(int cost, TIntList internalMoves, int internalMoveCost, int traceLength, boolean isExact) {
		this.cost = cost;
		this.internalMoveCost = internalMoveCost;
		this.predecessor = null;
		this.modelMove = AStarThread.NOMOVE;
		this.movedEvent = AStarThread.NOMOVE;
		this.modelMoveCount = cost;
		if (internalMoves != null) {
			this.internalMoves = new int[internalMoves.size()];
			for (int i = 0; i < internalMoves.size(); i++) {
				this.internalMoves[i] = internalMoves.get(i);
			}
		} else {
			this.internalMoves = null;
		}
		this.executed = new BitMask(traceLength);
		this.backtraceSize = 0;
		this.exact = isExact;

	}

	public <H extends Head, T extends Tail> State<H, T> getState(CompressedStore<State<H, T>> storage)
			throws StorageException {
		return storage.getObject(state);
	}

	public long getState() {
		return state;
	}

	public int getCostSoFar() {
		return cost;
	}

	public double getTotalCost() {
		return totalCost;
	}

	public TreeRecord getPredecessor() {
		return predecessor;
	}

	public void setState(long index) {
		this.state = index;
	}

	/**
	 * In case of a LogMove only, then logMove>=0, modelMove ==
	 * AStarThread.NOMOVE,
	 * 
	 * In case of a ModelMove only, then logMove == AStarThread.NOMOVE,
	 * modelMove >=0,
	 * 
	 * in case of both log and model move, then logMove>=0, modelMove>=0,
	 * 
	 */
	public TreeRecord getNextRecord(Delegate<? extends Head, ? extends Tail> d, Trace trace, Head nextHead, long state,
			int modelMove, int movedEvent, int activity) {
		TreeDelegate<?, ?> delegate = (TreeDelegate<?, ?>) d;

		int c = delegate.getCostFor(modelMove, activity);
		int ci = 0;

		int[] internalMoves = null;

		TIntList moves = ((TreeHead) nextHead).getMovesMade();
		if (moves != null && moves.size() > 0) {
			internalMoves = new int[moves.size()];
			for (int i = moves.size(); i-- > 0;) {
				internalMoves[i] = moves.get(i);
				ci += delegate.getModelMoveCost(internalMoves[i]);
			}
		}
		BitMask newExecuted;
		if (movedEvent != AStarThread.NOMOVE) {
			newExecuted = executed.clone();
			newExecuted.set(movedEvent, true);
			//			newExecuted = Arrays.copyOf(executed, executed.length);
			//			newExecuted[movedEvent] = true;
		} else {
			newExecuted = executed;
		}

		TreeRecord r = new TreeRecord(state, cost + c + ci, this, modelMove, movedEvent, modelMoveCount
				+ (modelMove == AStarThread.NOMOVE ? 0 : modelMove < delegate.getTree().size() ? 1 : 0), internalMoves,
				ci, newExecuted, backtraceSize + 1);
		return r;
		//		}
	}

	public double getEstimatedRemainingCost() {
		return this.totalCost - this.cost;
	}

	public void setEstimatedRemainingCost(double estimate, boolean isExactEstimate) {
		this.exact = isExactEstimate;
		this.totalCost = this.cost + estimate;

	}

	public boolean equals(Object o) {
		return (o instanceof Record) && ((Record) o).getState() == state;
		//&& ((Record) o).getCostSoFar() == cost
		//&& ((Record) o).getEstimatedRemainingCost() == estimate;
	}

	public int hashCode() {
		return (int) state;
	}

	public String toString() {
		return "(" + (getMovedEvent() == AStarThread.NOMOVE ? "_" : getMovedEvent()) + ","
				+ (getModelMove() == AStarThread.NOMOVE ? "_" : getModelMove()) + ")"
				+ (internalMoves == null ? "" : Arrays.toString(internalMoves));
	}

	public int getModelMove() {
		return modelMove;
	}

	public static List<TreeRecord> getHistory(TreeRecord r) {
		if (r == null) {
			return Collections.emptyList();
		}
		List<TreeRecord> history = new ArrayList<TreeRecord>(r.getModelMoveCount());
		while (r != null) {
			history.add(0, r);
			r = r.getPredecessor();
		}
		return history;
	}

	public static void printRecord(TreeDelegate<?, ?> delegate, Trace trace, TreeRecord r) {
		List<TreeRecord> history = getHistory(r);

		for (int i = 0; i < history.size(); i++) {
			r = history.get(i);
			String s = "(";
			short act = -1;
			if (r.getMovedEvent() != AStarThread.NOMOVE) {
				act = (short) trace.get(r.getMovedEvent());
			}
			if (r.getModelMove() == AStarThread.NOMOVE) {
				s += "_";
			} else {
				short m = (short) r.getModelMove();
				s += delegate.toString(m, act);
			}

			s += ",";
			// r.getLogEvent() is the event that was moved, or AStarThread.NOMOVE
			if (r.getMovedEvent() == AStarThread.NOMOVE) {
				s += "_";
			} else {
				assert (act >= 0 || act < 0);
				s += delegate.getEventClass(act);
			}
			s += ")";
			s += "c:" + r.getCostSoFar();
			s += ",e:" + r.getEstimatedRemainingCost();
			s += ",b:" + r.getModelMoveCount();
			s += ",im: " + (r.internalMoves == null ? "[]" : Arrays.toString(r.internalMoves));
			s += (i < history.size() - 1 ? " --> " : " cost: " + (r.getCostSoFar()));

			System.out.print(s);
		}
		System.out.println();
	}

	public int getMovedEvent() {
		return movedEvent;
	}

	public TIntCollection getNextEvents(Delegate<? extends Head, ? extends Tail> delegate, Trace trace) {
		return trace.getNextEvents(executed);
	}

	public int getModelMoveCount() {
		return modelMoveCount;
	}

	public int[] getInternalMoves() {
		return internalMoves == null ? new int[0] : internalMoves;
	}

	public double getInternalMovesCost() {
		return internalMoveCost;
	}

	public boolean isExactEstimate() {
		return exact;
	}

	public int getBacktraceSize() {
		return backtraceSize;
	}

}
