package org.processmining.plugins.ywl.replayer.visualization;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class PerformanceInfo {
	// level of events
	private double throughput = 0;	// average throughput time
	private int completedCases = 0;

	public void clear(){
		setThroughput(0);	
	}

	public void setThroughput(double throughput) {
		this.throughput = throughput;
	}

	public double getThroughput() {
		return throughput;
	}

	public void setCompletedCases(int completedCases) {
		this.completedCases = completedCases;
	}

	public int getCompletedCases() {
		return completedCases;
	}
	
	public void incCompletedCases() {
		this.completedCases++;
	}

}