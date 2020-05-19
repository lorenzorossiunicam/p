package org.processmining.plugins.tsml.graphics.utils;

import java.awt.Color;

public class TsmlColor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7008219607728092668L;

	public static String encode(Color color) {
		if (color == null) {
			return "#FFFFFF";
		}
		String red = Integer.toHexString(color.getRed());
		if (red.length() < 2) {
			red = "0" + red;
		}
		String green = Integer.toHexString(color.getGreen());
		if (green.length() < 2) {
			green = "0" + green;
		}
		String blue = Integer.toHexString(color.getBlue());
		if (blue.length() < 2) {
			blue = "0" + blue;
		}
		return "#" + red + green + blue;
	}
}
