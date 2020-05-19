package org.processmining.plugins.tsml.graphics;

import java.awt.geom.Point2D;

import org.processmining.framework.util.Pair;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlNodeGraphics extends TsmlElement {

	/**
	 * PNML node graphics tag.
	 */
	public final static String TAG = "graphics";

	/**
	 * Position element.
	 */
	private TsmlPosition position;
	/**
	 * Fill element.
	 */
	private TsmlFill fill;
	/**
	 * Line element.
	 */
	private TsmlLine line;
	/**
	 * Dimension element.
	 */
	private TsmlDimension dimension;

	/**
	 * Creates a fresh PNML node graphics.
	 */
	public TsmlNodeGraphics() {
		super(TAG);
		position = null;
		fill = null;
		line = null;
		dimension = null;
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
		if (xpp.getName().equals(TsmlPosition.TAG)) {
			/*
			 * Position element.
			 */
			position = new TsmlPosition();
			position.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlDimension.TAG)) {
			/*
			 * Dimension element.
			 */
			dimension = new TsmlDimension();
			dimension.importElement(xpp, tsml);
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
		if (position != null) {
			s += position.exportElement(tsml);
		}
		if (dimension != null) {
			s += dimension.exportElement(tsml);
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
	 * Gets the bounding box for this node.
	 * 
	 * @return The bounding box of this object.
	 */
	public Pair<Point2D.Double, Point2D.Double> getBoundingBox() {
		Point2D.Double luc = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D.Double rbc = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		try {
			luc.x = position.getX() - dimension.getX() / 2.0;
			luc.y = position.getY() - dimension.getY() / 2.0;
			rbc.x = position.getX() + dimension.getX() / 2.0;
			rbc.y = position.getY() + dimension.getY() / 2.0;
		} catch (Exception ex) {
		}
		return new Pair<Point2D.Double, Point2D.Double>(luc, rbc);
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
	public void unmarshall(AbstractGraphElement element, GraphLayoutConnection layout) {
		if (position != null) {
			position.unmarshall(element, getBoundingBox(), layout);
		}
		if (fill != null) {
			fill.unmarshall(element);
		}
		if (line != null) {
			line.unmarshall(element);
		}
		if (dimension != null) {
			dimension.unmarshall(element, getBoundingBox());
		}
	}

	public TsmlNodeGraphics marshall(AbstractGraphElement element, GraphLayoutConnection layout) {
		TsmlNodeGraphics graphics = null;
		position = new TsmlPosition().marshall(element, layout);
		fill = new TsmlFill().marshall(element);
		line = new TsmlLine().marshall(element);
		dimension = new TsmlDimension().marshall(element);
		if ((position != null) || (fill != null) || (line != null) || (dimension != null)) {
			graphics = this;
		}
		return graphics;
	}
}
