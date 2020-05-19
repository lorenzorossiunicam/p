package org.processmining.plugins.ilpminer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Represents a prefix closed language (i.e. PCL = {w | exi w2 in Alphabet* : (w
 * w2) in Language}) Uses integer representations for the alphabet.
 * 
 * @author T. van der Wiel
 * 
 */
public class PrefixClosedLanguage {
	protected final Map<Trace, Integer> traces = new HashMap<Trace, Integer>();
	protected final Map<Integer, Integer> weights = new HashMap<Integer, Integer>();

	/**
	 * Constructor. Creates the PCL from a log using a mapping.
	 * 
	 * @param log
	 * @param mapping
	 *            between the eventclasses and an integer
	 * @param eventclasses
	 *            in the log
	 */
	public PrefixClosedLanguage(XLog log, Map<XEventClass, Integer> indices, XEventClasses classes) {
		int index = 0;
		for (XTrace t : log) {
			// we want all prefixes of t (including t)
			// since traces is a set, duplicates should be removed automatically because the trace overrides the equals method
			for (int i = 1; i <= t.size(); i++) {
				Trace trace = new Trace(classes.size());
				for (int j = 0; j < i; j++) {
					trace.addEvent(indices.get(classes.getClassOf(t.get(j))));
				}
				if (!traces.containsKey(trace)) {
					traces.put(trace, index);
					weights.put(index, 1);
					index++;
				} else {
					weights.put(traces.get(trace), weights.get(traces.get(trace)));
				}
			}
		}
	}

	/**
	 * returns the amount of words in the language minus one (the maximum index
	 * of the words)
	 * 
	 * @return last index
	 */
	public int last() {
		return traces.size();
	}

	/**
	 * returns a list containing the amounts by which each letter ocurred in
	 * each word in the language. First index: word index Second index: letter
	 * index
	 * 
	 * @return int[][]
	 */
	public int[][] getTransitionCountMatrix() {
		return getTransitionCountMatrix(0);
	}

	/**
	 * returns a list containing the amounts by which each letter ocurred in
	 * each word in the language. First index: word index Second index: letter
	 * index
	 * 
	 * @param ignoredEventsFromEnd
	 *            - number of ignored letters from the end of the word
	 * @return letter count per word
	 */
	public int[][] getTransitionCountMatrix(int ignoredEventsFromEnd) {
		int[][] matrix = new int[traces.size()][];
		for (Map.Entry<Trace, Integer> trace : traces.entrySet()) {
			matrix[trace.getValue()] = trace.getKey().getEventCount(ignoredEventsFromEnd);
		}
		return matrix;
	}

	public int[] getLastTransitionVector() {
		int[] vector = new int[traces.size()];
		for (Map.Entry<Trace, Integer> trace : traces.entrySet()) {
			vector[trace.getValue()] = trace.getKey().getLast();
		}
		return vector;
	}

	public int getWeight(int index) {
		return weights.get(index);
	}

	/**
	 * returns a list containing the amounts by which each word ocurred in the
	 * language
	 * 
	 * @return weights
	 */
	public int[] getWeights() {
		int[] array = new int[traces.size()];
		for (int i : traces.values()) {
			array[i] = weights.get(i);
		}
		return array;
	}

	public int getWeightsTotal() {
		int total = 0;
		for (int i : getWeights()) {
			total += i;
		}
		return total;
	}

	/**
	 * returns the length of the longest word in the language
	 * 
	 * @return longest word length
	 */
	public int getMaxWordLength() {
		int longest = 0;
		for (Trace t : traces.keySet()) {
			if (t.length() > longest) {
				longest = t.length();
			}
		}
		return longest;
	}

	/**
	 * Internally used to represent a word in the language (consisting of an
	 * ordered list of letters)
	 * 
	 * @author T. van der Wiel
	 * 
	 */
	public class Trace {
		private final ArrayList<Integer> events = new ArrayList<Integer>();
		private final int classesSize;
		protected String id = "id";

		public Trace(int classesSize) {
			this.classesSize = classesSize;
		}

		public void addEvent(int event) {
			events.add(event);
			id += "-" + event;
		}

		public int length() {
			return events.size();
		}

		/**
		 * undefined behavior when ignoredEventsFromEnd > Trace.length()
		 */
		public int[] getEventCount(int ignoredEventsFromEnd) {
			int[] evCount = new int[classesSize];
			for (int i = 0; i < events.size() - ignoredEventsFromEnd; i++) {
				evCount[events.get(i)]++;
			}
			return evCount;
		}

		public int getLast() {
			return events.get(events.size() - 1);
		}

		public boolean equals(Object trace) {
			if ((trace != null) && (trace.getClass() == Trace.class)) {
				return ((Trace) trace).id.equals(id);
			}
			return false;
		}

		public int hashCode() {
			return id.hashCode();
		}

		public String toString() {
			return id;
		}
	}
}
