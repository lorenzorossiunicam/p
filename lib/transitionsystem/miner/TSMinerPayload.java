package org.processmining.plugins.transitionsystem.miner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.util.collection.TreeMultiSet;
import org.processmining.plugins.transitionsystem.miner.modir.TSMinerModirInput;
import org.processmining.plugins.transitionsystem.miner.util.TSDirections;

/**
 * Transition System Miner Payload
 * 
 * These payloads are used as identifier objects for the states.
 * 
 * @author Eric Verbeek
 * @version 0.1
 */

public class TSMinerPayload {

	/**
	 * A payload for every 'modir' (MODe and DIRection).
	 */
	private final Map<XEventClassifier, Map<TSDirections, Collection<String>>> modirPayloads = new HashMap<XEventClassifier, Map<TSDirections, Collection<String>>>();

	/**
	 * To store attributes and values.
	 */
	private final Map<String, String> attributePayloads;

	/**
	 * The settings used to obtain this payload. To store the settings for every
	 * payload seems like overkill, but because we have the settings we can
	 * nicely forget about abstractions that were not selected. Thus, by adding
	 * the 1 handle (settings) we can remove 4 (the abstractions that were not
	 * selected).
	 * 
	 * BVD: Since the settings may refer to this object itself, if this is an
	 * initial, or final state, this field has to be put behind the payloads.
	 * This way, when deserializing, the payloads are loaded before, which
	 * implies that a hashCode can be computed, even if this object is not
	 * finished yet.
	 */
	private final TSMinerInput settings;

	/**
	 * Create an initial payload given the settings.
	 * 
	 * @param settings
	 *            The settings.
	 */
	public TSMinerPayload(TSMinerInput settings) {
		this.settings = settings;

		for (XEventClassifier classifier : settings.getClassifiers()) {
			modirPayloads.put(classifier, new HashMap<TSDirections, Collection<String>>(2));
			for (TSDirections direction : TSDirections.values()) {
				TSMinerModirInput horizonSettings = settings.getModirSettings(direction, classifier);
				if (horizonSettings.getUse()) {
					/**
					 * User has selected Sequence, Set, of Bag for this mode.
					 * Based on the selection, create suitable collection.
					 */
					switch (horizonSettings.getAbstraction()) {
						case SEQUENCE : {
							/**
							 * For a sequence, an ArrayList should do fine.
							 */
							modirPayloads.get(classifier).put(direction, new ArrayList<String>());
							break;
						}
						case SET : {
							/**
							 * For a set, a TreeSet should do (which sorts the
							 * string, which is nice).
							 */
							modirPayloads.get(classifier).put(direction, new TreeSet<String>());
							break;
						}
						case FIXED_LENGTH_SET : {
							/**
							 * For a fixed length set, a TreeSet should do (which sorts the
							 * string, which is nice).
							 */
							modirPayloads.get(classifier).put(direction, new TreeSet<String>());
							break;
						}
						case BAG : {
							/**
							 * For a bag, we take a sorted multiset.
							 */
							modirPayloads.get(classifier).put(direction, new TreeMultiSet<String>());
							break;
						}
					}
				}
			}
		}
		attributePayloads = new TreeMap<String, String>();
	}

	/**
	 * Gets the attribute payload.
	 * 
	 * @return The attribute payload.
	 */
	Map<String, String> getAttributePayload() {
		return attributePayloads;
	}

	/**
	 * Gets the settings used to obtain this payload.
	 * 
	 * @return The settings used.
	 */
	public TSMinerInput getSettings() {
		return settings;
	}

	/**
	 * Add the given string to the payload of the given modir.
	 * 
	 * @param classifier
	 *            The given classifier.
	 * @param direction
	 *            The given direction (BACKWARD, FORWARD).
	 * @param s
	 *            The given string.
	 */
	void add(XEventClassifier classifier, TSDirections direction, String s) {
		modirPayloads.get(classifier).get(direction).add(s);
	}
	
	/**
	 * New method to know the number of elements on the collection, used for some abstractions.
	 * @param classifier
	 * @param direction
	 * @return size
	 */
	int getSize(XEventClassifier classifier, TSDirections direction) {
		return modirPayloads.get(classifier).get(direction).size();
	}
	

	/**
	 * Gets a string representation for this payload.
	 * 
	 * @return The string representation for this payload.
	 */
	public String toString2() {
		/**
		 * s holds the result so far.
		 * 
		 * As from 2009-01-26, an HTML text is returned, which results in
		 * slightly higher but less wide state in the resulting transition
		 * system.
		 */
		String s = "<html>";
		/**
		 * d acts as field separator: '['for the first, ',' for the rest.
		 */
		String d = ""; // "["

		/**
		 * Append all relevant modir values to s.
		 */
		for (XEventClassifier classifier : settings.getClassifiers()) {
			for (TSDirections direction : TSDirections.values()) {
				if (settings.getModirSettings(direction, classifier).getUse()) {
					s += d + "<b>" + classifier + " " + direction.getLabel() + "</b>: "
							+ modirPayloads.get(classifier).get(direction).toString();
					d = "<br>"; // ","
				}
			}
		}
		/**
		 * Append all relevant data attribute values to s.
		 */
		if (settings.getUseAttributes()) {
			s += d + "<b>Attributes</b>: " + attributePayloads.toString();
			// d = "<br>"; // ","
		}
		/**
		 * Wrap up.
		 */
		if (d == "") {
			/**
			 * Nothing got appended. Return empty record.
			 */
			s = "<html></html>";
		} else {
			/**
			 * Append matching ']'.
			 */
			s += "</html>";
		}
		/**
		 * That's it!
		 */
		return s;
	}

	public String toString() {
		String s = "";
		String d = "";
		for (XEventClassifier classifier : settings.getClassifiers()) {
			for (TSDirections direction : TSDirections.values()) {
				if (settings.getModirSettings(direction, classifier).getUse()) {
					s = s + d + modirPayloads.get(classifier).get(direction).toString();
					d = ",";
				}
			}
		}
		if (settings.getUseAttributes()) {
			s += d + attributePayloads.toString();
		}
		return s;
	}
	
	/**
	 * Compares this payload to another payload.
	 * 
	 * @param object
	 *            Object the other payload.
	 * @return int
	 */
	public int compareTo(TSMinerPayload payload) {
		return toString().compareTo(payload.toString());
	}

	/**
	 * Whether the given object is equal to this object.
	 */
	public boolean equals(Object object) {
		if (object instanceof TSMinerPayload) {
			TSMinerPayload payload = (TSMinerPayload) object;
			return modirPayloads.equals(payload.modirPayloads) 
					&& attributePayloads.equals(payload.attributePayloads);
		}
		return false;
	}

	/**
	 * Hashcode for this object.
	 */
	public int hashCode() {
		return modirPayloads.hashCode() + 37 * attributePayloads.hashCode();
	}
}
