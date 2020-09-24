package it.unicam.pros.purple.util.eventlogs;

import java.util.*;

import it.unicam.pros.purple.util.eventlogs.trace.Trace;

public class EventLogImpl implements EventLog{


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EventLogImpl eventLog = (EventLogImpl) o;
		return Objects.equals(traces, eventLog.traces) &&
				Objects.equals(data, eventLog.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(traces, data);
	}

	private Set<Trace> traces;
	private Map<String, String> data;
	private String name;
	
	public EventLogImpl(String name, Map<String, String> data) {
		this.name = name;
		this.data = data;
		this.traces = new HashSet<Trace>();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Map<String, String> getData() {
		return data;
	}
 
	@Override
	public void addTrace(Trace t) {
		traces.add(t);
	}

	@Override
	public Set<Trace> getTraces() { 
		return traces;
	}

	@Override
	public void addTraces(Collection<Trace> t) {
		if(traces.containsAll(t))
		this.traces.addAll(t);
	}

	@Override
	public String toString() {
			String ret = "{";
			ret+= name+", ";
			if(data!=null) {
				ret += data+", ";
			}
			ret+= traces;
			return ret+"}";
	}

	public int size(){
		return traces.size();
	}

	public void removeTrace(Trace t){
		this.traces.remove(t);
	}
}
