/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performancematrix;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.manifestreplayresult.ManifestEvClassPattern;

/**
 * @author aadrians
 * Nov 14, 2012
 *
 */
public class PerfMatrixManifestProviderBetweenStarts extends PerfMatrixManifestProvider {

	public PerfMatrixManifestProviderBetweenStarts(ManifestEvClassPattern manifest) {
		super(manifest);
	}

	/**
	 * Main method to calculate performance after encoding Override this method
	 * if non-default method of calculation is used
	 * 
	 * @param logReplayResult
	 * @param int2trans
	 * @param trans2int
	 * @param log
	 * @param blockSize
	 */
	protected void calculateStats(final ManifestEvClassPattern manifest, int blockSize) {
		// utilities objects
		XTimeExtension tx = XTimeExtension.instance();
		XLog log = manifest.getLog();
		TransClass2PatternMap transClass2PatternMap = manifest.getTransClass2PatternMap();

		int traceCounter = 0;
		for (XTrace trace : log) {
			// first, serialize manifests
			List<ManifestTemp> orderedList = new ArrayList<ManifestTemp>(trace.size());

			Iterator<XEvent> it = trace.iterator();

			int[] man = manifest.getManifestForCase(traceCounter);
			traceCounter++;

			TIntIntMap manifestCount = new TIntIntHashMap(trace.size(), .9f, -1, -1);
			TIntIntMap manifestOrder = new TIntIntHashMap(trace.size(), .9f, -1, -1);

			int currIdx = 0;
			while (currIdx < man.length) {
				if (man[currIdx] == Manifest.MOVELOG) {
					// ignore the event
					it.next();
					currIdx++;
				} else if (man[currIdx] == Manifest.MOVEMODEL) {
					// add empty move model
					orderedList.add(new ManifestTemp(transClass2PatternMap.getTransClassEncFor(transClass2PatternMap
							.getTransClassOf(manifest.getTransitionOf(man[currIdx + 1]))), null));
					currIdx += 2;
				} else if (man[currIdx] == Manifest.MOVESYNC) {
					// shared variable
					XEvent currEvent = it.next();

					// extract time information 
					long time = tx.extractTimestamp(currEvent).getTime();

					// use the time info to calculate period of pattern
					// check if there is already a manifest with the same id
					int manifestID = man[currIdx + 1];
					int count = manifestCount.get(manifestID);
					if (count < 0) { // not yet counted
						int numEvents = transClass2PatternMap.getPatternIDNumElmts(manifest
								.getPatternIDOfManifest(manifestID));
						short encodedTransClass = transClass2PatternMap.getTransClassEncFor(transClass2PatternMap
								.getTransClassOf(manifest.getTransitionOf(manifest.getEncTransOfManifest(manifestID))));

						if (numEvents == 1) {
							// an instantaneous manifest
							orderedList.add(new ManifestTemp(encodedTransClass, time, time));
						} else {
							// this manifest has tails
							manifestCount.put(manifestID, 1);
							manifestOrder.put(manifestID, orderedList.size());

							orderedList.add(new ManifestTemp(encodedTransClass, time));
						}
					} else {
						// there is already existing manifest
						count++;
						if (count == manifest.getTransClass2PatternMap().getPatternIDNumElmts(
								manifest.getPatternIDOfManifest(manifestID))) {
							// last event
							orderedList.get(manifestOrder.get(manifestID)).setEnd(time);
						} else {
							manifestCount.put(manifestID, count);
						}
					}

					currIdx += 2;
				}
			}

			// then, calculate stats iteratively
			// for all FROM trans, boolean indicates used manifest of the TO trans
			BitSet usedIndex = new BitSet(orderedList.size() * int2transclasses.length);

			// local analysis
			BitSet bitset = new BitSet(int2transclasses.length);
			for (int i = 0; i < orderedList.size(); i++) {
				ManifestTemp fromItem = orderedList.get(i);

				int j = i;
				bitset.clear();
				while ((j < orderedList.size())
						&& (bitset.nextClearBit(0) < int2transclasses.length)) {
					ManifestTemp toItem = orderedList.get(j);
					j++;

					if ((!bitset.get(toItem.getEncodedTrans()))
							&& (!usedIndex.get((orderedList.size() * fromItem.getEncodedTrans()) + j))) { // if not set
						bitset.set(toItem.getEncodedTrans()); // set
						usedIndex.set((orderedList.size() * fromItem.getEncodedTrans()) + j); // set

						// calculate performance
						if ((fromItem.getBegin() != null) && (toItem.getEnd() != null)) {
							updateMandSVal(fromItem.getEncodedTrans(), toItem.getEncodedTrans(), data, occ,
									toItem.getBegin() - fromItem.getBegin(),
									int2transclasses.length);
						}
					}
				}
				;
			}
		}

		// finalize calculation of average and stddev
		for (int i = 0; i < blockSize; i++) {
			int loc = 4 * i;
			data[loc + 3] = Math.sqrt(data[loc + 3] / (occ[i] - 1));
		}
	}
}
