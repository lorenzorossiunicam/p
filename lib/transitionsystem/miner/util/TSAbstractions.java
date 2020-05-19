package org.processmining.plugins.transitionsystem.miner.util;

public enum TSAbstractions {
	/**
	 * Both order and number will be preserved in the abstraction.
	 */
	SEQUENCE,
	/**
	 * Both order and number will be lost, only presence will be preserved.
	 */
	SET,
	/**
	 * Both order and number will be lost, only presence will be preserved. Horizon is considered as the number of different elements, instead of the event window size.
	 */
	FIXED_LENGTH_SET,
	/**
	 * Order will be lost, but number will be preserved.
	 */
	BAG;

	public String getLabel() {
		switch (this) {
			case SEQUENCE :
				return "Sequence";
			case SET :
				return "Set";
			case BAG :
				return "Multiset";
			case FIXED_LENGTH_SET:
				return "Fixed Length Set";
		}
		return null;
	}
}
