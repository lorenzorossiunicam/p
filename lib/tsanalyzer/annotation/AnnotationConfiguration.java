package org.processmining.plugins.tsanalyzer.annotation;

import org.deckfour.xes.model.XTrace;

public interface AnnotationConfiguration {

	public long getValue(XTrace trace, int eventIndex);
	public long getMinValue(XTrace trace);
	public long getMaxValue(XTrace trace);
	
	String getString(long value);
	String getName(String name);
}
