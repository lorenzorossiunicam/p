package org.processmining.plugins.filtering;

public class PCLFilterSettings {

	private double RelThreshold; 
	private int AbsThreshold;	

	public PCLFilterSettings() {

		RelThreshold = PCLFilterSettingsConstants.RELATIVE_THRESHOLD;
		AbsThreshold = PCLFilterSettingsConstants.ABSOLUTE_THRESHOLD;
	}
	
	public PCLFilterSettings(double rel, int abs) {

		RelThreshold = rel;
		AbsThreshold = abs;
	}

	public int getAbsThreshold() {
		return AbsThreshold;
	}

	public void setAbsThreshold(int absThreshold) {
		AbsThreshold = absThreshold;
	}

	public double getRelThreshold() {
		return RelThreshold;
	}

	public void setRelThreshold(double relThreshold) {
		RelThreshold = relThreshold;
	}

}
