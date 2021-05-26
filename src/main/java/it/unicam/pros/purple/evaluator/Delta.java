package it.unicam.pros.purple.evaluator;

import it.unicam.pros.purple.util.eventlogs.EventLog;

import java.io.Serializable;
import java.util.Objects;


/**
 *
 * A behavioural distance measure.
 *
 * @author Lorenzo Rossi
 */
public class Delta implements Serializable {
	
		private EventLog missings;

		public Delta(EventLog missing) {
			this.missings = missing;
		}

		public EventLog getMissings() {
			return missings;
		}

		public boolean isEmpty() { return missings.getTraces().isEmpty(); }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Delta delta = (Delta) o;
		return Objects.equals(missings, delta.missings);
	}

	@Override
	public int hashCode() {
		return Objects.hash(missings);
	}


}