package org.processmining.plugins.tsml.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.List;

import org.processmining.framework.util.Pair;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlPosition extends TsmlElement {

	/**
	 * PNML position tag.
	 */
	public final static String TAG = "position";

	/**
	 * Whether the coordinates are valid.
	 */
	private boolean hasX;
	private boolean hasY;
	/**
	 * The coordinates.
	 */
	private double x;
	private double y;

	public static final double SCALE = 2.0;

	/**
	 * Creates a fresh PNML position.
	 */
	public TsmlPosition() {
		super(TAG);
		hasX = false;
		hasY = false;
	}

	/**
	 * Imports the known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		super.importAttributes(xpp, tsml);
		/*
		 * Import the x attribute.
		 */
		importX(xpp, tsml);
		/*
		 * Import the y attribute.
		 */
		importY(xpp, tsml);
	}

	/**
	 * Exports the dimension.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportX(tsml) + exportY(tsml);
	}

	/**
	 * Imports the x attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importX(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "x");
		if (value != null) {
			try {
				x = SCALE * Double.valueOf(value);
				hasX = true;
			} catch (NumberFormatException e) {
			}
		}
	}

	/**
	 * Exports the x attribute.
	 * 
	 * @return
	 */
	private String exportX(Tsml tsml) {
		if (hasX) {
			return exportAttribute("x", String.valueOf(x / SCALE), tsml);
		}
		return "";
	}

	/**
	 * Imports the y attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importY(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "y");
		if (value != null) {
			try {
				y = SCALE * Double.valueOf(value);
				hasY = true;
			} catch (NumberFormatException e) {
			}
		}
	}

	/**
	 * Exports the y attribute.
	 * 
	 * @return
	 */
	private String exportY(Tsml tsml) {
		if (hasY) {
			return exportAttribute("y", String.valueOf(y / SCALE), tsml);
		}
		return "";
	}

	/**
	 * Checks validity. Should have both an x and a y attribute.
	 */
	protected void checkValidity(Tsml tsml) {
		super.checkValidity(tsml);
		if (!hasX || !hasY) {
			tsml.log(tag, lineNumber, "Expected x and y");
		}
	}

	public double getX() {
		return hasX ? x : 0.0;
	}

	public double getY() {
		return hasY ? y : 0.0;
	}

	public void setX(Double x) {
		this.x = x;
		hasX = true;
	}

	public void setY(Double y) {
		this.y = y;
		hasY = true;
	}

	/**
	 * Sets the position of the given graph element to this position.
	 * 
	 * @param element
	 *            The given element.
	 */
	public void unmarshall(AbstractGraphElement element, Pair<Point2D.Double, Point2D.Double> boundingBox,
			GraphLayoutConnection layout) {
		if (hasX && hasY) {
//						System.err.println(subNet.getLabel() + ", " + element.getLabel() + ": " + displacement + ", " + boundingBox);
			layout.setPosition(element, new Point2D.Double(boundingBox.getFirst().x, boundingBox.getFirst().y));
		}
	}

	/**
	 * Extends the positions of the given graph element (should be an edge) with
	 * this position.
	 * 
	 * @param element
	 *            The given element.
	 * @param displacement
	 *            The displacement for this sub net.
	 */
	public void unmarshall(AbstractGraphElement element, List<Point2D> list) {
		if (hasX && hasY) {
			list.add(new Point2D.Double(x, y));
		}
	}

	/**
	 * Creates a TsmlPosition object for the given element with the given
	 * parent.
	 * 
	 * @param element
	 *            The given element.
	 * @return The created TsmlPosition object.
	 */
	public TsmlPosition marshall(AbstractGraphElement element, GraphLayoutConnection layout) {
		TsmlPosition result = null;
		try {
			/*
			 * Map the position from position-of-left-upper-corner to
			 * position-of-center.
			 */
			Point2D pos = layout.getPosition(element);
			if (pos == null) {
				pos = new Point2D.Double(10,10);
			}
			Dimension size = layout.getSize(element);
			x = pos.getX() + size.getWidth() / 2;
			y = pos.getY() + size.getHeight() / 2;
			hasX = true;
			hasY = true;
			result = this;
		} catch (Exception ex) {
		}
		return result;
	}

	public TsmlPosition marshall(Point2D point) {
		TsmlPosition result = null;
		try {
			x = point.getX();
			y = point.getY();
			hasX = true;
			hasY = true;
			result = this;
		} catch (Exception ex) {
		}
		return result;
	}
}
