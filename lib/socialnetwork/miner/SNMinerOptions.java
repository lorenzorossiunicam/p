/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2006 TU/e Eindhoven * and is licensed under the * Common
 * Public License, Version 1.0 * by Eindhoven University of Technology *
 * Department of Information Systems * http://is.tm.tue.nl * *
 **********************************************************/

package org.processmining.plugins.socialnetwork.miner;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class SNMinerOptions {

	// constants for the possible metrics
	public final static int SUBCONTRACTING = 0;
	public final static int HANDOVER_OF_WORK = 1000;
	public final static int WORKING_TOGETHER = 2000;
	public final static int SIMILAR_TASK = 3000;
	public final static int REASSIGNMENT = 4000;
	public final static int SETTINGS = 5000;

	// constants for 'subcontracting/handover of work' setting
	public final static int CONSIDER_CAUSALITY = 100;
	public final static int CONSIDER_DIRECT_SUCCESSION = 10;
	public final static int CONSIDER_MULTIPLE_TRANSFERS = 1;

	// constants for 'working together' setting
	public final static int SIMULTANEOUS_APPEARANCE_RATIO = 0;
	public final static int DISTANCE_WITHOUT_CAUSALITY = 1;
	public final static int DISTANCE_WITH_CAUSALITY = 2;

	// constants for 'similar task' setting
	public final static int EUCLIDIAN_DISTANCE = 0;
	public final static int CORRELATION_COEFFICIENT = 1;
	public final static int SIMILARITY_COEFFICIENT = 2;
	public final static int HAMMING_DISTANCE = 3;

	// constants for 'reassignment' setting
	//		public final static int DIRECT_REASSIGNMENT = 0;
	//      public final static int CONSIDER_DISTANCE = 1;
	public final static int MULTIPLE_REASSIGNMENT = 1;
	//        public final static int CONSIDER_DISTANCE = 1;

	// constants for 'settings_grouping' setting
	public final static int GROUP_BY_ORG_UNIT = 0;
	public final static int GROUP_BY_ROLE = 1;
	public final static int GROUP_BY_ORG_UNIT_ROLE = 2;

	//	    private PanelOriginators panelOriginators;

	public SNMinerOptions() {
	}
}
