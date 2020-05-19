package org.processmining.plugins.tsml.graphics;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlArcGraphics extends TsmlElement {

	/**
	 * PNML annotation graphics tag.
	 */
	public final static String TAG = "graphics";

	/**
	 * Positions elements (may be multiple).
	 */
	private final List<TsmlPosition> positionList;
	/**
	 * Line element.
	 */
	private TsmlLine line;

	/**
	 * Creates a fresh PNML arc graphics.
	 */
	public TsmlArcGraphics() {
		super(TAG);
		positionList = new ArrayList<TsmlPosition>();
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
		if (xpp.getName().equals(TsmlPosition.TAG)) {
			/*
			 * Position element.
			 */
			TsmlPosition position = new TsmlPosition();
			position.importElement(xpp, tsml);
			positionList.add(position);
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
	 * Exports the arc graphics.
	 */
	protected String exportElements(Tsml tsml) {
		String s = super.exportElements(tsml);
		for (TsmlPosition position : positionList) {
			s += position.exportElement(tsml);
		}
		if (line != null) {
			s += line.exportElement(tsml);
		}
		return s;
	}

	/**
	 * Sets the graphics for the given graph element.
	 * 
	 * @param subNet
	 *            The given sub net.
	 * @param element
	 *            The given element.
	 * @param displacement
	 *            The displacement for this sub net.
	 */
	public void unmarshall(AbstractGraphElement element, GraphLayoutConnection layout) {
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		layout.setEdgePoints(element, points);
		for (TsmlPosition position : positionList) {
			position.unmarshall(element, points);
		}
		if (line != null) {
			line.unmarshall(element);
		}
	}

	public TsmlArcGraphics marshall(AbstractGraphElement element, GraphLayoutConnection layout) {
		TsmlArcGraphics result = null;
		try {
			List<Point2D> points = layout.getEdgePoints(element);
			for (Point2D point : points) {
				positionList.add(new TsmlPosition().marshall(point));
			}
			line = new TsmlLine().marshall(element);
			if (!positionList.isEmpty() || (line != null)) {
				result = this;
			}
		} catch (Exception ex) {
		}
		return result;
	}
}
