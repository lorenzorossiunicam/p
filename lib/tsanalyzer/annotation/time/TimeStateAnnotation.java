package org.processmining.plugins.tsanalyzer.annotation.time;

import java.util.ArrayList;
import java.util.Collection;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;
import org.processmining.plugins.tsanalyzer.annotation.StateAnnotation;

public class TimeStateAnnotation extends StateAnnotation {
	private static final String SOJOURN = "sojourn";
	private static final String ELAPSED = "elapsed";
	private static final String REMAINING = "remaining";

	public TimeStateAnnotation(State state) {
		super(state);
		addProperty(SOJOURN, new StatisticsAnnotationProperty());
		addProperty(ELAPSED, new StatisticsAnnotationProperty());
		addProperty(REMAINING, new StatisticsAnnotationProperty());
	}

	private StatisticsAnnotationProperty getTimeAnnotationProperty(String name) {
		return (StatisticsAnnotationProperty) getProperty(name);
	}

	public StatisticsAnnotationProperty getSoujourn() {
		return getTimeAnnotationProperty(SOJOURN);
	}

	public StatisticsAnnotationProperty getElapsed() {
		return getTimeAnnotationProperty(ELAPSED);
	}

	public StatisticsAnnotationProperty getRemaining() {
		return getTimeAnnotationProperty(REMAINING);
	}

	public static Iterable<String> getNamesOfProperties() {
		Collection<String> temp = new ArrayList<String>();
		temp.add(SOJOURN);
		temp.add(ELAPSED);
		temp.add(REMAINING);
		return temp;
	}
}
