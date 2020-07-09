package it.unicam.pros.pplg.semanticengine.bpmn.configuration;

import com.google.gson.JsonObject;
import it.unicam.pros.pplg.util.eventlogs.trace.event.ActivityState;

import java.util.Objects;

public class TaskState {

	private static final String ACTIVE = "A";
	private static final String SENDING = "S";
	private static final String RECEIVING = "R";
	private int active, sending, receiving;
	
	public TaskState() {
		this.active = 0;
		this.sending = 0; 
		this.receiving = 0;
	}

	public ActivityState getState() {
		if(active > 0) return ActivityState.START;
		if(sending > 0) return ActivityState.SEND;
		if(receiving > 0) return ActivityState.RECEIVE;
		else return ActivityState.COMPLETE;
	}
	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getSending() {
		return sending;
	}

	public void setSending(int sending) {
		this.sending = sending;
	}

	public int getReceiving() {
		return receiving;
	}

	public void setReceiving(int receiving) {
		this.receiving = receiving;
	}
	 
	public JsonObject toJson() {
		JsonObject ret = new JsonObject();
		ret.addProperty(ACTIVE, getActive());
		ret.addProperty(SENDING, getSending());
		ret.addProperty(RECEIVING, getReceiving()); 
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaskState taskState = (TaskState) o;
		return active == taskState.active &&
				sending == taskState.sending &&
				receiving == taskState.receiving;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, sending, receiving);
	}
}
