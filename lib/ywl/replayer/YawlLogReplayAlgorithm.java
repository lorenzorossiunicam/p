package org.processmining.plugins.ywl.replayer;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.collections15.map.HashedMap;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.utils.Node;
import org.processmining.models.graphbased.directed.yawl.animation.GraphAnimation;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPD;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPDCondition;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPDEdge;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPDFactory;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPDNode;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPDTask;
import org.processmining.models.graphbased.directed.yawlperformancediagram.elements.YPDConditionElement;
import org.processmining.models.graphbased.directed.yawlperformancediagram.elements.YPDTaskElement;
import org.processmining.plugins.ywl.replayer.visualization.PerformanceInfo;
import org.processmining.plugins.ywl.replayer.visualization.SequenceEventsConformanceInfo;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.CompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.Condition;
import org.yawlfoundation.yawl.editor.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.elements.model.JoinDecorator;
import org.yawlfoundation.yawl.editor.elements.model.MultipleAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.MultipleCompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.elements.model.SplitDecorator;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 31, 2010
*/
public class YawlLogReplayAlgorithm implements IYwlLogReplayAlgorithm {

	public String toString(){
		return "A* heuristic log replay";
	}

	/**
	 * This replay process one log at once and return these variables in array of object. 
	 * 0 - YPD
	 * 1 - YawlConformanceResult
	 * 2 - GraphAnimation
	 */
	@SuppressWarnings("unchecked")
	public Object[] replayLog(PluginContext context, NetGraph yawl, XLog log, Object[] vertices,
			HashSet<YAWLFlowRelation> flows, Collection<Pair<YAWLVertex, XEventClass>> mapping,
			int misTokenWeight, int remTokenWeight, int heurDistanceWeight, boolean cancellationOption) {
		// set progress bar
		final Progress progress = context.getProgress();
		progress.setMinimum(0);
		progress.setMaximum(log.size());
		progress.setIndeterminate(false);
		
		// get event class for this event
		XLogInfo summary = XLogInfoFactory.createLogInfo(log);
		XEventClasses classes = summary.getEventClasses();
		
		// create reference hashtable from yawl to event, and the other way around
		Map<YAWLVertex, XEventClass> yawlToEvent = new HashedMap<YAWLVertex, XEventClass>(vertices.length);
		Map<XEventClass, List<YAWLVertex>> eventToYawl = new HashedMap<XEventClass, List<YAWLVertex>>(classes.size());
		for (Pair<YAWLVertex, XEventClass> pair : mapping){
			// only include nodes that are mapped to activity, nodes that are not mapped to any activity are ignored
			if (pair.getSecond() != null){
				yawlToEvent.put(pair.getFirst(), pair.getSecond());
				if (eventToYawl.containsKey(pair.getSecond())){ // one event can be mapped to more than one yawl node
					List<YAWLVertex> temp = eventToYawl.get(pair.getSecond());
					temp.add(pair.getFirst());
				} else {
					List<YAWLVertex> temp = new LinkedList<YAWLVertex>();
					temp.add(pair.getFirst());
					eventToYawl.put(pair.getSecond(), temp);
				}
			}
		}
		
		/**
		 * create YPD and its mapping to yawl net
		 */
		YPD ypd = YPDFactory.newYPD("YPD of " + yawl.getName());
		Map<YAWLVertex, YPDNode> mapYawlNodeToYPDNode = new HashMap<YAWLVertex, YPDNode>();
		Map<YAWLFlowRelation, YPDEdge<? extends YPDNode, ? extends YPDNode>> 
			mapYawlFlowToYPDEdge = new HashMap<YAWLFlowRelation, YPDEdge<? extends YPDNode,? extends YPDNode>>();

		// create nodes
		int totalNumOfCases = summary.getNumberOfTraces();
		for (Object yawlNode : vertices){
			YPDNode ypdNode = null;
			if (yawlNode instanceof InputCondition) {
				if (((YAWLVertex) yawlNode).getLabel() == null) {
					ypdNode = ypd.addInputCondition(((YAWLVertex) yawlNode).getEngineId());
				} else {
					ypdNode = ypd.addInputCondition(((YAWLVertex) yawlNode).getLabel());
				}
				mapYawlNodeToYPDNode.put((YAWLVertex) yawlNode, ypdNode);
			}
			if (yawlNode instanceof OutputCondition) {
				if (((YAWLVertex) yawlNode).getLabel() == null) {
					ypdNode = ypd.addOutputCondition(((YAWLVertex) yawlNode).getEngineId());
				} else {
					ypdNode = ypd.addOutputCondition(((YAWLVertex) yawlNode).getLabel());
				}
				mapYawlNodeToYPDNode.put((YAWLVertex) yawlNode, ypdNode);
			}
			if (yawlNode instanceof Condition) {
				if (((YAWLVertex) yawlNode).getLabel() == null) {
					ypdNode = ypd.addCondition(((YAWLVertex) yawlNode).getEngineId());
				} else {
					ypdNode = ypd.addCondition(((YAWLVertex) yawlNode).getLabel());
				}
				mapYawlNodeToYPDNode.put((YAWLVertex) yawlNode, ypdNode);
			}
			if (!(yawlNode instanceof Condition || yawlNode instanceof InputCondition ||
				  yawlNode instanceof OutputCondition || yawlNode instanceof CompositeTask ||
				  yawlNode instanceof MultipleCompositeTask)) {
				if (((YAWLVertex) yawlNode).getLabel() == null) {
					ypdNode = ypd.addTask(((YAWLVertex) yawlNode).getEngineId());
				} else {
					ypdNode = ypd.addTask(((YAWLVertex) yawlNode).getLabel());
				}
				((YPDTask) ypdNode).setNumberOfCases(totalNumOfCases);
				JoinDecorator joinDecorator = null;
				SplitDecorator splitDecorator = null;
				if (yawlNode instanceof AtomicTask) {
					joinDecorator = ((AtomicTask) yawlNode).getJoinDecorator();
					splitDecorator = ((AtomicTask) yawlNode).getSplitDecorator();
				}
				if (yawlNode instanceof MultipleAtomicTask) {
					joinDecorator = ((MultipleAtomicTask) yawlNode).getJoinDecorator();
					splitDecorator = ((MultipleAtomicTask) yawlNode).getSplitDecorator();
				}
				
				if (joinDecorator == null || joinDecorator.getType() == JoinDecorator.AND_TYPE) {
					((YPDTask) ypdNode).incrementANDJOINrf();
				} else {
					if (joinDecorator.getType() == JoinDecorator.OR_TYPE) {
						((YPDTask) ypdNode).incrementORJOINrf();
					}
					if (joinDecorator.getType() == JoinDecorator.XOR_TYPE) {
						((YPDTask) ypdNode).incrementXORJOINrf();
					}
				}
				if (splitDecorator == null || splitDecorator.getType() == SplitDecorator.AND_TYPE) {
					((YPDTask) ypdNode).incrementANDSPLITrf();
				} else {
					if (splitDecorator.getType() == SplitDecorator.OR_TYPE) {
						((YPDTask) ypdNode).incrementORSPLITrf();
					}
					if (splitDecorator.getType() == SplitDecorator.XOR_TYPE) {
						((YPDTask) ypdNode).incrementXORSPLITrf();
					}
				}
				mapYawlNodeToYPDNode.put((YAWLVertex) yawlNode, ypdNode);
				
				// cancellation regions
				if (((YAWLTask) yawlNode).getCancellationSet().getSetMembers().size() > 0 &&
					(cancellationOption)) {
					((YPDTask) ypdNode).setHasCancellationSet(true);
				}
			}			
			
		}
		
		// create edges
		for (YAWLFlowRelation yawlFlow : flows){
			if (yawlFlow.getSource() instanceof AtomicTask) {
				YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdge = 
					ypd.addArc(mapYawlNodeToYPDNode.get(yawlFlow.getSource()), mapYawlNodeToYPDNode.get(yawlFlow.getTargetVertex()));	
				mapYawlFlowToYPDEdge.put(yawlFlow, ypdEdge);
			} else if (!(yawlFlow.getSourceVertex() instanceof CompositeTask ||
				yawlFlow.getSourceVertex() instanceof MultipleCompositeTask)) {
				YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdge = 
					ypd.addArc(mapYawlNodeToYPDNode.get(yawlFlow.getSourceVertex()), mapYawlNodeToYPDNode.get(yawlFlow.getTargetVertex()));	
				mapYawlFlowToYPDEdge.put(yawlFlow, ypdEdge);
			}
		}
		
		/**
		 * create graph animation
		 */
		GraphAnimation graphAnimation = new GraphAnimation();
		
		/**
		 * case accumulator variables
		 */
		// missing, remaining, and heuristicDistance accumulator
		List<Bag<YAWLFlowRelation>> missingTokens = 
			new LinkedList<Bag<YAWLFlowRelation>>();
		List<Bag<YAWLFlowRelation>> remainingTokens = 
			new LinkedList<Bag<YAWLFlowRelation>>();
		List<Integer> heurDistance = new LinkedList<Integer>();
		
		// conformance accumulator
		SequenceEventsConformanceInfo confInfo = new SequenceEventsConformanceInfo();
		PerformanceInfo performanceInfo = new PerformanceInfo();
		Set<XEventClass> setUnreplayableEventClasses = new HashSet<XEventClass>();
		Set<XEventClass> setUncoveredEventClasses = new HashSet<XEventClass>();
		Set<XEventClass> setAllEventClasses = new HashSet<XEventClass>();
		int numNonReplayableEvents = 0;

		// other accumulator
		Map<YAWLVertex, Set<Integer>> setIdentifiedPair = new HashMap<YAWLVertex, Set<Integer>>(); // nodes and set of indexes that becomes their next nodes
		Map<Integer, YAWLVertex> setIdentifiedPairReversed = new HashMap<Integer, YAWLVertex>();
		Map<Integer, Set<Integer>> setIdentifiedPredPair = new HashMap<Integer, Set<Integer>>(); // index of node in trace and index of YAWLVertex that synchronize with it
		Set<YAWLVertex> visitedNodes = new HashSet<YAWLVertex>();
		
		// state to be expanded
		// TODO what to do with this??
		List<Node<YawlState>> statesToBeExpanded = new LinkedList<Node<YawlState>>();

		// initial state
		Node<YawlState> root = new Node<YawlState>();
		root.setParent(null);

		// instantiate the calculator class
		YawlConformanceCalculator calculator = new YawlConformanceCalculator();
		YawlPerformanceCalculator calculatorPerf = new YawlPerformanceCalculator();
		
		// replay each trace independently using A* algorithm approach
		for (XTrace trace : log){
			
			Date startNet = null;
			Date endNet = null;
			
			// initiate accumulator variables
			statesToBeExpanded.add(root);
			missingTokens.add(new HashBag<YAWLFlowRelation>());
			remainingTokens.add(new HashBag<YAWLFlowRelation>());
						
			// replay per-trace. 
			List<XEventClass> listEventClass = new LinkedList<XEventClass>();
			List<Date> listTimeStamp = new LinkedList<Date>();

			// create sequence of activity representation of the current trace (while also calculate timestamps)
			// check if it has timestamp as well. In the same time, check the real heurDistance, as some event may not be mapped to any node
			int maxHeurDistance = 0;
			XTimeExtension xTimeExtension = XTimeExtension.instance();
			if (xTimeExtension.extractTimestamp(trace.get(0)) != null){
				// there is timestamp information, use real timestamp
				
				// for each event, if the event is mapped, store the event in listEventClass and the corresponding
				// timestamp in listTimeStamp
				
				for (XEvent event : trace) {
					// if no yawl node is mapped to this event, ignore the event
					XEventClass eventClass = classes.getClassOf(event); 
					
					setAllEventClasses.add(eventClass);
					if (eventToYawl.get(eventClass) != null ||
						((eventClass.toString().split("\\+")[1]).equals("complete") ||
						(eventClass.toString().split("\\+")[1]).equals("start"))){
						listEventClass.add(eventClass);
						listTimeStamp.add(xTimeExtension.extractTimestamp(event));
						maxHeurDistance++;
					} else {
						setUncoveredEventClasses.add(eventClass);
					}
				}
			} else {
				// use dummy timestamp with seed
				for (XEvent event : trace) {
					// if no yawl node is mapped to this event, ignore the event
					XEventClass eventClass = classes.getClassOf(event);
					setAllEventClasses.add(eventClass);
					if (eventToYawl.get(eventClass) != null){
						listEventClass.add(eventClass);
						listTimeStamp.add(new Date(maxHeurDistance));
						maxHeurDistance++;
					} else {
						setUncoveredEventClasses.add(eventClass);
					}
				}
			}
			
			if (listTimeStamp.size() >= 1) {
				startNet = listTimeStamp.get(0);
			}
			
			// cancellation calculations
			// First: determine which tasks are cancelled by the user
			List<XEventClass> listEventClassCancellation = new LinkedList<XEventClass>();
			List<Date> listTimeStampCancellation = new LinkedList<Date>();
			for (XEvent event : trace) {
				boolean contained = false;
				XEventClass eventClass = classes.getClassOf(event);
				String eventType = eventClass.toString().split("\\+")[1];
				if (eventType.equals("pi_abort")) {
					for (XEventClass cancelledEvent : listEventClassCancellation) {
						if (cancelledEvent == eventClass) {
							contained = true;
						}
					}
					if (!contained) {
						listEventClassCancellation.add(eventClass);
						listTimeStampCancellation.add(xTimeExtension.extractTimestamp(event));
					}
				}
			}
			
			// It is known which tasks are canceled by the user, find the related YPDTask and change the
			// cancellation metrics.
			if (cancellationOption) {
			for (XEventClass cListEvent : listEventClassCancellation) {
				String cTaskName = cListEvent.toString().split("\\+")[0];
//				String cEventType = cListEvent.toString().split("\\+")[1];
				boolean changed = false;
				for (XEventClass listEvent : listEventClass) {
					String taskName = listEvent.toString().split("\\+")[0];
					String eventType = listEvent.toString().split("\\+")[1];
					if (cTaskName.equals(taskName) && eventType.equals("start")) {
						// found corresponding task, find corresponding YPDNode
						for (YAWLVertex yawlVertex : eventToYawl.get(listEvent)) {
							YPDTask cancelledTask = (YPDTask) mapYawlNodeToYPDNode.get(yawlVertex);
							cancelledTask.incrementCancelledByUser();
							changed = true;
						}					
					}		
				}
				if (!changed) {
					// task is never started, increment cancellation frequency of incoming edge (or condition?)
					// if task has multiple incoming conditions, how to determine which was one is cancelled???
				}
			}}
			
			
			// continue other calculations
			
			// set heuristic distance
			heurDistance.add(maxHeurDistance);
			
			// update conformance info
			confInfo.setNumOfEvents(trace.size());
			confInfo.setNumOfUncoveredEvents(trace.size() - maxHeurDistance);
			confInfo.setNumOfUncoveredActivities(setUncoveredEventClasses.size());
			
			// this node is a part of a whole state space
			Object[] replayResult = replayTrace(yawl, listEventClass, misTokenWeight, remTokenWeight, heurDistanceWeight, 
					eventToYawl, root, statesToBeExpanded, missingTokens, remainingTokens, heurDistance, flows,
					mapYawlFlowToYPDEdge, ypd);
			
			Node<YawlState> node = null;
			YawlState[] additionalStates = null;
			
			if (replayResult != null) {
				node = (Node<YawlState>) replayResult[0];
				additionalStates = (YawlState[]) replayResult[1];
			}
			
			if (node != null){ 
				
			List<Pair<YAWLVertex, Pair<Set<YAWLVertex>, Set<YAWLVertex>>>> listPair = 
				new LinkedList<Pair<YAWLVertex,Pair<Set<YAWLVertex>,Set<YAWLVertex>>>>();  
				while (node.getData() != null){
					YawlState solutionState = node.getData();
					Pair<Set<YAWLVertex>, Set<YAWLVertex>> pairPredNextNodes = new Pair<Set<YAWLVertex>, Set<YAWLVertex>>(solutionState.getPrevNodes(), solutionState.getNextNodes());
					listPair.add(0, new Pair<YAWLVertex, Pair<Set<YAWLVertex>, Set<YAWLVertex>>>(solutionState.getYawlNode(), pairPredNextNodes)); 
					node = node.getParent();
				}
				
				for (int i=0; i < listPair.size(); i++) {
					YAWLVertex currentVertex = listPair.get(i).getFirst();
					boolean contained = false;
					YAWLVertex[] visitedVertices = new YAWLVertex[ypd.getNodes().size()];
					int l = 0;
					Set<YAWLFlowRelation> incomingFlows = getInFlows(currentVertex, flows);
					Set<YAWLVertex> inVertices = new HashSet<YAWLVertex>(getInFlows(currentVertex, flows).size());
					if (!(currentVertex instanceof InputCondition)) {
						for (int j=0; j < listPair.size(); j++) {
							for (YAWLFlowRelation inFlow : incomingFlows) {
								inVertices.add(inFlow.getSourceVertex());
							}
							for (YAWLVertex inVertex : inVertices) {
								if (inVertex == listPair.get(j).getFirst()) {
									contained = true;
									visitedVertices[l] = inVertex;
									l++;
								}
							}
						}
					} else {
						contained = true;
					}
					if (!contained) {
						for (YAWLVertex inVertex : inVertices) {
							for (int k=0; k < visitedVertices.length; k++) {
								if (inVertex == visitedVertices[k]) {
									inVertices.remove(inVertex);
								}
							}
						}
						for (YAWLVertex inVertex : inVertices) {
							for (int m = 0; m < additionalStates.length ; m++) {
								if (additionalStates[m] != null) {
									if (additionalStates[m].getYawlNode() == inVertex) {	
												for (int y = 0; y < listPair.size(); y++) {
													if (listPair.get(y).getFirst() == currentVertex) {
														Pair<Set<YAWLVertex>, Set<YAWLVertex>> pairPredNextNodes =
															new Pair<Set<YAWLVertex>, Set<YAWLVertex>>(additionalStates[m].getPrevNodes(), additionalStates[m].getNextNodes());
														listPair.add(y, new Pair<YAWLVertex, Pair<Set<YAWLVertex>, Set<YAWLVertex>>>(additionalStates[m].getYawlNode(), pairPredNextNodes));
														break;
													}
												}
									}
								}
							}
						}
					}
					
				}
				
				// execute procedure to detect cancellation events (ate_abort)
				if (cancellationOption) {
					traceCancellations(listPair, ypd, listEventClass, trace, classes, xTimeExtension, eventToYawl,
							mapYawlNodeToYPDNode, flows, vertices);
				}

	
				// iterate from the start
				int prevI = -2;
				Map<YAWLVertex, Integer> extraIncrementsEdges = new HashMap<YAWLVertex, Integer>();	
				for (int i=0; i < listPair.size(); i++){
					if (i < listTimeStamp.size()) {
										
					// getcurrent node
					YAWLVertex curYawlNode = listPair.get(i).getFirst();
					Pair<Set<YAWLVertex>, Set<YAWLVertex>> pairPrevAndSucc = listPair.get(i).getSecond(); 
					
					double[] curNodeThroughPutTimes = new double[listPair.size()];
					double[] curEdgeThroughPutTimes = new double[flows.size()];
					
					
					// update visited nodes
					// only add a task to visited nodes if it has a complete event
					if (!(curYawlNode instanceof Condition) && !(curYawlNode instanceof InputCondition) &&
						!(curYawlNode instanceof OutputCondition)) {
							boolean completeEvent = false;
							String currentTask = listEventClass.get(i).toString().split("\\+")[0];
							String eventType = listEventClass.get(i).toString().split("\\+")[1];
							if (eventType.equals("complete")) {
								visitedNodes.add(curYawlNode);
							} else {
								for (int m = i+1; m< listEventClass.size(); m++) {
									if ((currentTask.equals(listEventClass.get(m).toString().split("\\+")[0])) &&
										(listEventClass.get(m).toString().split("\\+")[1]).equals("complete")) {
										completeEvent = true;
									}
								}
							}
							if (completeEvent) {
								visitedNodes.add(curYawlNode);
							}
					} else {
						visitedNodes.add(curYawlNode);
					}
					
					//// update fpd node
					YPDNode curYPDNode = mapYawlNodeToYPDNode.get(curYawlNode); 
					// only increment the YPDNode's frequency once
					boolean stop = false;
					if (curYPDNode instanceof YPDTask) {
						if (pairPrevAndSucc.getSecond() != null) {
						for (YAWLVertex nextYawlNode : pairPrevAndSucc.getSecond()){
							if (!stop) {
								for (int j = i+1; j < listPair.size(); j++){
									if ((listPair.get(j).getFirst().equals(nextYawlNode)) && 
									(listPair.get(j).getSecond().getFirst().contains(curYawlNode))){
										((YPDTask) curYPDNode).incrementActivationFreq();
										stop = true;
										break;
									}
								}
							}		
						}
						}
					}
					
					//// update graphAnimation for node
					if (!(curYawlNode instanceof OutputCondition)) {
						graphAnimation.addNodeKeyFrame(curYawlNode, listTimeStamp.get(i));
					}
					
					//// update SPLIT semantic for this node
					Set<YAWLVertex> executedNextNode = new HashSet<YAWLVertex>();
					
					// find pair for the output of this node
					if (pairPrevAndSucc.getSecond() != null &&
						prevI != i - 1){
						nodeIteration : for (YAWLVertex nextYawlNode : pairPrevAndSucc.getSecond()){
							
							if ((curYawlNode instanceof Condition) &&
								(listPair.get(i-1).getFirst() instanceof Condition) &&
								(prevI != i - 1)) {
								for (YAWLVertex previousYawlNode : pairPrevAndSucc.getFirst()) {
									for (int w = 0; w < listPair.size(); w++) {
										if (listPair.get(w).getFirst() == previousYawlNode) {
											listEventClass.add(i, listEventClass.get(w+1));
										    listTimeStamp.add(i, listTimeStamp.get(w+1));
										    break;
									    }
									}
									
								}
								i = i -1;
								prevI = i;

							}
							
							// find a successor of the element in the list that has curYawlNode as it's input
							for (int j = i+1; j < listPair.size(); j++){
								Date startDate = null;
								Date endDate = null;
								Date startDateEdge = null;
								Date endDateEdge = null;
								double diffEdge = 0;
								double diff = 0;
						
								// check if the node is the same as nextNode and has curYawlNode as one of its input
								if ((listPair.get(j).getFirst().equals(nextYawlNode)) && 
										(listPair.get(j).getSecond().getFirst().contains(curYawlNode))){
									// check if there is no other same node that 'pairs' with this one
																		
									if (setIdentifiedPair.containsKey(curYawlNode)){
										if (!setIdentifiedPair.get(curYawlNode).contains(j)){
											// all requirement is satisfied
											// update setIdentifiedPair
											setIdentifiedPair.get(curYawlNode).add(j);
											
											// update setIdentifiedPredPair (for identifying JOIN construct)
											if (setIdentifiedPredPair.containsKey(j)){
												// update
												setIdentifiedPredPair.get(j).add(i);
											} else {
												// put new
												Set<Integer> newIntegerSet = new HashSet<Integer>();
												newIntegerSet.add(i);
												setIdentifiedPredPair.put(j, newIntegerSet);
											}
											
											// update value of fpd edge
											YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdge = ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
//											ypdEdge.incrementFrequency();
											
											YAWLVertex prevVertex = null;
											for (YAWLVertex vertex: pairPrevAndSucc.getFirst()) {
												prevVertex = vertex;
											}
											
											// also increment frequency of YPDEdge from Task to Condition (moving time is always 'zero')
											if (curYPDNode instanceof YPDConditionElement) {
												YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdgePrev = 
													ypd.getArc(mapYawlNodeToYPDNode.get(prevVertex), curYPDNode);
												ypdEdgePrev.incrementFrequency();
											}
											
											if (!(nextYawlNode instanceof Condition) && !(nextYawlNode instanceof OutputCondition)) {
												// calculate edge throughput info	
																				
												startDateEdge = listTimeStamp.get(j);
												// get timestamp of start event of next node
												XEventClass eventClass = yawlToEvent.get(nextYawlNode);
												
												int index = listEventClass.indexOf(eventClass);
												if ((mapYawlNodeToYPDNode.get(nextYawlNode) instanceof YPDTaskElement) &&
												    (curYPDNode instanceof YPDTaskElement)) {
						//							ypdEdge.incrementFrequency();
												}
												endDateEdge = listTimeStamp.get(index);
												
												
												// calculate difference between dates in seconds									
												diffEdge = (endDateEdge.getTime() - startDateEdge.getTime())/1000;
												
												// add diff to throughput times array										
												curEdgeThroughPutTimes[i] = diffEdge;
																											
												if (ypdEdge.getMovingTimeBoundaryLow() > diffEdge) {
													ypdEdge.setMovingTimeBoundaryLow(diffEdge);
												}
												if (ypdEdge.getMovingTimeBoundaryHigh() < diffEdge) {
													ypdEdge.setMovingTimeBoundaryHigh(diffEdge);
												}
												
												long throughputSumEdge = 0;		
												int c = 0;
												for (int k=0; k < curEdgeThroughPutTimes.length; k++) {
													if (curEdgeThroughPutTimes[k] != 0) {
														c = c +1;
													}
													throughputSumEdge += curEdgeThroughPutTimes[k];
												}
												
												if (curYPDNode instanceof YPDTaskElement &&
													(mapYawlNodeToYPDNode.get(nextYawlNode) instanceof YPDTaskElement)) {
													ypdEdge.incrementFrequency();
												}
														
												if (c != 0 && (throughputSumEdge != 0)) {
													ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
													ypdEdge.setAvgMovingTime(ypdEdge.getAvgMovingTime() + (throughputSumEdge/c));
												}
											}
											
											if ((listEventClass.get(i).toString().split("\\+")[1]).equals("start") &&
													(listEventClass.get(j).toString().split("\\+")[1]).equals("complete")) {
												
												// Calculate throughput for node											
												startDate = listTimeStamp.get(i);
												endDate = listTimeStamp.get(j);
												
												// calculate difference between dates in seconds								
												diff = (endDate.getTime() - startDate.getTime())/1000;
												
												// add diff to throughput times array											
												curNodeThroughPutTimes[i] = diff;
												
												if (curYPDNode instanceof YPDTask) {																			
													if (((YPDTask) curYPDNode).getThroughputTimeLow() > diff) {
														((YPDTask) curYPDNode).setThroughputTimeLow(diff);
													}
													if (((YPDTask) curYPDNode).getThroughputTimeHigh() < diff) {
														((YPDTask) curYPDNode).setThroughputTimeHigh(diff);
													}
												}
											}
											
											// update graphAnimation for edge
											Object[] cells = yawl.getRoots();
											for (Object cell : cells) {
												if (cell instanceof YAWLFlowRelation) {
													if ((((YAWLFlowRelation) cell).getSourceVertex() == curYawlNode) &&
														(((YAWLFlowRelation) cell).getTargetVertex() == curYawlNode)) {
														graphAnimation.addArcKeyFrame((YAWLFlowRelation) cell, listTimeStamp.get(i), listTimeStamp.get(j));
													}
												}
											}
											
											// update executedNextNode
											executedNextNode.add(nextYawlNode);
											continue nodeIteration;
										}
									} else {
										// all requirement is satisfied
										// add new set in setIdentifiedPair
										Set<Integer> newSetInteger = new HashSet<Integer>();
										newSetInteger.add(j);
										setIdentifiedPair.put(curYawlNode, newSetInteger);
										setIdentifiedPairReversed.put(j, curYawlNode);
										// update setIdentifiedPredPair (for identifying JOIN construct)
										if (setIdentifiedPredPair.containsKey(j)){
											// update
											setIdentifiedPredPair.get(j).add(i);
										} else {
											// put new
											Set<Integer> newIntegerSet = new HashSet<Integer>();
											newIntegerSet.add(i);
											setIdentifiedPredPair.put(j, newIntegerSet);
										}
										
										if (listEventClass.size() > j) {
										if (((listEventClass.get(i).toString().split("\\+")[1]).equals("start") &&
											(listEventClass.get(j).toString().split("\\+")[1]).equals("complete")) ||
											((listEventClass.get(i).toString().split("\\+")[1]).equals("start") &&
											(listEventClass.get(j).toString().split("\\+")[1]).equals("start"))) {
											
											if ((mapYawlNodeToYPDNode.get(nextYawlNode) instanceof YPDConditionElement) &&
													(curYPDNode instanceof YPDTaskElement)) {
												YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdgePrev = 
													ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
												ypdEdgePrev.incrementFrequency();
												Integer freq = extraIncrementsEdges.get(nextYawlNode);
												extraIncrementsEdges.put(nextYawlNode, (freq == null) ? 1 : freq - 1);
												
												// if other node is not contained in the trace, increment frequency as well
												for (YAWLVertex nextYawlNodeN : pairPrevAndSucc.getSecond()) {
													boolean contained = false;
													for (int q = i+1; q < listPair.size(); q++) {
														if (listPair.get(q).getFirst().equals(nextYawlNodeN) &&
															!(listPair.get(q).getFirst().equals(nextYawlNode))) {
															contained = true;
														}
													}
													if (nextYawlNodeN != nextYawlNode && !contained) {
														if (curYawlNode instanceof AtomicTask || curYawlNode instanceof MultipleAtomicTask) {
															SplitDecorator decorator = null;
															if (curYawlNode instanceof AtomicTask) {
																decorator = ((AtomicTask) curYawlNode).getSplitDecorator();
															}
															if (curYawlNode instanceof MultipleAtomicTask) {
																decorator = ((MultipleAtomicTask) curYawlNode).getSplitDecorator();
															}
															if (decorator != null) {
																if (decorator.getType() == SplitDecorator.AND_TYPE) {
																	ypdEdgePrev = 
																		ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNodeN));
																	ypdEdgePrev.incrementFrequency();
																	// this increment could be done double in another section, therefor store how many increments have been done before
																}
															}
														}
//														ypdEdgePrev = 
//															ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNodeN));
//														ypdEdgePrev.incrementFrequency();
														// this increment could be done double in another section, therefor store how many increments have been done before
													}													
												}
											}
										
											XEventClass eventClass = yawlToEvent.get(curYawlNode);
											int index = listEventClass.indexOf(eventClass);
											startDate = listTimeStamp.get(index);
											do {index++;
											} while (!(listEventClass.get(index).toString().split("\\+")[1]).equals("complete") || 
													(listEventClass.size() < index));					
											endDate = listTimeStamp.get(index);
											// Calculate throughput for node								
//											startDate = listTimeStamp.get(i);
//											endDate = listTimeStamp.get(j);
											if (nextYawlNode instanceof OutputCondition) {
												YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdgePrev = 
													ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
												ypdEdgePrev.incrementFrequency();
											}
											
											if (nextYawlNode instanceof OutputCondition) {
												endNet = endDate;
											}
										
											// calculate difference between dates in seconds									
											diff = (endDate.getTime() - startDate.getTime())/1000;
											
											// add diff to throughput times array										
											curNodeThroughPutTimes[i] = diff;
										
											if (curYPDNode instanceof YPDTask) {
																				
												if (((YPDTask) curYPDNode).getThroughputTimeLow() > diff) {
													((YPDTask) curYPDNode).setThroughputTimeLow(diff);
												}
												if (((YPDTask) curYPDNode).getThroughputTimeHigh() < diff) {
													((YPDTask) curYPDNode).setThroughputTimeHigh(diff);
												}
											}
											
											if ((mapYawlNodeToYPDNode.get(nextYawlNode) instanceof YPDTaskElement) &&
													(curYPDNode instanceof YPDTaskElement)) {
													eventClass = yawlToEvent.get(curYawlNode);
													index = listEventClass.indexOf(eventClass);
													do {index++;
													} while (!(listEventClass.get(index).toString().split("\\+")[1]).equals("complete") || 
															(listEventClass.size() < index));					
													startDateEdge = listTimeStamp.get(index);
													eventClass = yawlToEvent.get(nextYawlNode);
													index = listEventClass.indexOf(eventClass);
													endDateEdge = listTimeStamp.get(index);
													YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdge = 
														ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
													ypdEdge.incrementFrequency();
													diffEdge = (endDateEdge.getTime() - startDateEdge.getTime())/1000;							
													curEdgeThroughPutTimes[i] = diffEdge;					
													if (ypdEdge.getMovingTimeBoundaryLow() > diffEdge) {
														ypdEdge.setMovingTimeBoundaryLow(diffEdge);
													}
													if (ypdEdge.getMovingTimeBoundaryHigh() < diffEdge) {
														ypdEdge.setMovingTimeBoundaryHigh(diffEdge);
													}												
													long throughputSumEdge = 0;		
													int c = 0;
													for (int k=0; k < curEdgeThroughPutTimes.length; k++) {
														if (curEdgeThroughPutTimes[k] != 0) {
															c = c +1;
														}
														throughputSumEdge += curEdgeThroughPutTimes[k];
													}														
													if (c != 0 && (throughputSumEdge != 0)) {
														ypdEdge = ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
														ypdEdge.setAvgMovingTime(ypdEdge.getAvgMovingTime() + (throughputSumEdge/c));
													}
												}
										}
										}
										
//										if (listEventClass.size() == j &&
//										   (listEventClass.get(i).toString().split("\\+")[1]).equals("start")) {
//												
//										}
										
										
										
										// update value of fpd edge if the event we consider has type complete
										if ((listEventClass.get(i).toString().split("\\+")[1]).equals("complete") ||
											(listEventClass.size() == j &&
											(listEventClass.get(i).toString().split("\\+")[1]).equals("start"))) {
											
											if ((mapYawlNodeToYPDNode.get(nextYawlNode) instanceof YPDTaskElement) &&
													(curYPDNode instanceof YPDTaskElement) ||
												(nextYawlNode instanceof OutputCondition) &&
													(curYPDNode instanceof YPDTaskElement)) {
												XEventClass eventClass = yawlToEvent.get(curYawlNode);
												int index = listEventClass.indexOf(eventClass);
												startDate = listTimeStamp.get(index);
												do {index++;
												} while (!(listEventClass.get(index).toString().split("\\+")[1]).equals("complete") || 
														(listEventClass.size() < index));					
												endDate = listTimeStamp.get(index);
												
												endNet = endDate;
											
												// calculate difference between dates in seconds									
												diff = (endDate.getTime() - startDate.getTime())/1000;
												
												// add diff to throughput times array										
												curNodeThroughPutTimes[i] = diff;
											
												if (curYPDNode instanceof YPDTask) {
																					
													if (((YPDTask) curYPDNode).getThroughputTimeLow() > diff) {
														((YPDTask) curYPDNode).setThroughputTimeLow(diff);
													}
													if (((YPDTask) curYPDNode).getThroughputTimeHigh() < diff) {
														((YPDTask) curYPDNode).setThroughputTimeHigh(diff);
													}
												}
											}
											
											YAWLVertex prevVertex = null;
											for (YAWLVertex vertex: pairPrevAndSucc.getFirst()) {
												prevVertex = vertex;
											}
											
											YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdge = 
												ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
											// TODO: After complete event, edge after is not always 'moved'
											ypdEdge.incrementFrequency();
																				
											// also increment frequency of YPDEdge from Task to Condition (moving time is always 'zero')
											// only if not moved before by and split task
											if (curYPDNode instanceof YPDConditionElement) {
												YPDEdge<? extends YPDNode, ? extends YPDNode> ypdEdgePrev = 
													ypd.getArc(mapYawlNodeToYPDNode.get(prevVertex), curYPDNode);
												if (extraIncrementsEdges.get(curYawlNode) == null ||
													extraIncrementsEdges.get(curYawlNode) == 0) {
													ypdEdgePrev.incrementFrequency();
												} else {
											//		Integer freq = extraIncrementsEdges.get(curYawlNode);
										//			extraIncrementsEdges.put(curYawlNode, freq - 1);
												}
											}
										
										
											// calculate edge throughput info	
											XEventClass eventClassN = yawlToEvent.get(prevVertex);
											int indexN = listEventClass.indexOf(eventClassN);
											do {indexN++;
											} while (!(listEventClass.get(indexN).toString().split("\\+")[1]).equals("complete") || 
													(listEventClass.size() < indexN));					
											startDateEdge = listTimeStamp.get(indexN);
											if ((mapYawlNodeToYPDNode.get(nextYawlNode) instanceof YPDTaskElement) &&
												(curYPDNode instanceof YPDTaskElement)) {
												XEventClass eventClass = yawlToEvent.get(curYawlNode);
												int index = listEventClass.indexOf(eventClass);
												do {index++;
												} while (!(listEventClass.get(index).toString().split("\\+")[1]).equals("complete") || 
														(listEventClass.size() < index));					
												startDateEdge = listTimeStamp.get(index);
											}
											// get timestamp of start event of next node
											XEventClass eventClass = yawlToEvent.get(nextYawlNode);
											int index = listEventClass.indexOf(eventClass);
											endDateEdge = listTimeStamp.get(index);
											
											// calculate difference between dates in seconds									
											diffEdge = (endDateEdge.getTime() - startDateEdge.getTime())/1000;
										
											// add diff to throughput times array										
											curEdgeThroughPutTimes[i] = diffEdge;
											
											if (!((nextYawlNode instanceof OutputCondition) &&
											(curYPDNode instanceof YPDTaskElement))) {
																				
												if (ypdEdge.getMovingTimeBoundaryLow() > diffEdge) {
													ypdEdge.setMovingTimeBoundaryLow(diffEdge);
												}
												if (ypdEdge.getMovingTimeBoundaryHigh() < diffEdge) {
													ypdEdge.setMovingTimeBoundaryHigh(diffEdge);
												}
												
												long throughputSumEdge = 0;		
												int c = 0;
												for (int k=0; k < curEdgeThroughPutTimes.length; k++) {
													if (curEdgeThroughPutTimes[k] != 0) {
														c = c +1;
													}
													throughputSumEdge += curEdgeThroughPutTimes[k];
												}
														
												if (c != 0 && (throughputSumEdge != 0)) {
													ypdEdge = ypd.getArc(curYPDNode, mapYawlNodeToYPDNode.get(nextYawlNode));
													ypdEdge.setAvgMovingTime(ypdEdge.getAvgMovingTime() + (throughputSumEdge/c));
												}
											}
																		
											// TODO: calculate performance for fpd edge
											
											// update graphAnimation for edge
											if (j < listTimeStamp.size()) {
											Object[] cells = yawl.getRoots();
											for (Object cell : cells) {
												if (cell instanceof YAWLFlowRelation) {
													if ((((YAWLFlowRelation) cell).getSourceVertex() == curYawlNode) &&
														(((YAWLFlowRelation) cell).getTargetVertex() == nextYawlNode)) {
														if (!(((YAWLFlowRelation) cell).getTargetVertex() instanceof OutputCondition)) {
															graphAnimation.addArcKeyFrame((YAWLFlowRelation) cell, listTimeStamp.get(i), listTimeStamp.get(j));
														}
													}
												}
											}
											}
										}

										// update executedNextNode
										executedNextNode.add(nextYawlNode);
										continue nodeIteration;
									}
								}
							} // end of finding successor
							// TODO: no proper successor event is found for nextYawlNode. Try to consider this in the new conformance metric.						
							
						} // end of iteration to all nextFlexNode
										
					
					} // successor of this node is null			
					
					// TODO update average throughput time of curFPDNode
					if (prevI != i - 1) {
						long throughputSum = 0;		
						int c = 0;
						for (int k=0; k < curNodeThroughPutTimes.length; k++) {
							if (curNodeThroughPutTimes[k] != 0) {
								c = c +1;
							}
							throughputSum += curNodeThroughPutTimes[k];
						}
								
						if (c != 0 && curYPDNode instanceof YPDTask) {
							((YPDTask) curYPDNode).setAvgThroughput(((YPDTask) curYPDNode).getAvgThroughput() + (throughputSum/c));
						}
						
						// Split construct is calculated based on the structure of the node
						if (executedNextNode.size() != 0 && curYPDNode instanceof YPDTask){
							// there are nodes that receives token from this node
							int nextNodesSizeIdentified = executedNextNode.size();
							
							if (curYawlNode instanceof AtomicTask || curYawlNode instanceof MultipleAtomicTask) {
								SplitDecorator decorator = null;
								if (curYawlNode instanceof AtomicTask) {
									decorator = ((AtomicTask) curYawlNode).getSplitDecorator();
								}
								if (curYawlNode instanceof MultipleAtomicTask) {
									decorator = ((MultipleAtomicTask) curYawlNode).getSplitDecorator();
								}
								
								if (decorator != null) {
								if (decorator.getType() == SplitDecorator.AND_TYPE) {
									((YPDTask) curYPDNode).incrementANDSPLITrf();
								}
								if (decorator.getType() == SplitDecorator.OR_TYPE) {
									((YPDTask) curYPDNode).incrementORSPLITrf();
								}
								if (decorator.getType() == SplitDecorator.XOR_TYPE) {
									((YPDTask) curYPDNode).incrementXORSPLITrf();
								}
								}
							}
//							} else {
//							// size of successor yawl nodes
//							int nextNodesSize = getOutFlows(curYawlNode, flows).size();
//							
//							// update SPLIT semantic of the node 
//							if (nextNodesSizeIdentified == nextNodesSize){
//								// AND SPLIT
//								((YPDTask) curYPDNode).incrementANDSPLITrf();
//							} else if (nextNodesSizeIdentified > 1){
//								// OR SPLIT
//								((YPDTask) curYPDNode).incrementORSPLITrf();
//							} else if (nextNodesSizeIdentified == 1){
//								// XOR SPLIT
//								((YPDTask) curYPDNode).incrementXORSPLITrf();
//							}
//							}
						}
						
						//// update JOIN semantic for this node
						
						YAWLVertex prevVertex = null;
						if (pairPrevAndSucc.getFirst() != null) {
							for (YAWLVertex vertex: pairPrevAndSucc.getFirst()) {
								prevVertex = vertex;
							}
						}
						if ((setIdentifiedPredPair.containsKey(i) && curYPDNode instanceof YPDTask) ||
							(prevVertex instanceof InputCondition)) {
							// there is a node that synchronize with this one
							int predNodesSizeIdentified = 0;
							if (prevVertex instanceof InputCondition) {
								predNodesSizeIdentified = 1;
							} else {
								predNodesSizeIdentified = setIdentifiedPredPair.get(i).size();
							}		
							
							//if (curYawlNode instanceof AtomicTask || prevVertex instanceof AtomicTask) {
							if (curYawlNode instanceof AtomicTask || curYawlNode instanceof MultipleAtomicTask) {
								JoinDecorator decorator = null;
								if (curYawlNode instanceof AtomicTask) {
									decorator = ((AtomicTask) curYawlNode).getJoinDecorator();
								}
								if (curYawlNode instanceof MultipleAtomicTask) {
									decorator = ((MultipleAtomicTask) curYawlNode).getJoinDecorator();
								}
//								if (prevVertex instanceof AtomicTask) {
//									decorator = ((AtomicTask) prevVertex).getJoinDecorator();
//								}
								if (decorator != null) {
								if (decorator.getType() == JoinDecorator.AND_TYPE) {
									((YPDTask) curYPDNode).incrementANDJOINrf();
								}
								if (decorator.getType() == JoinDecorator.OR_TYPE) {
									((YPDTask) curYPDNode).incrementORJOINrf();
								}
								if (decorator.getType() == JoinDecorator.XOR_TYPE) {
									((YPDTask) curYPDNode).incrementXORJOINrf();
								}
								}
							}
//							} else {
//							// size of predecessor yawl nodes
//							int predNodesSize = getInFlows(curYawlNode, flows).size();
//							
//							// update JOIN semantic of the node 
//							if (predNodesSizeIdentified == predNodesSize){
//								// AND JOIN
//								((YPDTask) curYPDNode).incrementANDJOINrf();
//							} else if (predNodesSizeIdentified > 1){
//								// OR JOIN
//								((YPDTask) curYPDNode).incrementORJOINrf();
//							} else if (predNodesSizeIdentified == 1){
//								// XOR JOIN
//								((YPDTask) curYPDNode).incrementXORJOINrf();
//							}
//							}
						} else { // no nodes giving its control to this node
							if (i > 0){	// only consider this as unreplayable event class if its not the first event 
								setUnreplayableEventClasses.add(yawlToEvent.get(curYawlNode));
								numNonReplayableEvents++;
								// TODO: consider distance to make this eventclass valid in conformance calculation
							}
						}
					}
					
				}	
				} // end iteration of all trace
				

				
			} // end of check whether node != null
				
			// for each node in setIdentifiedPair, update freq case has the node
			for (YAWLVertex yawlNode : visitedNodes){
				// increment freqCaseHasThisNode
				if (mapYawlNodeToYPDNode.get(yawlNode) instanceof YPDTask) {
					((YPDTask) mapYawlNodeToYPDNode.get(yawlNode)).incrementFreqCaseHasThisNode();
				}
			}
			
			// update conformance info
			confInfo.setNumOfNotReplayableActivities(setUnreplayableEventClasses.size());
			confInfo.setNumOfNotReplayableEvents(numNonReplayableEvents);
			
			confInfo.appendUnreplayableEventClass(setUnreplayableEventClasses);
			confInfo.appendUncoveredEventClass(setUncoveredEventClasses);
			confInfo.appendEventClass(setAllEventClasses);
			
			setUnreplayableEventClasses.addAll(setUncoveredEventClasses);	// unite the set
			confInfo.setNumOfUncoveredAndNotReplayableActivities(setUnreplayableEventClasses.size());
			
			// append information to conformance calculator
			calculator.accumulateConformanceInfo(confInfo);
			
			// reset accumulator variables
			for (int i = 0; i < root.getNumberOfChildren(); i++){
				root.removeChildAt(0);
			}
			statesToBeExpanded.clear();
			missingTokens.clear();
			remainingTokens.clear();
			heurDistance.clear();

			confInfo.clear();
			setUnreplayableEventClasses.clear();
			setUncoveredEventClasses.clear();
			setAllEventClasses.clear();
			numNonReplayableEvents = 0;
			
			setIdentifiedPair.clear();
			setIdentifiedPredPair.clear();
			visitedNodes.clear();
			
			progress.inc();
			
			if (startNet != null && endNet != null) {
				performanceInfo.incCompletedCases();
				double throughputTrace = (endNet.getTime() - startNet.getTime())/1000;		
				performanceInfo.setThroughput(performanceInfo.getThroughput() + throughputTrace);
			}
		}
		
		// after all done, update every fpd node's maximum activation
		// search maxFreqEventClass (same to maximum activation, as one node correspond to one event only)
		int maxFreqEventClass = -1;
		for (YPDNode node : ypd.getNodes()){
			if (node instanceof YPDTask) {
				// set freq event class
				List<Integer> tempList = new LinkedList<Integer>();
				tempList.add(((YPDTask) node).getActivationFreq());
				((YPDTask) node).setFreqEventClasses(tempList);
				
				// find max freq event class
				if (((YPDTask) node).getActivationFreq() > maxFreqEventClass){
					maxFreqEventClass = ((YPDTask) node).getActivationFreq(); 
				}
			}
		}
		
		// set every fpd node's maximum activation
		for (YPDNode node : ypd.getNodes()){
			if (node instanceof YPDTask) {
				((YPDTask) node).setAvgThroughput(((YPDTask) node).getAvgThroughput()/((YPDTask) node).getFreqCaseHasThisNode());
				((YPDTask) node).setMaxFreqEventClass(maxFreqEventClass);
				((YPDTask) node).updateNodeSize();
			}

		}
			
		// set appearance of every fpd edge
		for (YPDEdge<? extends YPDNode, ? extends YPDNode> edge : ypd.getEdges()){
			edge.setAvgMovingTime((edge.getAvgMovingTime())/(edge.getFrequency()));
			edge.updateEdgeInterface();		
		}
		
		performanceInfo.setThroughput(performanceInfo.getThroughput()/performanceInfo.getCompletedCases());
		
		return new Object[] { ypd, calculatorPerf.calculatePerformanceInfo(performanceInfo), graphAnimation };
	}
	
	@SuppressWarnings("unchecked")
	private void traceCancellations(List<Pair<YAWLVertex, Pair<Set<YAWLVertex>, Set<YAWLVertex>>>> listPair, YPD ypd,
			List<XEventClass> listEventClass, XTrace trace, XEventClasses classes, XTimeExtension xTimeExtension,
			Map<XEventClass, List<YAWLVertex>> eventToYawl, Map<YAWLVertex, YPDNode> mapYawlNodeToYPDNode,
			HashSet<YAWLFlowRelation> flows, Object[] vertices) {
		
		// first collect all ate_abort events from the trace considered
		List<XEventClass> ateAbortEvents = new LinkedList<XEventClass>();
		List<Date> ateAbortTimestamps = new LinkedList<Date>();
		for (XEvent event : trace) {
			boolean contained = false;
			XEventClass eventClass = classes.getClassOf(event);
			String eventType = eventClass.toString().split("\\+")[1];
			if (eventType.equals("ate_abort")) {
				for (XEventClass cancelledEvent : ateAbortEvents) {
					if (cancelledEvent == eventClass) {
						contained = true;
					}
				}
				if (!contained) {
					ateAbortEvents.add(eventClass);
					ateAbortTimestamps.add(xTimeExtension.extractTimestamp(event));
				}
			}
		}
		
		// then check if the task to which the ate_abort events corresponds to was already started
		// (cancellation in task), had a resource assigned to (cancellation in incoming flow), or was
		// just enabled (cancellation in condition)
		
		for (XEventClass abortEvent : ateAbortEvents) {
			String abortName = abortEvent.toString().split("\\+")[0];
			boolean changed = false;
			YPDTask abortedTask = null;
			YAWLVertex yawlVertexOriginal = null;
			for (XEventClass listEvent : listEventClass) {
				String taskName = listEvent.toString().split("\\+")[0];
				String eventType = listEvent.toString().split("\\+")[1];
				if (abortName.equals(taskName) && eventType.equals("start")) {
					// found corresponding task, find corresponding YPDNode
					if (eventToYawl.get(listEvent) != null) {
					for (YAWLVertex yawlVertex : eventToYawl.get(listEvent)) {
						abortedTask = (YPDTask) mapYawlNodeToYPDNode.get(yawlVertex);
						abortedTask.incrementCancelledByOtherTask();
						changed = true;
						yawlVertexOriginal = yawlVertex;
					}
					}
				}		
			}
			
			if (changed) {
				boolean found = false;
				for (Object vertex : vertices) {
				     if ((vertex instanceof AtomicTask ||
					     vertex instanceof MultipleAtomicTask) &&
			    	    (((YAWLTask) vertex).getCancellationSet().getSetMembers().size() > 0) &&
			    	    !found) {
			    		YAWLTask task = (YAWLTask) vertex;
			    		HashSet<YAWLCell> cancellationSet = task.getCancellationSet().getSetMembers();

	    				// although token is assigned to task, token is not yet on flow, find condition
			    		for (YAWLCell cancelledItem : cancellationSet) {
			    			if (cancelledItem instanceof YAWLTask && !found) {
			    				if ((YAWLVertex) cancelledItem == yawlVertexOriginal) {
			    					found = true;
				    				((YPDTask) mapYawlNodeToYPDNode.get(vertex)).incrementCancelledOutOtherActiveNodes();
			    				}
			    				//TODO what if condition/flow is cancelled by multiple tasks
			    			}
			    		}	    			
				     }	
				}
			}
			
			for (XEvent event : trace) {
				if (!changed) {
				XEventClass eventClass = classes.getClassOf(event);
				String eventType = eventClass.toString().split("\\+")[1];
				String taskName = eventClass.toString().split("\\+")[0];
				if (abortName.equals(taskName) && eventType.equals("assign")) {
					// found corresponding task, find corresponding YPDNode
					// find corresponding flow
					changed = true;
					List<YAWLVertex> targetList = eventToYawl.get(classes.getByIdentity(abortName + "+start"));
					//List<YAWLVertex> targetList = eventToYawl.get(classes.getByIdentity(abortName + "+start"));
					if (targetList != null) {
					YAWLVertex target = null;
					for (YAWLVertex targetL : targetList) {
						target = targetL;
					}
					// find source condition/task
					Set<YAWLFlowRelation> inFlows = getInFlows(target, flows);
					YAWLVertex inVertex = null;
					YAWLVertex inVertexOriginal = null;
					int indexPrevNode = -1;
					for (YAWLFlowRelation inflow : inFlows) {
						inVertexOriginal = inflow.getSourceVertex();
						inVertex = inVertexOriginal;
						if (inVertex instanceof Condition) {
							Set<YAWLFlowRelation> inFlowsCondition = getInFlows(inVertex, flows);
							for (YAWLFlowRelation inflowCondition : inFlowsCondition) {
								inVertex = inflowCondition.getSourceVertex();
							}
						}
						if (inVertexOriginal instanceof Condition) {
							for (int i=0; i < listPair.size(); i++){											
								// find input condition
								if (inVertexOriginal == listPair.get(i).getFirst()) {
									// found condition in listPair that is input for target Node
									// find the enabled condition that is aborted
									Pair<Set<YAWLVertex>, Set<YAWLVertex>> pairPrevAndSucc = listPair.get(i).getSecond(); 
									Set<YAWLVertex> prevVertices = pairPrevAndSucc.getFirst();
									for (YAWLVertex prevVertex : prevVertices) {
										for (int j=0; j < listPair.size(); j++) {
											if (listPair.get(j).getFirst() == prevVertex) {
												if (indexPrevNode < j) {
													indexPrevNode = j;
												}
											}
										}
									}
								}
							}
						}
					}
					
					YAWLVertex source = null;					
					if (inVertexOriginal instanceof Condition && indexPrevNode != -1) {
						source = listPair.get(indexPrevNode).getFirst();
					} else {
						source = inVertexOriginal;
					}
					YPDEdge<? extends YPDNode, ? extends YPDNode> ypdArc = 
						ypd.getArc(mapYawlNodeToYPDNode.get(source), mapYawlNodeToYPDNode.get(target));
					ypdArc.setCancellation(true);
					ypdArc.incrementCancelledByOtherTask();
					
					// find the task that cancelled this flow and increments it's 
					// 'timesCancelledOutOtherActiveNodes parameter
	    			boolean found = false;
					for (Object vertex : vertices) {
					     if ((vertex instanceof AtomicTask ||
						     vertex instanceof MultipleAtomicTask) &&
				    	    (((YAWLTask) vertex).getCancellationSet().getSetMembers().size() > 0)) {
				    		YAWLTask task = (YAWLTask) vertex;
				    		HashSet<YAWLCell> cancellationSet = task.getCancellationSet().getSetMembers();
	
				    		for (YAWLCell cancelledItem : cancellationSet) {
				    			if (cancelledItem instanceof YAWLFlowRelation) {
				    				YAWLFlowRelation flow = (YAWLFlowRelation) cancelledItem;
				    				if (flow.getSourceVertex() == source &&
				    					flow.getTargetVertex() == target) {
				    					found = true;
				    					if (flow.getSourceTask() == task) {
				    						((YPDTask) mapYawlNodeToYPDNode.get(source)).incrementCancelledOutOtherActiveNodes();
				    					}
				    					if (flow.getTargetTask() == task) {
				    						((YPDTask) mapYawlNodeToYPDNode.get(target)).incrementCancelledOutOtherActiveNodes();
				    					}
				    				}
				    			}
				    		}
					     }
					}
	    			if (!found) {
						for (Object vertex : vertices) {
						     if ((vertex instanceof AtomicTask ||
							     vertex instanceof MultipleAtomicTask) &&
					    	    (((YAWLTask) vertex).getCancellationSet().getSetMembers().size() > 0) &&
					    	    !found) {
					    		YAWLTask task = (YAWLTask) vertex;
					    		HashSet<YAWLCell> cancellationSet = task.getCancellationSet().getSetMembers();
		
			    				// although token is assigned to task, token is not yet on flow, find condition
					    		for (YAWLCell cancelledItem : cancellationSet) {
					    			if (cancelledItem instanceof Condition && !found) {
					    				Condition c = (Condition) cancelledItem;
					    				if (c == source) {
					    					found = true;
					    					((YPDTask) mapYawlNodeToYPDNode.get(task)).incrementCancelledOutOtherActiveNodes();
					    				}
					    				//TODO what if condition/flow is cancelled by multiple tasks
					    			}
					    		}	    			
						     }	
						}
	    			}
				}
				}
				}
			}
			
			if (!changed) {
				// corresponding condition of incoming flow for task is cancelled, no assign and start event present
				// TODO: how to determine which of the input conditions has a token in it??
				
				for (XEvent event : trace) {
					XEventClass eventClass = classes.getClassOf(event);
					String taskName = eventClass.toString().split("\\+")[0];
					if (abortName.equals(taskName)) {
						// found corresponding task, find corresponding YPDNode
						// find corresponding flow
						changed = true;
						List<YAWLVertex> targetList = eventToYawl.get(classes.getByIdentity(abortName + "+start"));
						//List<YAWLVertex> targetList = eventToYawl.get(classes.getByIdentity(abortName + "+start"));
						YAWLVertex target = null;
						for (YAWLVertex targetL : targetList) {
							target = targetL;
						}
						// find source condition/task
						Set<YAWLFlowRelation> inFlows = getInFlows(target, flows);
						YAWLVertex inVertex = null;
						YAWLVertex inVertexOriginal = null;
						int indexPrevNode = -1;
						for (YAWLFlowRelation inflow : inFlows) {
							inVertexOriginal = inflow.getSourceVertex();
							inVertex = inVertexOriginal;
							if (inVertex instanceof Condition) {
								Set<YAWLFlowRelation> inFlowsCondition = getInFlows(inVertex, flows);
								for (YAWLFlowRelation inflowCondition : inFlowsCondition) {
									inVertex = inflowCondition.getSourceVertex();
								}
							}
							if (inVertexOriginal instanceof Condition) {
								for (int i=0; i < listPair.size(); i++){											
									// find input condition
									if (inVertexOriginal == listPair.get(i).getFirst()) {
										// found condition in listPair that is input for target Node
										// find the enabled condition that is aborted
										Pair<Set<YAWLVertex>, Set<YAWLVertex>> pairPrevAndSucc = listPair.get(i).getSecond(); 
										Set<YAWLVertex> prevVertices = pairPrevAndSucc.getFirst();
										for (YAWLVertex prevVertex : prevVertices) {
											for (int j=0; j < listPair.size(); j++) {
												if (listPair.get(j).getFirst() == prevVertex) {
													if (indexPrevNode < j) {
														indexPrevNode = j;
													}
												}
											}
										}
									}
								}
							}
						}
						
						YAWLVertex source = null;					
						if (inVertexOriginal instanceof Condition && indexPrevNode != -1) {
							source = listPair.get(indexPrevNode).getFirst();
						} else {
							source = inVertexOriginal;
						}
						if (source instanceof Condition) {
							YPDCondition inputCondition = (YPDCondition) mapYawlNodeToYPDNode.get(source);
							inputCondition.incrementCancelledByOtherTask();
							inputCondition.setCancellation(true);
						}
						
						// find the task that cancelled this condition and increments it's 
						// 'timesCancelledOutOtherActiveNodes parameter
						for (Object vertex : vertices) {
						     if ((vertex instanceof AtomicTask ||
							     vertex instanceof MultipleAtomicTask) &&
					    	    (((YAWLTask) vertex).getCancellationSet().getSetMembers().size() > 0)) {
					    		YAWLTask task = (YAWLTask) vertex;
					    		HashSet<YAWLCell> cancellationSet = task.getCancellationSet().getSetMembers();
					    		for (YAWLCell cancelledItem : cancellationSet) {
					    			boolean found = false;
					    			if (cancelledItem instanceof YAWLFlowRelation) {
					    				YAWLFlowRelation flow = (YAWLFlowRelation) cancelledItem;
					    				if (flow.getSourceVertex() == source &&
					    					flow.getTargetVertex() == target) {
					    					found = true;
					    					if (flow.getSourceTask() == task) {
					    						((YPDTask) mapYawlNodeToYPDNode.get(source)).incrementCancelledOutOtherActiveNodes();
					    					}
					    					if (flow.getTargetTask() == task) {
					    						((YPDTask) mapYawlNodeToYPDNode.get(target)).incrementCancelledOutOtherActiveNodes();
					    					}
					    				}
					    			}
					    			if (!found) {
					    				// although token is assigned to task, token is not yet on flow, find condition
					    				if (cancelledItem instanceof Condition) {
						    				Condition c = (Condition) cancelledItem;
						    				if (c == source) {
						    					found = true;
						    					((YPDTask) mapYawlNodeToYPDNode.get(target)).incrementCancelledOutOtherActiveNodes();
						    				}
						    				if (c == target) {
						    					((YPDTask) mapYawlNodeToYPDNode.get(source)).incrementCancelledOutOtherActiveNodes();
						    				}
						    			}
					    			}
					    		}		    			
					    	 }
						}
					}		
				}
			}
		}
	}

	private Object[] replayTrace(NetGraph yawl, 
			List<XEventClass> listEventClass, 
			int misTokenWeight,
			int remTokenWeight, 
			int heurDistanceWeight, 
			Map<XEventClass, List<YAWLVertex>> eventToYawl, 
			Node<YawlState> root,
			List<Node<YawlState>> statesToBeExpanded,
			List<Bag<YAWLFlowRelation>> missingTokens,
			List<Bag<YAWLFlowRelation>> remainingTokens, 
			List<Integer> heurDistance,
			HashSet<YAWLFlowRelation> flows,
			Map<YAWLFlowRelation, YPDEdge<? extends YPDNode, ? extends YPDNode>> mapYFlowToYPDEdge,
			YPD ypdS) {
		int listEventClassSize = listEventClass.size();
			
		// init stop criteria
		int newHeurDistance = heurDistance.get(0);
		Set<Node<YawlState>> newNodes = new HashSet<Node<YawlState>>();
		YawlState[] statesMultipleMappings = new YawlState[ypdS.getNodes().size()*2];	
		int mMappingsIndex = 0;
		YAWLVertex[] visitedVertices = new YAWLVertex[ypdS.getNodes().size()];

		// check if there is an event to be considered
		if (newHeurDistance > 0){
			do {
				// reset stop criteria
				newNodes.clear();
				
				// get current event under consideration
				XEventClass currEvent = listEventClass.get(listEventClassSize - heurDistance.get(0));
				
				
				// find yawlnodes for current event
				List<YAWLVertex> currentNodes = eventToYawl.get(currEvent);			
	
				// get all attributes of current state to be expanded
//				Node<YawlState> currStateToBeExpanded = statesToBeExpanded.remove(0);
//				Bag<YAWLFlowRelation> currMissingTokens = missingTokens.remove(0);
//				Bag<YAWLFlowRelation> currRemainingTokens = remainingTokens.remove(0);
//				newHeurDistance = heurDistance.remove(0) - 1;
				
				// for each node that correspond to an event, extends the states
				if (currentNodes == null) {
					heurDistance.set(0, heurDistance.get(0) - 1);
				}
				if (currentNodes != null) {
					newHeurDistance = heurDistance.remove(0) - 1;
					Node<YawlState> currStateToBeExpanded = statesToBeExpanded.remove(0);
					Bag<YAWLFlowRelation> currMissingTokens = missingTokens.remove(0);
					Bag<YAWLFlowRelation> currRemainingTokens = remainingTokens.remove(0);
				for (YAWLVertex currentNode : currentNodes){
					
					//Node<YawlState> currStateToBeExpanded = statesToBeExpanded.remove(0);
					// for each possible input of the node
					//if (currentNode.getInputNodes().size() > 0){	// there is a change in tokens distribution
					if (getInFlows(currentNode, flows).size() > 0) {
						
						// determine event type
						String eventType = currEvent.toString().split("\\+")[1];
						
						// if the current event is an event of type start and it follows an InputCondition,
						// increment the frequency of the flow from InputCondition to that task
						Set<YAWLFlowRelation> inFlows = getInFlows(currentNode, flows);
						YAWLFlowRelation incomingFlow = null;
						for (YAWLFlowRelation inFlow : inFlows) {
							incomingFlow = inFlow; 
						}
						if (incomingFlow.getSourceVertex() instanceof InputCondition &&
							eventType.equals("start")) {
							YPDEdge<? extends YPDNode, ? extends YPDNode> yEdge = mapYFlowToYPDEdge.get(currentNode.getOnlyIncomingFlow());
							yEdge.incrementFrequency();
						}
						
						
						Set<YAWLVertex> inVertices = new HashSet<YAWLVertex>(getInFlows(currentNode, flows).size());
						Set<YAWLFlowRelation> incomingFlows = new HashSet<YAWLFlowRelation>(getInFlows(currentNode, flows).size()); 
						incomingFlows =	getInFlows(currentNode, flows);
						for (YAWLFlowRelation inFlow : incomingFlows) {
							inVertices.add(inFlow.getSourceVertex());
						}
						
						//for (Set<VertexContainer> inputSet : currentNode.getInputNodes()){
							// it can also be that the node does not have any input, in this case, this block is not executed
							// init missing and remaining tokens
							Bag<YAWLFlowRelation> newMissingTokensAfterInput = new HashBag<YAWLFlowRelation>();
							Bag<YAWLFlowRelation> newRemainingTokensAfterInput = new HashBag<YAWLFlowRelation>();
							
							for (YAWLVertex inputNode : inVertices){
								// initiate attributes for new state
								newMissingTokensAfterInput.addAll(currMissingTokens);
								newRemainingTokensAfterInput.addAll(currRemainingTokens);
								
								YAWLFlowRelation arcInputToCurrent = null;
								// find flow from currentNode to inputNode
								for (YAWLFlowRelation inFlow : incomingFlows) {
									if (inFlow.getSourceVertex() == inputNode){
										arcInputToCurrent = inFlow;
										break;
									}
								}
								
								// find if there is an edge with inputNode as its source
								if (!newRemainingTokensAfterInput.contains(arcInputToCurrent)){
									newMissingTokensAfterInput.add(arcInputToCurrent);
								} else {								
									newRemainingTokensAfterInput.remove(arcInputToCurrent, 1);
								}
							}
							
							PairMissingRemaining newPair = new PairMissingRemaining(newMissingTokensAfterInput, newRemainingTokensAfterInput);
							
							// TODO we don't deal with sets here! like in SPD's etc
							
							if (getOutFlows(currentNode, flows).size() > 0){
								Set<YAWLVertex> outVertices = new HashSet<YAWLVertex>(getOutFlows(currentNode, flows).size());
								Set<YAWLFlowRelation> outgoingFlows = new HashSet<YAWLFlowRelation>(getOutFlows(currentNode, flows).size());
								outgoingFlows =	getOutFlows(currentNode, flows);
								for (YAWLFlowRelation outFlow : outgoingFlows) {
									outVertices.add(outFlow.getTargetVertex());
								}
								Bag<YAWLFlowRelation> newRemainingTokensAfterOutput = new HashBag<YAWLFlowRelation>();
								newRemainingTokensAfterOutput.addAll(newPair.getNewRemainingTokens());
								for (@SuppressWarnings("unused") YAWLVertex outputNode : outVertices){
									// final remaining tokens
									
									// 'execute' the ouput
									for (YAWLFlowRelation outFlow : outgoingFlows) {
										newRemainingTokensAfterOutput.add(outFlow);
									}
								}
									
								int newWeight = (newPair.getNewMissingTokens().size() * misTokenWeight) + (newRemainingTokensAfterOutput.size() * remTokenWeight) + (newHeurDistance * heurDistanceWeight); 
									
								// generate a state for each combination of input set and output set
								YawlState newState = new YawlState();
								newState.setYawlNode(currentNode);
								newState.setPrevNodes(inVertices);
								newState.setNextNodes(outVertices);
								newState.setWeight(newWeight);
								
								boolean b = false;
								for (int x = 0; x < visitedVertices.length ; x++) {
									if (visitedVertices[x] == currentNode) {
										b = true;
									}
								}
								if (!b) {
									visitedVertices[mMappingsIndex] = currentNode;
									statesMultipleMappings[mMappingsIndex] = newState;
									mMappingsIndex++;  
								}
								
								Node<YawlState> newNode = new Node<YawlState>(newState);
								newNode.setParent(currStateToBeExpanded);
								
								// place the node in correct order
								int i = 0;
								while ((i < statesToBeExpanded.size()) && (newWeight >= statesToBeExpanded.get(i).getData().getWeight())){
									i++;
								}
								if (i < statesToBeExpanded.size()){
									statesToBeExpanded.add(i, newNode);
									missingTokens.add(i, newPair.getNewMissingTokens());
									remainingTokens.add(i, newRemainingTokensAfterOutput);
									heurDistance.add(i, newHeurDistance);								
								} else {
									statesToBeExpanded.add(newNode);
									missingTokens.add(newPair.getNewMissingTokens());
									remainingTokens.add(newRemainingTokensAfterOutput);
									heurDistance.add(newHeurDistance);
								}
									
									// update new nodes
									newNodes.add(newNode);
								}
							else {
								// no output node, but there is an input node
								// final remaining tokens
								int newWeight = (newPair.getNewMissingTokens().size() * misTokenWeight) + (newPair.getNewRemainingTokens().size() * remTokenWeight) + (newHeurDistance * heurDistanceWeight); 
								
								// generate a state for each combination of input set and output set
								YawlState newState = new YawlState();
								newState.setYawlNode(currentNode);
								newState.setPrevNodes(inVertices);
								newState.setNextNodes(null);
								newState.setWeight(newWeight);
								
								Node<YawlState> newNode = new Node<YawlState>(newState);
								boolean b = false;
								for (int x = 0; x < visitedVertices.length ; x++) {
									if (visitedVertices[x] == currentNode) {
										b = true;
									}
								}
								if (!b) {
									visitedVertices[mMappingsIndex] = currentNode;
									statesMultipleMappings[mMappingsIndex] = newState;
									mMappingsIndex++;  
								}
								newNode.setParent(currStateToBeExpanded);
								
								// place the node in correct order
								int i = 0;
								while ((i < statesToBeExpanded.size()) && (newWeight >= statesToBeExpanded.get(i).getData().getWeight())){
									i++;
								}
								if (i < statesToBeExpanded.size()){
									statesToBeExpanded.add(i, newNode);
									missingTokens.add(i, newPair.getNewMissingTokens());
									remainingTokens.add(i, newPair.getNewRemainingTokens());
									heurDistance.add(i, newHeurDistance);								
								} else {
									statesToBeExpanded.add(newNode);
									missingTokens.add(newPair.getNewMissingTokens());
									remainingTokens.add(newPair.getNewRemainingTokens());
									heurDistance.add(newHeurDistance);
								}
								
								// update new nodes
								newNodes.add(newNode);
							}
						 // end of for each possible input of the node			
					} else { // there is no changes in tokens after input (because no input set is encountered)
						// currentNode.getInputNodes().size() == 0

						if (getOutFlows(currentNode, flows).size() > 0){ // there are output sets
							Set<YAWLVertex> outVertices = new HashSet<YAWLVertex>(getOutFlows(currentNode, flows).size());
							Set<YAWLFlowRelation> outgoingFlows = new HashSet<YAWLFlowRelation>(getOutFlows(currentNode, flows).size());
							outgoingFlows =	getOutFlows(currentNode, flows);
							for (YAWLFlowRelation outFlow : outgoingFlows) {
								outVertices.add(outFlow.getTargetVertex());
							}

							Bag<YAWLFlowRelation> newRemainingTokensAfterOutput = new HashBag<YAWLFlowRelation>();
							newRemainingTokensAfterOutput.addAll(currRemainingTokens);
							for (@SuppressWarnings("unused") YAWLVertex outputNode : outVertices){
								// final remaining tokens
								
								// 'execute' the ouput
								for (YAWLFlowRelation outFlow : outgoingFlows) {
									newRemainingTokensAfterOutput.add(outFlow);
								}
							}
							
							int newWeight = (currMissingTokens.size() * misTokenWeight) + (newRemainingTokensAfterOutput.size() * remTokenWeight) + (newHeurDistance * heurDistanceWeight); 

							// generate a state for each combination of input set and output set
							YawlState newState = new YawlState();
							newState.setYawlNode(currentNode);
							newState.setPrevNodes(null);
							newState.setNextNodes(outVertices);
							newState.setWeight(newWeight);
								
							Node<YawlState> newNode = new Node<YawlState>(newState);
							boolean b = false;
							for (int x = 0; x < visitedVertices.length ; x++) {
								if (visitedVertices[x] == currentNode) {
									b = true;
								}
							}
							if (!b) {
								statesMultipleMappings[mMappingsIndex] = newState;
								visitedVertices[mMappingsIndex] = currentNode;
								mMappingsIndex++;  
							} 
							newNode.setParent(currStateToBeExpanded);
							
							// place the node in correct order
							int i = 0;
							while ((i < statesToBeExpanded.size()) && (newWeight >= statesToBeExpanded.get(i).getData().getWeight())){
								i++;
							}
							if (i < statesToBeExpanded.size()){
								statesToBeExpanded.add(i, newNode);
								missingTokens.add(i, currRemainingTokens);
								remainingTokens.add(i, newRemainingTokensAfterOutput);
								heurDistance.add(i, newHeurDistance);								
							} else {
								statesToBeExpanded.add(newNode);
								missingTokens.add(currRemainingTokens);
								remainingTokens.add(newRemainingTokensAfterOutput);
								heurDistance.add(newHeurDistance);
							}
							
							// update new nodes
							newNodes.add(newNode);						
						} else {
							// no output sets, no input set
							// final remaining tokens
							
							int newWeight = (currMissingTokens.size() * misTokenWeight) + (currRemainingTokens.size() * remTokenWeight) + (newHeurDistance * heurDistanceWeight); 
							
							// generate a state for each combination of input set and output set
							YawlState newState = new YawlState();
							newState.setYawlNode(currentNode);
							newState.setPrevNodes(null);
							newState.setNextNodes(null);
							newState.setWeight(newWeight);
							
							Node<YawlState> newNode = new Node<YawlState>(newState);
							boolean b = false;
							for (int x = 0; x < visitedVertices.length ; x++) {
								if (visitedVertices[x] == currentNode) {
									b = true;
								}
							}
							if (!b) {
								statesMultipleMappings[mMappingsIndex] = newState;
								visitedVertices[mMappingsIndex] = currentNode;
								mMappingsIndex++;  
							}
							newNode.setParent(currStateToBeExpanded);
							
							// place the node in correct order
							int i = 0;
							while ((i < statesToBeExpanded.size()) && (newWeight >= statesToBeExpanded.get(i).getData().getWeight())){
								i++;
							}
							if (i < statesToBeExpanded.size()){
								statesToBeExpanded.add(i, newNode);
								missingTokens.add(i, currMissingTokens);
								remainingTokens.add(i, currRemainingTokens);
								heurDistance.add(i, newHeurDistance);								
							} else {
								statesToBeExpanded.add(newNode);
								missingTokens.add(currMissingTokens);
								remainingTokens.add(currRemainingTokens);
								heurDistance.add(newHeurDistance);
							}
							
							// update new nodes
							newNodes.add(newNode);
						}
					} // end of after input
				}} // end for each node that correspond to an event, extends the states 
			} while (heurDistance.get(0) != 0);
			return new Object[] {statesToBeExpanded.get(0), statesMultipleMappings, visitedVertices };
		} else {
			return null;
		}
		
	}
	
	// two methods below added to retrieve the new in-and outgoing flows (in case of existence of subnet) for vertices
	public Set<YAWLFlowRelation> getOutFlows(YAWLVertex yNode, HashSet<YAWLFlowRelation> yFlows) {
		Set<YAWLFlowRelation> newYFlows = new HashSet<YAWLFlowRelation>(yNode.getOutgoingFlows().size());
		for (YAWLFlowRelation flow : yFlows) {
			if (flow.getSourceVertex() == yNode) {
				newYFlows.add(flow);
			}
		}
		return newYFlows;
	}
	
	public Set<YAWLFlowRelation> getInFlows(YAWLVertex yNode, HashSet<YAWLFlowRelation> yFlows) {
		Set<YAWLFlowRelation> newYFlows = new HashSet<YAWLFlowRelation>(yNode.getIncomingFlows().size());
		for (YAWLFlowRelation flow : yFlows) {
			if (flow.getTargetVertex() == yNode) {
				newYFlows.add(flow);
			}
		}
		return newYFlows;
	}
	 

	protected class PairMissingRemaining {
		private Bag<YAWLFlowRelation> newMissingTokens;
		private Bag<YAWLFlowRelation> newRemainingTokens;
		
		public PairMissingRemaining(){
			this.newMissingTokens = new HashBag<YAWLFlowRelation>();
			this.newRemainingTokens = new HashBag<YAWLFlowRelation>();
		}

		/**
		 * @param newMissingTokensAfterInput
		 * @param newRemainingTokensAfterInput
		 */
		public PairMissingRemaining(Bag<YAWLFlowRelation> newMissingTokensAfterInput,
				Bag<YAWLFlowRelation> newRemainingTokensAfterInput) {
			super();
			setNewMissingTokens(newMissingTokensAfterInput);
			setNewRemainingTokens(newRemainingTokensAfterInput);
		}

		/**
		 * @return the newMissingTokens
		 */
		public Bag<YAWLFlowRelation> getNewMissingTokens() {
			return newMissingTokens;
		}

		/**
		 * @param newMissingTokens the newMissingTokens to set
		 */
		public void setNewMissingTokens(Bag<YAWLFlowRelation> newMissingTokens) {
			this.newMissingTokens = newMissingTokens;
		}

		/**
		 * @return the newRemainingTokens
		 */
		public Bag<YAWLFlowRelation> getNewRemainingTokens() {
			return newRemainingTokens;
		}

		/**
		 * @param newRemainingTokens the newRemainingTokens to set
		 */
		public void setNewRemainingTokens(Bag<YAWLFlowRelation> newRemainingTokens) {
			this.newRemainingTokens = newRemainingTokens;
		}
	}
}
