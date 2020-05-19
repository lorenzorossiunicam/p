package org.processmining.plugins.socialnetwork.miner.miningoperation.workingtogether;

import java.util.HashSet;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class WorkingtogetherSAR extends BasicOperation {

	// SIMULTANEOUS_APPEARANCE_RATIO
	public WorkingtogetherSAR(XLog inputLog) {
		super(inputLog);
	};

	public DoubleMatrix2D calculation() {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);
		int[] number = new int[numOriginator];

		for (XTrace trace : log) {
			HashSet<Integer> tempSet = new HashSet<Integer>();
			for (int k = 0; k < trace.size(); k++) {
				XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(k));
				if (xEvent.getResource() == null) {
					continue;
				}
				int row = originatorList.indexOf(xEvent.getResource());
				if (row != -1) {
					tempSet.add(row);
				}
			}
			for (Integer itr1 : tempSet) {
				number[itr1]++;
				for (Integer itr2 : tempSet) {
					D.set(itr1, itr2, D.get(itr1, itr2) + 1.0);
				}
			}
		}

		for (int i = 0; i < numOriginator; i++) {
			for (int j = 0; j < numOriginator; j++) {
				if (i == j) {
					D.set(i, j, 0.0);
				} else {
					D.set(i, j, D.get(i, j) / number[i]);
				}
			}
		}

		return D;

	};
}
