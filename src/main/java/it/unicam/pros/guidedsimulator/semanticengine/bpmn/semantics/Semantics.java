package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics;

import java.util.List;
import java.util.Map;
import java.util.Set;
 
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

public interface Semantics {

	public Map<Configuration, Set<Event>> getNexts(Map<Process, List<FlowNode>> pools, Configuration c) throws Exception;

}
