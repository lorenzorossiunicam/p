package org.processmining.plugins.ilpminer.netproperties;

public class NetPropertiesResult {
	private double avgConnectorDegree, density;

	public NetPropertiesResult(double averageConnectorDegree, double density) {
		this.avgConnectorDegree = averageConnectorDegree;
		this.density = density;
	}

	public void setAverageConnectorDegree(double averageConnectorDegree) {
		this.avgConnectorDegree = averageConnectorDegree;
	}

	public double getAverageConnectorDegree() {
		return avgConnectorDegree;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public double getDensity() {
		return density;
	}
}
