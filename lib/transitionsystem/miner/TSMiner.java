package org.processmining.plugins.transitionsystem.miner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.plugins.transitionsystem.converter.TSConverter;
import org.processmining.plugins.transitionsystem.converter.TSConverterInput;
import org.processmining.plugins.transitionsystem.converter.util.TSConversions;
import org.processmining.plugins.transitionsystem.miner.util.TSMinerLog;

/**
 * Transition System Miner.
 * 
 * Mines a log for a transition system.
 * 
 * @author Eric Verbeek
 * @version 0.1
 * 
 */

public class TSMiner {

	/**
	 * The context of this miner.
	 */
	private final PluginContext context;

	/**
	 * Cache for all events in one trace.
	 */
	//private TSEventCache eventCache;

	/**
	 * Creates a miner, given its context.
	 * 
	 * @param context
	 */
	public TSMiner(final PluginContext context) {
		this.context = context;
		//eventCache = new TSEventCache();
	}

	/**
	 * Mines a transition system according to the given settings.
	 * 
	 * @param settings
	 *            The given settings, which includes the log to mine.
	 * @return The mining result, which includes the mined transition system.
	 */
	public TSMinerOutput mine(final TSMinerInput settings) {
		TSMinerPayloadHandler payloadHandler = new TSMinerPayloadHandler(settings);
		/**
		 * All results from the mining stage are stored in the input for the
		 * conversion stage.
		 */
		TSConverterInput converterSettings = settings.getConverterSettings();
		TSMinerTransitionSystem ts = new TSMinerTransitionSystem("", payloadHandler);
		converterSettings.setTransitionSystem(ts);
		DirectedGraphElementWeights weights = converterSettings.getWeights();
		StartStateSet starts = converterSettings.getStarts();
		AcceptStateSet accepts = converterSettings.getAccepts();

		int stateCtr = 0;
		int traceCounter = 0;
		int percCounter = 0;

		boolean truncated = false;
		/**
		 * Mining stage.
		 */
		XLog log = settings.getLog();
		int nofTraces = TSMinerLog.getTraces(log).size();
		context.getProgress().setMinimum(0);
		/**
		 * For every trace a tick on the progress bar, and an extra tick for the
		 * modification phase.
		 */
		context.getProgress().setMaximum(nofTraces + 1);
		context.log("Constructing initial transition system");
		context.getProgress().setIndeterminate(false);
		for (XTrace trace : TSMinerLog.getTraces(log)) {

			// Cache all events in this trace. This prevents reading the same events over and over again.
			//eventCache = new XEvent[trace.size()];
			//for (int i = 0; i < trace.size(); i++) {
			//	eventCache[i] = trace.get(i);
			//}

			for (int i = 0; i < trace.size(); i++) {

				/**
				 * An Xevent corresponds to a transition in the transition
				 * system. First, construct the payload of the state preceding
				 * the transition.
				 */
				TSMinerPayload fromPayload = (TSMinerPayload) payloadHandler.getSourceStateIdentifier(trace, i);

				/**
				 * Second, in a similar way, create the payload of the state
				 * succeeding the transition.
				 */
				TSMinerPayload toPayload = (TSMinerPayload) payloadHandler.getTargetStateIdentifier(trace, i);

				if (stateCtr > settings.getMaxStates()) {
					if (ts.getNode(fromPayload) == null) {
						if (!truncated) {
							truncated = true;
							if (context instanceof UIPluginContext) {
								JOptionPane.showMessageDialog(null,
										"This transition system contains too many states, and will be truncated.");
							}
						}
						continue;
					}
					if (ts.getNode(toPayload) == null) {
						if (!truncated) {
							truncated = true;
							JOptionPane.showMessageDialog(null,
									"This transition system contains too many states, and will be truncated.");
						}
						continue;
					}
				}

				/**
				 * Create both states with the constructed payloads.
				 */
				if (ts.addState(fromPayload)) {
					stateCtr++;
					ts.getNode(fromPayload).getAttributeMap().put(AttributeMap.LABEL, String.valueOf(stateCtr));
					ts.getNode(fromPayload).getAttributeMap().put(AttributeMap.TOOLTIP, fromPayload.toString());
				}
				weights.add(fromPayload, 1);
				if (ts.addState(toPayload)) {
					stateCtr++;
					ts.getNode(toPayload).getAttributeMap().put(AttributeMap.LABEL, String.valueOf(stateCtr));
					ts.getNode(toPayload).getAttributeMap().put(AttributeMap.TOOLTIP, toPayload.toString());
				}
				weights.add(toPayload, 1);

				/**
				 * Create the transition. Add label if not filtered out.
				 */
				XEvent event = payloadHandler.getSequenceElement(trace, i);
				Object transitionIdentifier = payloadHandler.getTransitionIdentifier(event);

				/**
				 * Note: if the transition already exists, a new one will not be
				 * added.
				 */
				ts.addTransition(fromPayload, toPayload, transitionIdentifier);
				weights.add(fromPayload, toPayload, transitionIdentifier, 1);

				/**
				 * Update start payloads and/or accept payloads if necessary.
				 */
				if (i == 0) {
					starts.add(fromPayload);
				}
				if (i == trace.size() - 1) {
					accepts.add(toPayload);
				}
			}
			//context.getProgress().inc();
			traceCounter++;
			if ((100 * traceCounter / (nofTraces + 1)) > percCounter) {
				context.getProgress().setValue(traceCounter);
				percCounter++;
			}
		}

		//		context.log("Weights after mining: " + converterSettings.getWeights().toString());

		context.log("Converting transition system");
		/**
		 * Conversion stage.
		 */
		TSConverter converter = new TSConverter(context);
		TSMinerOutput output = converter.convert(converterSettings, false);
		context.getProgress().setValue(nofTraces + 1); // We're done.

		//		context.log("Weights after converting: " + output.getWeights().toString());
		context.log("Done!");

		boolean useSettings = true;
		if (ts != output.getTransitionSystem()) {
			/*
			 * Reduction rules have been applied. This may affect states and/or
			 * transitions.
			 */
			if (converterSettings.getUse(TSConversions.EXTEND)) {
				/*
				 * Transitions may have been added.
				 */
				//useSettings = false;
			}
			if (converterSettings.getUse(TSConversions.MERGEBYINPUT)) {
				/*
				 * States may have been merged.
				 */
				//useSettings = false;
			}
			if (converterSettings.getUse(TSConversions.MERGEBYOUTPUT)) {
				/*
				 * States may have been merged.
				 */
				//useSettings = false;
			}
		}

		if (useSettings) {
			//			System.out.println("Creating provided object for settings");
			context.getProvidedObjectManager().createProvidedObject("TS Miner settings", settings, TSMinerInput.class,
					context);
			//			System.out.println("Created provided object for settings");
		}

		//		System.out.println("Creating connection");
		context.addConnection(new TransitionSystemConnection(output.getTransitionSystem(), output.getWeights(),
				output.getStarts(), output.getAccepts(), useSettings ? settings : null));
		//		System.out.println("Created connection");

		return output;
	}

	/**
	 * Creates a log based on the selected states and transitions together with
	 * the settings as used for mining the transition system.
	 * 
	 * @param settings
	 *            Settings used for obtaining the transition system. Includes
	 *            the log to filter.
	 * @param transitionSystem
	 *            The transition system used to filter the log.
	 * @param selectedStates
	 *            The selected states.
	 * @param selectedTransitions
	 *            The selected transitions.
	 * @param filterOnAll
	 *            Whether a filtered trace covers all selected objects (true) or
	 *            any (false).
	 * @return The filtered log.
	 */
	public XLog filter(final TSMinerInput settings, TransitionSystem transitionSystem, Collection<State> selectedStates,
			Collection<Transition> selectedTransitions, int treshold) {
		/*
		 * Get a payload handler for the given settings. This allows us to
		 * compute payloads.
		 */
		TSMinerPayloadHandler payloadHandler = new TSMinerPayloadHandler(settings);
		/*
		 * Create a copy of the log. Unwanted traces will be removed afterwards.
		 */
		XLog log = (XLog) settings.getLog().clone();
		/*
		 * Create a set of unwanted traces.
		 */
		Collection<XTrace> tracesToRemove = new ArrayList<XTrace>();
		/*
		 * Check for every trace whether it is wanted or not.
		 */
		for (XTrace trace : TSMinerLog.getTraces(log)) {
			/*
			 * Copy all selected objects. The copy holds all objects that can be
			 * matched.
			 */
			Collection<State> states = new HashSet<State>(selectedStates);
			Collection<Transition> transitions = new HashSet<Transition>(selectedTransitions);
			for (int i = 0; i < trace.size(); i++) {
				/*
				 * Compute the from and to state.
				 */
				TSMinerPayload fromPayload = (TSMinerPayload) payloadHandler.getSourceStateIdentifier(trace, i);
				State fromState = transitionSystem.getNode(fromPayload);
				TSMinerPayload toPayload = (TSMinerPayload) payloadHandler.getTargetStateIdentifier(trace, i);
				State toState = transitionSystem.getNode(toPayload);
				/*
				 * Remove the from and to states, if present.
				 */
				states.remove(fromState);
				states.remove(toState);
				/*
				 * Compute the transition.
				 */
				XEvent event = payloadHandler.getSequenceElement(trace, i);
				Object transitionIdentifier = payloadHandler.getTransitionIdentifier(event);
				Transition transition = transitionSystem.findTransition(fromPayload, toPayload, transitionIdentifier);
				/*
				 * Remove the transition, if present.
				 */
				transitions.remove(transition);
			}
			if ((states.size() + transitions.size() + treshold) > (selectedStates.size()
					+ selectedTransitions.size())) {
				/*
				 * Not enough elements covered: remove from log.
				 */
				tracesToRemove.add(trace);
			}
		}
		/*
		 * Remove all unwanted traces from the log.
		 */
		log.removeAll(tracesToRemove);
		/*
		 * Return the log.
		 */
		return log;
	}
}
