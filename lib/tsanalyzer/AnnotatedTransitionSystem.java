package org.processmining.plugins.tsanalyzer;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.payload.event.EventPayloadTransitionSystem;
import org.processmining.plugins.tsanalyzer.annotation.frequency.FrequencyStateAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.frequency.FrequencyTransitionAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.frequency.FrequencyTransitionSystemAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeStateAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeTransitionAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeTransitionSystemAnnotation;


/**
 * @author abolt
 * 
 * Wrapper for all the perspectives that we can annotate in a transition system
 *
 */
public class AnnotatedTransitionSystem {
	
	//list of annotations. Extendable to cost, resource, etc...
	private TimeTransitionSystemAnnotation timeAnnotation;
	private FrequencyTransitionSystemAnnotation frequencyAnnotation;
	private EventPayloadTransitionSystem transitionSystem;
	
	
	public AnnotatedTransitionSystem (EventPayloadTransitionSystem ts) //default constructor
	{
		this(new TimeTransitionSystemAnnotation(), new FrequencyTransitionSystemAnnotation(),ts );
	}
	
	public AnnotatedTransitionSystem (TimeTransitionSystemAnnotation time, FrequencyTransitionSystemAnnotation frequency, EventPayloadTransitionSystem ts)
	{
		timeAnnotation = time;
		frequencyAnnotation = frequency;
		transitionSystem = ts;
	}
	
	public TimeTransitionSystemAnnotation getTimeAnnotation()
	{
		return timeAnnotation;
	}
	public FrequencyTransitionSystemAnnotation getFrequencyAnnotation()
	{
		return frequencyAnnotation;
	}
	public EventPayloadTransitionSystem getTransitionSystem()
	{
		return transitionSystem;
	}
	
	/**
	 * Returns the time annotation object for the given element (state or transition). If the time annotation
	 * object for the element does not exist, a new annotation object is created
	 * for the element and returned.
	 * 
	 * @param state
	 *            for which to find the annotation
	 * @return time annotation for the element
	 */
	public TimeStateAnnotation getTime_StateAnnotation(State state) {
		TimeStateAnnotation stateAnnotation = timeAnnotation.getStateAnnotation(state);
		if (stateAnnotation == null) {
			stateAnnotation = new TimeStateAnnotation(state);
			timeAnnotation.addStateAnnotation(stateAnnotation);
		}
		return stateAnnotation;
	}
	
	public TimeTransitionAnnotation getTime_TransitionAnnotation(Transition transition) {
		TimeTransitionAnnotation transitionAnnotation = timeAnnotation.getTransitionAnnotation(transition);
		if (transitionAnnotation == null) {
			transitionAnnotation = new TimeTransitionAnnotation(transition);
			timeAnnotation.addTransitionAnnotation(transitionAnnotation);
		}
		return transitionAnnotation;
	}
	
	/**
	 * Returns the frequency annotation object for the given element (state or transition). If the frequency annotation
	 * object for the element does not exist, a new annotation object is created
	 * for the element and returned.
	 * 
	 * @param state
	 *            for which to find the annotation
	 * @return frequency annotation for the element
	 */
	public FrequencyStateAnnotation getFrequency_StateAnnotation(State state) {
		FrequencyStateAnnotation stateAnnotation = frequencyAnnotation.getStateAnnotation(state);
		if (stateAnnotation == null) {
			stateAnnotation = new FrequencyStateAnnotation(state);
			frequencyAnnotation.addStateAnnotation(stateAnnotation);
		}
		return stateAnnotation;
	}
	
	public FrequencyTransitionAnnotation getFrequency_TransitionAnnotation(Transition transition) {
		FrequencyTransitionAnnotation transitionAnnotation = frequencyAnnotation.getTransitionAnnotation(transition);
		if (transitionAnnotation == null) {
			transitionAnnotation = new FrequencyTransitionAnnotation(transition);
			frequencyAnnotation.addTransitionAnnotation(transitionAnnotation);
		}
		return transitionAnnotation;
	}
	
	

}
