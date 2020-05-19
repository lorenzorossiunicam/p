package org.processmining.plugins.socialnetwork.miner.miningoperation.similartask;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleMatrix2D;

public class SimilartaskHD extends SimilartaskBase {

	// consider multiple transfer

	public SimilartaskHD(XLog inputLog) {
		super(inputLog);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D OTMatrix = super.makeOTMatrix();

		return UtilOperation.hammingdistance(OTMatrix);
	};
}
