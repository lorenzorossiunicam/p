package org.processmining.plugins.fuzzymodel.adapter;

public class NodesigListResult {
	protected double nodesignificance;
	protected double optimalvalue;

	public NodesigListResult() {
		nodesignificance = 0.0;
		optimalvalue = 0.0;
	}

	public void setValues(double nodesig, double optimalvalue) {
		if ((nodesig < 0.0 || nodesig > 1.0) || (optimalvalue < 0.0 || optimalvalue > 1.0)) {
			System.err.println("invalid value! ");
			return;
		}
		this.nodesignificance = nodesig;
		this.optimalvalue = optimalvalue;
	}
}
