package it.unicam.pros.purple.util.eventlogs.trace;

import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Trace extends Serializable {
	
	public void appendEvent(Event e);
	
	public Event get(int i);
	
	public List<Event> getTrace();
	
	public String getCaseID();
	
	public void remove(int i);

	Map<String, String> getData();

	@Override
	public boolean equals(Object o);

	void setCaseId(String id);

    void insert(List<Event> repetitions, int i, double looprep);
}
