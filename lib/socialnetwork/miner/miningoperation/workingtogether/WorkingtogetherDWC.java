package org.processmining.plugins.socialnetwork.miner.miningoperation.workingtogether;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.socialnetwork.miner.SNMinerOptions;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.OperationFactory;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class WorkingtogetherDWC extends BasicOperation {

	// Consider distance without causality (beta=0.5)
	public WorkingtogetherDWC(XLog inputLog) {
		super(inputLog);
	};

	public DoubleMatrix2D calculation() {
		int numOriginator = originatorList.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(numOriginator, numOriginator, 0);
		BasicOperation baseObject = OperationFactory.getOperation(SNMinerOptions.HANDOVER_OF_WORK, log);
		DoubleMatrix2D imsiD = baseObject.calculation(0.5, 10);
		for (int i = 0; i < numOriginator; i++) {
			for (int j = i; j < numOriginator; j++) {
				D.set(i, j, (imsiD.get(i, j) + imsiD.get(j, i)) / 2);
				D.set(j, i, D.get(i, j));
			}
		}
		return D;
	};
}
