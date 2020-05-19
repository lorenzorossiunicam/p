package org.processmining.plugins.transitionsystem.miner.util;

public enum TSDirections {
	BACKWARD, FORWARD;

	public String getLabel() {
		switch (this) {
			case BACKWARD :
				return "Backward";
			case FORWARD :
				return "Forward";
		}
		return null;
	}

}
