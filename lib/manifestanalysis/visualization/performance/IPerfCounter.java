/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performance;

import gnu.trove.map.TObjectIntMap;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;

/**
 * @author aadrians
 * May 17, 2012
 *
 */
public interface IPerfCounter <N extends Manifest>{
	public static short THROUGHPUTTIME = 0;
	public static short WAITINGTIME = 1;
	public static short SOJOURNTIME = 2;
	public static short MULTIPLIER = 16;
	
	public void init(N manifest, String timeAtt, Class<?> c, boolean[] caseFilter);
	
	public Place[] getIdx2Place();

	public TObjectIntMap<Place> getPlace2Idx();

	public Transition[] getIdx2Trans();

	public TObjectIntMap<Transition> getTrans2Idx();

	public XEventClass[] getIdx2Resource();

	public TObjectIntMap<XEventClass> getResource2Idx();

	public int getCaseNonFittingFreq();

	public int getCaseProperlyStartedFreq();

	public double getCaseThroughputAvg();

	public double getCaseThroughputMax();

	public double getCaseThroughputMin();

	public double getCaseThroughputStdDev();

	public int getCaseTotalFreq();

	public double[] getPatternInfoDouble();

	public int[] getPatternInfoInt();

	public int[] getMoveModelOnlyCounter();

	public double[] getPlaceInfoDouble();

	public int[] getPlaceInfoInt();

	public void calculatePerformance(N manifest, String timeAtt, Class<?> bindedClass, boolean[] caseFilter);

	public void calculatePerformance(N manifest, boolean[] caseFilter);

	public double getCasePeriod();

	public int getNumPlaces();

	public int getNumTrans();

	public Transition[] getTransArray();

	public Place[] getPlaceArray();

	public double[] getPlaceStats(int encodedPlaceID);

	public double[] getTransStats(N manifest, int encodedTransID);

	public String getPatternString(N manifest, short patternID);

	public double getMoveModelOfTrans(int encodedTransID);

	public int getUniqueCaseMoveModelOfTrans(int encodedTransID);

	public int getEncOfTrans(Transition trans);

	public int getEncOfPlace(Place place);
	
	public long[] getCaseThroughputTime();
	
	public int getFreqPatternWaitingTime(int patternIDOfManifest);
	
	public int getFreqPatternSojournTime(int patternIDOfManifest);
	
	
	
}
