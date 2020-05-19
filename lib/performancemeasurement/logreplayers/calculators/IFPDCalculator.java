/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.calculators;

import java.util.Date;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.fuzzyperformancediagram.FPDNode;

/**
 * Interface for a class which calculate performance values in FPD
 * 
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 10, 2009
 */
public interface IFPDCalculator {
	public static double UNIDENTIFIED_AVG = -1; // a value which sign that average can't be obtained (division by zero)

	/**
	 * Calculate performance values from a case to update total calculation in
	 * FPD
	 * 
	 * @param listFPDNode
	 * @param listEventClass
	 * @param listEventTypes
	 * @param listTimestamp
	 * @param listResources
	 * @param caseID
	 */
	public void updateCalculation(List<FPDNode> listFPDNode, List<XEventClass> listEventClass,
			List<Integer> listEventTypes, List<Date> listTimestamp, List<String> listResources, int caseID);

	/**
	 * Finalize calculation after all cases are calculated.
	 */
	public void finalizeCalculation();
}
