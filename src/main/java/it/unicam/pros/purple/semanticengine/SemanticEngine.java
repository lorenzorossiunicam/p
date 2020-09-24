package it.unicam.pros.purple.semanticengine;

import java.util.Map;
import java.util.Set;

import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public interface SemanticEngine {
	
	public Map<Configuration, Set<Event>> getNexts(Configuration c) throws Exception;

	public Configuration getInitConf();

	public Configuration initData(Configuration conf);

	public Configuration initInstances(Configuration conf) throws Exception;

	public String getModelName(); 
	
}
