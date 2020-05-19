package org.processmining.plugins.socialnetwork.miner.miningoperation.handover;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class HandoverICCDIM extends BasicOperation {

	// ignore casuality, consider direct succession, ignore multiple appearance

	public HandoverICCDIM(XLog inputLog) {
		super(inputLog);
	};

	public DoubleMatrix2D calculation() {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);

		for (XTrace trace : log) {
			DoubleMatrix2D m = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);

			for (int k = 0; k < trace.size() - 1; k++) {
				XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(k));
				XExtendedEvent xEvent2 = XExtendedEvent.wrap(trace.get(k + 1));

				if (xEvent.getResource() == null || xEvent2.getResource() == null) {
					continue;
				}
				
				int row = originatorList.indexOf(xEvent.getResource());
				int column = originatorList.indexOf(xEvent2.getResource());

				if ((row != -1) && (column != -1)) {
					m.set(row, column, D.get(row, column) + 1.0);
				} else {
					throw new Error("Implementation error: couldn't find user in the user list: "
							+ xEvent.getResource() + " or " + xEvent2.getResource());

				}
			}
			for (int i = 0; i < originatorList.size(); i++) {
				for (int j = 0; j < originatorList.size(); j++) {
					D.set(i, j, D.get(i, j) + m.get(i, j));
				}
			}
		}
		return UtilOperation.normalize(D, log.size());
	};
}
