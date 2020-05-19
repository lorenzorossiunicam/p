package org.processmining.plugins.tsml;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.tsml.graphics.TsmlNodeGraphics;
import org.xmlpull.v1.XmlPullParser;

public class TsmlNode extends TsmlBasicObject {

	/**
	 * Id attribute.
	 */
	protected String id;
	/**
	 * Graphics element.
	 */
	protected TsmlNodeGraphics graphics;

	/**
	 * Creates a fresh PNML node.
	 * 
	 * @param tag
	 */
	public TsmlNode(String tag) {
		super(tag);
		id = null;
		graphics = null;
	}

	/**
	 * Imports all known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		/*
		 * Import all basic object attributes.
		 */
		super.importAttributes(xpp, tsml);
		/*
		 * Import id attribute.
		 */
		importId(xpp);
	}

	/**
	 * Exports all attributes.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportId(tsml);
	}

	/**
	 * Imports id attribute.
	 * 
	 * @param xpp
	 */
	private void importId(XmlPullParser xpp) {
		String value = xpp.getAttributeValue(null, "id");
		if (value != null) {
			id = value;
		}
	}

	/**
	 * Exports id attribute.
	 * 
	 * @return
	 */
	private String exportId(Tsml tsml) {
		if (id != null) {
			return exportAttribute("id", id, tsml);
		}
		return "";
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
			/*
			 * Start tag corresponds to a known child element of a PNML basic
			 * object.
			 */
			return true;
		}
		if (xpp.getName().equals(TsmlNodeGraphics.TAG)) {
			/*
			 * Graphics element. Create a graphics object and import graphics
			 * element.
			 */
			graphics = new TsmlNodeGraphics();
			graphics.importElement(xpp, tsml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	/**
	 * Exports all child elements.
	 */
	protected String exportElements(Tsml tsml) {
		/*
		 * Export basic node child elements.
		 */
		String s = super.exportElements(tsml);
		/*
		 * Export graphics element.
		 */
		if (graphics != null) {
			s += graphics.exportElement(tsml);
		}
		return s;
	}

	/**
	 * Checks the validity of this node. Should have an id attribute.
	 */
	protected void checkValidity(Tsml tsml) {
		super.checkValidity(tsml);
		if (id == null) {
			tsml.log(tag, lineNumber, "Expected id");
		}
	}

	public TsmlNodeGraphics getGraphics() {
		return graphics;
	}

	protected void unmarshall(State state, DirectedGraphElementWeights weights, GraphLayoutConnection layout) {
		if (graphics != null) {
			super.unmarshall(state, weights);
			graphics.unmarshall(state, layout);
		}
	}

	public TsmlNode marshall(State state, String id, DirectedGraphElementWeights weights, GraphLayoutConnection layout) {
		super.marshall(state.getLabel(), weights.get(state.getIdentifier(), -1));
		this.id = id;
		graphics = new TsmlNodeGraphics().marshall(state, layout);
		return this;
	}
}
