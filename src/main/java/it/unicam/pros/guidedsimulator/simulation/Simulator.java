package it.unicam.pros.guidedsimulator.simulation;
 

import it.unicam.pros.guidedsimulator.evaluator.Delta;
import it.unicam.pros.guidedsimulator.util.eventlogs.EventLog;

public interface Simulator {

	public EventLog simulate(Delta delta);
}
