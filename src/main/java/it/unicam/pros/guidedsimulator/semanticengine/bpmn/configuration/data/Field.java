package it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.data;

public class Field {

	private boolean isCorrelation;
	private String content;
	
	public Field(String cont, boolean isCorrelation) {
		this.isCorrelation = isCorrelation;
		this.content = cont;
	}
	
	public boolean isCorrelation() {
		return isCorrelation;
	}
	
	public String  getContent() {
		return content;
	}
}
