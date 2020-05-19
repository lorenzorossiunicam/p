package org.processmining.plugins.tsml;

import java.util.ArrayList;
import java.util.List;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.tsml.graphics.TsmlAnnotationGraphics;
import org.processmining.plugins.tsml.toolspecific.TsmlToolSpecific;
import org.xmlpull.v1.XmlPullParser;

public class TsmlAnnotation extends TsmlElement {

	/**
	 * Text element.
	 */
	public TsmlText text;
	/**
	 * Graphics element.
	 */
	protected TsmlAnnotationGraphics graphics;
	/**
	 * ToolSpecifics element.
	 */
	protected List<TsmlToolSpecific> toolSpecificList;

	/**
	 * Creates a fresh annotation object.
	 * 
	 * @param tag
	 *            The tag for the annotation.
	 */
	public TsmlAnnotation(String tag) {
		super(tag);
		text = null;
		graphics = null;
		toolSpecificList = new ArrayList<TsmlToolSpecific>();
	}

	public TsmlAnnotation(String text, String tag) {
		super(tag);
		this.text = new TsmlText(text);
		graphics = null;
		toolSpecificList = new ArrayList<TsmlToolSpecific>();
	}

	/**
	 * Checks whether the current start tag is known. If known, it imports the
	 * corresponding child element and returns true. Otherwise, it returns
	 * false.
	 * 
	 * @return Whether the start tag was known.
	 */
	protected boolean importElements(XmlPullParser xpp, Tsml tsml) {
		if (super.importElements(xpp, tsml)) {
			return true;
		}
		if (xpp.getName().equals(TsmlText.TAG)) {
			/*
			 * Text element. Create text object and import text element.
			 */
			text = new TsmlText();
			text.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlAnnotationGraphics.TAG)) {
			/*
			 * Graphics element. Create graphics object and import graphics
			 * element.
			 */
			graphics = new TsmlAnnotationGraphics();
			graphics.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlToolSpecific.TAG)) {
			/*
			 * Tool specific element. Create tool specific object and import
			 * tool specific element.
			 */
			TsmlToolSpecific toolSpecific = new TsmlToolSpecific();
			toolSpecific.importElement(xpp, tsml);
			toolSpecificList.add(toolSpecific);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements(Tsml tsml) {
		String s = super.exportElements(tsml);
		if (text != null) {
			s += text.exportElement(tsml);
		}
		if (graphics != null) {
			s += graphics.exportElement(tsml);
		}
		for (TsmlToolSpecific toolSpecific : toolSpecificList) {
			s += toolSpecific.exportElement(tsml);
		}
		return s;
	}

	public void unmarshall(State state) {
		/*
		 * Set the name.
		 */
		if (text != null) {
			state.getAttributeMap().put(AttributeMap.LABEL, text.getText());
		}
		/*
		 * Set optional name graphics.
		 */
		if (graphics != null) {
			graphics.unmarshall(state);
		}
	}

	public void unmarshall(Transition transition) {
		/*
		 * Set the name.
		 */
		//edge.getAttributeMap().put(AttributeMap.LABELALONGEDGE, true);
		if (text != null) {
			transition.getAttributeMap().put(AttributeMap.LABEL, text.getText());
		}
		/*
		 * Set optional name graphics.
		 */
		if (graphics != null) {
			graphics.unmarshall(transition);
		}
	}

	public TsmlAnnotation marshall(AbstractGraphElement element) {
		TsmlAnnotation result = null;
		try {
			graphics = new TsmlAnnotationGraphics().marshall(element);
			if ((text.getText().length() > 0) || (graphics != null)) {
				result = this;
			}
		} catch (Exception ex) {
		}
		return result;
	}
}
