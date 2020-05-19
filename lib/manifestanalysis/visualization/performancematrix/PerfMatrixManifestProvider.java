/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performancematrix;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.manifestreplayresult.ManifestEvClassPattern;
import org.processmining.plugins.pnalignanalysis.visualization.performancematrix.PerfMatrixStats;

/**
 * Create matrix of transition class x transition class measured from the
 * beginning of a manifest to the end of its manifest pair
 * 
 * @author aadrians Nov 13, 2012
 * 
 */
public class PerfMatrixManifestProvider {

	protected double[] data = null; // min, max, average, stddev
	protected int[] occ = null;
	protected TransClass[] int2transclasses = null;

	public PerfMatrixManifestProvider(ManifestEvClassPattern manifest) {
		// encode transitions
		int2transclasses = manifest.getTransClass2PatternMap().getTransClassEnc();

		// variables to measure averages and standard deviations
		int blockSize = int2transclasses.length * int2transclasses.length;
		data = new double[blockSize * 4];
		Arrays.fill(data, Double.NaN);
		occ = new int[blockSize]; // number of occurrence
		Arrays.fill(occ, 0);

		// calculate elapsed time between transitions
		calculateStats(manifest, blockSize);
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
				while ((j < orderedList.size()) && (bitset.nextClearBit(0) < int2transclasses.length)) {
					ManifestTemp toItem = orderedList.get(j);
					j++;

					if ((!bitset.get(toItem.getEncodedTrans()))
							&& (!usedIndex.get((orderedList.size() * fromItem.getEncodedTrans()) + j))) { // if not set
						bitset.set(toItem.getEncodedTrans()); // set
						usedIndex.set((orderedList.size() * fromItem.getEncodedTrans()) + j); // set

						// calculate performance
						if ((fromItem.getBegin() != null) && (toItem.getEnd() != null)) {
							updateMandSVal(fromItem.getEncodedTrans(), toItem.getEncodedTrans(), data, occ,
									toItem.getEnd() - fromItem.getBegin(), int2transclasses.length);
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

	protected void updateMandSVal(int fromIndex, int toIndex, double[] data, int[] occ, long val, int length) {
		int loc = (length * toIndex) + fromIndex;
		int pos = 4 * loc;
		if (Double.isNaN(data[pos])) {
			data[pos] = val; // min
			data[pos + 1] = val; // max
			data[pos + 2] = val; // mval
			data[pos + 3] = 0; // sval
			occ[loc] = 1; // occurrence
		} else {
			occ[loc]++;
			if (Double.compare(data[pos], val) > 0) {
				data[pos] = val; // min				
			}
			if (Double.compare(data[pos + 1], val) < 0) {
				data[pos + 1] = val; // max
			}
			double oldMVal = data[pos + 2];
			data[pos + 2] += (val - oldMVal) / occ[loc];
			data[pos + 3] += ((val - oldMVal) * (val - data[pos + 2]));
		}
	}

	/**
	 * Get only label of transitions
	 * 
	 * @return
	 */
	public String[] getTransLabel() {
		String[] newStr = new String[int2transclasses.length];
		for (int i = 0; i < int2transclasses.length; i++) {
			newStr[i] = int2transclasses[i].getId();
		}
		return newStr;
	}

	/**
	 * Get number of transitions
	 * 
	 * @return
	 */
	public int getNumTransClasses() {
		return int2transclasses.length;
	}

	/**
	 * get encoding of transitions
	 * 
	 * @return
	 */
	public TransClass[] getInt2TransClasses() {
		return int2transclasses;
	}

	/**
	 * 
	 * @param toEncTransClass
	 *            row index of transitions class
	 * @param fromEncTransClass
	 *            column index of transitions c;ass
	 * @return
	 */
	public double getTimeBetween(int fromEncTransClass, int toEncTransClass, PerfMatrixStats metric) {
		int loc = 4 * ((toEncTransClass * getNumTransClasses()) + fromEncTransClass);
		switch (metric) {
			case MIN :
				return (this.data[loc]);
			case MAX :
				return (this.data[loc + 1]);
			case AVG :
				return (this.data[loc + 2]);
			case STDDEV :
				return (this.data[loc + 3]);
		}
		return Double.NaN;
	}

	/**
	 * Frequency of index i followed by j
	 * @param toEncTransClass
	 * @param fromEncTransClass
	 * @return
	 */
	public int getFrequency(int fromEncTransClass, int toEncTransClass) {
		return (this.occ[(toEncTransClass * getNumTransClasses()) + fromEncTransClass]);
	}

	/**
	 * get all information
	 * 
	 * @return
	 */
	public double[] getAllInfo() {
		double[] data = new double[getNumTransClasses() * getNumTransClasses()];
		Arrays.fill(data, Double.NaN);

		int dataPtr = 0;
		for (int i = 0; i < getNumTransClasses(); i++) {
			for (int j = 0; j < getNumTransClasses(); j++) {
				data[dataPtr] = getTimeBetween(i, j, PerfMatrixStats.AVG);
				dataPtr++;
			}
		}
		return data;
	}

	/**
	 * Construct mapping from trans class to integer index
	 * @return
	 */
	public Map<TransClass, Integer> constructMapTransClass2Int(){
		Map<TransClass, Integer> map = new HashMap<TransClass, Integer>();
		for (int i=0; i < int2transclasses.length; i++){
			map.put(int2transclasses[i], i);
		}
		return map;
	}


}

class ManifestTemp {
	private int encodedTransClass = -1;
	private Long begin = null;
	private Long end = null;

	@SuppressWarnings("unused")
	private ManifestTemp() {
	}

	/**
	 * Default constructor
	 * 
	 * @param trans
	 * @param beginTime
	 */
	public ManifestTemp(int trans, Long beginTime) {
		this.encodedTransClass = trans;
		this.begin = beginTime;
	}

	/**
	 * Default constructor
	 * 
	 * @param trans
	 * @param beginTime
	 */
	public ManifestTemp(int trans, long beginTime, long endTime) {
		this.encodedTransClass = trans;
		this.begin = beginTime;
		this.end = endTime;
	}

	/**
	 * @return the encodedTrans
	 */
	public int getEncodedTrans() {
		return encodedTransClass;
	}

	/**
	 * @param encodedTrans
	 *            the encodedTrans to set
	 */
	public void setEncodedTrans(int encodedTrans) {
		this.encodedTransClass = encodedTrans;
	}

	/**
	 * @return the begin
	 */
	public Long getBegin() {
		return begin;
	}

	/**
	 * @param begin
	 *            the begin to set
	 */
	public void setBegin(Long begin) {
		this.begin = begin;
	}

	/**
	 * @return the end
	 */
	public Long getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(Long end) {
		this.end = end;
	}
}