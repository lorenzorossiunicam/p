package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XEventImpl;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.payload.event.EventPayloadTransitionSystem;
import org.processmining.models.operationalsupport.Analysis;
import org.processmining.models.operationalsupport.Attributes;
import org.processmining.models.operationalsupport.Prediction;
import org.processmining.models.operationalsupport.Recommendation;
import org.processmining.models.operationalsupport.Request;
import org.processmining.models.operationalsupport.WorkItem;
import org.processmining.plugins.transitionsystem.miner.util.TSEventCache;
import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;
import org.processmining.plugins.tsanalyzer.annotation.time.Duration;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeStateAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeTransitionSystemAnnotation;

public class TimeTSAnnotationOS {

	private static final String NO_ANNOTATION = "Time information about past traces is not available.";
	private static final String NO_STATE = "Current trace does not resemble past traces.";

	private final EventPayloadTransitionSystem transitionSystem;
	private final TimeTransitionSystemAnnotation tsAnnotation;
	private final AnnotationElementComparator comparator;

	private final ConfidenceInterval elapsedConfidenceInterval;
	private final TSEventCache eventCache;

	public TimeTSAnnotationOS(final TimeTransitionSystemAnnotation tsAnnotation,
			final EventPayloadTransitionSystem transitionSystem, ConfidenceInterval elapsedConfidenceInterval) {
		super();
		this.tsAnnotation = tsAnnotation;
		this.transitionSystem = transitionSystem;
		comparator = new AnnotationElementComparator();
		this.elapsedConfidenceInterval = elapsedConfidenceInterval;
		eventCache = new TSEventCache();
	}

	private Iterable<AnnotationElement> sortRecommendedAnnotations(final Request request) {
		List<AnnotationElement> list = new ArrayList<AnnotationElement>();
		XTrace partialTrace = request.getTrace();
		/**
		 * precede only if current trace can be replayed in the TS
		 */
		State currentState = getCurrentState(partialTrace);
		if (currentState != null) {
			/**
			 * we clone the partial trace because we want to later add events to
			 * it
			 */
			XTrace clone = (XTrace) partialTrace.clone();
			/**
			 * Check for each available item if it is recommended
			 */
			for (WorkItem available : request.getAvailableItems()) {
				/**
				 * now we are generating recommendations for the following
				 * combination of task and transition different recommendations
				 * can be created for different groups of authorized resources.
				 */
				String task = available.getTask();
				StandardModel transition = available.getTransition();
				/**
				 * If there are no authorized resources just try to replay the
				 * task and transition Else, replay all combinations of
				 * task-transition and authorized resources.
				 */
				if (!available.getAuthorizedResources().iterator().hasNext()) {
					XExtendedEvent event = generateEvent(available.getTask(), available.getTransition());
					clone.add(event);
					State target = getCurrentState(clone);
					clone.remove(clone.size() - 1);
					TimeStateAnnotation stateAnnotation = tsAnnotation.getStateAnnotation(target);
					list.add(new AnnotationElement(stateAnnotation, new WorkItem(task, transition)));

				} else {
					/**
					 * in this HashMap we store recommendations that lead to
					 * different states of TS
					 */
					HashMap<State, WorkItem> states = new HashMap<State, WorkItem>();
					/**
					 * go through all authorized resources
					 */
					for (String originator : available.getAuthorizedResources()) {
						/**
						 * make a fake next event and add it to the cloned
						 * current trace, check if the clone now can be
						 * replayed, and remove the added event from the clone
						 */
						XExtendedEvent event = generateEvent(available.getTask(), available.getTransition(), originator);
						clone.add(event);
						State target = getCurrentState(clone);
						clone.remove(clone.size() - 1);
						/**
						 * if the trace with the fake event can be replayed in
						 * the TS, add this resource to the state.
						 */
						if (target != null) {
							WorkItem stateValue = states.get(target);
							if (stateValue == null) {
								stateValue = new WorkItem(task, transition);
								states.put(target, stateValue);
							}
							stateValue.addAuthorizedResource(originator);
						}
					}
					/**
					 * Loop through all combinations State-Resources and create
					 * a eecommendation for each of them.
					 */
					for (Entry<State, WorkItem> entry : states.entrySet()) {
						TimeStateAnnotation stateAnnotation = tsAnnotation.getStateAnnotation(entry.getKey());
						/**
						 * if all resources are authorized, send an empty list
						 * othervise, send the list of authorized resources
						 */
						if (available.getAuthorizedResources().equals(entry.getValue().getAuthorizedResources())) {
							list.add(new AnnotationElement(stateAnnotation, new WorkItem(entry.getValue().getTask(),
									entry.getValue().getTransition())));
						} else {
							list.add(new AnnotationElement(stateAnnotation, entry.getValue()));
						}

					}
				}
			}
		}
		Collections.sort(list, comparator);
		return list;
	}

	Iterable<Recommendation> getRecommendations(final Request request) {
		List<Recommendation> recommendations = new ArrayList<Recommendation>();
		for (AnnotationElement ann : sortRecommendedAnnotations(request)) {
			if (ann != null) {
				/**
				 * First get the information about the event for which we are
				 * generating the recommendation item.
				 */
				WorkItem event = ann.getEvent();
				if (event != null) {

					String msg = event.getTask() + "." + event.getTransition();

					TimeStateAnnotation timeAnnotation = ann.getAnnotation();
					if (timeAnnotation != null) {
						StatisticsAnnotationProperty remaining = timeAnnotation.getRemaining();
						if (remaining != null) {
							String rem = durationToString(remaining.getValue());
							Recommendation rec = new Recommendation("Execute event \"" + msg + "\" to finish in " + rem
									+ ".", event);
							recommendations.add(rec);
							addStatisticsAttribute(rec, remaining);
						} else {
							recommendations
									.add(new Recommendation("For event \"" + msg + "\"." + NO_ANNOTATION, event));
						}
					} else {
						recommendations.add(new Recommendation("For event \"" + msg + "\"." + NO_ANNOTATION, event));
					}
				}
			}
		}
		return recommendations;
	}

	Prediction getPrediction(final Request request) {
		State currentState = getCurrentState(request.getTrace());
		if (currentState != null) {
			TimeStateAnnotation timeAnnotation = tsAnnotation.getStateAnnotation(currentState);
			if (timeAnnotation != null) {
				StatisticsAnnotationProperty remaining = timeAnnotation.getRemaining();
				String rec = durationToString(remaining.getValue());
				Prediction prediction = new Prediction("Remaining execution time is " + rec + ".");
				addStatisticsAttribute(prediction, remaining);
				return prediction;
			}
			return new Prediction(NO_ANNOTATION);
		}
		return new Prediction(NO_STATE);
	}

	private void addStatisticsAttribute(Analysis analysis, StatisticsAnnotationProperty property) {
		Attributes statistics = analysis.getAttributes("Statistics");
		statistics.put("average", durationToString(property.getAverage()));
		statistics.put("std. dev.", durationToString(property.getStandardDeviation()));
		statistics.put("nr. instances", property.getFrequency());
		statistics.put("min", durationToString(property.getMin()));
		statistics.put("max", durationToString(property.getMax()));
	}

	Analysis getElapsedAnalysts(final Request request) {
		XTrace partialTrace = request.getTrace();
		State currentState = getCurrentState(request.getTrace());
		if (currentState != null) {
			TimeStateAnnotation timeAnnotation = tsAnnotation.getStateAnnotation(currentState);
			if (timeAnnotation != null) {
				StatisticsAnnotationProperty elapsed = timeAnnotation.getElapsed();
				final long elapsedTime = getElapsedTime(partialTrace);
				String result = null;
				final int inInterval = elapsedConfidenceInterval.inInterval(elapsedTime, elapsed);
				switch (inInterval) {
					case ConfidenceInterval.ABOVE :
						result = "Alert: This case is to slow!";
						break;
					case ConfidenceInterval.UNDER :
						result = "Alert: This case is to fast!";
						break;
					default :
						result = null;
						break;
				}
				Analysis analysis = null;
				if (result != null) {
					analysis = new Analysis(result);
					Attributes attributes = analysis.getAttributes("Elapsed time");
					attributes.put("current time", durationToString(elapsedTime));
					attributes.put("lower border", durationToString(elapsedConfidenceInterval.getLowerBorder(elapsed)));
					attributes.put("upper border", durationToString(elapsedConfidenceInterval.getUpperBorder(elapsed)));
					addStatisticsAttribute(analysis, elapsed);
				}
				return analysis;
			}
			return new Analysis(NO_ANNOTATION);
		}
		return new Analysis(NO_STATE);
	}

	private long getElapsedTime(XTrace partialTrace) {
		long elapsed = 0;
		try {
			XExtendedEvent first = convertToExtendedEvent(eventCache.get(partialTrace, 0));
			XExtendedEvent last = convertToExtendedEvent(eventCache.get(partialTrace, partialTrace.size() - 1));
			if ((first != null) && (last != null)) {
				Date firstDate = first.getTimestamp();
				Date lastDate = last.getTimestamp();
				if ((firstDate != null) && (lastDate != null)) {
					elapsed = lastDate.getTime() - firstDate.getTime();
				}
			}
		} catch (Exception e) {
			// just do nothing
		}
		return elapsed;
	}

	private State getCurrentState(XTrace partialTrace) {
		State currentState = null;
		if (partialTrace != null) {
			if (partialTrace.isEmpty()) {
				currentState = transitionSystem.getSourceState(partialTrace, 0);
			} else {
				currentState = transitionSystem.getTargetState(partialTrace, partialTrace.size() - 1);
			}
		}
		return currentState;
	}

	private String durationToString(double miliseconds) {
		Double d = new Double(miliseconds);
		Duration duration = new Duration(d.longValue());
		return duration.toString();
	}

	private XExtendedEvent convertToExtendedEvent(XEvent event) {
		if (event != null) {
			return XExtendedEvent.wrap(event);
		}
		return null;
	}

	private XExtendedEvent generateEvent(String task, StandardModel transition, String originator) {
		XExtendedEvent ate = XExtendedEvent.wrap(new XEventImpl());
		ate.setStandardTransition(transition);
		ate.setName(task);
		ate.setResource(originator);
		return ate;
	}

	private XExtendedEvent generateEvent(String task, StandardModel transition) {
		XExtendedEvent ate = XExtendedEvent.wrap(new XEventImpl());
		ate.setStandardTransition(transition);
		ate.setName(task);
		return ate;
	}
}
