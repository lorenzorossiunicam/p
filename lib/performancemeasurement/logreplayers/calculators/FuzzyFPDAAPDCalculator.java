/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.calculators;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.Pair;
import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDFactory;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.dataelements.FPDElementPerformanceMeasurementData;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDEdge;
import org.processmining.models.simpleprecedencediagram.SPDNode;
import org.processmining.plugins.log.transitionchecker.LifecycleTransitionChecker;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 9, 2009
 */
public class FuzzyFPDAAPDCalculator extends AbstractFPDAAPDCalculator {
	// internal data
	private int lookaheadValue = 4;

	/**
	 * SPECIALLY TO CALCULATE FPD
	 */
	// stores consecutive execution
	private final Map<Pair<FPDNode, Date>, Date> consecutiveNodes = new HashMap<Pair<FPDNode, Date>, Date>();
	private final Set<FPDNode> hotNodes = new HashSet<FPDNode>();

	private final Map<FPDNode, Integer> nodeFrequency = new HashMap<FPDNode, Integer>(); // to count frequency for each case
	private final Map<FPDNode, Set<String>> nodePerformer = new HashMap<FPDNode, Set<String>>(); // to count the performer of this node

	/**
	 * forbidden to use normal constructor
	 */
	@SuppressWarnings("unused")
	private FuzzyFPDAAPDCalculator() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param spd
	 * @param mapping
	 * @param lookaheadValue
	 * @param lifecycleTransitionChecker
	 */
	public FuzzyFPDAAPDCalculator(SPD spd, Map<SPDNode, Set<XEventClass>> mapping, int lookaheadValue,
			LifecycleTransitionChecker lifecycleTransitionChecker) {
		fpd = FPDFactory.newFPD("FPD of " + spd.getLabel());
		this.lookaheadValue = lookaheadValue;
		this.lifecycleTransitionChecker = lifecycleTransitionChecker;

		// mapping 
		mapSPDNodetoFPDNode = new HashMap<SPDNode, FPDNode>();
		mapSPDEdgetoFPDEdge = new HashMap<SPDEdge<? extends SPDNode, ? extends SPDNode>, FPDEdge<? extends FPDNode, ? extends FPDNode>>();

		// init performance measurement for node
		nodePerformance = new HashMap<FPDNode, List<List<Double>>>(spd.getNodes().size());

		for (SPDNode spdNode : spd.getNodes()) {
			FPDNode fpdNode = fpd.addNode(spdNode.getLabel());
			fpdNode.addAllNodeMapping(mapping.get(spdNode));

			mapSPDNodetoFPDNode.put(spdNode, fpdNode);

			List<List<Double>> performanceMeasurements = new LinkedList<List<Double>>();
			for (int i = 0; i < 3; i++) {
				List<Double> listTemp = new LinkedList<Double>();
				performanceMeasurements.add(listTemp);
			}
			nodePerformance.put(fpdNode, performanceMeasurements);

			nodeFrequency.put(fpdNode, 0);
			nodePerformer.put(fpdNode, new HashSet<String>());
		}

		// init edge performance
		edgePerformance = new HashMap<FPDEdge<? extends FPDNode, ? extends FPDNode>, List<Double>>(spd.getEdges()
				.size());
		for (SPDEdge<? extends SPDNode, ? extends SPDNode> spdEdge : spd.getEdges()) {
			FPDEdge<? extends FPDNode, ? extends FPDNode> fpdEdge = fpd.addArc(mapSPDNodetoFPDNode.get(spdEdge
					.getSource()), mapSPDNodetoFPDNode.get(spdEdge.getTarget()));
			mapSPDEdgetoFPDEdge.put(spdEdge, fpdEdge);

			List<Double> listTemp = new LinkedList<Double>();
			edgePerformance.put(fpdEdge, listTemp);
		}

		fpdNodeInstanceAAPDCalculator = new FPDNodeInstanceAAPDCalculator(fpd, this.lifecycleTransitionChecker);
		twoNodesPerformanceCalculator = new TwoNodesPerformanceCalculator(fpd);

	}

	/**
	 * update calculation base on a case in listFPDNode
	 */
	public void updateCalculation(List<FPDNode> listFPDNode, List<XEventClass> listEventClass,
			List<Integer> listEventTypes, List<Date> listTimestamp, List<String> listResources, int caseID) {
		// check 
		assert (listTimestamp.size() == listFPDNode.size());
		assert (listTimestamp.size() == listEventClass.size());

		// variable to record flow
		List<Pair<Pair<FPDNode, Date>, Set<Pair<FPDNode, Date>>>> flowList = new LinkedList<Pair<Pair<FPDNode, Date>, Set<Pair<FPDNode, Date>>>>();

		// reset
		hotNodes.clear();
		consecutiveNodes.clear();
		lastCaseCalculationValidity = true;

		// increment first FPDNode 
		listFPDNode.get(0).incrementInitializationFreq();

		// increment terminating frequency
		listFPDNode.get(listFPDNode.size() - 1).incrementTerminationFreq();

		// set the starttime of a case
		for (int i = 0; i < listTimestamp.size(); i++) {
			if (listTimestamp.get(i) != null) {
				fpdNodeInstanceAAPDCalculator.setCaseStartTime(listTimestamp.get(i));
				break;
			}
		}

		// to calculate freqCaseHasThisNode
		Set<FPDNode> calculatedNodes = new HashSet<FPDNode>();

		/**
		 * calculate and accumulate
		 */
		for (int index = 0; index < listFPDNode.size(); index++) {
			FPDNode currentNode = listFPDNode.get(index);

			if (currentNode != null) {
				Pair<FPDNode, Date> currentNodeInPair = new Pair<FPDNode, Date>(currentNode, listTimestamp.get(index));

				calculatedNodes.add(currentNode); // add nodes which has already been visited in this case

				// update ActivityInstanceBar of this node
				fpdNodeInstanceAAPDCalculator.addEventClass(listEventClass.get(index), listEventTypes.get(index),
						listTimestamp.get(index), currentNode, caseID);

				// increment activation frequency
				currentNode.incrementActivationFreq();

				// increase frequency of performer
				nodePerformer.get(currentNode).add(listResources.get(index));

				// check for consecutively executed nodes
				if (!consecutiveNodes.containsKey(currentNodeInPair)) { // this node is NOT a consecutively executed node
					// check if the flow already determined before
					List<Pair<FPDNode, Date>> listOfPredecessor = new LinkedList<Pair<FPDNode, Date>>(); // predecessor of this node. Is needed to determine semantic
					List<Integer> watchList = new LinkedList<Integer>(); // store indexes of nodes in flowList which probably need to be deleted

					Date closestPredTime = null; // latest time a predecessor node instance occurs
					for (int predIndex = 0; predIndex < flowList.size(); predIndex++) {
						if (flowList.get(predIndex).getSecond().contains(currentNodeInPair)) {
							// predecessor of this node is found
							Date predTime = flowList.get(predIndex).getFirst().getSecond();
							if (closestPredTime != null) {
								if (predTime != null) {
									if (predTime.after(closestPredTime)) {
										closestPredTime = predTime;
									}
								}
							} else {
								closestPredTime = predTime;
							}
							listOfPredecessor.add(flowList.get(predIndex).getFirst());
							flowList.get(predIndex).getSecond().remove(currentNodeInPair);
							watchList.add(predIndex);
						}
					}

					// check validity of this case according to process model
					// check if the predecessor is a member of hotNodes
					// only if no element of hotNodes is a predecessor of currentNode, the move is not valid. Hence, not valid according to process model
					if ((lastCaseCalculationValidity) && (index > 0)) { // first event doesn't have to be checked
						boolean hasIntersection = false;
						for (Pair<FPDNode, Date> pairPredNode : listOfPredecessor) {
							hasIntersection = hasIntersection || hotNodes.contains(pairPredNode.getFirst());
						}
						lastCaseCalculationValidity = lastCaseCalculationValidity && hasIntersection;
					}

					// update synchronization time and waiting time if it's possible
					if (closestPredTime != null) { // there is a predecessor with time information
						long timeOfClosestPredTime = closestPredTime.getTime();

						// update synchronization time of this node's predecessor
						for (Pair<FPDNode, Date> predNode : listOfPredecessor) {
							if (predNode.getSecond() != null) {
								nodePerformance.get(predNode.getFirst()).get(
										FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME).add(
										(double) (timeOfClosestPredTime - predNode.getSecond().getTime()));
							}
						}

						// update waiting time for current node
						if (currentNodeInPair.getSecond() != null) {
							nodePerformance.get(currentNode).get(FPDElementPerformanceMeasurementData.WAITINGTIME).add(
									(double) (currentNodeInPair.getSecond().getTime() - timeOfClosestPredTime));
						}

					} // else, the node has no predecessor with time information

					// update Join semantic
					Collection<FPDEdge<? extends FPDNode, ? extends FPDNode>> tempInEdges = fpd.getInEdges(currentNode);
					if ((tempInEdges != null) && (listOfPredecessor.size() > 0)) {
						if (tempInEdges.size() == listOfPredecessor.size()) {
							currentNode.incrementANDJOINrf();
						} else if (listOfPredecessor.size() > 1) {
							currentNode.incrementORJOINrf();
						} else if (listOfPredecessor.size() == 1) {
							currentNode.incrementXORJOINrf();
						}
					}// else, tempInEdges == null or no predecessor

					// delete all predecessor in flowList which doesn't have any successor
					int decrementor = 0;
					for (int removeIndex : watchList) {
						if (flowList.get(removeIndex - decrementor).getSecond().size() == 0) {
							flowList.remove(removeIndex - decrementor);
							decrementor++;
						}
					}

				}// else, this current node is a consecutively executed node

				/**
				 * Determine outgoing flows
				 */
				// list successor of currentNode, also check for violating move
				Set<FPDNode> setOfSuccessor = new HashSet<FPDNode>(); // needed to determine semantic
				for (FPDEdge<? extends FPDNode, ? extends FPDNode> tempOutEdge : fpd.getOutEdges(currentNode)) {
					// check if successor of the node is in hot Nodes, so that the arc from the successor to the node is considered to be violating arc
					if (hotNodes.contains(tempOutEdge.getTarget())) {
						// if there is no arc in the opposite direction and it's not an arc to itself 
						if ((fpd.getArc(tempOutEdge.getTarget(), tempOutEdge.getSource()) == null)
								&& (!tempOutEdge.getTarget().equals(currentNode))) {
							tempOutEdge.incrementViolatingFrequency();
						}
					}

					setOfSuccessor.add(tempOutEdge.getTarget());
				}

				// for nodes within lookahead value
				List<FPDNode> listOfSuccessors = new LinkedList<FPDNode>();
				List<Date> listOfSuccessorsDates = new LinkedList<Date>();
				List<Integer> indexOfSuccessors = new LinkedList<Integer>();
				int limiter = ((index + lookaheadValue + 1) > listFPDNode.size()) ? listFPDNode.size() : (index
						+ lookaheadValue + 1);

				successorIteration: for (int i = index + 1; i < limiter; i++) {
					// check if in the future, we will encounter the node itself
					FPDNode checkNode = listFPDNode.get(i);
					if (checkNode != null) {
						if (checkNode.equals(currentNode)) { // possibly from this node to itself
							if (fpd.getArc(currentNode, currentNode) != null) { // there is an arc to itself
								// is the node already visited?
								if (listOfSuccessors.contains(checkNode)) {
									// stop the loop until this, as path to the node already followed
									break successorIteration;
								} else {
									// the node hasn't been visited yet, so visit it
									listOfSuccessors.add(checkNode);
									listOfSuccessorsDates.add(listTimestamp.get(i));
									indexOfSuccessors.add(i);
									break successorIteration;
								}
							} else { // there is no arc to itself, but this node is revisited within lookahead value
								// is it a hot node?
								// if previously identified nodes in listOfSuccessors do not have any other predecessor than currentNode, currentNode is not executed twice 
								boolean goToItself = true;

								iterateSuccessors: for (int successorIndex : indexOfSuccessors) {
									for (int j = successorIndex - 1; j > index; j--) {
										if (fpd.getArc(listFPDNode.get(j), listFPDNode.get(successorIndex)) != null) {
											continue iterateSuccessors;
										}
									}
									// one of the successor don't have any other predecessor than currentNode
									goToItself = false;
									break iterateSuccessors;
								}

								if (goToItself) {
									// update consecutive nodes
									Pair<FPDNode, Date> hotNodeInPair = new Pair<FPDNode, Date>(checkNode,
											listTimestamp.get(i));
									if (!consecutiveNodes.containsKey(currentNodeInPair)) {
										consecutiveNodes.put(hotNodeInPair, currentNodeInPair.getSecond());
									} else { // the nodes is a sequence
										consecutiveNodes.put(hotNodeInPair, consecutiveNodes.get(currentNodeInPair));
										if (!currentNodeInPair.equals(hotNodeInPair)) {
											consecutiveNodes.remove(currentNodeInPair);
										}
									}

									listOfSuccessors.clear();
									listOfSuccessorsDates.clear();
									indexOfSuccessors.clear();
									break successorIteration;
								} else { // else, not a consecutive node
									break successorIteration;
								}
							} // end of checking of arc to itself
						} else { // check whether this other node is a successor
							if (setOfSuccessor.contains(checkNode) && !listOfSuccessors.contains(checkNode)) {
								// the node hasn't been visited yet, so visit it
								listOfSuccessors.add(checkNode);
								listOfSuccessorsDates.add(listTimestamp.get(i));
								indexOfSuccessors.add(i);
							}
						} // end of checking possibility to go to itself
					} // end of checkNode != null
				}

				assert (listOfSuccessors.size() == listOfSuccessorsDates.size());
				assert (indexOfSuccessors.size() == listOfSuccessorsDates.size());

				if (listOfSuccessors.size() > 0) {
					// update moving time arcs, flowList, and two nodes performance
					Set<Pair<FPDNode, Date>> setOfPair = new HashSet<Pair<FPDNode, Date>>();
					for (int i = 0; i < listOfSuccessors.size(); i++) {
						Pair<FPDNode, Date> newPair = new Pair<FPDNode, Date>(listOfSuccessors.get(i),
								listOfSuccessorsDates.get(i));
						setOfPair.add(newPair);
						// in case timestamp is not available
						if (currentNodeInPair.getSecond() != null) {
							edgePerformance.get(fpd.getArc(currentNode, listOfSuccessors.get(i))).add(
									(double) (listOfSuccessorsDates.get(i).getTime() - currentNodeInPair.getSecond()
											.getTime()));
						}
						fpd.getArc(currentNode, listOfSuccessors.get(i)).incrementFrequency();
					}
					flowList.add(new Pair<Pair<FPDNode, Date>, Set<Pair<FPDNode, Date>>>(currentNodeInPair, setOfPair));

					// update split flow
					if (setOfSuccessor.size() == listOfSuccessors.size()) {
						currentNode.incrementANDSPLITrf();
					} else if (listOfSuccessors.size() > 1) {
						currentNode.incrementORSPLITrf();
					} else if (listOfSuccessors.size() == 1) {
						currentNode.incrementXORSPLITrf();
					}

					// update consecutive nodes
					if (consecutiveNodes.containsKey(currentNodeInPair)) {
						if (consecutiveNodes.get(currentNodeInPair) != null) {
							nodePerformance.get(currentNode).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME)
									.add(
											(double) (listTimestamp.get(index).getTime() - consecutiveNodes.get(
													currentNodeInPair).getTime()));
						}
						consecutiveNodes.remove(currentNodeInPair);
					}

					// update activity instances
					int numOfBars = fpdNodeInstanceAAPDCalculator.finalizeNodeInstance(currentNode);

					// update also frequency with number of activity instances
					nodeFrequency.put(currentNode, nodeFrequency.get(currentNode) + numOfBars);

				} // else, tempOutEdges == null or no successors

				// handle the case if the control stays in the node 
				if (consecutiveNodes.containsKey(currentNodeInPair)) {
					nodePerformance.get(currentNode).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME).add(
							(double) listTimestamp.get(index).getTime()
									- consecutiveNodes.get(currentNodeInPair).getTime());
					consecutiveNodes.remove(currentNodeInPair);
				}

				// special handling for last index
				if (index == (listFPDNode.size() - 1)) {
					Map<FPDNode, Integer> mapToBeIncremented = fpdNodeInstanceAAPDCalculator.finalizeAllNodeInstance();
					for (FPDNode node : mapToBeIncremented.keySet()) {
						nodeFrequency.put(node, mapToBeIncremented.get(node));
					}

					if (consecutiveNodes.containsKey(currentNodeInPair)) {
						nodePerformance.get(currentNode).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME).add(
								(double) listTimestamp.get(index).getTime()
										- consecutiveNodes.get(currentNodeInPair).getTime());
						consecutiveNodes.remove(currentNodeInPair);
					}
				}

				// no matter what, always update hotNodes in order to identify validity of this node
				// also check validity of this case (if currentNode is a successor of a hotNode or a hotNode itself, the case is valid)
				hotNodes.add(currentNode);

				// remove predecessor of hotNodes whose all successors are hot nodes
				checkInEdge: for (FPDEdge<? extends FPDNode, ? extends FPDNode> inEdge : fpd.getInEdges(currentNode)) {
					if (hotNodes.contains(inEdge.getSource())) {
						// check if all successor of hotNodes already in hotNodes
						for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd
								.getOutEdges(inEdge.getSource())) {
							if (!hotNodes.contains(outEdge.getTarget())) {
								continue checkInEdge;
							}
						}
						// all successor of inEdge.getSource() is a member of hotNodes
						hotNodes.remove(inEdge.getSource());
					}
				}
			} // else (listFPDNode.get(index) == null)
		}// end of calculate and accumulate		

		// increment caseHasANodeFreq
		for (FPDNode node : calculatedNodes) {
			node.incrementFreqCaseHasThisNode();
		}

		// set number of performer for a node
		for (FPDNode node : nodePerformer.keySet()) {
			node.setNumberOfPerformer(nodePerformer.get(node).size());
		}

		// calculate two nodes performance
		twoNodesPerformanceCalculator.updateCalculation(listFPDNode, listTimestamp, getLastCaseCalculationValidity());
	}

	/**
	 * Finalize calculation after all cases are met
	 */
	public void finalizeCalculation() {
		int maxFreqEventClasses = 0;

		List<Double> totalWaitBoundaries = new LinkedList<Double>();
		List<Double> totalThroughputBoundaries = new LinkedList<Double>();
		List<Double> totalSyncBoundaries = new LinkedList<Double>();

		for (FPDNode node : fpd.getNodes()) {
			// average waiting, throughput, and synchronization time 
			node.setWaitingTime(calculateAverage(nodePerformance.get(node).get(
					FPDElementPerformanceMeasurementData.WAITINGTIME)));
			node.setAvgThroughput(calculateAverage(nodePerformance.get(node).get(
					FPDElementPerformanceMeasurementData.THROUGHPUTTIME)));
			node.setSynchronizationTime(calculateAverage(nodePerformance.get(node).get(
					FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME)));

			// set boundaries for waiting time
			if (nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.WAITINGTIME).size() > 0) {
				// calculate average for each node
				double sumVal = 0;
				for (double val : nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.WAITINGTIME)) {
					sumVal += val;
				}
				totalWaitBoundaries.add(sumVal
						/ nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.WAITINGTIME).size());
				//totalWaitBoundaries.addAll(nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.WAITINGTIME));
			}

			// set boundaries for throughput time
			if (nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME).size() > 0) {
				// calculate average for each node
				double sumVal = 0;
				for (double val : nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME)) {
					sumVal += val;
				}
				totalThroughputBoundaries.add(sumVal
						/ nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME).size());
				//totalThroughputBoundaries.addAll(nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME));

				// set also for AAPD
				fpdNodeInstanceAAPDCalculator.finalizeAAPDBoundaryCalculation();
			}

			// set boundaries for synchronization time
			if (nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME).size() > 0) {
				// calculate average for each node
				double sumVal = 0;
				for (double val : nodePerformance.get(node).get(
						FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME)) {
					sumVal += val;
				}
				totalSyncBoundaries.add(sumVal
						/ nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME)
								.size());
				//totalSyncBoundaries.addAll(nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME));
			}

			// set frequency for each event classes, also find MAXIMUM frequency value
			int totalFreqEventClasses = 0;
			List<Integer> freqEventClasses = new LinkedList<Integer>();
			for (XEventClass eventClass : node.getEventClasses()) {
				int tempTotal = fpdNodeInstanceAAPDCalculator.getNumInstanceBars(node, eventClass);
				totalFreqEventClasses += tempTotal;
				freqEventClasses.add(tempTotal);
			}
			node.setFreqEventClasses(freqEventClasses);
			if (totalFreqEventClasses > maxFreqEventClasses) {
				maxFreqEventClasses = totalFreqEventClasses;
			}

			// set num of case to node
			node.setNumberOfCases(getNumOfCases());
		}

		// calculation
		List<Double> waitBoundaries = getBoundaries(totalWaitBoundaries);
		List<Double> throughputBoundaries = getBoundaries(totalThroughputBoundaries);
		List<Double> syncBoundaries = getBoundaries(totalSyncBoundaries);

		for (FPDNode node : fpd.getNodes()) {
			// set boundaries for waiting time
			if (nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.WAITINGTIME).size() > 0) {
				node.setWaitingTimeLow(waitBoundaries.get(0));
				node.setWaitingTimeHigh(waitBoundaries.get(1));
			}

			// set boundaries for throughput time
			if (nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.THROUGHPUTTIME).size() > 0) {
				node.setThroughputTimeLow(throughputBoundaries.get(0));
				node.setThroughputTimeHigh(throughputBoundaries.get(1));

				// set also for AAPD
				fpdNodeInstanceAAPDCalculator.finalizeAAPDBoundaryCalculation();
			}

			// set boundaries for synchronization time
			if (nodePerformance.get(node).get(FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME).size() > 0) {
				node.setSynchronizationTimeLow(syncBoundaries.get(0));
				node.setSynchronizationTimeHigh(syncBoundaries.get(1));
			}
			// set frequency for each event classes, also find MAXIMUM frequency value
			int totalFreqEventClasses = 0;
			List<Integer> freqEventClasses = new LinkedList<Integer>();
			for (XEventClass eventClass : node.getEventClasses()) {
				int tempTotal = fpdNodeInstanceAAPDCalculator.getNumInstanceBars(node, eventClass);
				totalFreqEventClasses += tempTotal;
				freqEventClasses.add(tempTotal);
			}
			node.setFreqEventClasses(freqEventClasses);
			if (totalFreqEventClasses > maxFreqEventClasses) {
				maxFreqEventClasses = totalFreqEventClasses;
			}

			// set num of case to node
			node.setNumberOfCases(getNumOfCases());
		}
		// end of added

		for (FPDNode node : fpd.getNodes()) {
			node.setMaxFreqEventClass(maxFreqEventClasses);
			node.updateNodeSize();
		}

		int maxEdgeFreq = 0;
		List<Double> totalMoveBoundaries = new LinkedList<Double>();
		for (FPDEdge<? extends FPDNode, ? extends FPDNode> edge : fpd.getEdges()) {
			// set boundaries for edge's moving time
			if (edgePerformance.get(edge).size() > 0) {
				// calculate average for each edge
				double sumVal = 0;
				for (double val : edgePerformance.get(edge)) {
					sumVal += val;
				}
				totalMoveBoundaries.add(sumVal / edgePerformance.get(edge).size());
				//totalMoveBoundaries.addAll(edgePerformance.get(edge));
			}

			edge.setAvgMovingTime(calculateAverage(edgePerformance.get(edge)));

			// update maxEdge
			if (maxEdgeFreq < edge.getFrequency()) {
				maxEdgeFreq = edge.getFrequency();
			}
		}

		// calculate
		List<Double> moveBoundaries = getBoundaries(totalMoveBoundaries);

		for (FPDEdge<? extends FPDNode, ? extends FPDNode> edge : fpd.getEdges()) {
			edge.setMaxEdgeFreq(maxEdgeFreq);

			edge.setMovingTimeBoundaryLow(moveBoundaries.get(0));
			edge.setMovingTimeBoundaryHigh(moveBoundaries.get(1));

			// set interface
			edge.updateEdgeInterface();
		}

		// set AAPD
		aapd = fpdNodeInstanceAAPDCalculator.getAAPD();
		mapFPDNodeToAAPDFocusElements = fpdNodeInstanceAAPDCalculator.getMapFPDNodeToAAPDElement();
	}

	/**
	 * Private method to obtain green/yellow/red color boundaries.
	 * 
	 * @param listTime
	 * @return
	 */
	private List<Double> getBoundaries(List<Double> listTime) {
		/**
		 * BASED ON STD DEVIATION
		 */
		//		// calculate average and std deviation
		//		double average = 0.00;
		//		for (double time : listTime){
		//			average += time;
		//		}
		//		average = average / listTime.size();
		//		
		//		// calculate stdDev
		//		double stdDev = 0.00;
		//		for (double time : listTime){
		//			stdDev += ((time - average)*(time - average));
		//		}
		//		stdDev = Math.sqrt(stdDev / listTime.size());
		//		
		//		List<Double> result = new LinkedList<Double>();
		//		result.add(average - stdDev);
		//		result.add(average + stdDev);
		//		return result;
		/**
		 * END OF BASED ON STD DEVIATION
		 */

		/**
		 * Based on K-Means clustering
		 */
		//		List<Double> result = new LinkedList<Double>();
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
		//			result.add(middle);
		//			result.add(middle);
		//		} else if (listTime.size() == 3){
		//			result.add(listTime.get(0));
		//			result.add(listTime.get(1));
		//		} else {
		//			// continue with upper initial centroid
		//			Double upper = listTime.get(listTime.size() - 1);
		//			if (Double.compare(upper, middle) == 0){	//only 2 values
		//				result.add(upper);
		//				result.add(upper);
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
		//				// initialClusters = kMeans.computeKMeans(KMeans.UNLIMITEDITERATION, new DoubleDataClusterAvgPointCalculator(), initialClusters, initialCentroids);
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
		//					result.add(lowerBound.get(lowerBound.size() - 1));	// lower bound
		//					result.add(upperBound.get(0)); // upper bound
		//				} else {
		//					if (Double.compare(lowerBound.get(0), upperBound.get(upperBound.size() - 1)) != 0){
		//						result.add(lowerBound.get(0));	// lower bound
		//						result.add(upperBound.get(upperBound.size() - 1)); // upper bound
		//					} else {
		//						result.add(0.00);	// lower bound
		//						result.add(upperBound.get(0)); // upper bound
		//					}
		//				}
		//				
		//			}
		//		}
		//
		//		return result;
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
		for (double time : listTime) {
			average += time;
			if (time > max) {
				max = time;
			}
			if (time < min) {
				min = time;
			}
		}
		average = average / listTime.size();

		// see which distance are closer
		double distance = 0.00;
		if ((average - min) > (max - average)) {
			distance = 0.5 * (max - average);
		} else {
			distance = 0.5 * (average - min);
		}

		// calculate stdDev
		List<Double> result = new LinkedList<Double>();
		result.add(average - distance);
		result.add(average + distance);
		return result;
		/**
		 * End of based on precentage
		 */
	}

	/**
	 * General method to calculate average value of a list of Double values
	 * 
	 * @param values
	 * @return
	 */
	public double calculateAverage(List<Double> values) {
		Double accumulatedVal = 0.0;
		for (Double value : values) {
			if (value != null) {
				accumulatedVal += value;
			}
		}
		if ((accumulatedVal >= 0) && (values.size() > 0)) {
			return (accumulatedVal / values.size());
		} else {
			return IFPDCalculator.UNIDENTIFIED_AVG;
		}
	}

}
