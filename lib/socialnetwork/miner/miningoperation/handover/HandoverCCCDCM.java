package org.processmining.plugins.socialnetwork.miner.miningoperation.handover;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class HandoverCCCDCM extends BasicOperation {

	// consider casuality, consider direct succession, consider multiple appearance
	//	private LogRelations relations = null;

	public HandoverCCCDCM(XLog inputLog) {
		super(inputLog);
	};

	public DoubleMatrix2D calculation() {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);
		int count = 0;

		for (XTrace trace : log) {
			for (int k = 0; k < trace.size() - 1; k++) {
				XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(k));
				XExtendedEvent xEvent2 = XExtendedEvent.wrap(trace.get(k + 1));

				if (xEvent.getResource() == null || xEvent2.getResource() == null) {
					continue;
				}
				
				int row = originatorList.indexOf(xEvent.getResource());
				int column = originatorList.indexOf(xEvent2.getResource());

				if ((row != -1) && (column != -1)) {
					D.set(row, column, D.get(row, column) + 1.0);
				} else {
					throw new Error("Implementation error: couldn't find user in the user list: "
							+ xEvent.getResource() + " or " + xEvent2.getResource());

				}
				count++;
			}
		}
		return UtilOperation.normalize(D, count);
	};
}
