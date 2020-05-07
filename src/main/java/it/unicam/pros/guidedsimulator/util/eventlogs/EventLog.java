package it.unicam.pros.guidedsimulator.util.eventlogs;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import it.unicam.pros.guidedsimulator.util.eventlogs.trace.Trace;

public interface EventLog extends Serializable {
 

	void addTrace(Trace t);

	Set<Trace> getTraces();

	void addTraces(Collection<Trace> collection);

	String getName();

	Map<String, String> getData();

    void removeTrace(Trace d);

    int size();
}
