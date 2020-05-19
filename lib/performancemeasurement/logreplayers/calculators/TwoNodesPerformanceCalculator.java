/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.calculators;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.dataelements.TwoFPDNodesPerformanceData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 6, 2009
 */
public class TwoNodesPerformanceCalculator {
	// internal data for calculation
	private TwoFPDNodesPerformanceData twoNodesPerformanceData;

	// calculation for a case
	private final Set<FPDNode> possibleNodes;

	public TwoNodesPerformanceCalculator(FPD fpd) {
		// initiate twoNodesPerformance
		twoNodesPerformanceData = new TwoFPDNodesPerformanceData(fpd);

		// initiate internal data which are needed to calculate
		possibleNodes = new HashSet<FPDNode>();
		for (FPDNode node : fpd.getNodes()) {
			possibleNodes.add(node);
		}
	}

	/**
	 * @return the twoNodesPerformanceData
	 */
	public TwoFPDNodesPerformanceData getTwoNodesPerformanceData() {
		return twoNodesPerformanceData;
	}

	/**
	 * @param twoNodesPerformanceData
	 *            the twoNodesPerformanceData to set
	 */
	public void setTwoNodesPerformanceData(TwoFPDNodesPerformanceData twoNodesPerformanceData) {
		this.twoNodesPerformanceData = twoNodesPerformanceData;
	}

	/**
	 * update calculation of FPDNode performance
	 * 
	 * @param listFPDNode
	 * @param listTimestamp
	 * @param isFitCase
	 */
	public void updateCalculation(List<FPDNode> listFPDNode, List<Date> listTimestamp, boolean isFitCase) {
		List<FPDNode> filteredNodes = new LinkedList<FPDNode>();
		List<Date> filteredNodesTimestamps = new LinkedList<Date>();

		int counter = 0;
		while ((counter < listFPDNode.size()) && (filteredNodes.size() < possibleNodes.size())) {
			if (!filteredNodes.contains(listFPDNode.get(counter)) && (listFPDNode.get(counter) != null)) {
				filteredNodes.add(listFPDNode.get(counter));
				filteredNodesTimestamps.add(listTimestamp.get(counter));
			}
			counter++;
		}

		// after all nodes are unique
		for (int i = 0; i < filteredNodes.size() - 1; i++) {
			for (int j = i + 1; j < filteredNodes.size(); j++) {
				twoNodesPerformanceData.addPerformanceValue(filteredNodes.get(i), filteredNodes.get(j),
						filteredNodesTimestamps.get(j).getTime() - filteredNodesTimestamps.get(i).getTime());
				twoNodesPerformanceData.addPerformanceValue(filteredNodes.get(j), filteredNodes.get(i),
						filteredNodesTimestamps.get(i).getTime() - filteredNodesTimestamps.get(j).getTime());

				// update validity and frequency
				twoNodesPerformanceData.updateFrequencyAndFittingCase(filteredNodes.get(i), filteredNodes.get(j),
						isFitCase);
				twoNodesPerformanceData.updateFrequencyAndFittingCase(filteredNodes.get(j), filteredNodes.get(i),
						isFitCase);
			}
		}
	}
}
