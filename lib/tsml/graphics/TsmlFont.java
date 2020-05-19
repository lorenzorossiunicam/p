package org.processmining.plugins.tsml.graphics;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlFont extends TsmlElement {

	/**
	 * PNML font tag.
	 */
	public final static String TAG = "font";

	/**
	 * Possible decorations.
	 */
	enum Decoration {
		DECORATION_DEFAULT, DECORATION_UNDERLINE, DECORATION_OVERLINE, DECORATION_LINETHROUGH;
	}

	/**
	 * Possible alignments.
	 */
	enum Align {
		ALIGN_DEFAULT, ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT;
	}

	/**
	 * Family attribute.
	 */
	private String family;
	/**
	 * Style attribute.
	 */
	private String style;
	/**
	 * Weight attribute.
	 */
	private String weight;
	/**
	 * Size attribute.
	 */
	private String size;
	/**
	 * Decoration attribute.
	 */
	private Decoration decoration;
	/**
	 * Align attribute.
	 */
	private Align align;
	/**
	 * Rotation attribute (and whether valid).
	 */
	private boolean hasRotation;
	private double rotation;

	/**
	 * Creates a fresh PNML font object.
	 */
	public TsmlFont() {
		super(TAG);
		family = null;
		style = null;
		weight = null;
		size = null;
		decoration = Decoration.DECORATION_DEFAULT;
		align = Align.ALIGN_DEFAULT;
		hasRotation = false;
	}

	/**
	 * Imports all known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		super.importAttributes(xpp, tsml);
		/*
		 * Import family attribute.
		 */
		importFamily(xpp, tsml);
		/*
		 * Import style attribute.
		 */
		importStyle(xpp, tsml);
		/*
		 * Import weight attribute.
		 */
		importWeight(xpp, tsml);
		/*
		 * Import size attribute.
		 */
		importSize(xpp, tsml);
		/*
		 * Import decoration attribute.
		 */
		importDecoration(xpp, tsml);
		/*
		 * Import align attribute.
		 */
		importAlign(xpp, tsml);
		/*
		 * Import rotation attribute.
		 */
		importRotation(xpp, tsml);
	}

	/**
	 * Exports all attributes.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportFamily(tsml) + exportStyle(tsml) + exportWeight(tsml)
				+ exportSize(tsml) + exportDecoration(tsml) + exportAlign(tsml) + exportRotation(tsml);
	}

	/**
	 * Imports family attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importFamily(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "family");
		if (value != null) {
			family = value;
		}
	}

	/**
	 * Exports family attribute.
	 * 
	 * @return
	 */
	private String exportFamily(Tsml tsml) {
		if (family != null) {
			return exportAttribute("family", family, tsml);
		}
		return "";
	}

	/**
	 * Imports style attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importStyle(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "style");
		if (value != null) {
			style = value;
		}
	}

	/**
	 * Exports style attribute.
	 * 
	 * @return
	 */
	private String exportStyle(Tsml tsml) {
		if (style != null) {
			return exportAttribute("style", style, tsml);
		}
		return "";
	}

	/**
	 * Imports weight attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importWeight(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "weight");
		if (value != null) {
			weight = value;
		}
	}

	/**
	 * Exports weight attribute.
	 * 
	 * @return
	 */
	private String exportWeight(Tsml tsml) {
		if (weight != null) {
			return exportAttribute("weight", weight, tsml);
		}
		return "";
	}

	/**
	 * Imports size attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importSize(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "size");
		if (value != null) {
			size = value;
		}
	}

	/**
	 * Exports size attribute.
	 * 
	 * @return
	 */
	private String exportSize(Tsml tsml) {
		if (size != null) {
			return exportAttribute("size", size, tsml);
		}
		return "";
	}

	/**
	 * Imports decoration attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importDecoration(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "decoration");
		if (value != null) {
			if (value.equalsIgnoreCase("underline")) {
				decoration = Decoration.DECORATION_UNDERLINE;
			} else if (value.equalsIgnoreCase("overline")) {
				decoration = Decoration.DECORATION_OVERLINE;
			} else if (value.equalsIgnoreCase("line-through")) {
				decoration = Decoration.DECORATION_LINETHROUGH;
			}
		}
	}

	/**
	 * Exports decoration attribute.
	 * 
	 * @return
	 */
	private String exportDecoration(Tsml tsml) {
		switch (decoration) {
			case DECORATION_UNDERLINE :
				return exportAttribute("decoration", "underline", tsml);
			case DECORATION_OVERLINE :
				return exportAttribute("decoration", "overline", tsml);
			case DECORATION_LINETHROUGH :
				return exportAttribute("decoration", "line-through", tsml);
			default :
				// fall thru
		}
		return "";
	}

	/**
	 * Imports align attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importAlign(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "align");
		if (value != null) {
			if (value.equalsIgnoreCase("left")) {
				align = Align.ALIGN_LEFT;
			} else if (value.equalsIgnoreCase("center")) {
				align = Align.ALIGN_CENTER;
			} else if (value.equalsIgnoreCase("right")) {
				align = Align.ALIGN_RIGHT;
			}
		}
	}

	/**
	 * Exports align attribute.
	 * 
	 * @return
	 */
	private String exportAlign(Tsml tsml) {
		switch (align) {
			case ALIGN_LEFT :
				return exportAttribute("align", "left", tsml);
			case ALIGN_CENTER :
				return exportAttribute("align", "center", tsml);
			case ALIGN_RIGHT :
				return exportAttribute("align", "right", tsml);
			default :
				// fall thru
		}
		return "";
	}

	/**
	 * Imports rotation attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importRotation(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "rotation");
		if (value != null) {
			try {
				rotation = Double.valueOf(value);
				hasRotation = true;
			} catch (NumberFormatException e) {
				rotation = 0.0;
			}
		}
	}

	/**
	 * Exports rotation attribute.
	 * 
	 * @return
	 */
	private String exportRotation(Tsml tsml) {
		if (hasRotation) {
			return exportAttribute("rotation", String.valueOf(rotation), tsml);
		}
		return "";
	}

	/**
	 * Sets the font of the given graph element to this font.
	 * 
	 * @param element
	 *            The given element.
	 */
	public void unmarshall(AbstractGraphElement element) {

	}

	public TsmlFont marshall(AbstractGraphElement element) {
		return null;
	}

}
