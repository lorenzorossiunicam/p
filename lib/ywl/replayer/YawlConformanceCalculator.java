package org.processmining.plugins.ywl.replayer;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.yawl.YawlConformanceResult;
import org.processmining.plugins.ywl.replayer.visualization.SequenceEventsConformanceInfo;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class YawlConformanceCalculator {
	// accumulator per fitness modes
	//// total fitness model
	private int totalModeNumNotFittingTrace = 0;
	private double totalModePropProblematicEvents = 0;
	private double totalModePropProblematicEventTypes = 0;
	
	//// replay fitness model
	private int replayModeNumNotFittingTrace = 0;
	private double replayModePropProblematicEvents = 0;
	private double replayModePropProblematicEventTypes = 0;
	
	//// coverage fitness model
	private int coverageModeNumNotFittingTrace = 0;
	private double coverageModePropProblematicEvents = 0;
	private double coverageModePropProblematicEventTypes = 0;

	// others
	private int numOfEvents = 0;
	private int numOfUnreplayableEvents = 0;
	private int numOfUncoveredEvents = 0;
	private int numOfTrace = 0;
	
	private Set<XEventClass> setUnreplayableEventClasses = new HashSet<XEventClass>();
	private Set<XEventClass> setUncoveredEventClasses = new HashSet<XEventClass>();
	private Set<XEventClass> setAllEventClasses = new HashSet<XEventClass>();
	
	public void accumulateConformanceInfo(SequenceEventsConformanceInfo confInfo){
		// trace fitness
		if (confInfo.getNumOfUncoveredEvents() > 0){
			coverageModeNumNotFittingTrace++;
		} 
		if (confInfo.getNumOfNotReplayableEvents() > 0){
			replayModeNumNotFittingTrace++;
		}
		if (confInfo.getNumOfNotReplayableEvents() > 0 || confInfo.getNumOfUncoveredEvents() > 0){
			totalModeNumNotFittingTrace++;
		}

		// average trace fitness - event log level
		totalModePropProblematicEvents += 
			(double)(confInfo.getNumOfNotReplayableEvents() + confInfo.getNumOfUncoveredEvents()) / confInfo.getNumOfEvents();
		replayModePropProblematicEvents += 
			(double)confInfo.getNumOfNotReplayableEvents() / (confInfo.getNumOfEvents() - confInfo.getNumOfUncoveredEvents());
		coverageModePropProblematicEvents +=
			(double)confInfo.getNumOfUncoveredEvents() / confInfo.getNumOfEvents();
		
		// average trace fitness - event log level
		Set<XEventClass> setAllEventClass = confInfo.getSetAllEventClass();
		totalModePropProblematicEventTypes += 
			(double)confInfo.getNumOfUncoveredAndNotReplayableActivities() / setAllEventClass.size();
		replayModePropProblematicEventTypes += 
			(double)confInfo.getNumOfNotReplayableActivities() / (setAllEventClass.size() - confInfo.getNumOfUncoveredActivities());
		coverageModePropProblematicEventTypes +=
			(double)confInfo.getNumOfUncoveredActivities() / setAllEventClass.size();

		// absolute fitness
		numOfUnreplayableEvents += confInfo.getNumOfNotReplayableEvents();
		numOfUncoveredEvents += confInfo.getNumOfUncoveredEvents();

		setUncoveredEventClasses.addAll(confInfo.getSetUncoveredEventClass());
		setUnreplayableEventClasses.addAll(confInfo.getSetUnreplayableEventClass());
		setAllEventClasses.addAll(confInfo.getSetAllEventClass());
		
		// divisor
		numOfEvents += confInfo.getNumOfEvents();
		numOfTrace++;
	}
	
	public YawlConformanceResult calculateConformanceInfo(){
		YawlConformanceResult result = new YawlConformanceResult();
		
		// total fitness model
		result.addConformanceValue(YawlConformanceResult.TOTAL_MODE_TRACE_FITNESS, 1 - ((double)totalModeNumNotFittingTrace / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.TOTAL_MODE_AVERAGE_TRACE_FITNESS_EL, 1 - (totalModePropProblematicEvents / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.TOTAL_MODE_AVERAGE_TRACE_FITNESS_TEL, 1 - (totalModePropProblematicEventTypes / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.TOTAL_MODE_ABSOLUTE_FITNESS_EL, 1 - (((double)numOfUncoveredEvents + (double)numOfUnreplayableEvents) / numOfEvents));
		
		Set<XEventClass> setOfUnreplayableAndUncovered = new HashSet<XEventClass>();
		setOfUnreplayableAndUncovered.addAll(setUncoveredEventClasses);
		setOfUnreplayableAndUncovered.addAll(setUnreplayableEventClasses);
		result.addConformanceValue(YawlConformanceResult.TOTAL_MODE_ABSOLUTE_FITNESS_TEL, 1 - ((double)setOfUnreplayableAndUncovered.size() / setAllEventClasses.size()));
		
		// coverage fitness model
		result.addConformanceValue(YawlConformanceResult.COVERAGE_MODE_TRACE_FITNESS, 1 - ((double)coverageModeNumNotFittingTrace / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.COVERAGE_MODE_AVERAGE_TRACE_FITNESS_EL, 1 - (coverageModePropProblematicEvents / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.COVERAGE_MODE_AVERAGE_TRACE_FITNESS_TEL, 1 - (coverageModePropProblematicEventTypes / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.COVERAGE_MODE_ABSOLUTE_FITNESS_EL, 1 - ((double)numOfUncoveredEvents / numOfEvents));
		result.addConformanceValue(YawlConformanceResult.COVERAGE_MODE_ABSOLUTE_FITNESS_TEL, 1 - ((double)setUncoveredEventClasses.size() / setAllEventClasses.size()));
		
		// replay fitness model
		result.addConformanceValue(YawlConformanceResult.REPLAY_MODE_TRACE_FITNESS, 1 - ((double)replayModeNumNotFittingTrace / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.REPLAY_MODE_AVERAGE_TRACE_FITNESS_EL, 1 - (replayModePropProblematicEvents / numOfTrace));
		result.addConformanceValue(YawlConformanceResult.REPLAY_MODE_AVERAGE_TRACE_FITNESS_TEL, 1 - (replayModePropProblematicEventTypes / numOfTrace));
		
		result.addConformanceValue(YawlConformanceResult.REPLAY_MODE_ABSOLUTE_FITNESS_EL, 1 - ((double)numOfUnreplayableEvents / (numOfEvents - numOfUncoveredEvents)));
		
		// set of event classes minus set of uncovered event types
		setAllEventClasses.removeAll(setUncoveredEventClasses);
		result.addConformanceValue(YawlConformanceResult.REPLAY_MODE_ABSOLUTE_FITNESS_TEL, 1 - ((double)setUnreplayableEventClasses.size() / setAllEventClasses.size()));
		
		return result;
	}

	/**
	 * @return the numOfEvents
	 */
	public int getNumOfEvents() {
		return numOfEvents;
	}

	/**
	 * @param numOfEvents the numOfEvents to set
	 */
	public void setNumOfEvents(int numOfEvents) {
		this.numOfEvents = numOfEvents;
	}
	
	
}
