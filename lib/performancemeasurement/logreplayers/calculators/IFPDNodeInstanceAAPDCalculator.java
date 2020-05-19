/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.calculators;

import java.util.Date;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.fuzzyperformancediagram.FPDNode;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 11, 2009
 */
public interface IFPDNodeInstanceAAPDCalculator {
	public void setCaseStartTime(Date startTime);

	public void addEventClass(XEventClass eventClass, int eventTypeIdentifier, Date timestamp, FPDNode fpdNode,
			int caseID);

	public int finalizeNodeInstance(FPDNode currentNode); // this is called when calculation on a node is finished

	public Map<FPDNode, Integer> finalizeAllNodeInstance();

	public int getNumInstanceBars(FPDNode node, XEventClass eventClass);

	public AAPD getAAPD();

	public Map<FPDNode, AAPDElement> getMapFPDNodeToAAPDElement();

	public void finalizeAAPDBoundaryCalculation();
}
