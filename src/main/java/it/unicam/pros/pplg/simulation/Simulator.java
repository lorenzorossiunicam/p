package it.unicam.pros.pplg.simulation;
 

import it.unicam.pros.pplg.evaluator.Delta;
import it.unicam.pros.pplg.util.eventlogs.EventLog;

public interface Simulator {

	public EventLog simulate(Delta delta);
}
