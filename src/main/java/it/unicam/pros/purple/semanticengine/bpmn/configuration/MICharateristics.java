package it.unicam.pros.purple.semanticengine.bpmn.configuration;

import java.io.Serializable;
import java.util.Objects;

public class MICharateristics implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3149182208097415922L;
	private int min, max, live;

	public MICharateristics(int min, int max) {
		this.min = min;
		this.max = max;
		this.live = 0;
	}

	public int getLive() {
		return live;
	}

	public void addLive() {
		live++;
	}

	public void removeLive() {
		live--;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MICharateristics that = (MICharateristics) o;
		return min == that.min &&
				max == that.max &&
				live == that.live;
	}

	@Override
	public int hashCode() {
		return Objects.hash(min, max, live);
	}
}
