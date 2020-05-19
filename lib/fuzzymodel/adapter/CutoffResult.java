package org.processmining.plugins.fuzzymodel.adapter;

public class CutoffResult {
	protected double cutoff;
	protected double optimalvalue;

	public CutoffResult() {
		cutoff = 0.0;
		optimalvalue = 0.0;
	}

	public void setValues(double cutoff, double optimalvalue) {
		if ((cutoff < 0.0 || cutoff > 1.0) || (optimalvalue < 0.0 || optimalvalue > 1.0)) {
			System.err.println("invalid value! ");
			return;
		}
		this.cutoff = cutoff;
		this.optimalvalue = optimalvalue;
	}
}
