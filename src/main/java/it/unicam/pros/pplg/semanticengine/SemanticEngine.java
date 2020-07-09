package it.unicam.pros.pplg.semanticengine;

import java.util.Map;
import java.util.Set;

import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;

public interface SemanticEngine {
	
	public Map<Configuration, Set<Event>> getNexts(Configuration c) throws Exception;

	public Configuration getInitConf();

	public Configuration initData(Configuration conf);

	public Configuration initInstances(Configuration conf) throws Exception;

	public String getModelName(); 
	
}
