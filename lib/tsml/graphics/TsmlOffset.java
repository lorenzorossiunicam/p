package org.processmining.plugins.tsml.graphics;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlOffset extends TsmlElement {

	/**
	 * PNML offset tag.
	 */
	public final static String TAG = "offset";

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
	 * Creates a fresh PNML offset.
	 */
	public TsmlOffset() {
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
			return exportAttribute("x", String.valueOf(x), tsml);
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
			return exportAttribute("y", String.valueOf(y), tsml);
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

	/**
	 * Sets the offset of the given graph element to this offset.
	 * 
	 * @param element
	 *            The given element.
	 */
	public void unmarshall(AbstractGraphElement element) {

	}

	public TsmlOffset marshall(AbstractGraphElement element) {
		return null;
	}
}
