/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.calculators;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.Pair;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDFactory;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.dataelements.ActivityInstanceBar;
import org.processmining.models.performancemeasurement.dataelements.TimelineInfoData;
import org.processmining.plugins.log.transitionchecker.LifecycleTransitionChecker;
import org.processmining.plugins.log.transitionchecker.StandardLifecycleTransitionChecker;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 8, 2009
 */
public class FPDNodeInstanceAAPDCalculator implements IFPDNodeInstanceAAPDCalculator {
	// timeline static variable
	public static final int SCHEDULE_TIME = 0;
	public static final int START_TIME = 1;
	public static final int END_TIME = 2;

	// for bar measurement
	public static final int ELEMENT_START_TIME = 0;
	public static final int ELEMENT_SERVICE_TIME = 1;
	public static final int ELEMENT_WAITING_TIME = 2;
	public static final int ELEMENT_INTERSECTION_TIME = 3;
	public static final int ELEMENT_THROUGHPUT_TIME = 4;

	private final FPD fpd;
	private final LifecycleTransitionChecker lifecycleTransitionChecker;
	private final Set<Integer> caseIdentifier;

	private final Map<FPDNode, Map<XEventClass, Map<Integer, Queue<ActivityInstanceBar>>>> activeActivityInstances; // store active node instance bar	
	private final Map<FPDNode, Map<XEventClass, Integer>> fpdNodeCounter; // store number of fpdNode
	private Map<FPDNode, AAPDElement> mapFPDNodeToAAPDElement;

	// variables to calculate performance. These variables are cleared as soon as a case is finished
	private int aapdElementID = 0;
	private final List<TimelineInfoData> timeLine; // type of event (SCHEDULE_TIME, START_TIME, END_TIME), barID, and time. This list is cleared after 1 case
	private Date startTimeOfACase = null; // star time of a case
	private final Set<FPDNode> involvedNodesInACase; // set which stores what are the nodes which are involved in a case
	private final Map<Integer, Integer> barProgress; // progress between SCHEDULE TIME, START_TIME, and END_TIME
	private final Map<FPDNode, Map<FPDNode, Map<Integer, List<Long>>>> aapdElementRelativeMeasurements; // start time, service time, waiting time, intersection time, throughput time
	private final Map<FPDNode, List<Long>> aapdBarThroughputTimeBoundary;

	// result
	private final AAPD aapd;

	public FPDNodeInstanceAAPDCalculator(FPD fpd) {
		this(fpd, new StandardLifecycleTransitionChecker());
	}

	public FPDNodeInstanceAAPDCalculator(FPD fpd, LifecycleTransitionChecker lifecycleTransitionChecker) {
		this.fpd = fpd;
		this.lifecycleTransitionChecker = lifecycleTransitionChecker;
		caseIdentifier = new HashSet<Integer>();

		// initiate variables specially created for AAPD
		aapd = AAPDFactory.newAAPD(fpd);
		mapFPDNodeToAAPDElement = new HashMap<FPDNode, AAPDElement>();
		timeLine = new LinkedList<TimelineInfoData>();
		involvedNodesInACase = new HashSet<FPDNode>();
		barProgress = new HashMap<Integer, Integer>();

		aapdElementRelativeMeasurements = new HashMap<FPDNode, Map<FPDNode, Map<Integer, List<Long>>>>();
		aapdBarThroughputTimeBoundary = new HashMap<FPDNode, List<Long>>();

		// initiate inactive and node counters
		activeActivityInstances = new HashMap<FPDNode, Map<XEventClass, Map<Integer, Queue<ActivityInstanceBar>>>>();
		fpdNodeCounter = new HashMap<FPDNode, Map<XEventClass, Integer>>();

		for (FPDNode fpdNode : this.fpd.getNodes()) {
			Map<XEventClass, Map<Integer, Queue<ActivityInstanceBar>>> tempMapEventClass = new HashMap<XEventClass, Map<Integer, Queue<ActivityInstanceBar>>>();
			Map<XEventClass, Map<Integer, Queue<ActivityInstanceBar>>> tempMapEventClass2 = new HashMap<XEventClass, Map<Integer, Queue<ActivityInstanceBar>>>();

			Map<XEventClass, Integer> tempMapEventClass3 = new HashMap<XEventClass, Integer>();

			for (XEventClass eventClass : fpdNode.getEventClasses()) {
				Map<Integer, Queue<ActivityInstanceBar>> tempMapIntList = new HashMap<Integer, Queue<ActivityInstanceBar>>();
				Map<Integer, Queue<ActivityInstanceBar>> tempMapIntList2 = new HashMap<Integer, Queue<ActivityInstanceBar>>();
				for (int lifecycleActivity : lifecycleTransitionChecker.getAvailableLifecycleTransitions()) {
					Queue<ActivityInstanceBar> tempList = new LinkedList<ActivityInstanceBar>();
					tempMapIntList.put(lifecycleActivity, tempList);

					Queue<ActivityInstanceBar> tempList2 = new LinkedList<ActivityInstanceBar>();
					tempMapIntList2.put(lifecycleActivity, tempList2);
				}

				tempMapEventClass3.put(eventClass, 0);

				// for the unknown lifecycle transition
				Queue<ActivityInstanceBar> tempList = new LinkedList<ActivityInstanceBar>();
				tempMapIntList.put(LifecycleTransitionChecker.UNKNOWN, tempList);

				tempMapEventClass.put(eventClass, tempMapIntList);

				Queue<ActivityInstanceBar> tempList2 = new LinkedList<ActivityInstanceBar>();
				tempMapIntList2.put(LifecycleTransitionChecker.UNKNOWN, tempList2);

				tempMapEventClass2.put(eventClass, tempMapIntList2);
			}
			activeActivityInstances.put(fpdNode, tempMapEventClass2);
			fpdNodeCounter.put(fpdNode, tempMapEventClass3);

			// add mapping to mapFPDAAPD
			mapFPDNodeToAAPDElement.put(fpdNode, aapd.addElement(fpdNode.getLabel()));

			// for aapdBoundaries
			List<Long> newLongList = new LinkedList<Long>();
			aapdBarThroughputTimeBoundary.put(fpdNode, newLongList);
		}

		// for AAPD, must be finalized
		aapd.initiateCalculation();
	}

	/**
	 * set a case start time, which is the first event encountered in a case
	 */
	public void setCaseStartTime(Date startTime) {
		startTimeOfACase = startTime;
	}

	/**
	 * get a case start time, which is the time when first event is encountered
	 * in a case
	 * 
	 * @return
	 */
	public Date getCaseStartTime() {
		return startTimeOfACase;
	}

	/**
	 * add information about encountered event in a log
	 */
	public void addEventClass(XEventClass eventClass, int eventTypeIdentifier, Date timestamp, FPDNode fpdNode,
			int caseID) {
		caseIdentifier.add(caseID);
		involvedNodesInACase.add(fpdNode);

		// find previous node instance
		// find the one which has more instances
		int selectedPredEventType = LifecycleTransitionChecker.ALLLIFECYCLETRANSITION;
		searchPredEventType: for (int predecessorEventType : lifecycleTransitionChecker
				.getAvailableTransitionsTo(eventTypeIdentifier)) {
			if (LifecycleTransitionChecker.ALLLIFECYCLETRANSITION == predecessorEventType) {
				// insert it immediately to eventTypeIdentifier
				selectedPredEventType = LifecycleTransitionChecker.ALLLIFECYCLETRANSITION;
				break searchPredEventType;
			} else {
				// check if there is any which are available
				if (activeActivityInstances.get(fpdNode).get(eventClass).get(predecessorEventType).size() > 0) {
					selectedPredEventType = predecessorEventType;
					break searchPredEventType;
				}
			}
		}

		// determine which node instances is selected
		ActivityInstanceBar selectedNodeInstanceBar = null;
		if (selectedPredEventType == LifecycleTransitionChecker.ALLLIFECYCLETRANSITION) {
			selectedNodeInstanceBar = new ActivityInstanceBar(eventClass, caseID, aapdElementID); // new instance bar

			// record it to timeline bar measurement as either SCHEDULE_TIME or START_TIME
			if (eventTypeIdentifier == LifecycleTransitionChecker.SCHEDULE) {
				TimelineInfoData newTimeLineInfoData = new TimelineInfoData(fpdNode,
						FPDNodeInstanceAAPDCalculator.SCHEDULE_TIME, aapdElementID, timestamp);
				timeLine.add(newTimeLineInfoData);
				barProgress.put(aapdElementID, FPDNodeInstanceAAPDCalculator.SCHEDULE_TIME);
			} else {
				// add artificial scheduled time
				TimelineInfoData newTimeLineInfoDataSchedule = new TimelineInfoData(fpdNode,
						FPDNodeInstanceAAPDCalculator.SCHEDULE_TIME, aapdElementID, timestamp);
				timeLine.add(newTimeLineInfoDataSchedule);
				TimelineInfoData newTimeLineInfoDataStart = new TimelineInfoData(fpdNode,
						FPDNodeInstanceAAPDCalculator.START_TIME, aapdElementID, timestamp);
				timeLine.add(newTimeLineInfoDataStart);
				barProgress.put(aapdElementID, FPDNodeInstanceAAPDCalculator.START_TIME);
			}
			aapdElementID++;
		} else { // we need to remove one instance from the predecessor EventType
			selectedNodeInstanceBar = activeActivityInstances.get(fpdNode).get(eventClass).get(selectedPredEventType)
					.remove();

			// only if the timeline is START_TIME and not yet updated, update
			if (eventTypeIdentifier == LifecycleTransitionChecker.START) {
				if (barProgress.containsKey(selectedNodeInstanceBar.getBarID())) { // check if the bar already checked before
					if (barProgress.get(selectedNodeInstanceBar.getBarID()) != FPDNodeInstanceAAPDCalculator.START_TIME) {
						TimelineInfoData newTimeLineInfoData = new TimelineInfoData(fpdNode,
								FPDNodeInstanceAAPDCalculator.START_TIME, selectedNodeInstanceBar.getBarID(), timestamp);
						timeLine.add(newTimeLineInfoData);
						barProgress.put(selectedNodeInstanceBar.getBarID(), FPDNodeInstanceAAPDCalculator.START_TIME);
					} // else, there is already START_TIME for this bar, no need to add another one
				} else {
					// bar is never checked, but the first event is START event. So, artificial schedule is added. Then, start time is added.  
					TimelineInfoData newTimeLineInfoDataSchedule = new TimelineInfoData(fpdNode,
							FPDNodeInstanceAAPDCalculator.SCHEDULE_TIME, selectedNodeInstanceBar.getBarID(), timestamp);
					timeLine.add(newTimeLineInfoDataSchedule);
					TimelineInfoData newTimeLineInfoDataStart = new TimelineInfoData(fpdNode,
							FPDNodeInstanceAAPDCalculator.START_TIME, selectedNodeInstanceBar.getBarID(), timestamp);
					timeLine.add(newTimeLineInfoDataStart);
					barProgress.put(selectedNodeInstanceBar.getBarID(), FPDNodeInstanceAAPDCalculator.START_TIME);
				}
			}
		}

		// now, determine is the nodeInstance is still active? 
		if (lifecycleTransitionChecker.getAvailableTransitionsFrom(eventTypeIdentifier).get(0) == LifecycleTransitionChecker.UNKNOWN) {
			// no successors, then we need to close this bar

			// check if start time already exist
			if (barProgress.get(selectedNodeInstanceBar.getBarID()) != FPDNodeInstanceAAPDCalculator.START_TIME) {
				// need to add artificial start time at the same time as SCHEDULE_TIME
				for (int j = timeLine.size() - 1; j >= 0; j--) {
					if (timeLine.get(j).getBarID() == selectedNodeInstanceBar.getBarID()) {
						// start time is added after scheduled time
						TimelineInfoData newTimeLineInfoDataStart = new TimelineInfoData(fpdNode,
								FPDNodeInstanceAAPDCalculator.START_TIME, selectedNodeInstanceBar.getBarID(), timeLine
										.get(j).getTime());
						timeLine.add(j + 1, newTimeLineInfoDataStart);
						break;
					}
				}
			}

			// end this bar by adding end time
			TimelineInfoData newTimeLineInfoDataEnd = new TimelineInfoData(fpdNode,
					FPDNodeInstanceAAPDCalculator.END_TIME, selectedNodeInstanceBar.getBarID(), timestamp);
			timeLine.add(newTimeLineInfoDataEnd);
			barProgress.remove(selectedNodeInstanceBar.getBarID());

			// update also time fpdNodeCounter
			int oldCounter = fpdNodeCounter.get(fpdNode).get(eventClass);
			fpdNodeCounter.get(fpdNode).put(eventClass, oldCounter + 1);
		} else {
			// it is possible that a successor will be found in the future. Property of bar should be calculated later
			selectedNodeInstanceBar.addLifecycleTransition(eventTypeIdentifier, timestamp);
			activeActivityInstances.get(fpdNode).get(eventClass).get(eventTypeIdentifier).add(selectedNodeInstanceBar);
		}
	}

	/**
	 * finalize performance calculation after control flow of an FPD node is
	 * moved to other node
	 */
	public int finalizeNodeInstance(FPDNode currentNode) { // this is called when calculation on a node is finished. Return number of node instance bar
		// for every event class
		for (XEventClass eventClass : currentNode.getEventClasses()) {
			//for every eventType
			for (int eventTypeIdentifier : activeActivityInstances.get(currentNode).get(eventClass).keySet()) {
				Queue<ActivityInstanceBar> activeQueueNodeInstanceBar = activeActivityInstances.get(currentNode).get(
						eventClass).get(eventTypeIdentifier);
				while (!activeQueueNodeInstanceBar.isEmpty()) {
					ActivityInstanceBar tempNodeInstanceBar = activeQueueNodeInstanceBar.remove();

					// check if start time already exist
					if (barProgress.get(tempNodeInstanceBar.getBarID()) != FPDNodeInstanceAAPDCalculator.START_TIME) {
						// need to add artificial start time and end time at the same time as SCHEDULE_TIME
						for (int j = timeLine.size() - 1; j >= 0; j--) {
							if (timeLine.get(j).getBarID() == tempNodeInstanceBar.getBarID()) { // must be scheduled time
								// start time and end time are added after scheduled time
								TimelineInfoData newTimeLineInfoDataStart = new TimelineInfoData(currentNode,
										FPDNodeInstanceAAPDCalculator.START_TIME, tempNodeInstanceBar.getBarID(),
										timeLine.get(j).getTime());
								timeLine.add(j + 1, newTimeLineInfoDataStart);
								TimelineInfoData newTimeLineInfoDataEnd = new TimelineInfoData(currentNode,
										FPDNodeInstanceAAPDCalculator.END_TIME, tempNodeInstanceBar.getBarID(),
										timeLine.get(j).getTime());
								timeLine.add(j + 2, newTimeLineInfoDataEnd);

								barProgress.remove(tempNodeInstanceBar.getBarID());
								break;
							}
						}
					} else {
						// only need to add finish time
						for (int j = timeLine.size() - 1; j >= 0; j--) {
							if (timeLine.get(j).getBarID() == tempNodeInstanceBar.getBarID()) { // must be scheduled time
								// end time is added after scheduled time
								TimelineInfoData newTimeLineInfoDataEnd = new TimelineInfoData(currentNode,
										FPDNodeInstanceAAPDCalculator.END_TIME, tempNodeInstanceBar.getBarID(),
										timeLine.get(j).getTime());
								timeLine.add(j + 1, newTimeLineInfoDataEnd);

								barProgress.remove(tempNodeInstanceBar.getBarID());
								break;
							}
						}
					}

					// update also time fpdNodeCounter
					int oldCounter = fpdNodeCounter.get(currentNode).get(eventClass);
					fpdNodeCounter.get(currentNode).put(eventClass, oldCounter + 1);
				}
			}
		}

		// calculate num of node instance bar
		int numOfNodeInstanceBar = 0;
		Map<XEventClass, Integer> mapInteger = fpdNodeCounter.get(currentNode);
		if (mapInteger != null) {
			for (int subtotal : mapInteger.values()) {
				numOfNodeInstanceBar += subtotal;
			}
		}
		return numOfNodeInstanceBar;
	}

	/**
	 * @return number the number of instances
	 */
	public int getNumInstanceBars(FPDNode node, XEventClass eventClass) {
		if (fpdNodeCounter.containsKey(node)) {
			if (fpdNodeCounter.get(node).containsKey(eventClass)) {
				return fpdNodeCounter.get(node).get(eventClass);
			}
		}
		return 0;
	}

	/**
	 * finalize performance calculation after all event in a case is encountered
	 */
	public Map<FPDNode, Integer> finalizeAllNodeInstance() { // this is called when a case is already finished
		Map<FPDNode, Integer> result = new HashMap<FPDNode, Integer>();

		// all active should be moved to non active
		for (FPDNode node : activeActivityInstances.keySet()) {
			result.put(node, finalizeNodeInstance(node));
		}

		// populate AAPD
		populateAAPDData();

		// reset all data
		startTimeOfACase = null;
		aapdElementID = 0;
		timeLine.clear();
		involvedNodesInACase.clear();
		barProgress.clear();

		// reset aapdbarRelativePerformance
		aapdElementRelativeMeasurements.clear();
		return result;
	}

	/**
	 * calculate performance measurement to generate an AAPD base on a case
	 */
	private void populateAAPDData() { // only called after a case calculation is finished
		// iterate through time line to calculate AAPD
		List<TimelineInfoData> activeTimeline = new LinkedList<TimelineInfoData>(); // timeline for bars which hasn't closed yet
		Map<Integer, FPDNode> activeBars = new HashMap<Integer, FPDNode>();

		// start calculation
		for (int i = 0; i < timeLine.size(); i++) {
			TimelineInfoData currTimelineInfo = timeLine.get(i);
			if (currTimelineInfo.getTypeEvent() == FPDNodeInstanceAAPDCalculator.SCHEDULE_TIME) {
				updateSingleElementMeasurementNode(currTimelineInfo.getFpdNode(), ELEMENT_START_TIME, currTimelineInfo
						.getTime().getTime()
						- startTimeOfACase.getTime());
				activeTimeline.add(currTimelineInfo);

				// update list of active bar ID
				activeBars.put(currTimelineInfo.getBarID(), currTimelineInfo.getFpdNode());
			} else if (currTimelineInfo.getTypeEvent() == FPDNodeInstanceAAPDCalculator.START_TIME) {
				activeTimeline.add(currTimelineInfo);
			} else if (currTimelineInfo.getTypeEvent() == FPDNodeInstanceAAPDCalculator.END_TIME) {
				// for current bar
				TimelineInfoData startTime = null;
				SortedSet<Integer> toBeDeletedIndex = new TreeSet<Integer>();
				Set<Integer> calculatedBarIDs = new HashSet<Integer>();

				// calculate its intersection time and all other performance measurement
				for (int j = activeTimeline.size() - 1; j >= 0; j--) {
					TimelineInfoData checkTimelineInfo = activeTimeline.get(j);

					if (checkTimelineInfo.getTypeEvent() == FPDNodeInstanceAAPDCalculator.SCHEDULE_TIME) {
						if (currTimelineInfo.getBarID() == checkTimelineInfo.getBarID()) { // schedule time of current node
							// start time must not be null
							assert (startTime != null);
							assert (activeBars.containsKey(currTimelineInfo.getBarID()));

							// derive waiting time for this bar
							updateSingleElementMeasurementNode(currTimelineInfo.getFpdNode(), ELEMENT_WAITING_TIME,
									startTime.getTime().getTime() - checkTimelineInfo.getTime().getTime());
							updateSingleElementMeasurementNode(currTimelineInfo.getFpdNode(), ELEMENT_THROUGHPUT_TIME,
									currTimelineInfo.getTime().getTime() - checkTimelineInfo.getTime().getTime());
							toBeDeletedIndex.add(j);

							// every bar that are active in this time and haven't been calculated should intersect this bar
							for (int activeBarID : activeBars.keySet()) {
								if (!calculatedBarIDs.contains(activeBarID)) {
									addIntersectionTime(activeBars.get(activeBarID), currTimelineInfo.getFpdNode(),
											currTimelineInfo.getTime().getTime()
													- checkTimelineInfo.getTime().getTime());
									if (activeBarID != currTimelineInfo.getBarID()) {
										addIntersectionTime(currTimelineInfo.getFpdNode(), activeBars.get(activeBarID),
												currTimelineInfo.getTime().getTime()
														- checkTimelineInfo.getTime().getTime());
									}
								}
							}

							// update also service time to calculate boundary of AAPD focus elements
							aapdBarThroughputTimeBoundary.get(currTimelineInfo.getFpdNode()).add(
									currTimelineInfo.getTime().getTime() - checkTimelineInfo.getTime().getTime());

							// remove this node from active bars
							activeBars.remove(currTimelineInfo.getBarID());

							// delete activeTimeline
							int substractor = 0;
							for (int index : toBeDeletedIndex) {
								activeTimeline.remove(index - substractor);
								substractor++;
							}
							toBeDeletedIndex.clear();
							break;
						} else { // currTimelineInfo.getBarID() != checkTimelineInfo.getBarID() && (checkTimelineInfo.getTypeEvent() == FPDNodeInstanceAAPDCalculator.SCHEDULE_TIME)
							// checkTimelineInfo is the scheduled time of other bar. Count intersection time
							if (activeBars.containsKey(checkTimelineInfo.getBarID())) {
								addIntersectionTime(checkTimelineInfo.getFpdNode(), currTimelineInfo.getFpdNode(),
										currTimelineInfo.getTime().getTime() - checkTimelineInfo.getTime().getTime());
								if (checkTimelineInfo.getBarID() != currTimelineInfo.getBarID()) {
									addIntersectionTime(currTimelineInfo.getFpdNode(), checkTimelineInfo.getFpdNode(),
											currTimelineInfo.getTime().getTime()
													- checkTimelineInfo.getTime().getTime());
								}
								calculatedBarIDs.add(checkTimelineInfo.getBarID());
							}
						}
					} else if (checkTimelineInfo.getTypeEvent() == FPDNodeInstanceAAPDCalculator.START_TIME) {
						if (currTimelineInfo.getBarID() == checkTimelineInfo.getBarID()) { // start time of current node
							// start time must be null
							assert (startTime == null);
							startTime = checkTimelineInfo;

							// derive service time for this bar
							updateSingleElementMeasurementNode(currTimelineInfo.getFpdNode(), ELEMENT_SERVICE_TIME,
									currTimelineInfo.getTime().getTime() - startTime.getTime().getTime());

							toBeDeletedIndex.add(j);
						}
					}
				} // end loop to check END_TIME
			} // end check SCHEDULE_TIME, START_TIME, and END_TIME
		}

		// move calculation to AAPD
		for (FPDNode fpdNode : involvedNodesInACase) {
			for (FPDNode otherFPDNode : involvedNodesInACase) {
				// update frequency
				aapd.incrementFocusElementsCaseFrequency(mapFPDNodeToAAPDElement.get(fpdNode), mapFPDNodeToAAPDElement
						.get(otherFPDNode));

				// update measurements
				// check if the measurements between fpdNode and otherFPDNode is exist
				if (aapdElementRelativeMeasurements.containsKey(fpdNode)) {
					if (aapdElementRelativeMeasurements.get(fpdNode).containsKey(otherFPDNode)) {
						aapd.appendRelationTime(mapFPDNodeToAAPDElement.get(fpdNode), mapFPDNodeToAAPDElement
								.get(otherFPDNode), AAPD.BAR_START_TIME, aapdElementRelativeMeasurements.get(fpdNode)
								.get(otherFPDNode).get(ELEMENT_START_TIME));
						aapd.appendRelationTime(mapFPDNodeToAAPDElement.get(fpdNode), mapFPDNodeToAAPDElement
								.get(otherFPDNode), AAPD.BAR_WAITING_TIME, aapdElementRelativeMeasurements.get(fpdNode)
								.get(otherFPDNode).get(ELEMENT_WAITING_TIME));
						aapd.appendRelationTime(mapFPDNodeToAAPDElement.get(fpdNode), mapFPDNodeToAAPDElement
								.get(otherFPDNode), AAPD.BAR_SERVICE_TIME, aapdElementRelativeMeasurements.get(fpdNode)
								.get(otherFPDNode).get(ELEMENT_SERVICE_TIME));
						aapd.appendRelationTime(mapFPDNodeToAAPDElement.get(fpdNode), mapFPDNodeToAAPDElement
								.get(otherFPDNode), AAPD.BAR_INTERSECTION_TIME, aapdElementRelativeMeasurements.get(
								fpdNode).get(otherFPDNode).get(ELEMENT_INTERSECTION_TIME));
						aapd.appendRelationTime(mapFPDNodeToAAPDElement.get(fpdNode), mapFPDNodeToAAPDElement
								.get(otherFPDNode), AAPD.BAR_THROUGHPUT_TIME, aapdElementRelativeMeasurements.get(
								fpdNode).get(otherFPDNode).get(ELEMENT_THROUGHPUT_TIME));
					}
				}
			}
		}
	}

	/**
	 * update performance measurement for a single element in AAPD. Performance
	 * dimension is one of the followings : start time, waiting time, service
	 * time, intersection time, or throughput time
	 * 
	 * @param fpdBaseNode
	 * @param typeOfStats
	 * @param value
	 */
	private void updateSingleElementMeasurementNode(FPDNode fpdBaseNode, int typeOfStats, long value) {
		assert (value >= 0);
		// update in all involved node
		for (FPDNode node : involvedNodesInACase) {
			if (!aapdElementRelativeMeasurements.containsKey(node)) {
				Map<FPDNode, Map<Integer, List<Long>>> tempMap = new HashMap<FPDNode, Map<Integer, List<Long>>>();
				aapdElementRelativeMeasurements.put(node, tempMap);
			}

			// key already created
			if (aapdElementRelativeMeasurements.get(node).containsKey(fpdBaseNode)) {
				Map<Integer, List<Long>> tempMap = aapdElementRelativeMeasurements.get(node).get(fpdBaseNode);
				if (tempMap.containsKey(typeOfStats)) {
					tempMap.get(typeOfStats).add(value);
				} else {
					List<Long> newList = new LinkedList<Long>();
					newList.add(value);
					tempMap.put(typeOfStats, newList);
				}
			} else {
				Map<Integer, List<Long>> tempMap = new HashMap<Integer, List<Long>>();
				List<Long> newList = new LinkedList<Long>();
				newList.add(value);
				tempMap.put(typeOfStats, newList);
				aapdElementRelativeMeasurements.get(node).put(fpdBaseNode, tempMap);
			}
		}
	}

	/**
	 * append intersection time between firstNode and secondNode with 'time'
	 * value
	 * 
	 * @param firstNode
	 * @param secondNode
	 * @param time
	 */
	private void addIntersectionTime(FPDNode firstNode, FPDNode secondNode, long time) {
		assert (time >= 0);
		Map<FPDNode, Map<Integer, List<Long>>> tempMap = aapdElementRelativeMeasurements.get(firstNode);
		if (tempMap.containsKey(secondNode)) {
			Map<Integer, List<Long>> mapListLong = tempMap.get(secondNode);
			if (mapListLong.containsKey(ELEMENT_INTERSECTION_TIME)) {
				mapListLong.get(ELEMENT_INTERSECTION_TIME).add(time);
			} else {
				List<Long> listLong = new LinkedList<Long>();
				listLong.add(time);
				mapListLong.put(ELEMENT_INTERSECTION_TIME, listLong);
			}
		} else {
			// totally new encountered node
			List<Long> listIntersectionTime = new LinkedList<Long>();
			listIntersectionTime.add(time);

			Map<Integer, List<Long>> mapListLong = new HashMap<Integer, List<Long>>();
			mapListLong.put(ELEMENT_INTERSECTION_TIME, listIntersectionTime);

			tempMap.put(secondNode, mapListLong);
		}
	}

	/**
	 * @return the mapFPDAAPD
	 */
	public Map<FPDNode, AAPDElement> getMapFPDAAPD() {
		return mapFPDNodeToAAPDElement;
	}

	/**
	 * @param mapFPDAAPD
	 *            the mapFPDAAPD to set
	 */
	public void setMapFPDAAPD(Map<FPDNode, AAPDElement> mapFPDAAPD) {
		mapFPDNodeToAAPDElement = mapFPDAAPD;
	}

	/**
	 * return aapd
	 */
	public AAPD getAAPD() {
		return aapd;
	}

	/**
	 * return mapping between FPDNode to AAPDElement
	 */
	public Map<FPDNode, AAPDElement> getMapFPDNodeToAAPDElement() {
		return mapFPDNodeToAAPDElement;
	}

	/**
	 * calculate throughput time boundary
	 */
	public void finalizeAAPDBoundaryCalculation() {
		//List<Long> totalListTimeInLong = new LinkedList<Long>();
		List<Double> totalListTimeInDouble = new LinkedList<Double>();

		// calculate boundary 
		for (FPDNode node : aapdBarThroughputTimeBoundary.keySet()) {
			// calculate average for each node
			long sumVal = 0;
			for (long val : aapdBarThroughputTimeBoundary.get(node)) {
				sumVal += val;
			}
			totalListTimeInDouble.add((double) sumVal / aapdBarThroughputTimeBoundary.get(node).size());
			//totalListTimeInLong.addAll(this.aapdBarThroughputTimeBoundary.get(node));
		}

		// calculate boundary for each AAPDFocusElements
		Pair<Double, Double> boundaryPair = getBoundaries(totalListTimeInDouble);

		for (FPDNode node : aapdBarThroughputTimeBoundary.keySet()) {
			List<Long> listTimeInLong = aapdBarThroughputTimeBoundary.get(node);
			if (listTimeInLong != null) {
				if (listTimeInLong.size() > 0) {
					aapd.setBoundaries(mapFPDNodeToAAPDElement.get(node), boundaryPair);
					continue;
				}
			}
			Pair<Double, Double> dummyBoundaryPair = new Pair<Double, Double>(0.0, 0.0);
			aapd.setBoundaries(mapFPDNodeToAAPDElement.get(node), dummyBoundaryPair);
		}
	}

	/**
	 * Private method to obtain green/yellow/red color boundaries.
	 * 
	 * @param listTime
	 * @return
	 */
	private Pair<Double, Double> getBoundaries(List<Double> listTimeInLong) {
		/**
		 * BASED ON STD DEVIATION
		 */
		//		// calculate average and std deviation
		//		double average = 0.00;
		//		for (double time : listTimeInLong){
		//			average += time;
		//		}
		//		average = average / listTimeInLong.size();
		//		
		//		// calculate stdDev
		//		double stdDev = 0.00;
		//		for (double time : listTimeInLong){
		//			stdDev += ((time - average)*(time - average));
		//		}
		//		stdDev = Math.sqrt(stdDev / listTimeInLong.size());
		//		
		//		return new Pair<Double, Double>((average - stdDev), (average + stdDev));
		/**
		 * END OF BASED ON STD DEVIATION
		 */

		/**
		 * Based on K-Means clustering
		 */
		// convert list of long to list of double
		//		List<Double> listTime = new LinkedList<Double>();
		//		for (long longVal : listTimeInLong){
		//			listTime.add(new Double(longVal));
		//		}
		//		
		//		Pair<Double, Double> result;
		//		Collections.sort(listTime);
		//		
		//		// create initial centroids
		//		int counter = 0;
		//		List<Double> initialCentroids = new LinkedList<Double>();
		//		Double low = listTime.get(0); // lower initial centroid
		//		
		//		
		//		counter = listTime.size() / 2;
		//		Double middle = listTime.get(counter);
		//		while ((Double.compare(middle, low) == 0)&&(counter < listTime.size())){	// middle bound shouldn't be the same as lower bound
		//			middle = listTime.get(counter++); // medium initial centroid
		//		}
		//		
		//		// if all list is the same, or if there are less than 4 values, no need to do clustering
		//		if ((counter == listTime.size())||(listTime.size() < 3)){
		//			result = new Pair<Double, Double>(middle, middle);
		//		} else if (listTime.size() == 3){
		//			result = new Pair<Double, Double>(listTime.get(0), listTime.get(1));
		//		} else {
		//			// continue with upper initial centroid
		//			Double upper = listTime.get(listTime.size() - 1);
		//			if (Double.compare(upper, middle) == 0){	//only 2 values
		//				result = new Pair<Double, Double>(upper, upper);
		//			} else { // there are 3 different values
		//				initialCentroids.add(low);	
		//				initialCentroids.add(middle);
		//				initialCentroids.add(upper);
		//				
		//				// create empty clusters 
		//				List<IClusteredData<Double>> initialClusters = new LinkedList<IClusteredData<Double>>();
		//				for (int i=0; i < 3; i++){
		//					IClusteredData<Double> clusteredDouble = new DoubleDataCluster();
		//					initialClusters.add(clusteredDouble);
		//				}
		//				
		//				// use Kmeans to cluster
		//				KMeans<Double> kMeans = new KMeans<Double> (3, listTime);
		////				initialClusters = kMeans.computeKMeans(KMeans.UNLIMITEDITERATION, new DoubleDataClusterAvgPointCalculator(), initialClusters, initialCentroids);
		//				initialClusters = kMeans.computeKMeans(5, new DoubleDataClusterAvgPointCalculator(), initialClusters, initialCentroids);
		//				
		//				// get lower and upper bound
		//				List<Double> lowerBound = initialClusters.get(0).getElements();
		//				Collections.sort(lowerBound);
		//				
		//				List<Double> upperBound = initialClusters.get(2).getElements();
		//				Collections.sort(upperBound);
		//				
		//				if (Double.compare(lowerBound.get(lowerBound.size() - 1), upperBound.get(0)) != 0){
		//					result = new Pair<Double, Double>(lowerBound.get(lowerBound.size() - 1), upperBound.get(0));
		//				} else {
		//					if (Double.compare(lowerBound.get(0), upperBound.get(upperBound.size() - 1)) != 0){
		//						result = new Pair<Double, Double>(lowerBound.get(0), upperBound.get(upperBound.size() - 1));
		//					} else {
		//						result = new Pair<Double, Double>(0.00, upperBound.get(0));
		//					}
		//				}
		//				
		//			}
		//		}
		//		return result;
		//		
		/**
		 * End of K-Means Clustering
		 */

		/**
		 * Based on 50% precentage on average
		 */
		// calculate average and std deviation
		double average = 0.00;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (double time : listTimeInLong) {
			average += time;
			if (time > max) {
				max = time;
			}
			if (time < min) {
				min = time;
			}
		}
		average = average / listTimeInLong.size();

		// see which distance are closer
		double distance = 0.00;
		if ((average - min) > (max - average)) {
			distance = 0.5 * (max - average);
		} else {
			distance = 0.5 * (average - min);
		}

		// calculate stdDev
		return new Pair<Double, Double>((average - distance), (average + distance));
		/**
		 * End of based on precentage
		 */
	}

}
