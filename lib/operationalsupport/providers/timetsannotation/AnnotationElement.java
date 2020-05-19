package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import org.processmining.models.operationalsupport.WorkItem;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeStateAnnotation;

public class AnnotationElement {

	private final TimeStateAnnotation annotation;
	private final WorkItem event;

	public AnnotationElement(TimeStateAnnotation annotation, WorkItem event) {
		super();
		this.annotation = annotation;
		this.event = event;
	}

	public TimeStateAnnotation getAnnotation() {
		return annotation;
	}

	public WorkItem getEvent() {
		return event;
	}

}
