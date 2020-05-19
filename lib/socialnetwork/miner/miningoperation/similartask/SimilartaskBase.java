package org.processmining.plugins.socialnetwork.miner.miningoperation.similartask;

import java.util.ArrayList;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class SimilartaskBase extends BasicOperation {

	protected ArrayList<String> elements = null;

	public SimilartaskBase(XLog inputLog) {
		super(inputLog);
		XLogInfo summary = XLogInfoFactory.createLogInfo(inputLog);
		XEventClasses tasks = summary.getNameClasses();
		elements = new ArrayList<String>();
		for (int k = 0; k < tasks.getClasses().size(); k++) {
			elements.add(tasks.getByIndex(k).toString());
		}
	};

	public DoubleMatrix2D makeOTMatrix() {
		int numOriginator = originatorList.size();
		DoubleMatrix2D otMatrix = DoubleFactory2D.sparse.make(numOriginator, elements.size(), 0);

		for (XTrace trace : log) {
			for (int k = 0; k < trace.size(); k++) {
				XExtendedEvent xEvent = XExtendedEvent.wrap(trace.get(k));
				if (xEvent.getResource() == null) {
					continue;
				}
				int row = originatorList.indexOf(xEvent.getResource());
				if (row == -1) {
					continue;
				}
				int column = elements.indexOf(xEvent.getName());
				otMatrix.set(row, column, otMatrix.get(row, column) + 1.0);
			}
		}
		return otMatrix;
	};
}
