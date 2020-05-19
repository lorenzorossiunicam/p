/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performance;

import java.text.NumberFormat;

/**
 * @author aadrians
 * Mar 5, 2012
 *
 */
public class TimeFormatter {
	public static String formatTime(double value, NumberFormat nfDouble) {
		if (Double.isNaN(value)){
			return "NaN";
		} else if (value > (60 * 60 * 24 * 30 * 1000L)){
			return nfDouble.format(value / (60 * 60 * 24 * 30 * 1000L)) + " months";
		} else if (value > (60 * 60 * 24 * 1000L)){
			return nfDouble.format(value / (60 * 60 * 24 * 1000L)) + " days";
		} else if (value > (60 * 60 * 1000L)){
			return nfDouble.format(value / (60 * 60 * 1000L)) + " hours";
		} else if (value > (60 * 1000L)){
			return nfDouble.format(value / (60 * 1000L)) + " min";
		} else if (value > 1000L){
			return nfDouble.format(value / 1000L) + " seconds";
		} else {
			return nfDouble.format(value) + " ms";
		}
	}
}
