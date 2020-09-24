package it.unicam.pros.purple.semanticengine.bpmn.semantics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;

public interface Semantics {

	public Map<Configuration, Set<Event>> getNexts(Map<Process, List<FlowNode>> pools, Configuration c) throws Exception;

}
