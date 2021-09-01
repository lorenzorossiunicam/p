package it.unicam.pros.purple.util.eventlogs.trace.event;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Event extends Serializable {
	
	String getProcess();
	
	String getInstance();
	
	String getEventName();

	Date getTimestamp();

	ActivityState getState();

	List<String> getAssignments();

	MsgType isSendOrReceive();

	String getMsgName();

	Map<String, String> getMessage();

	Integer getCounter();

	Double getCost();

	boolean isEmptyEvent();

	void setEventName(String s);

    void setTimestamp(Date date);
}
