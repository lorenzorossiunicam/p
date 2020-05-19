package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import java.util.Comparator;

import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeStateAnnotation;

public class AnnotationElementComparator implements Comparator<AnnotationElement> {

	private final StatisticsAnnotationPropertyComparator propertyComparator;

	public AnnotationElementComparator() {
		super();
		propertyComparator = new StatisticsAnnotationPropertyComparator();
	}

	private StatisticsAnnotationProperty getProperty(AnnotationElement annotationElement) {
		TimeStateAnnotation annotation = annotationElement.getAnnotation();
		if (annotation != null) {
			return annotation.getRemaining();
		} else {
			return null;
		}
	}

	public int compare(AnnotationElement e1, AnnotationElement e2) {
		return propertyComparator.compare(getProperty(e1), getProperty(e2));
	}
}
