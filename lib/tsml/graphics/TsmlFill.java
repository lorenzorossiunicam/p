package org.processmining.plugins.tsml.graphics;

import java.awt.Color;
import java.net.URI;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.processmining.plugins.tsml.graphics.utils.TsmlColor;
import org.xmlpull.v1.XmlPullParser;

public class TsmlFill extends TsmlElement {

	/**
	 * PNML fill tag.
	 */
	public final static String TAG = "fill";

	/**
	 * Possible gradient rotations.
	 */
	enum GradientRotation {
		GRADIENT_ROTATION_DEFAULT, GRADIENT_ROTATION_VERTICAL, GRADIENT_ROTATION_HORIZONTAL, GRADIENT_ROTATION_DIAGONAL;
	}

	/**
	 * Color attribute.
	 */
	private String color;
	/**
	 * Gradient color attribute.
	 */
	private String gradientColor;
	/**
	 * Gradient rotation attribute.
	 */
	private GradientRotation gradientRotation;
	/**
	 * Image attribute.
	 */
	private URI image;

	/**
	 * Creates a fresh fill element.
	 */
	public TsmlFill() {
		super(TAG);
		color = null;
		gradientColor = null;
		gradientRotation = GradientRotation.GRADIENT_ROTATION_DEFAULT;
		image = null;
	}

	/**
	 * Imports all known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		super.importAttributes(xpp, tsml);
		/*
		 * Import color attribute.
		 */
		importColor(xpp, tsml);
		/*
		 * Import gradient color attribute.
		 */
		importGradientColor(xpp, tsml);
		/*
		 * Import gradient rotation attribute.
		 */
		importGradientRotation(xpp, tsml);
		/*
		 * Import image attribute.
		 */
		importImage(xpp, tsml);
	}

	/**
	 * Exports all attributes.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportColor(tsml) + exportGradientColor(tsml)
				+ exportGradientRotation(tsml) + exportImage(tsml);
	}

	/**
	 * Imports color attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importColor(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "color");
		if (value != null) {
			color = value;
		}
	}

	/**
	 * Exports color attribute.
	 * 
	 * @return
	 */
	private String exportColor(Tsml tsml) {
		if (color != null) {
			return exportAttribute("color", color, tsml);
		}
		return "";
	}

	/**
	 * Imports gradient color attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importGradientColor(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "gradient-color");
		if (value != null) {
			gradientColor = value;
		}
	}

	/**
	 * Exports gradient color attribute.
	 * 
	 * @return
	 */
	private String exportGradientColor(Tsml tsml) {
		if (gradientColor != null) {
			return exportAttribute("gradient-color", gradientColor, tsml);
		}
		return "";
	}

	/**
	 * Imports gradient rotation attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importGradientRotation(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "gradient-rotation");
		if (value != null) {
			if (value.equalsIgnoreCase("vertical")) {
				gradientRotation = GradientRotation.GRADIENT_ROTATION_VERTICAL;
			} else if (value.equalsIgnoreCase("horizontal")) {
				gradientRotation = GradientRotation.GRADIENT_ROTATION_HORIZONTAL;
			} else if (value.equalsIgnoreCase("diagonal")) {
				gradientRotation = GradientRotation.GRADIENT_ROTATION_DIAGONAL;
			}
		}
	}

	/**
	 * Exports gradient rotation attribute.
	 * 
	 * @return
	 */
	private String exportGradientRotation(Tsml tsml) {
		switch (gradientRotation) {
			case GRADIENT_ROTATION_VERTICAL :
				return exportAttribute("gradient-rotation", "vertical", tsml);
			case GRADIENT_ROTATION_HORIZONTAL :
				return exportAttribute("gradient-rotation", "horizontal", tsml);
			case GRADIENT_ROTATION_DIAGONAL :
				return exportAttribute("gradient-rotation", "diagonal", tsml);
			default :
				// fall thru
		}
		return "";
	}

	/**
	 * Imports image attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importImage(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "image");
		if (value != null) {
			image = URI.create(value);
		}
	}

	/**
	 * Exports image attribute.
	 * 
	 * @return
	 */
	private String exportImage(Tsml tsml) {
		if (image != null) {
			exportAttribute("image", image.toString(), tsml);
		}
		return "";
	}

	/**
	 * Sets the fill of the given graph element to this fill.
	 * 
	 * @param element
	 *            The given element.
	 */
	public void unmarshall(AbstractGraphElement element) {
		if (color != null) {
			Color decodedColor = Color.decode(color);
			if (decodedColor != null) {
				element.getAttributeMap().put(AttributeMap.FILLCOLOR, decodedColor);
			}
		}
	}

	public TsmlFill marshall(AbstractGraphElement element) {
		TsmlFill fill = null;
		try {
			Color color = element.getAttributeMap().get(AttributeMap.FILLCOLOR, new Color(0));
			if (!Color.BLACK.equals(color)) {
				this.color = TsmlColor.encode(color);
				fill = this;
			}
		} catch (Exception ex) {
		}
		return fill;
	}
}
