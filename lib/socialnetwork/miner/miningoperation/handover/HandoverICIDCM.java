package org.processmining.plugins.socialnetwork.miner.miningoperation.handover;

import java.util.ArrayList;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class HandoverICIDCM extends BasicOperation {

	// ignore casuality, ignore direct succession, consider multiple appearance
	public HandoverICIDCM(XLog inputLog) {
		super(inputLog);
	};

	public DoubleMatrix2D calculation(double beta, int depth) {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);

		double normal = 0;

		for (XTrace trace : log) {
			int flag = 0;
			//			DoubleMatrix2D m = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);
			ArrayList<String> users_list_by_entries = new ArrayList<String>();
			for (int k = 0; k < trace.size() - 1; k++) {
				XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(k));
				XExtendedEvent xEvent2 = XExtendedEvent.wrap(trace.get(k + 1));

				if (xEvent.getResource() == null || xEvent2.getResource() == null) {
					continue;
				}
				
				int row = originatorList.indexOf(xEvent.getResource());
				users_list_by_entries.add(String.valueOf(row));
				int column = originatorList.indexOf(xEvent2.getResource());
				flag++;
				normal++;
				if ((row != -1) && (column != -1)) {
					D.set(row, column, D.get(row, column) + 1.0);
				} else {
					throw new Error("Implementation error: couldn't find user in the user list: "
							+ xEvent.getResource() + " or " + xEvent2.getResource());

				}
				if (column != -1) {
					for (int i = 0; (i < depth - 1) && (i < flag - 1); i++) {
						if (!users_list_by_entries.get(users_list_by_entries.size() - i - 2).equals("-1")) {
							row = Integer.valueOf((users_list_by_entries.get(users_list_by_entries.size() - i - 2)));

							D.set(row, column, D.get(row, column) + Math.pow(beta, i + 1));
						}
						normal += Math.pow(beta, i + 1);
					}
				}
			}
		}

		return UtilOperation.normalize(D, normal);
	};
}
