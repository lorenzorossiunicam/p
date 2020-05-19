/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2006 TU/e Eindhoven * and is licensed under the * Common
 * Public License, Version 1.0 * by Eindhoven University of Technology *
 * Department of Information Systems * http://is.tm.tue.nl * *
 **********************************************************/

package org.processmining.plugins.socialnetwork.miner.miningoperation;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.socialnetwork.miner.SNMinerOptions;
import org.processmining.plugins.socialnetwork.miner.miningoperation.handover.HandoverCCCDCM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.handover.HandoverICCDCM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.handover.HandoverICCDIM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.handover.HandoverICIDCM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.handover.HandoverICIDIM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.reassignment.ReassignmentCM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.reassignment.ReassignmentIM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.similartask.SimilartaskCC;
import org.processmining.plugins.socialnetwork.miner.miningoperation.similartask.SimilartaskED;
import org.processmining.plugins.socialnetwork.miner.miningoperation.similartask.SimilartaskHD;
import org.processmining.plugins.socialnetwork.miner.miningoperation.similartask.SimilartaskSC;
import org.processmining.plugins.socialnetwork.miner.miningoperation.subcontract.SubcontractICCDCM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.subcontract.SubcontractICCDIM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.subcontract.SubcontractICIDCM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.subcontract.SubcontractICIDIM;
import org.processmining.plugins.socialnetwork.miner.miningoperation.workingtogether.WorkingtogetherDWC;
import org.processmining.plugins.socialnetwork.miner.miningoperation.workingtogether.WorkingtogetherDWTC;
import org.processmining.plugins.socialnetwork.miner.miningoperation.workingtogether.WorkingtogetherSAR;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class OperationFactory {
	public OperationFactory() {
	}

	public static BasicOperation getOperation(int indexType, XLog inputLog) {
		BasicOperation object = null;
		object = new HandoverCCCDCM(inputLog);
		switch (indexType) {
			// SUBCONTRACTING //
			case (SNMinerOptions.SUBCONTRACTING + SNMinerOptions.CONSIDER_DIRECT_SUCCESSION + SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS) :
				object = new SubcontractICCDCM(inputLog);
				break;
			case (SNMinerOptions.SUBCONTRACTING + SNMinerOptions.CONSIDER_DIRECT_SUCCESSION) :
				object = new SubcontractICCDIM(inputLog);
				break;
			case (SNMinerOptions.SUBCONTRACTING + SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS) :
				object = new SubcontractICIDCM(inputLog);
				break;
			case (SNMinerOptions.SUBCONTRACTING) :
				object = new SubcontractICIDIM(inputLog);
				break;
			/*
			 * case (SNMinerOptions.SUBCONTRACTING +
			 * SNMinerOptions.CONSIDER_CAUSALITY +
			 * SNMinerOptions.CONSIDER_DIRECT_SUCCESSION +
			 * SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS): object = new
			 * Subcontract_CCCDCM(summary, log); break; case
			 * (SNMinerOptions.SUBCONTRACTING +
			 * SNMinerOptions.CONSIDER_CAUSALITY +
			 * SNMinerOptions.CONSIDER_DIRECT_SUCCESSION): object = new
			 * Subcontract_CCCDIM(summary, log); break; case
			 * (SNMinerOptions.SUBCONTRACTING +
			 * SNMinerOptions.CONSIDER_CAUSALITY +
			 * SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS): object = new
			 * Subcontract_CCIDCM(summary, log); break; case
			 * (SNMinerOptions.SUBCONTRACTING +
			 * SNMinerOptions.CONSIDER_CAUSALITY): object = new
			 * Subcontract_CCIDIM(summary, log); break;
			 */// HANDOVER_OF_WORK
			case (SNMinerOptions.HANDOVER_OF_WORK + SNMinerOptions.CONSIDER_DIRECT_SUCCESSION + SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS) :
				object = new HandoverICCDCM(inputLog);
				break;
			case (SNMinerOptions.HANDOVER_OF_WORK + SNMinerOptions.CONSIDER_DIRECT_SUCCESSION) :
				object = new HandoverICCDIM(inputLog);
				break;
			case (SNMinerOptions.HANDOVER_OF_WORK + SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS) :
				object = new HandoverICIDCM(inputLog);
				break;
			case (SNMinerOptions.HANDOVER_OF_WORK) :
				object = new HandoverICIDIM(inputLog);
				break;
			/*
			 * case (SNMinerOptions.HANDOVER_OF_WORK +
			 * SNMinerOptions.CONSIDER_CAUSALITY +
			 * SNMinerOptions.CONSIDER_DIRECT_SUCCESSION +
			 * SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS): object = new
			 * Handover_CCCDCM(summary, log); break; case
			 * (SNMinerOptions.HANDOVER_OF_WORK +
			 * SNMinerOptions.CONSIDER_CAUSALITY +
			 * SNMinerOptions.CONSIDER_DIRECT_SUCCESSION): object = new
			 * Handover_CCCDIM(summary, log); break;
			 * 
			 * case (SNMinerOptions.HANDOVER_OF_WORK +
			 * SNMinerOptions.CONSIDER_CAUSALITY +
			 * SNMinerOptions.CONSIDER_MULTIPLE_TRANSFERS): object = new
			 * Handover_CCIDCM(summary, log); break; case
			 * (SNMinerOptions.HANDOVER_OF_WORK +
			 * SNMinerOptions.CONSIDER_CAUSALITY): object = new
			 * Handover_CCIDIM(summary, log); break;
			 */// WORKING_TOGETHER
			case (SNMinerOptions.WORKING_TOGETHER + SNMinerOptions.SIMULTANEOUS_APPEARANCE_RATIO) :
				object = new WorkingtogetherSAR(inputLog);
				break;
			case (SNMinerOptions.WORKING_TOGETHER + SNMinerOptions.DISTANCE_WITHOUT_CAUSALITY) :
				object = new WorkingtogetherDWC(inputLog);
				break;
			case (SNMinerOptions.WORKING_TOGETHER + SNMinerOptions.DISTANCE_WITH_CAUSALITY) :
				object = new WorkingtogetherDWTC(inputLog);
				break;
			// SIMILAR_TASK
			case (SNMinerOptions.SIMILAR_TASK + SNMinerOptions.EUCLIDIAN_DISTANCE) :
				object = new SimilartaskED(inputLog);
				break;
			case (SNMinerOptions.SIMILAR_TASK + SNMinerOptions.CORRELATION_COEFFICIENT) :
				object = new SimilartaskCC(inputLog);
				break;
			case (SNMinerOptions.SIMILAR_TASK + SNMinerOptions.SIMILARITY_COEFFICIENT) :
				object = new SimilartaskSC(inputLog);
				break;
			case (SNMinerOptions.SIMILAR_TASK + SNMinerOptions.HAMMING_DISTANCE) :
				object = new SimilartaskHD(inputLog);
				break;
			// REASSIGNMENT
			case (SNMinerOptions.REASSIGNMENT + SNMinerOptions.MULTIPLE_REASSIGNMENT) :
				object = new ReassignmentCM(inputLog);
				break;
			case (SNMinerOptions.REASSIGNMENT) :
				object = new ReassignmentIM(inputLog);
				break;
		}
		return object;
	}
}
