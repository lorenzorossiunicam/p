package org.processmining.plugins.psd.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.log.logabstraction.LogRelations;
import org.processmining.plugins.psd.model.pattern.PSDPattern;
import org.processmining.plugins.psd.model.sequence.PSDSequence;
import org.processmining.plugins.psd.utils.PSDLogUtils;

public class PSDModel {

	/**
	 * ArrayList containing the sequences in the log
	 */
	private List<PSDSequence> sequences = new ArrayList<PSDSequence>();
	private List<PSDPattern> patterns = new ArrayList<PSDPattern>();
	/**
	 * Logrelations used
	 */
	private LogRelations relations;
	/**
	 * list to store the names of data-elements of the selected
	 * data-element/component type
	 */
	private List<String> dataElts = new ArrayList<String>();
	Date beginTotal = null;
	Date endTotal = null;

	public PSDModel(PluginContext context, XLog log) {
		try {
			relations = context.tryToFindOrConstructFirstObject(LogRelations.class, null, null, log);
		} catch (ConnectionCannotBeObtained e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Replays the log to derive the sequence diagram and the pattern diagram
	 */
	public void construct(XLog log, XLogInfo logInfo, XEventClassifier classifier, double noiseRatio,
			String dataEltType, boolean isStrict, int[] selectedInstanceIndices) {
		//		patternDrawn = false;
		Date endDate = null;
		patterns.clear();
		sequences.clear();
		dataElts.clear();
		if (relations != null) {
			Map<Pair<XEventClass, XEventClass>, Double> causalPrecedences = relations.getCausalDependencies();
			//			DoubleMatrix2D causalMatrix = relations..getCausalFollowerMatrix();
			//still have to reset, even though instanceIterator() is used
			//			inputLog.reset();
			XEventClasses eventClasses = logInfo.getEventClasses();
			int index = 0, selectedIndex = 0;
			Iterator<XTrace> instances = log.iterator();
			while (instances.hasNext()) {
				//run through process instances
				XTrace pi = instances.next(); //myLog.getInstance(selectedInstanceIndices[index]);
				//only use selected process instances
				if (selectedIndex < selectedInstanceIndices.length && index++ == selectedInstanceIndices[selectedIndex]) {
					selectedIndex++;
					//initialize list that is used to store relations between ates
					List<XEvent[]> relationList = new ArrayList<XEvent[]>();
					//initialize hashmap that maps event numbers to the ates that have that number
					Map<XEventClass, List<XEvent>> numberToAteMap = new HashMap<XEventClass, List<XEvent>>();
					//initialize list that is used to store the event-numbers in the order they
					//appear in the process instance
					List<XEventClass> ateNumbers = new ArrayList<XEventClass>();
					//obtain audit trail entries of the pi
					XTrace ates = pi;
					Iterator<XEvent> it = ates.iterator();
					//initialize variables
					Date firstDate = null;
					int number = 0;
					while (it.hasNext()) {
						//run through audit trail entries
						XEvent ate = it.next();
						//obtain the event number belonging to the ate
						XEventClass eventNumber = eventClasses.getClassOf(ate);
						//keep track of order of appearance of event numbers in each trace
						ateNumbers.add(number, eventNumber);
						//keep track of which ates belong to each eventnumber
						List<XEvent> belongingAtes = new ArrayList<XEvent>();
						if (numberToAteMap.get(eventNumber) != null) {
							//number already occurred
							belongingAtes = numberToAteMap.get(eventNumber);
						}
						belongingAtes.add(ate);
						numberToAteMap.put(eventNumber, belongingAtes);
						boolean noPrecessor = true;
						for (int i = 0; i < number; i++) {
							//obtain event numbers that occured before
							XEventClass num = ateNumbers.get(i);
							//							int num = ((Integer) ateNumbers.get(i)).intValue(); //((Integer) tasks.next()).intValue();
							if (!((get(relations, eventNumber) > 0) && num.equals(eventNumber))) {
								if (get(causalPrecedences, num, eventNumber) > noiseRatio * logInfo.getNumberOfTraces()) {
									//Check if num is the closest causal precessor of eventNumber
									boolean isClosest = true;
									noPrecessor = false;
									for (int j = i + 1; j < number && isClosest; j++) {
										XEventClass k = ateNumbers.get(j);
										if (((get(causalPrecedences, num, k) > noiseRatio
												* logInfo.getNumberOfTraces()) && (get(causalPrecedences
												, k, eventNumber) > noiseRatio
												* logInfo.getNumberOfTraces()))
												|| k.equals(num)) {
											//k is closer causal precessor, or k is the same event
											isClosest = false;
										}
									}
									if (isClosest) {
										//create relation between ate of num and ate of eventNumber
										XEvent[] rl = new XEvent[2];
										List<XEvent> ates0 = numberToAteMap.get(num);
										List<XEvent> ates1 = numberToAteMap.get(eventNumber);
										rl[0] = ates0.get(ates0.size() - 1);
										rl[1] = ates1.get(ates1.size() - 1);
										if (PSDLogUtils.getTimestamp(rl[0]) != null
												&& PSDLogUtils.getTimestamp(rl[1]) != null) {
											//and add it to the relationlist if the timestamps are valid
											relationList.add(rl);
										}
									}
								}
							} else {
								//loop of length one occurred
								XEvent[] rl = new XEvent[2];
								List<XEvent> ates0 = numberToAteMap.get(num);
								try {
									rl[0] = ates0.get(ates0.size() - 1);
									rl[1] = ates0.get(ates0.size() - 2);
									if (PSDLogUtils.getTimestamp(rl[0]) != null && PSDLogUtils.getTimestamp(rl[1]) != null) {
										relationList.add(rl);
									}
								} catch (ArrayIndexOutOfBoundsException e) {
									//Can occur if ates0.size() is 0 or 1
									System.err.println("[PSDPlugin] Exception occurred during sequence diagram build");
								}
							}
						}
						if (noPrecessor) {
							XEvent[] rl = new XEvent[2];
							rl[0] = ate;
							rl[1] = ate;
							relationList.add(rl);
						}
						number++; //occurredNumbers.add(eventNumber);
						String current = PSDLogUtils.getEltOfAte(ate, dataEltType);
						if (current != null) {
							//keep track of the data elements that occurred
							String[] elts = dataElts.toArray(new String[0]);
							Arrays.sort(elts);
							if (Arrays.binarySearch(elts, current) <= -1) {
								//current element is not yet present, so add it
								dataElts.add(current);
							}
						}
						if (PSDLogUtils.getTimestamp(ate) != null) {
							//set endDate to the date of the last audit trail entry in the current
							//process instance
							endDate = PSDLogUtils.getTimestamp(ate);
							//and firstDate to the date of the first audit trail entry
							if (firstDate == null || firstDate.after(PSDLogUtils.getTimestamp(ate))) {
								firstDate = PSDLogUtils.getTimestamp(ate);
							}
						}
					}
					//create sequence
					String piName = PSDLogUtils.getName(pi);
					PSDSequence seq = new PSDSequence(firstDate, endDate, piName);
					if (firstDate == null) {
						System.out.println("[PSDPlugin] Skipped PI " + piName + " as it lacks time information.");
						continue; // Forget about this instance
					} else if (beginTotal == null || firstDate.before(beginTotal)) {
						//first date of all dates in diagram
						beginTotal = firstDate;
					}
					if (endTotal == null || (endDate != null && endDate.after(endTotal))) {
						//last date of all dates in diagram
						endTotal = endDate;
					}
					seq.initializeSequence(relationList, dataEltType, isStrict);
					//generate a random color for the sequence
					Random generator = new Random();
					int randomR = generator.nextInt(256);
					int randomG = generator.nextInt(256);
					int randomB = generator.nextInt(256);
					seq.setColor(new Color(randomR, randomG, randomB));
					//add seq to sequences
					sequences.add(seq);
					compareAndAddToPattern(seq, isStrict);
				}
			}
		}
	}

	private Double get(Map<Pair<XEventClass, XEventClass>, Double> causalDependencies, XEventClass from, XEventClass to) {
		Double d = causalDependencies.get(new Pair<XEventClass, XEventClass>(from, to));
		if (d == null) {
			return 0.0;
		}
		return d;
	}

	private Integer get(LogRelations relations, XEventClass eventNumber) {
		Integer i = relations.getLengthOneLoops().get(eventNumber);
		if (i == null) {
			return 0;
		}
		return i;
	}
	
	/**
	 * Compares the sequence to all existing patterns, until a match is found.
	 * When a match is found, the sequence is added to that pattern. If no match
	 * is found, a new pattern is created based on the sequence
	 * 
	 * @param sequence
	 *            Sequence
	 * 
	 */
	private void compareAndAddToPattern(PSDSequence sequence, boolean isStrict) {
		boolean inPattern = false;
		Iterator<PSDPattern> pts = patterns.listIterator();
		//run through existing patterns
		while (pts.hasNext() && !inPattern) {
			PSDPattern pattern = pts.next();
			if (pattern.compareToSequence(sequence, isStrict)) {
				//sequence matches pattern, add it
				pattern.addSequence(sequence);
				inPattern = true;
			}
		}
		if (!inPattern) {
			//no match with an existing pattern, create a new one
			PSDPattern pattern = new PSDPattern(sequence);
			//and add it to the list of patterns
			patterns.add(pattern);
		}
	}

	public List<PSDSequence> getSequences() {
		return sequences;
	}

	public List<PSDPattern> getPatterns() {
		return patterns;
	}

	public List<String> getDataElts() {
		return dataElts;
	}

	public Date getBeginTotal() {
		return beginTotal;
	}

	public Date getEndTotal() {
		return endTotal;
	}
}

