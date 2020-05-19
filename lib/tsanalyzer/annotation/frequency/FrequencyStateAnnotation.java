package org.processmining.plugins.tsanalyzer.annotation.frequency;

import java.util.ArrayList;
import java.util.Collection;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;
import org.processmining.plugins.tsanalyzer.annotation.StateAnnotation;

public class FrequencyStateAnnotation  extends StateAnnotation {

	private static final String OBSERVATIONS = "observations";
	private static final String TRACES = "traces";
	

	public FrequencyStateAnnotation(State state) {
		super(state);
		addProperty(OBSERVATIONS, new StatisticsAnnotationProperty());
		addProperty(TRACES, new StatisticsAnnotationProperty());
		
	}

	private StatisticsAnnotationProperty getFrequencyAnnotationProperty(String name) {
		return (StatisticsAnnotationProperty) getProperty(name);
	}

	public StatisticsAnnotationProperty getObservations() {
		return getFrequencyAnnotationProperty(OBSERVATIONS);
	}

	public StatisticsAnnotationProperty getTraces() {
		return getFrequencyAnnotationProperty(TRACES);
	}

	public static Iterable<String> getNamesOfProperties() {
		Collection<String> temp = new ArrayList<String>();
		temp.add(OBSERVATIONS);
		temp.add(TRACES);
		return temp;
	}
}
