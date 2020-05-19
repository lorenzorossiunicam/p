/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.connections.aggregatedactivitiesperformancediagram.AAPDLogReplayConnection;
import org.processmining.connections.aggregatedactivitiesperformancediagram.FPDAAPDConnection;
import org.processmining.connections.fuzzyperformancediagram.FPDLogReplayConnection;
import org.processmining.connections.fuzzyperformancediagram.SPDFPDConnection;
import org.processmining.connections.simpleprecedencediagram.LogSPDConnection;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.models.performancemeasurement.dataelements.CaseKPIData;
import org.processmining.models.performancemeasurement.dataelements.FPDElementPerformanceMeasurementData;
import org.processmining.models.performancemeasurement.dataelements.TwoFPDNodesPerformanceData;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDNode;
import org.processmining.plugins.log.transitionchecker.LifecycleTransitionChecker;
import org.processmining.plugins.log.transitionchecker.StandardLifecycleTransitionChecker;
import org.processmining.plugins.performancemeasurement.logreplayers.calculators.AbstractFPDAAPDCalculator;
import org.processmining.plugins.performancemeasurement.logreplayers.calculators.FuzzyFPDAAPDCalculator;
import org.processmining.plugins.performancemeasurement.logreplayers.nodeidentifiers.FuzzyFPDNodeLogIdentifier;
import org.processmining.plugins.performancemeasurement.logreplayers.nodeidentifiers.IFPDNodeLogIdentifier;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 6, 2009
 */
@Plugin(name = "Replay Log in Simple Precedence Diagram (SPD)", returnLabels = { "Fuzzy Performance Diagram (FPD)",
		"Case KPI Data", "Global Settings", "Element Performance Measurement Data", "Two Nodes Performance Data",
		"Aggregated Activity Performance Diagram (AAPD)" }, returnTypes = { FPD.class, CaseKPIData.class,
		GlobalSettingsData.class, FPDElementPerformanceMeasurementData.class, TwoFPDNodesPerformanceData.class,
		AAPD.class }, parameterLabels = { "Simple Precedence Diagram", "Event Log", "Lookahead Value",
		"FPD node identifier", "Lifecycle transition checker", "FPD-AAPD Calculator" }, help = "Replay log in Simple Precedence Diagram (SPD).", userAccessible = true)
public class FPDAAPDLogReplayer {

	@PluginVariant(variantLabel = "From SPD, Log, lookahead value, and FPD node identifier", requiredParameterLabels = {
			0, 1, 2, 3 })
	public Object[] replayLog(PluginContext context, SPD spd, XLog log, int lookaheadValue,
			IFPDNodeLogIdentifier nodeLogIdentifier) throws Exception {
		return replayLog(context, spd, log, lookaheadValue, nodeLogIdentifier, new StandardLifecycleTransitionChecker());
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "A. Adriansyah and B.F. van Dongen", email = "a.adriansyah@tue.nl; b.f.v.dongen@tue.nl", pack="Performance")
	@PluginVariant(variantLabel = "From SPD, Log, lookahead value, and FPD node identifier", requiredParameterLabels = {
			0, 1 })
	public Object[] replayLog(PluginContext context, SPD spd, XLog log) throws Exception {
		return replayLog(context, spd, log, 5, new FuzzyFPDNodeLogIdentifier(5000),
				new StandardLifecycleTransitionChecker());
	}

	@PluginVariant(variantLabel = "From SPD, Log, lookahead value, FPD node identifier, and lifecycle trans. checker", requiredParameterLabels = {
			0, 1, 2, 3, 4 })
	public Object[] replayLog(PluginContext context, SPD spd, XLog log, int lookaheadValue,
			IFPDNodeLogIdentifier nodeLogIdentifier, LifecycleTransitionChecker lifecycleTransitionChecker)
			throws Exception {
		// create FPD from SPD
		// spd and log needs to be connected
		try {
			LogSPDConnection conn = context.getConnectionManager().getFirstConnection(LogSPDConnection.class, context,
					spd, log);

			// create spdMapping
			Map<SPDNode, Set<XEventClass>> spdMapping = new HashMap<SPDNode, Set<XEventClass>>();
			for (SPDNode node : spd.getNodes()) {
				spdMapping.put(node, conn.getActivitiesFor(node));
			}

			AbstractFPDAAPDCalculator fpdAAPDCalculator = new FuzzyFPDAAPDCalculator(spd, spdMapping, lookaheadValue,
					lifecycleTransitionChecker);

			// create connection between SPD and FPD
			context.addConnection(new SPDFPDConnection(spd, fpdAAPDCalculator.getFpd(), fpdAAPDCalculator
					.getMapSPDNodetoFPDNode(), fpdAAPDCalculator.getMapSPDEdgetoFPDEdge()));

			return replayLog(context, spd, log, lookaheadValue, nodeLogIdentifier, lifecycleTransitionChecker,
					fpdAAPDCalculator);
		} catch (Exception e) {
			// if there is no connection between SPD and log, log cannot be replayed
			context.log(
					"No connection between SPD and the log. Create mapping between SPD nodes and event class in log before retry to replay log again",
					MessageLevel.DEBUG);
			throw e;
		}
	}

	@PluginVariant(variantLabel = "From Simple Precedence Diagram, Log, lookahead value, FPD node identifier, lifecycle transition checker, and fpd calculator", requiredParameterLabels = {
			0, 1, 2, 3, 4, 5 })
	public Object[] replayLog(PluginContext context, SPD spd, XLog log, int lookaheadValue,
			IFPDNodeLogIdentifier nodeLogIdentifier, LifecycleTransitionChecker lifecycleTransitionChecker,
			AbstractFPDAAPDCalculator fpdAAPDCalculator) throws Exception {
		// check connection between spd and log
		try {
			@SuppressWarnings("unused")
			LogSPDConnection logSPDconn = context.getConnectionManager().getFirstConnection(LogSPDConnection.class,
					context, spd, log);

			// then, check for FPD connection
			@SuppressWarnings("unused")
			SPDFPDConnection spdFPDConn = context.getConnectionManager().getFirstConnection(SPDFPDConnection.class,
					context, spd, fpdAAPDCalculator.getFpd());
			return replayLogPrivate(context, spd, log, lookaheadValue, nodeLogIdentifier, lifecycleTransitionChecker,
					fpdAAPDCalculator);
		} catch (Exception e) {
			// if there is no connection between SPD and log, log cannot be replayed
			context.log(
					"No connection between SPD and the log. Create mapping between SPD nodes and event class in log before retry to replay log again",
					MessageLevel.DEBUG);
			throw e;
		}
	}

	/**
	 * The real method to replay log based on SPD in order to obtain performance
	 * values in FPD
	 * 
	 * @param context
	 * @param spd
	 * @param log
	 * @param lookaheadValue
	 * @param nodeLogIdentifier
	 * @param lifecycleTransitionChecker
	 * @param fpdAAPDCalculator
	 * @return
	 */
	private Object[] replayLogPrivate(PluginContext context, SPD spd, XLog log, int lookaheadValue,
			IFPDNodeLogIdentifier nodeLogIdentifier, LifecycleTransitionChecker lifecycleTransitionChecker,
			AbstractFPDAAPDCalculator fpdAAPDCalculator) {

		long startComputation = System.currentTimeMillis();

		Map<SPDNode, Integer> spdCounterMap = new HashMap<SPDNode, Integer>();
		for (SPDNode key : spd.getNodes()) {
			spdCounterMap.put(key, 0);
		}

		// create CaseKPIData
		CaseKPIData caseKPIData = new CaseKPIData();
		List<Boolean> caseValidities = new LinkedList<Boolean>();
		Set<Set<String>> setOfTeams = new HashSet<Set<String>>();
		Map<String, Set<XEventClass>> resourcesWork = new HashMap<String, Set<XEventClass>>(); // resource -> activity done, number

		// progress update
		Progress progress = context.getProgress();
		progress.setMinimum(0);
		progress.setMaximum(log.size() + 4);
		progress.setIndeterminate(false);

		// get event class for this event
		XEventNameClassifier classifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
		XEventClasses classes = summary.getEventClasses();

		// case performance calculation
		int caseID = 0;
		Date firstTimeExecution = null;
		Date lastTimeExecution = null;
		Date firstTimeInACaseExecution = null;
		Date lastTimeInACaseExecution = null;
		Set<List<FPDNode>> setOfSequences = new HashSet<List<FPDNode>>();
		int numEvents = 0;
		int involvedLabor = 0;

		for (XTrace trace : log) {
			// get all node mapping 
			Queue<Set<FPDNode>> queueOfNodeMappings = new LinkedList<Set<FPDNode>>();

			// needed for performance calculation
			List<Date> listTimestamp = new LinkedList<Date>();
			List<XEventClass> listEventClass = new LinkedList<XEventClass>();
			List<Integer> listEventTypes = new LinkedList<Integer>();
			List<String> listResources = new LinkedList<String>();

			// use extension classes to extract information about an event
			XLifecycleExtension xLifecycleExtension = XLifecycleExtension.instance();
			XTimeExtension xTimeExtension = XTimeExtension.instance();
			XOrganizationalExtension xOrganizationalExtension = XOrganizationalExtension.instance();

			// init Set of resources
			Set<String> teamMembers = new HashSet<String>();

			// init time measurement for every case
			lastTimeInACaseExecution = null;
			firstTimeInACaseExecution = null;

			// create list time stamp, set of resources, set of teams, etc.
			for (XEvent event : trace) {
				XEventClass currEventClass = classes.getClassOf(event);
				queueOfNodeMappings.add(fpdAAPDCalculator.getFpd().getNodes(currEventClass)); // add to mapped node queue

				listEventClass.add(classes.getClassOf(event)); // add to list of class event
				Date timeStamp = xTimeExtension.extractTimestamp(event);

				// check first and last time execution
				if (firstTimeExecution == null) {
					firstTimeExecution = timeStamp;
				} else {
					if (timeStamp != null) {
						// update First Time Execution 
						if (firstTimeExecution.after(timeStamp)) {
							firstTimeExecution = timeStamp;
						}
						// update Last Time Execution
						if (lastTimeExecution == null) {
							lastTimeExecution = timeStamp;
						} else {
							if (lastTimeExecution.before(timeStamp)) {
								lastTimeExecution = timeStamp;
							}
						}
					}
				}

				// check first and last time execution for this case
				if (firstTimeInACaseExecution == null) {
					firstTimeInACaseExecution = timeStamp;
				} else {
					if (timeStamp != null) {
						// update First Time Execution 
						if (firstTimeInACaseExecution.after(timeStamp)) {
							firstTimeInACaseExecution = timeStamp;
						}

						// update Last Time Execution
						if (lastTimeInACaseExecution == null) {
							lastTimeInACaseExecution = timeStamp;
						} else {
							if (lastTimeInACaseExecution.before(timeStamp)) {
								lastTimeInACaseExecution = timeStamp;
							}
						}
					}
				}

				listTimestamp.add(timeStamp); // extract time
				listEventTypes.add(lifecycleTransitionChecker.getIndexOfLifecycleTransitionString(xLifecycleExtension
						.extractStandardTransition(event).name()));

				// extract resource
				String resource = xOrganizationalExtension.extractResource(event);
				listResources.add(resource);

				if (resourcesWork.containsKey(resource)) {
					// check if the event class already exist
					resourcesWork.get(resource).add(currEventClass);
				} else {
					Set<XEventClass> tempSet = new HashSet<XEventClass>();
					tempSet.add(currEventClass);
					resourcesWork.put(resource, tempSet);
				}

				// extract teams
				teamMembers.add(resource);

				// update num of events
				numEvents++;
			} // end of event loop

			// identify list of nodes
			List<FPDNode> identifiedNodes = nodeLogIdentifier.identifyFPDNodes(queueOfNodeMappings,
					fpdAAPDCalculator.getFpd());

			//			// filter out the nodes that are not identified
			//			List<FPDNode> tempIdentifiedNodes = new LinkedList<FPDNode>();
			//			List<XEventClass> tempListEventClass = new LinkedList<XEventClass>();
			//			List<Integer> tempListEventTypes = new LinkedList<Integer>();
			//			List<Date> tempListTimestamp = new LinkedList<Date>();
			//			List<String> tempListResources = new LinkedList<String>();
			//			
			//			for (int i = 0; i < identifiedNodes.size(); i++){
			//				if (identifiedNodes.get(i) != null){
			//					tempIdentifiedNodes.add(identifiedNodes.get(i));
			//					tempListEventClass.add(listEventClass.get(i));
			//					tempListEventTypes.add(listEventTypes.get(i));
			//					tempListTimestamp.add(listTimestamp.get(i));
			//					tempListResources.add(listResources.get(i));
			//				}
			//			}
			//			
			//			// clear all list
			//			identifiedNodes.clear();
			//			listEventClass.clear();
			//			listEventTypes.clear();
			//			listTimestamp.clear();
			//			listResources.clear();
			//			
			//			// add all
			//			identifiedNodes.addAll(tempIdentifiedNodes);
			//			listEventClass.addAll(tempListEventClass);
			//			listEventTypes.addAll(tempListEventTypes);
			//			listTimestamp.addAll(tempListTimestamp);
			//			listResources.addAll(tempListResources);

			// use the list to update values in FPD
			fpdAAPDCalculator.updateCalculation(identifiedNodes, listEventClass, listEventTypes, listTimestamp,
					listResources, caseID);

			// update others
			progress.inc();
			caseID++;

			/**
			 * update case performance calculation
			 */
			if (fpdAAPDCalculator.getLastCaseCalculationValidity()) {
				caseKPIData.addFittingCaseID(caseID);
			}
			caseValidities.add(fpdAAPDCalculator.getLastCaseCalculationValidity());

			// update for case KPI
			setOfSequences.add(identifiedNodes);

			// add team members to setOfTeams
			setOfTeams.add(teamMembers);

			// update involvedLabor
			involvedLabor += teamMembers.size();

			// update throughput time of a case
			if ((lastTimeInACaseExecution != null) && (firstTimeInACaseExecution != null)) {
				if (firstTimeInACaseExecution.before(lastTimeInACaseExecution)) {
					caseKPIData.addCaseThroughputTimeData(lastTimeInACaseExecution.getTime()
							- firstTimeInACaseExecution.getTime());
				}
			}
		} // end of for trace : log

		fpdAAPDCalculator.setNumOfCases(caseID); // set number of cases which are used to generate this statistic
		fpdAAPDCalculator.finalizeCalculation();
		progress.inc();
		progress.inc();
		progress.inc();

		// finalize CaseKPIData
		caseKPIData.setNumCases(caseID);

		// arrival rate
		if ((firstTimeExecution != null) && (lastTimeExecution != null)) {
			if (!firstTimeExecution.equals(lastTimeExecution)) {
				caseKPIData.setArrivalRate((double) caseID
						/ (double) (lastTimeExecution.getTime() - firstTimeExecution.getTime()));
			}
		}

		caseKPIData.setNumInvolvedResource(resourcesWork.size());
		caseKPIData.setNumInvolvedTeams(setOfTeams.size());
		caseKPIData.setNumUniqueTraces(setOfSequences.size());
		caseKPIData.setExecutedEventsPerResource(numEvents / resourcesWork.size());

		int executedActivity = 0;
		for (String resource : resourcesWork.keySet()) {
			executedActivity += resourcesWork.get(resource).size();
		}
		caseKPIData.setExecutedActivityPerResource(((double) executedActivity) / resourcesWork.size());
		caseKPIData.setResourcePerCase((double) involvedLabor / caseID);

		progress.inc();

		// get result
		FPD fpd = fpdAAPDCalculator.getFpd();

		// create global settings
		GlobalSettingsData globalSettingsData = new GlobalSettingsData();

		// attach all nodes and edges to globalSettingsData
		for (FPDNode node : fpd.getNodes()) {
			node.setDivider(globalSettingsData.getDividerValue());
			node.updateNodeSize();
		}
		for (FPDEdge<? extends FPDNode, ? extends FPDNode> edge : fpd.getEdges()) {
			edge.setDivider(globalSettingsData.getDividerValue());
			edge.updateEdgeInterface();
		}

		// create element performance measurement
		FPDElementPerformanceMeasurementData elementPerformanceMeasurementData = new FPDElementPerformanceMeasurementData();
		elementPerformanceMeasurementData.setNodePerformance(fpdAAPDCalculator.getNodePerformance());
		elementPerformanceMeasurementData.setEdgePerformance(fpdAAPDCalculator.getEdgePerformance());

		// retrieve twoPerformanceData
		TwoFPDNodesPerformanceData twoNodesPerformanceData = fpdAAPDCalculator.getTwoNodesPerformanceData();

		// retrieve AAPD
		AAPD aapd = fpdAAPDCalculator.getAAPD();

		// connection between FPD and all result of log replay
		context.getConnectionManager().addConnection(
				new FPDLogReplayConnection(fpd, caseKPIData, globalSettingsData, elementPerformanceMeasurementData,
						twoNodesPerformanceData));

		// connection between AAPD and FPD
		FPDAAPDConnection connection = new FPDAAPDConnection(fpd, aapd);
		connection.setNodeMapping(fpdAAPDCalculator.getMapFPDNodeToAAPDFocusElements());
		context.getConnectionManager().addConnection(connection);

		// connection between AAPD and global setting
		context.getConnectionManager().addConnection(new AAPDLogReplayConnection(aapd, globalSettingsData));

		// set label
		context.getFutureResult(0).setLabel("FPD of " + spd.getLabel());
		context.getFutureResult(1).setLabel("Case KPI data of " + spd.getLabel());
		context.getFutureResult(2).setLabel("Global setting of " + spd.getLabel());
		context.getFutureResult(3).setLabel("Elements' performance of FPD of " + spd.getLabel());
		context.getFutureResult(4).setLabel("Two nodes performance of FPD of " + spd.getLabel());
		context.getFutureResult(5).setLabel("AAPD of " + spd.getLabel());

		// output time spent for calculation
		long endComputation = System.currentTimeMillis();
		context.log("Performance calculation is performed in " + ((endComputation - startComputation) / (double) 1000)
				+ "seconds", MessageLevel.DEBUG);
		return new Object[] { fpd, caseKPIData, globalSettingsData, elementPerformanceMeasurementData,
				twoNodesPerformanceData, aapd };
	}
}
