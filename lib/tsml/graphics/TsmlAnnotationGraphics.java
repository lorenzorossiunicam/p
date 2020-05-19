package org.processmining.plugins.tsml.graphics;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlAnnotationGraphics extends TsmlElement {

	/**
	 * PNML annotation graphics tag.
	 */
	public final static String TAG = "graphics";

	/**
	 * Offset element.
	 */
	private TsmlOffset offset;
	/**
	 * Font element.
	 */
	private TsmlFont font;
	/**
	 * Fill element.
	 */
	private TsmlFill fill;
	/**
	 * Line element.
	 */
	private TsmlLine line;

	/**
	 * Creates a fresh PNML annotation graphics.
	 */
	public TsmlAnnotationGraphics() {
		super(TAG);
		offset = null;
		font = null;
		fill = null;
		line = null;
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
		if (xpp.getName().equals(TsmlOffset.TAG)) {
			/*
			 * Offset element.
			 */
			offset = new TsmlOffset();
			offset.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlFont.TAG)) {
			/*
			 * Font element.
			 */
			font = new TsmlFont();
			font.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlFill.TAG)) {
			/*
			 * Fill element.
			 */
			fill = new TsmlFill();
			fill.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlLine.TAG)) {
			/*
			 * Line element.
			 */
			line = new TsmlLine();
			line.importElement(xpp, tsml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	/**
	 * Exports the annotation graphics.
	 */
	protected String exportElements(Tsml tsml) {
		String s = super.exportElements(tsml);
		if (offset != null) {
			s += offset.exportElement(tsml);
		}
		if (font != null) {
			s += font.exportElement(tsml);
		}
		if (fill != null) {
			s += fill.exportElement(tsml);
		}
		if (line != null) {
			s += line.exportElement(tsml);
		}
		return s;
	}

	/**
	 * Checks validity. Should have an offset element.
	 */
	protected void checkValidity(Tsml tsml) {
		super.checkValidity(tsml);
		if (offset == null) {
			tsml.log(tag, lineNumber, "Expected offset");
		}
	}

	/**
	 * Sets the graphics for the given graph element.
	 * 
	 * @param net
	 *            The given net.
	 * @param subNet
	 *            The given sub net.
	 * @param element
	 *            The given element.
	 */
	public void unmarshall(AbstractGraphElement element) {
		if (offset != null) {
			offset.unmarshall(element);
		}
		if (font != null) {
			font.unmarshall(element);
		}
		if (fill != null) {
			fill.unmarshall(element);
		}
		if (line != null) {
			line.unmarshall(element);
		}
	}

	public TsmlAnnotationGraphics marshall(AbstractGraphElement element) {
		TsmlAnnotationGraphics result = null;
		try {
			offset = new TsmlOffset().marshall(element);
			font = new TsmlFont().marshall(element);
			fill = new TsmlFill().marshall(element);
			line = new TsmlLine().marshall(element);
			if ((offset != null) || (font != null) || (fill != null) || (line != null)) {
				result = this;
			}
		} catch (Exception ex) {
		}
		return result;
	}
}
