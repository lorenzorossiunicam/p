package org.processmining.plugins.socialnetwork.miner.miningoperation.subcontract;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class SubcontractICIDCM extends BasicOperation {

	// ignore casuality, ignore direct succession, consider multiple appearance

	public SubcontractICIDCM(XLog inputLog) {
		super(inputLog);
	}

	public DoubleMatrix2D calculation(double beta, int depth) {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);

		double normal = 0;

		for (XTrace trace : log) {
			if (trace.size() < 3) {
				continue;
			}

			int minK = 0;
			if (trace.size() < depth) {
				minK = trace.size();
			} else {
				minK = depth + 1;
			}

			if (minK < 3) {
				minK = 3;
			}

			for (int k = 2; k < minK; k++) {
				normal += Math.pow(beta, k - 2) * (trace.size() - k) * (k - 1);

				for (int i = 0; i < trace.size() - k; i++) {
					XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(i));
					XExtendedEvent xEvent3 = XExtendedEvent.wrap(trace.get(i + k));
					if (xEvent.getResource() == null || xEvent3.getResource() == null) {
						continue;
					}
					int row = originatorList.indexOf(xEvent.getResource());
					int row3 = originatorList.indexOf(xEvent3.getResource());

					if ((row != -1) && (row == row3)) {
						for (int j = i + 1; j < i + k; j++) {
							XExtendedEvent xEvent2 = XExtendedEvent.wrap(trace.get(j));
							if (xEvent2.getResource() == null) {
								continue;
							}
							int column = originatorList.indexOf(xEvent2.getResource());
							if (column == -1) {
								continue;
							}
							D.set(row, column, D.get(row, column) + Math.pow(beta, k - 2));
						}
					}
				}
			}

		}

		return UtilOperation.normalize(D, normal);
	}
}
