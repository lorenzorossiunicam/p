package org.processmining.plugins.socialnetwork.miner.miningoperation;

import java.util.ArrayList;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;

import cern.colt.matrix.DoubleMatrix2D;

public abstract class BasicOperation {
	protected XLogInfo summary;
	protected ArrayList<String> originatorList;
	protected XEventClasses modelElements;
	protected String[] elements;
	protected XLog log;

	public BasicOperation(XLog inputLog) {
		summary = XLogInfoFactory.createLogInfo(inputLog);

		originatorList = new ArrayList<String>();
		XEventClasses originators = summary.getResourceClasses();
		for (int k = 0; k < originators.getClasses().size(); k++) {
			originatorList.add(originators.getByIndex(k).toString());
		}

		XEventClasses tasks = summary.getNameClasses();
		elements = new String[tasks.getClasses().size()];
		tasks.getClasses();
		for (int k = 0; k < tasks.getClasses().size(); k++) {
			elements[k] = tasks.getByIndex(k).toString();
		}

		// events
		modelElements = summary.getTransitionClasses();
		log = inputLog;
	}

	public DoubleMatrix2D calculation(double beta, int depth) {
		return calculation();
	};

	public DoubleMatrix2D calculation() {
		return null;
	};

	public ArrayList<String> getOriginatorList() {
		return originatorList;
	}
}
