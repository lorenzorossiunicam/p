package org.processmining.plugins.socialnetwork.miner.miningoperation.subcontract;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class SubcontractICCDIM extends BasicOperation {

	// ignore casuality, consider direct succession, ignore multiple appearance

	public SubcontractICCDIM(XLog inputLog) {
		super(inputLog);
	}

	public DoubleMatrix2D calculation() {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);
		int count = 0;

		for (XTrace trace : log) {
			if (trace.size() < 3) {
				continue;
			}
			DoubleMatrix2D m = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);
			for (int k = 0; k < trace.size() - 2; k++) {
				XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(k));
				XExtendedEvent xEvent2 = XExtendedEvent.wrap(trace.get(k + 1));
				XExtendedEvent xEvent3 = XExtendedEvent.wrap(trace.get(k + 2));
				if (xEvent.getResource() == null || xEvent2.getResource() == null || xEvent3.getResource() == null) {
					continue;
				}
				int row = originatorList.indexOf(xEvent.getResource());
				int row2 = originatorList.indexOf(xEvent2.getResource());
				int row3 = originatorList.indexOf(xEvent3.getResource());

				if ((row != -1) && (row2 != -1) && (row == row3)) {
					m.set(row, row2, 1.0);
				}
			}
			count += trace.size() - 2;
			for (int i = 0; i < numOriginator; i++) {
				for (int j = 0; j < numOriginator; j++) {
					D.set(i, j, D.get(i, j) + m.get(i, j));
				}
			}

		}

		return UtilOperation.normalize(D, count);
	}
}
