package it.unicam.pros.purple.util.eventlogs.trace;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public interface Trace extends Serializable {
	
	public void appendEvent(Event e);
	
	public Event get(int i);
	
	public List<Event> getTrace();
	
	public String getCaseID();
	
	public void remove(int i);

	Map<String, String> getData();
}
