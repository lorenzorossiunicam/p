package org.processmining.plugins.socialnetwork.miner.miningoperation.reassignment;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class ReassignmentCM extends BasicOperation {

	// consider multiple transfer

	public ReassignmentCM(XLog inputLog) {
		super(inputLog);
	}

	public DoubleMatrix2D calculation() {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);
		int count = 0;

		for (XTrace trace : log) {
			count += trace.size() - 1;
			for (int k = 0; k < trace.size() - 1; k++) {
				XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(k));
				if (xEvent.getResource() == null || xEvent.getTransition() == null || xEvent.getName() == null) {
					continue;
				}
				int row = originatorList.indexOf(xEvent.getResource());
				if (row == -1) {
					continue;
				}
				if (xEvent.getTransition().equals("reassign")) {
					for (int i = k + 1; i < trace.size(); i++) {
						XExtendedEvent xEvent2 = XExtendedEvent.wrap(trace.get(i));
						if (xEvent2.getResource() == null || xEvent2.getName() == null) {
							continue;
						}
						int column = originatorList.indexOf(xEvent2.getResource());
						if (column != -1) {
							if (xEvent.getName().equals(xEvent2.getName())) {
								if ((row < 0) || (column < 0)) {
									throw new Error("Implementation error: couldn't find user in the user list: "
											+ xEvent.getResource() + " or " + xEvent2.getResource());
								} else {
									D.set(row, column, D.get(row, column) + 1);
									break;
								}
							}
						}
					}
				}
			}
		}
		return UtilOperation.normalize(D, count);
	}
}
