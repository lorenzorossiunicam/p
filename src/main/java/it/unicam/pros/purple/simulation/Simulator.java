package it.unicam.pros.purple.simulation;
 

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.util.eventlogs.EventLog;

public interface Simulator {

	public EventLog simulate(Delta delta);
}
