/**
 * 
 */
package org.processmining.plugins.aggregatedactivitiesperformancediagram;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.processmining.framework.util.Pair;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.graphbased.directed.aapdgraph.AAPDGraph;
import org.processmining.models.graphbased.directed.aapdgraph.AAPDGraphFactory;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 18, 2009
 */
public class AAPDGraphGenerator {
	public static int MAX_PIXEL_WIDTH = 5000;

	public static AAPDGraph generateAAPDGraph(AAPDElement aapdFocusElements, AAPD aapd, double maxHeightOfAnElement,
			double minHeightOfAnElement, double minDistanceBetweenElements, double x_scaler, double xElementScaler,
			double yElementScaler, double intersectionBarHeight, int maxXDistanceBetweenElements,
			int minWidthOfAnElement) {
		// temporary variable
		double sumRatio = 0;
		int numBar = 0;

		// control variables
		double powerNumber = 2;

		maxHeightOfAnElement = Math.pow(powerNumber, yElementScaler) * maxHeightOfAnElement;

		// result AAPDGraph
		AAPDGraph graph = AAPDGraphFactory.newAAPDGraph(aapd.getLabel());

		// calculate elements and make them AAPDGraphElements 
		// add other focusElements which are considered, also calculate their position
		Map<AAPDElement, Map<Integer, Pair<BigInteger[], Integer>>> relationWithOthersToBeVisualized = aapd
				.getRelationTime().get(aapdFocusElements);

		// find frequency of focus elements with biggest value
		int maxCaseFreq = aapd.getFocusElementsCaseFrequency().get(aapdFocusElements).get(aapdFocusElements);
		int standardElementFreq = aapd.getRelationTime().get(aapdFocusElements).get(aapdFocusElements).get(
				AAPD.BAR_START_TIME).getSecond();

		// create a sorted list of FocusElements
		List<AAPDElement> listFocusElements = new LinkedList<AAPDElement>();
		List<Double> startTimeList = new LinkedList<Double>();
		List<Integer> occurenceList = new LinkedList<Integer>();
		int biggestFreqDeviation = 0; // this is the reference for half a bar scale

		for (AAPDElement currentElements : relationWithOthersToBeVisualized.keySet()) {
			if (relationWithOthersToBeVisualized.get(currentElements).get(AAPD.BAR_START_TIME).getSecond() > 0) {
				// create sorted list of focus element
				Pair<BigInteger[], Integer> pairOfStartTime = relationWithOthersToBeVisualized.get(currentElements)
						.get(AAPD.BAR_START_TIME);
				double currentStartTime = pairOfStartTime.getFirst()[AAPD.SUM].doubleValue()
						/ pairOfStartTime.getSecond();

				boolean inserted = false;
				for (int i = 0; i < startTimeList.size(); i++) {
					if (startTimeList.get(i).compareTo(currentStartTime) > 0) {
						// insert here
						listFocusElements.add(i, currentElements);
						startTimeList.add(i, currentStartTime);
						occurenceList.add(i, pairOfStartTime.getSecond());
						inserted = true;
						break;
					}
				}
				if (!inserted) {
					listFocusElements.add(currentElements);
					startTimeList.add(currentStartTime);
					occurenceList.add(pairOfStartTime.getSecond());
				}

				// calculate biggest freq deviation
				if (Math.abs(pairOfStartTime.getSecond() - standardElementFreq) > biggestFreqDeviation) {
					biggestFreqDeviation = Math.abs(pairOfStartTime.getSecond() - standardElementFreq);
				}
			}
		}

		// add cells
		int occurenceFocusElement = aapd.getRelationTime().get(aapdFocusElements).get(aapdFocusElements).get(
				AAPD.BAR_THROUGHPUT_TIME).getSecond();

		// search for the distance between case start to focus node
		double leftBoundary = 0.0;
		double rightBoundary = 0.0;
		if (startTimeList.size() > 0) {
			Pair<BigInteger[], Integer> pairOfStartTimeFocusElement = aapd.getRelationTime().get(aapdFocusElements)
					.get(aapdFocusElements).get(AAPD.BAR_START_TIME);
			double startTimeFocusElement = pairOfStartTimeFocusElement.getFirst()[AAPD.SUM].doubleValue()
					/ pairOfStartTimeFocusElement.getSecond();
			double multiplierElement = Math.pow(powerNumber, xElementScaler);
			// try to find the longest positive and negative distance			
			for (int i = 0; i < startTimeList.size(); i++) {
				// waiting time
				Pair<BigInteger[], Integer> pairWaiting = aapd.getRelationTime().get(aapdFocusElements).get(
						listFocusElements.get(i)).get(AAPD.BAR_WAITING_TIME);
				double waitingTime = (pairWaiting.getFirst()[AAPD.SUM].doubleValue() / pairWaiting.getSecond())
						* multiplierElement;

				// service time
				Pair<BigInteger[], Integer> pairService = aapd.getRelationTime().get(aapdFocusElements).get(
						listFocusElements.get(i)).get(AAPD.BAR_SERVICE_TIME);
				double serviceTime = (pairService.getFirst()[AAPD.SUM].doubleValue() / pairService.getSecond())
						* multiplierElement;

				// start time
				double posXStart = 0.0;
				if ((startTimeList.get(i) - startTimeFocusElement) < 0) { // means it is executed before focus element
					posXStart = -1
							* Math.log10(1 + ((startTimeFocusElement - startTimeList.get(i)) * Math.pow(powerNumber,
									x_scaler))) * (startTimeFocusElement - startTimeList.get(i))
							* Math.pow(powerNumber, x_scaler);
					// check left boundary
					if (Double.compare(posXStart, leftBoundary) < 0) {
						leftBoundary = posXStart;
					}
				} else {
					posXStart = Math.log10(1 + ((startTimeList.get(i) - startTimeFocusElement) * Math.pow(powerNumber,
							x_scaler)))
							* (startTimeList.get(i) - startTimeFocusElement) * Math.pow(powerNumber, x_scaler);
				}

				// check right boundary
				double currentDistance = (waitingTime + serviceTime) + posXStart;
				if (Double.compare(currentDistance, rightBoundary) > 0) {
					rightBoundary = currentDistance;
				}
			}

			// check for maximum pixels
			double pixelNormalizer = 1.0;
			if ((rightBoundary - leftBoundary) > MAX_PIXEL_WIDTH) {
				pixelNormalizer = MAX_PIXEL_WIDTH / (rightBoundary - leftBoundary);
			}

			/**
			 * This is where elements are created
			 */
			multiplierElement = Math.pow(powerNumber, xElementScaler) * pixelNormalizer;
			for (int i = 0; i < startTimeList.size(); i++) {
				// real throughput time
				double realThroughputTime = 0.0;
				// waiting time
				Pair<BigInteger[], Integer> pairWaiting = aapd.getRelationTime().get(aapdFocusElements).get(
						listFocusElements.get(i)).get(AAPD.BAR_WAITING_TIME);
				double waitingTime = (pairWaiting.getFirst()[AAPD.SUM].doubleValue() / pairWaiting.getSecond());
				if (Double.compare(waitingTime, 0.0) <= 0) {
					// it does not have waiting time
					waitingTime = 1;
				} else {
					realThroughputTime += waitingTime;
					waitingTime = waitingTime * multiplierElement;
				}

				// service time
				Pair<BigInteger[], Integer> pairService = aapd.getRelationTime().get(aapdFocusElements).get(
						listFocusElements.get(i)).get(AAPD.BAR_SERVICE_TIME);
				double serviceTime = (pairService.getFirst()[AAPD.SUM].doubleValue() / pairService.getSecond());
				if (Double.compare(serviceTime, 0.0) <= 0) {
					// it does not have service time
					serviceTime = 1;
				} else {
					realThroughputTime += serviceTime;
					serviceTime = serviceTime * multiplierElement;
				}

				// intersection time
				Pair<BigInteger[], Integer> pairIntersection = aapd.getRelationTime().get(aapdFocusElements).get(
						listFocusElements.get(i)).get(AAPD.BAR_INTERSECTION_TIME);
				int occurenceCurrentElement = aapd.getRelationTime().get(aapdFocusElements).get(
						listFocusElements.get(i)).get(AAPD.BAR_SERVICE_TIME).getSecond();
				double intersectionTime = (pairIntersection.getFirst()[AAPD.SUM].doubleValue() / (occurenceCurrentElement * occurenceFocusElement));

				// calculate new ordinate
				double heightOfElementsRatio = (double) aapd.getFocusElementsCaseFrequency().get(aapdFocusElements)
						.get(listFocusElements.get(i))
						/ maxCaseFreq;

				// calculate line position of stdElementFrequency
				double lineHeightRatio = (((double) (occurenceList.get(i) - standardElementFreq)) / (double) biggestFreqDeviation); // this ratio should be between -1 and 1;

				// add bar with these specifications
				double posXStart = 0.0;
				if ((startTimeList.get(i) - startTimeFocusElement) < 0) { // means it is executed before focus element
					posXStart = ((-1
							* Math.log10(1 + ((startTimeFocusElement - startTimeList.get(i)) * Math.pow(powerNumber,
									x_scaler))) * (startTimeFocusElement - startTimeList.get(i)) * Math.pow(
							powerNumber, x_scaler)) - leftBoundary)
							* pixelNormalizer;
				} else {
					posXStart = ((Math.log10(1 + ((startTimeList.get(i) - startTimeFocusElement) * Math.pow(
							powerNumber, x_scaler)))
							* (startTimeList.get(i) - startTimeFocusElement) * Math.pow(powerNumber, x_scaler)) - leftBoundary)
							* pixelNormalizer;
				}
				double posYStart = ((sumRatio * maxHeightOfAnElement) + (numBar * (minHeightOfAnElement + minDistanceBetweenElements)));

				Pair<Double, Double> boundaries = aapd.getBoundaries(listFocusElements.get(i));
				graph.addNode(listFocusElements.get(i), posXStart, posYStart, waitingTime, serviceTime,
						realThroughputTime, intersectionTime * multiplierElement,
						(int) (heightOfElementsRatio * maxHeightOfAnElement) + minHeightOfAnElement, lineHeightRatio,
						intersectionBarHeight, boundaries.getFirst(), boundaries.getSecond(), aapdFocusElements
								.equals(listFocusElements.get(i)));

				// update value for visualization
				sumRatio += heightOfElementsRatio;
				numBar++;
			}
		}

		return graph;
	}
}
