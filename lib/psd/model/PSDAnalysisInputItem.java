package org.processmining.plugins.psd.model;

import org.processmining.framework.providedobjects.ProvidedObject;

public class PSDAnalysisInputItem {

	private String caption;
	private int min;
	private int max;
	private ProvidedObject[] objects;

	public PSDAnalysisInputItem(String caption) {
		this(caption, 1, 1);
	}

	public PSDAnalysisInputItem(String caption, int min, int max) {
		this.caption = caption;
		this.min = min;
		this.max = max;
	}

	public boolean accepts(ProvidedObject object) {
		return false;
	}

	public int getMinimum() {
		return min;
	}

	public int getMaximum() {
		return max;
	}

	public String getCaption() {
		return caption;
	}

	public void setProvidedObjects(ProvidedObject[] objects) {
		this.objects = objects;
	}

	public ProvidedObject[] getProvidedObjects() {
		return objects;
	}
}

