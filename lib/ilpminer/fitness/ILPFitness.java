package org.processmining.plugins.ilpminer.fitness;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

//@Plugin(name = "ILP Miner Fitness", parameterLabels = { "Log", "Petri net" }, returnLabels = { "Fitness" }, returnTypes = { ILPFitnessResult.class })
public class ILPFitness {
	long time, time2, time3;
	private Marking initialMarking = null;

//	@UITopiaVariant(uiLabel = "Transition Fitness", affiliation = UITopiaVariant.EHV, author = "T. van der Wiel", email = "t.v.d.wiel@student.tue.nl")
//	@PluginVariant(variantLabel = "Transition Fitness", requiredParameterLabels = {
//			0, 1 })
	public ILPFitnessResult doTransitionFitness(UIPluginContext context,
			XLog log, Petrinet net) {
		XEventClasses classes = XLogInfoFactory.createLogInfo(log)
				.getEventClasses();
		Map<Transition, Integer> missing = new HashMap<Transition, Integer>(), consumed = new HashMap<Transition, Integer>(), totalEvents = new HashMap<Transition, Integer>(), failedEvents = new HashMap<Transition, Integer>();
		Map<XEventClass, Transition> map = getMapping(classes, net);
		for (XTrace t : log) {
			Marking m = getInitialMarking(context, net);
			for (XEvent e : t) {
				executeEvent(map.get(classes.getClassOf(e)), net, m, missing,
						consumed, totalEvents, failedEvents);
			}
		}
		ILPFitnessResult result = new ILPFitnessResult(missing, consumed,
				totalEvents, failedEvents);
		ILPFitnessConnection connection = new ILPFitnessConnection(result, log,
				net);
		context.getConnectionManager().addConnection(connection);
		return result;
	}

	private void executeEvent(Transition transition, Petrinet net, Marking m,
			Map<Transition, Integer> missing,
			Map<Transition, Integer> consumed,
			Map<Transition, Integer> totalEvents,
			Map<Transition, Integer> failedEvents) {
		boolean failed = false;
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> a : net
				.getInEdges(transition)) {
			if (a instanceof Arc) {
				Place place = (Place) ((Arc) a).getSource();
				if (m.occurrences(place) == 0) {
					missing.put(transition,
							1 + (missing.get(transition) == null ? 0 : missing
									.get(transition)));
					failed = true;
				} else {
					m.remove(place);
				}
				consumed.put(transition,
						1 + (consumed.get(transition) == null ? 0 : consumed
								.get(transition)));
			}
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> a : net
				.getOutEdges(transition)) {
			if (a instanceof Arc) {
				m.add((Place) ((Arc) a).getTarget());
			}
		}
		totalEvents.put(transition,
				1 + (totalEvents.get(transition) == null ? 0 : totalEvents
						.get(transition)));
		failedEvents.put(transition, (failed ? 1 : 0)
				+ (failedEvents.get(transition) == null ? 0 : failedEvents
						.get(transition)));
	}

	private Map<XEventClass, Transition> getMapping(XEventClasses classes,
			Petrinet net) {
		Map<XEventClass, Transition> map = new HashMap<XEventClass, Transition>();
		for (Transition t : net.getTransitions()) {
			for (XEventClass ec : classes.getClasses()) {
				if (ec.getId().equals(
						t.getAttributeMap().get(AttributeMap.LABEL))) {
					map.put(ec, t);
				}
			}
		}
		return map;
	}

	private Marking getInitialMarking(UIPluginContext context, Petrinet net) {
		Marking clone = new Marking();
		if (initialMarking == null) {
			try {
				InitialMarkingConnection connection = context.getConnectionManager().getFirstConnection(
						InitialMarkingConnection.class, context, net);
				initialMarking = connection.getObjectWithRole(InitialMarkingConnection.MARKING);
			} catch (ConnectionCannotBeObtained ex) {
				initialMarking = new Marking();
			}
		}
		for (Place p : initialMarking) {
			clone.add(p);
		}
		return clone;
	}
}
