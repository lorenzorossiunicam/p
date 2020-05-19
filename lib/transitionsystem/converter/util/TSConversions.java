package org.processmining.plugins.transitionsystem.converter.util;

/**
 * Possible conversions on a transition system.
 * 
 * @author HVERBEEK
 * 
 */
public enum TSConversions {
	/**
	 * Removes self loops from the transition system.
	 */
	KILLSELFLOOPS,
	/**
	 * Adds missing transitions in incomplete diamond structures.
	 */
	EXTEND,
	/**
	 * Merges states which have identical outflux labels.
	 */
	MERGEBYOUTPUT,
	/**
	 * Merges states which have identical influx labels.
	 */
	MERGEBYINPUT;

	/**
	 * Returns the label of the conversion.
	 * 
	 * @return The label of the conversion.
	 */
	public String getLabel() {
		switch (this) {
			case KILLSELFLOOPS :
				return "Remove self loops";
			case EXTEND :
				return "Improve diamond structure";
			case MERGEBYOUTPUT :
				return "Merge states with identical outputs";
			case MERGEBYINPUT :
				return "Merge states with identical inputs";
		}
		return null;
	}
}
