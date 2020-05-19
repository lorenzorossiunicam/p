package org.processmining.plugins.tsml.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlDimension extends TsmlElement {

	/**
	 * PNML dimension tag.
	 */
	public final static String TAG = "dimension";

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

	/**
	 * Creates a fresh PNML dimension.
	 */
	public TsmlDimension() {
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
				x = TsmlPosition.SCALE * Double.valueOf(value);
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
			return exportAttribute("x", String.valueOf(x / TsmlPosition.SCALE), tsml);
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
				y = TsmlPosition.SCALE * Double.valueOf(value);
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
			return exportAttribute("y", String.valueOf(y / TsmlPosition.SCALE), tsml);
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
	 * Sets the size of the given graph element to this dimension.
	 * 
	 * @param element
	 *            The given element.
	 */
	public void unmarshall(AbstractGraphElement element, Pair<Point2D.Double, Point2D.Double> boundingBox) {
		if (hasX && hasY) {
			Dimension dim = new Dimension();
			dim.setSize(boundingBox.getSecond().x - boundingBox.getFirst().x, boundingBox.getSecond().y
					- boundingBox.getFirst().y);
			element.getAttributeMap().put(AttributeMap.SIZE, dim);
		}
	}

	public TsmlDimension marshall(AbstractGraphElement element) {
		TsmlDimension dimension = null;
		try {
			Dimension size = element.getAttributeMap().get(AttributeMap.SIZE, new Dimension());
			x = size.getWidth();
			y = size.getHeight();
			hasX = true;
			hasY = true;
			dimension = this;
		} catch (Exception ex) {
		}
		return dimension;
	}
}
