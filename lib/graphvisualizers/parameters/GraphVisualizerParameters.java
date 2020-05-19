package org.processmining.graphvisualizers.parameters;

public class GraphVisualizerParameters {

	private boolean appendTooltipToLabel;

	public GraphVisualizerParameters() {
		setAppendTooltipToLabel(false);
	}
	
	public boolean isAppendTooltipToLabel() {
		return appendTooltipToLabel;
	}

	public void setAppendTooltipToLabel(boolean appendTooltipToLabel) {
		this.appendTooltipToLabel = appendTooltipToLabel;
	}
}
