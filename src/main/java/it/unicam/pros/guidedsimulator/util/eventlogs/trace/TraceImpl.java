package it.unicam.pros.guidedsimulator.util.eventlogs.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.EventImpl;

public class TraceImpl implements Trace {

	private String caseID;
	private Map<String, String> data;
	private List<Event> trace;
	private static int caseNum = -1;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TraceImpl trace1 = (TraceImpl) o;
		return Objects.equals(data, trace1.data) &&
				Objects.equals(trace, trace1.trace);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, trace);
	}

	public TraceImpl(Map<String, String> data) {
		this.caseID = getNewCaseID();
		this.data = data;
		this.trace = new ArrayList<Event>();
	}

	private String getNewCaseID() {
		caseNum++;
		return "case_" + caseNum;
	}

	@Override
	public Map<String, String> getData() {
		return data;
	}

	@Override
	public void appendEvent(Event e) {
		if (!e.equals(EventImpl.emptyEvent()))
			trace.add(e);
	}

	@Override
	public Event get(int i) {
		if (trace.size() <= i || i < 0)
			return null;
		return trace.get(i);
	}

	@Override
	public List<Event> getTrace() {
		return trace;
	}

	@Override
	public String getCaseID() {
		return caseID;
	}

	@Override
	public String toString() {
		String ret = "[";
		ret += getCaseID() + ", ";
		if (data != null) {
			ret += data + ", ";
		}
		ret += trace;
		return ret + "]";
	}

	@Override
	public void remove(int i) {
		trace.remove(i);
	}

}
