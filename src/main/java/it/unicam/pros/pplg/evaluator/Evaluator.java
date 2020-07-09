package it.unicam.pros.pplg.evaluator;
 
import it.unicam.pros.pplg.util.eventlogs.EventLog;

/**
 *
 * An evaluator of event logs. The {@link Evaluator} interface permits to compare
 * a discovered event log with a reference.
 *
 * @author Lorenzo Rossi
 */
public interface Evaluator {

	/**
	 * Evaluates the distance between the discovered event log and the reference model using a tolerance parameter.
	 *
	 * @param disc the log to evaluate.
	 * @param tau the tolerance parameter (0.0 min - 1.0 max).
	 * @return the distance
	 */
	public Delta evaluate(EventLog disc, Double tau);

}
