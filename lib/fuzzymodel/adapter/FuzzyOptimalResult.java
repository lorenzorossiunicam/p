package org.processmining.plugins.fuzzymodel.adapter;

public class FuzzyOptimalResult {
	public double ratio;
	public double preserve;
	public double cutoff;
	public double utility;
	public double nodesig;
	public double conformance;
	public double detail;
	public double complexity;
	public double readability;
	public double optimalvalue;
	
	public FuzzyOptimalResult() {
		ratio=0.0;
		preserve=0.0;
		cutoff=0.0;
		utility=0.0;
		nodesig=0.0;
		optimalvalue=0.0;
		conformance=0.0;
		detail=0.0;
		complexity=0.0;
		readability=1;
	}
	public void setValues(double ratio,double preserve,double cutoff,double utility,double nodesig,double conformance,double detail, double complexity, double optimalvalue,boolean readabiltiy) {
		/*
		if((ratio < 0.0 || ratio > 1.0) || (preserve<0.0 || preserve>1.0)||(cutoff<0.0 || cutoff>1.0) || (utility<0.0 || utility>1.0)||(nodesig<0.0 || nodesig>1.0)
				||(optimalvalue<0.0 || optimalvalue>1.0)) {
			System.err.println("invalid value! ");
			return;
		}
		*/
		this.ratio = ratio;
		this.preserve=preserve;
		this.cutoff=cutoff;
		this.utility=utility;
		this.nodesig=nodesig;
		this.optimalvalue=optimalvalue;
		this.conformance=conformance;
		this.detail=detail;
		this.complexity=complexity;
		if (readabiltiy==true){
			this.readability=1.0;
		}
		else{
			this.readability=0.0;
		}
	}
}
