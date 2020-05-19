package org.processmining.plugins.transitionsystem.miner;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.transitionsystem.payload.event.EventPayloadHandler;
import org.processmining.plugins.transitionsystem.miner.modir.TSMinerModirInput;
import org.processmining.plugins.transitionsystem.miner.util.TSAbstractions;
import org.processmining.plugins.transitionsystem.miner.util.TSDirections;
import org.processmining.plugins.transitionsystem.miner.util.TSEventCache;
import org.processmining.plugins.transitionsystem.miner.util.TSMinerLog;

public class TSMinerPayloadHandler implements EventPayloadHandler {

	private final TSEventCache eventCache;
	private final TSMinerInput settings;

	public TSMinerPayloadHandler(TSMinerInput settings) {
		super();
		eventCache = new TSEventCache();
		this.settings = settings;
	}

	/*
	 * public Object getTransitionIdentifier(List<XEvent> sequence, int i) {
	 * XEvent event = getSequenceElement(sequence, i); String label =
	 * settings.getTransitionClassifier().getClassIdentity(event); if
	 * (settings.getVisibleFilter().contains(label)) { return label; } else {
	 * return ""; } }
	 */

	public Object getSourceStateIdentifier(List<XEvent> sequence, int i) {
		TSMinerPayload payload = new TSMinerPayload(settings);

		/**
		 * Add information for the selected 'modirs' (MODe and DIRection) to the
		 * payload.
		 */
		for (XEventClassifier classifier : settings.getClassifiers()) {
			mineBwd(sequence, i, 1, settings.getModirSettings(TSDirections.BACKWARD, classifier), payload, classifier);
			mineFwd(sequence, i, 0, settings.getModirSettings(TSDirections.FORWARD, classifier), payload, classifier);
		}

		/**
		 * Add information on the selected attributes to the payload.
		 */
		if (settings.getUseAttributes()) {
			for (int j = 0; j < i; j++) {
				XEvent precedingEvent = getSequenceElement(sequence, j); //trace.get(j);
				Map<String, String> dataSection = TSMinerLog.getDataAttributes(precedingEvent);
				for (String attribute : dataSection.keySet()) {
					if (settings.getAttributeFilter().contains(attribute)) {
						payload.getAttributePayload().put(attribute, dataSection.get(attribute));
					}
				}
			}
		}
		return payload;
	}

	public Object getTargetStateIdentifier(List<XEvent> sequence, int i) {
		TSMinerPayload payload = new TSMinerPayload(settings);
		for (XEventClassifier classifier : settings.getClassifiers()) {
			mineBwd(sequence, i, 0, settings.getModirSettings(TSDirections.BACKWARD, classifier), payload, classifier);
			mineFwd(sequence, i, 1, settings.getModirSettings(TSDirections.FORWARD, classifier), payload, classifier);
		}

		if (settings.getUseAttributes()) {
			for (int j = 0; j <= i; j++) {
				XEvent precedingEvent = getSequenceElement(sequence, j); //trace.get(j);
				Map<String, String> dataSection = TSMinerLog.getDataAttributes(precedingEvent);
				for (String attribute : dataSection.keySet()) {
					if (settings.getAttributeFilter().contains(attribute)) {
						payload.getAttributePayload().put(attribute, dataSection.get(attribute));
					}
				}
			}
		}
		return payload;
	}

	/**
	 * Create backward payload for the given event in the given trace.
	 * 
	 * @param context
	 *            The current context.
	 * @param trace
	 *            The given trace
	 * @param i
	 *            The index of the given event in the given trace.
	 * @param offset
	 *            Horizon offset
	 * @param settings
	 *            Settings to use
	 * @param payload
	 *            Payload to create
	 * @param classifier
	 *            Classifier to use
	 */
	private void mineBwd(List<XEvent> trace, int i, int offset, TSMinerModirInput settings, TSMinerPayload payload,
			XEventClassifier classifier) {
		
		if(settings.getAbstraction().equals(TSAbstractions.FIXED_LENGTH_SET)) //used with custom abstractions
			mineBwd_CustomAbs(trace, i, offset, settings, payload, classifier);
		else //standard abstractions
			mineBwd_StdAbs(trace, i, offset, settings, payload, classifier);
	}
	
	//Using standard abstractions (Sequence, Multiset and Set)
	private void mineBwd_StdAbs(List<XEvent> trace, int i, int offset, TSMinerModirInput settings, TSMinerPayload payload,
			XEventClassifier classifier) {
		/**
		 * Skip if these settings should not be used.
		 */
		if (settings.getUse()) {
			/**
			 * Initialize number of visible steps to take.
			 */
			int horizon = settings.getFilteredHorizon();
			for (int j = i - offset; (horizon != 0) && (j >= 0); j--) {
				/**
				 * Get the event.
				 */
				XEvent precedingEvent = getSequenceElement(trace, j);
				/**
				 * Get the info. This depends on mode.
				 */
				String s = classifier.getClassIdentity(precedingEvent);
				if (s == null) {
					s = "";
				}

				/**
				 * Check whether not filtered out.
				 */
				Collection<String> filter = settings.getFilter();
				if (filter != null) {
					if (filter.contains(s)) {
						/**
						 * Not filtered out, add to payload if number of steps
						 * not exceeded.
						 */
						if ((settings.getHorizon() < 0) || (settings.getHorizon() + j + offset > i)) {
							payload.add(classifier, TSDirections.BACKWARD, s);
						}
						/**
						 * Found an unfiltered step: decrease counter.
						 */
						horizon--;
					}
				}
			}
		}
	}
	
	//Using custom abstractions (fixed length set, etc...)
	private void mineBwd_CustomAbs(List<XEvent> trace, int i, int offset, TSMinerModirInput settings, TSMinerPayload payload,
			XEventClassifier classifier) {
		
		if (settings.getUse()) {
			int horizon = settings.getFilteredHorizon();
			for (int j = i - offset; (horizon != 0) && (j >= 0); j--) {
				XEvent precedingEvent = getSequenceElement(trace, j);
				String s = classifier.getClassIdentity(precedingEvent);
				if (s == null) {
					s = "";
				}

				Collection<String> filter = settings.getFilter();
				if (filter != null) {
					if (filter.contains(s)) {
						if ((settings.getHorizon() < 0) || (settings.getHorizon() + j + offset > i)) {
							
							int previous_size = payload.getSize(classifier, TSDirections.BACKWARD); //store the previous abstraction size
							payload.add(classifier, TSDirections.BACKWARD, s);
							
							if(payload.getSize(classifier, TSDirections.BACKWARD) > previous_size) //if the abstraction is now bigger, we decrease the "horizon", if not we keep looking.
								horizon--;
						}
						
					}
				}
			}
		}
	}

	/**
	 * Create forward payload for the given event in the given trace.
	 * 
	 * @param trace
	 *            The given trace
	 * @param i
	 *            The index of the given event in the given trace.
	 * @param offset
	 *            Horizon offset
	 * @param settings
	 *            Settings to use
	 * @param payload
	 *            Payload to create
	 * @param classifier
	 *            Classifier to use
	 */
	private void mineFwd(List<XEvent> trace, int i, int offset, TSMinerModirInput settings, TSMinerPayload payload,
			XEventClassifier classifier) {
		/**
		 * See MineBwd for additional comments.
		 */
		if(settings.getAbstraction().equals(TSAbstractions.FIXED_LENGTH_SET)) //used with custom abstractions
			mineFwd_CustomAbs(trace, i, offset, settings, payload, classifier);
		else //standard abstractions
			mineFwd_StdAbs(trace, i, offset, settings, payload, classifier);				
	}
	
	//Using standard abstractions (Sequence, Multiset and Set)
	private void mineFwd_StdAbs(List<XEvent> trace, int i, int offset, TSMinerModirInput settings, TSMinerPayload payload, XEventClassifier classifier) {
		if (settings.getUse()) {
			int horizon = settings.getFilteredHorizon();
			for (int j = i + offset; (horizon != 0) && (j < trace.size()); j++) {
				XEvent succeedingEvent = getSequenceElement(trace, j);
				String s = classifier.getClassIdentity(succeedingEvent);
				if (s == null) {
					s = "";
				}
				if (settings.getFilter().contains(s)) {
					if ((settings.getHorizon() < 0) || (settings.getHorizon() + i + offset > j)) {
						payload.add(classifier, TSDirections.FORWARD, s);						
					}
					horizon--;
				}
			}
		}		
	}
	
	//Using custom abstractions (fixed length set, etc...)
	private void mineFwd_CustomAbs(List<XEvent> trace, int i, int offset, TSMinerModirInput settings, TSMinerPayload payload, XEventClassifier classifier) {
		if (settings.getUse()) {
			int horizon = settings.getFilteredHorizon();
			for (int j = i + offset; (horizon != 0) && (j < trace.size()); j++) {
				XEvent succeedingEvent = getSequenceElement(trace, j);
				String s = classifier.getClassIdentity(succeedingEvent);
				if (s == null) {
					s = "";
				}
				if (settings.getFilter().contains(s)) {
					if ((settings.getHorizon() < 0) || (settings.getHorizon() + i + offset > j)) {
						
						int previous_size = payload.getSize(classifier, TSDirections.FORWARD); //store the previous abstraction size
						payload.add(classifier, TSDirections.FORWARD, s);	//add element
						
						if(payload.getSize(classifier, TSDirections.FORWARD) > previous_size) //if the abstraction is now bigger, we decrease the "horizon", if not we keep looking.
							horizon--;
					}
					
				}
			}
		}		
	}
	

	public Object getTransitionIdentifier(XEvent event) {
		String label = settings.getTransitionClassifier().getClassIdentity(event);
		if ((label != null) && settings.getVisibleFilter().contains(label)) {
			return label;
		} else {
			return "";
		}
	}

	public XEvent getSequenceElement(List<XEvent> sequence, int i) {
		if (sequence instanceof XEvent) {
			return eventCache.get((XTrace) sequence, i);
		} else {
			return sequence.get(i);
		}
	}
}
