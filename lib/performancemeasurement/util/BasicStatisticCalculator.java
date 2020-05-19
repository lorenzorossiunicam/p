/**
 * 
 */
package org.processmining.plugins.performancemeasurement.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class calculate basic statistics from a set of
 * 
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Mar 27, 2009
 */
public class BasicStatisticCalculator {
	public static final int MIN = 0;
	public static final int MAX = 1;
	public static final int AVERAGE = 2;
	public static final int STDDEV = 3;
	public static final int FAST = 4;
	public static final int SLOW = 5;
	public static final int REST = 6;

	public static final double UNAVAILABLE = Double.NEGATIVE_INFINITY;

	/**
	 * Calculate min, max, avg, std dev, average on fastestBoundaryPercentage,
	 * average on lowestBoundaryPercentage, and average on the rest with list of
	 * Long values as input
	 * 
	 * @param values
	 * @param fastestBoundaryPercentage
	 * @param lowestBoundaryPercentage
	 * @return
	 */
	public static double[] calculateBasicStatisticsLong(List<Long> values, double fastestBoundaryPercentage,
			double lowestBoundaryPercentage) {
		// initialize variables
		double[] stats = new double[7];
		
		if (values.size() > 0){
			double average = 0.0;
			List<Double> temp = new LinkedList<Double>();
			for (Long value : values) {
				temp.add((double) value);
			}
			Collections.sort(temp);
	
			// count limit
			int lowLimit = (int) (temp.size() * fastestBoundaryPercentage);
			int highLimit = (int) (temp.size() * (1.0 - lowestBoundaryPercentage));
	
			// low limit cannot be 0
			if (lowLimit == 0) {
				lowLimit = 1;
			}
	
			// calculate average from first iteration
			average = calculateAverage(temp);
	
			// count std dev
			double totalSqr = 0.0;
			for (double value : values) {
				totalSqr += (value - average) * (value - average);
			}
	
			stats[MIN] = temp.get(0);
			stats[MAX] = temp.get(temp.size() - 1);
			stats[AVERAGE] = average;
			stats[STDDEV] = Math.sqrt(totalSqr / values.size());
			stats[FAST] = calculateAverage(temp.subList(0, lowLimit));
			stats[SLOW] = calculateAverage(temp.subList(highLimit, temp.size()));
			if (highLimit - lowLimit > 1) {
				stats[REST] = calculateAverage(temp.subList(lowLimit, highLimit));
			} else {
				stats[REST] = UNAVAILABLE;
			}
		} else {
			stats[MIN] = 0;
			stats[MAX] = 0;
			stats[AVERAGE] = 0;
			stats[STDDEV] = 0;
			stats[FAST] = 0;
			stats[SLOW] = 0;
			stats[REST] = UNAVAILABLE;
		}
		return stats;
	}

	/**
	 * Calculate min, max, avg, std dev, average on fastestBoundaryPercentage,
	 * average on lowestBoundaryPercentage, and average on the rest with list of
	 * Double values as input
	 * 
	 * @param values
	 * @param fastestBoundaryPercentage
	 * @param slowestBoundaryPercentage
	 * @return
	 */
	public static double[] calculateBasicStatisticsDouble(List<Double> values, double fastestBoundaryPercentage,
			double slowestBoundaryPercentage) {
		// initialize variables
		double[] stats = new double[7];
		if (values.size() > 0){
			double average = 0.0;
			List<Double> temp = new LinkedList<Double>();
			temp.addAll(values);
			Collections.sort(temp);
	
			// count limit
			int lowLimit = (int) (temp.size() * fastestBoundaryPercentage);
			int highLimit = (int) (temp.size() * (1.0 - slowestBoundaryPercentage));
	
			// calculate average from first iteration
			average = calculateAverage(temp);
	
			// count std dev
			double totalSqr = 0.0;
			for (double value : values) {
				totalSqr += (value - average) * (value - average);
			}
	
			stats[MIN] = temp.get(0);
			stats[MAX] = temp.get(temp.size() - 1);
			stats[AVERAGE] = average;
			stats[STDDEV] = Math.sqrt(totalSqr / values.size());
			stats[FAST] = calculateAverage(temp.subList(0, lowLimit));
			stats[SLOW] = calculateAverage(temp.subList(highLimit, temp.size()));
			if (highLimit - lowLimit > 1) {
				stats[REST] = calculateAverage(temp.subList(lowLimit, highLimit));
			} else {
				stats[REST] = UNAVAILABLE;
			}
		} else {
			stats[MIN] = 0;
			stats[MAX] = 0;
			stats[AVERAGE] = 0;
			stats[STDDEV] = 0;
			stats[FAST] = 0;
			stats[SLOW] = 0;
			stats[REST] = UNAVAILABLE;
		}
		return stats;
	}

	/**
	 * static method to calculate average value of a list of double values
	 * 
	 * @param values
	 * @return
	 */
	public static double calculateAverage(List<Double> values) {
		double total = 0.0;
		for (double value : values) {
			total += value;
		}
		return total / values.size();
	}
}
