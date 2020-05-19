package org.processmining.plugins.transitionsystem.converter;

import java.util.HashMap;

import org.processmining.plugins.transitionsystem.converter.util.TSConversions;
import org.processmining.plugins.transitionsystem.miner.TSMinerOutput;

/**
 * Transition System Converter Input.
 * 
 * Input settings for the transition system converter.
 * 
 * @author hverbeek
 * @version 0.1
 * 
 */
public class TSConverterInput extends TSMinerOutput {

	/**
	 * Which conversions (see TSMinerConstants) to apply.
	 */
	private final HashMap<TSConversions, Boolean> use = new HashMap<TSConversions, Boolean>(4);

	/**
	 * Select default conversions.
	 */
	public TSConverterInput() {
		super();
		for (TSConversions strategy : TSConversions.values()) {
			use.put(strategy, false);
		}
		/**
		 * By default, the kill-self-loops conversion is selected.
		 */
		use.put(TSConversions.KILLSELFLOOPS, true);
	}

	/**
	 * Return whether to use a certain conversion.
	 * 
	 * @param conversion
	 *            The conversion.
	 * @return Whether to use this conversion.
	 */
	public boolean getUse(TSConversions conversion) {
		return use.get(conversion);
	}

	/**
	 * Sets whether to use a certain conversion.
	 * 
	 * @param conversion
	 *            The conversion.
	 * @param newValue
	 *            Whether to use the conversion.
	 * @return The old value.
	 */
	public boolean setUse(TSConversions conversion, boolean newValue) {
		boolean oldValue = use.get(conversion);
		use.put(conversion, newValue);
		return oldValue;
	}
}
