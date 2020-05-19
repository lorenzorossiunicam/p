package org.processmining.plugins.ywl.replayer.visualization;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class SequenceEventsConformanceInfo {
	// level of events
	private int numOfNotReplayableEvents = 0;	// number of events that can not be replayed
	private int numOfUncoveredEvents = 0;	// number of events that are not covered by process model
	private int numOfEvents = 0; // total number of events

	// level of type of events (activity)
	private int numOfNotReplayableActivities = 0;	// number of unique type of events that can not be replayed
	private int numOfUncoveredActivities = 0;	// number of unique type of events that are not covered by process model
	private int numOfUncoveredAndNotReplayableActivities = 0; // union of numOfNotFittingActivities and numOfUncoveredActivities 
	
	// set of event types
	private Set<XEventClass> setUnreplayableEventClass = new HashSet<XEventClass>();
	private Set<XEventClass> setUncoveredEventClass = new HashSet<XEventClass>();
	private Set<XEventClass> setAllEventClass = new HashSet<XEventClass>();
	
	
	public void appendUnreplayableEventClass(Set<XEventClass> newSet){
		setUnreplayableEventClass.addAll(newSet);
	}
	
	public void appendUncoveredEventClass(Set<XEventClass> newSet){
		setUncoveredEventClass.addAll(newSet);
	}
	
	public void appendEventClass(Set<XEventClass> newSet){
		setAllEventClass.addAll(newSet);
	}
	
	/**
	 * @return the setUnreplayableEventClass
	 */
	public Set<XEventClass> getSetUnreplayableEventClass() {
		return setUnreplayableEventClass;
	}

	/**
	 * @param setUnreplayableEventClass the setUnreplayableEventClass to set
	 */
	public void setSetUnreplayableEventClass(Set<XEventClass> setUnreplayableEventClass) {
		this.setUnreplayableEventClass = setUnreplayableEventClass;
	}

	/**
	 * @return the setUncoveredEventClass
	 */
	public Set<XEventClass> getSetUncoveredEventClass() {
		return setUncoveredEventClass;
	}

	/**
	 * @param setUncoveredEventClass the setUncoveredEventClass to set
	 */
	public void setSetUncoveredEventClass(Set<XEventClass> setUncoveredEventClass) {
		this.setUncoveredEventClass = setUncoveredEventClass;
	}

	/**
	 * @return the setAllEventClass
	 */
	public Set<XEventClass> getSetAllEventClass() {
		return setAllEventClass;
	}

	/**
	 * @param setAllEventClass the setAllEventClass to set
	 */
	public void setSetAllEventClass(Set<XEventClass> setAllEventClass) {
		this.setAllEventClass = setAllEventClass;
	}

	public void clear(){
		numOfNotReplayableEvents = 0;	
		numOfUncoveredEvents = 0;
		numOfEvents = 0;
		numOfNotReplayableActivities = 0;
		numOfUncoveredActivities = 0;
		numOfUncoveredAndNotReplayableActivities = 0; 
		
		setUnreplayableEventClass.clear();
		setUncoveredEventClass.clear();
		setAllEventClass.clear();
	}

	/**
	 * @return the numOfNotReplayableEvents
	 */
	public int getNumOfNotReplayableEvents() {
		return numOfNotReplayableEvents;
	}

	/**
	 * @param numOfNotReplayableEvents the numOfNotReplayableEvents to set
	 */
	public void setNumOfNotReplayableEvents(int numOfNotReplayableEvents) {
		this.numOfNotReplayableEvents = numOfNotReplayableEvents;
	}

	/**
	 * @return the numOfUncoveredEvents
	 */
	public int getNumOfUncoveredEvents() {
		return numOfUncoveredEvents;
	}

	/**
	 * @param numOfUncoveredEvents the numOfUncoveredEvents to set
	 */
	public void setNumOfUncoveredEvents(int numOfUncoveredEvents) {
		this.numOfUncoveredEvents = numOfUncoveredEvents;
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

	/**
	 * @return the numOfNotReplayableActivities
	 */
	public int getNumOfNotReplayableActivities() {
		return numOfNotReplayableActivities;
	}

	/**
	 * @param numOfNotReplayableActivities the numOfNotReplayableActivities to set
	 */
	public void setNumOfNotReplayableActivities(int numOfNotReplayableActivities) {
		this.numOfNotReplayableActivities = numOfNotReplayableActivities;
	}

	/**
	 * @return the numOfUncoveredActivities
	 */
	public int getNumOfUncoveredActivities() {
		return numOfUncoveredActivities;
	}

	/**
	 * @param numOfUncoveredActivities the numOfUncoveredActivities to set
	 */
	public void setNumOfUncoveredActivities(int numOfUncoveredActivities) {
		this.numOfUncoveredActivities = numOfUncoveredActivities;
	}

	/**
	 * @return the numOfUncoveredAndNotReplayableActivities
	 */
	public int getNumOfUncoveredAndNotReplayableActivities() {
		return numOfUncoveredAndNotReplayableActivities;
	}

	/**
	 * @param numOfUncoveredAndNotReplayableActivities the numOfUncoveredAndNotReplayableActivities to set
	 */
	public void setNumOfUncoveredAndNotReplayableActivities(int numOfUncoveredAndNotReplayableActivities) {
		this.numOfUncoveredAndNotReplayableActivities = numOfUncoveredAndNotReplayableActivities;
	}

}