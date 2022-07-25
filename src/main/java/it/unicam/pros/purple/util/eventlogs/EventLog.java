package it.unicam.pros.purple.util.eventlogs;

import it.unicam.pros.purple.util.eventlogs.trace.Trace;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface EventLog extends Serializable {
 

	void addTrace(Trace t);

	Set<Trace> getTraces();

	void addTraces(Collection<Trace> collection);

	String getName();

	Map<String, String> getData();

    void removeTrace(Trace d);

    int size();

    void setTraces(Set<Trace> l);
}
