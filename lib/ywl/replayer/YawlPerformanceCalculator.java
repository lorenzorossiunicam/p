package org.processmining.plugins.ywl.replayer;

import org.processmining.models.graphbased.directed.yawl.YawlPerformanceResult;
import org.processmining.plugins.ywl.replayer.visualization.PerformanceInfo;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class YawlPerformanceCalculator {

	private double throughput = 0;	// average throughput time
	private int completedCases = 0;
	
	public YawlPerformanceResult calculatePerformanceInfo(PerformanceInfo performanceInfo){
		YawlPerformanceResult result = new YawlPerformanceResult();
		this.throughput = performanceInfo.getThroughput();
		this.completedCases = performanceInfo.getCompletedCases();
		
		// total fitness model
		result.addPerformanceValue(YawlPerformanceResult.COMPLETED_CASES, completedCases);
		result.addPerformanceValue(YawlPerformanceResult.TOTAL_THROUGHPUT, throughput);
		
		return result;
	}

	
}
