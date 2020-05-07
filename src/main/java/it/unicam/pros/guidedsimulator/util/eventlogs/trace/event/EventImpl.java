package it.unicam.pros.guidedsimulator.util.eventlogs.trace.event;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class EventImpl implements Event {

	private String process;
	private String instance;
	private String eventName;
	private Date timestamp;
	private ActivityState state;
	private List<String> assignments;
	private MsgType sendOrReceive;
	private Map<String, String> message;
	private String msgName;
	private Integer counter;
	private Double cost;

	public EventImpl(String process, String instance, String eName, Date d, ActivityState state, List<String> assigns,
			MsgType sOr, String msgName, Map<String, String> msg, Integer count, Double cost) {
		this.process = process;
		this.instance = instance;
		this.eventName = eName;
		this.timestamp = d;
		this.state = state;
		this.assignments = assigns;
		this.sendOrReceive = sOr;
		this.message = msg;
		this.msgName = msgName;
		this.counter = count;
		this.cost = cost;
	}

	@Override
	public String getProcess() {
		return process;
	}

	@Override
	public String getInstance() {
		return instance;
	}

	@Override
	public String getEventName() {
		return eventName;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public ActivityState getState() {
		return state;
	}

	@Override
	public List<String> getAssignments() {
		return assignments;
	}

	@Override
	public MsgType isSendOrReceive() {
		return sendOrReceive;
	}

	@Override
	public Map<String, String> getMessage() {
		return message;
	}

	@Override
	public Integer getCounter() {
		return counter;
	}

	@Override
	public String getMsgName() {
		return msgName;
	}

	@Override
	public Double getCost() { return cost; }

	@Override
	public String toString() {
		String ret = "(";
		if (eventName != null) {
			ret += eventName + ", ";
		}
		if (timestamp != null) {
			ret += timestamp + ", ";
		}
		if (state != null) {
			ret += state + ", ";
		}
		if (assignments != null) {
			ret += assignments + ", ";
		}
		if (sendOrReceive != null) {
			ret += sendOrReceive + ", ";
		}
		if (message != null) {
			ret += message + ", ";
		}
		if (msgName != null) {
			ret += msgName + ", ";
		}
		if (counter != null) {
			ret += counter;
		}
		return ret + ")";
	}

	public static Event emptyEvent() {
		return new EventImpl(null, null, null, null, null, null, null, null, null, null, null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assignments == null) ? 0 : assignments.hashCode());
		result = prime * result + ((counter == null) ? 0 : counter.hashCode());
		result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((msgName == null) ? 0 : msgName.hashCode());
		result = prime * result + ((process == null) ? 0 : process.hashCode());
		result = prime * result + ((sendOrReceive == null) ? 0 : sendOrReceive.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventImpl other = (EventImpl) obj;
		if (assignments == null) {
			if (other.assignments != null)
				return false;
		} else if (!assignments.equals(other.assignments))
			return false;
		if (counter == null) {
			if (other.counter != null)
				return false;
		} else if (!counter.equals(other.counter))
			return false;
		if (eventName == null) {
			if (other.eventName != null)
				return false;
		} else if (!eventName.equals(other.eventName))
			return false;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (msgName == null) {
			if (other.msgName != null)
				return false;
		} else if (!msgName.equals(other.msgName))
			return false;
		if (process == null) {
			if (other.process != null)
				return false;
		} else if (!process.equals(other.process))
			return false;
		if (sendOrReceive != other.sendOrReceive)
			return false;
		if (state != other.state)
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	@Override
	public boolean isEmptyEvent() {
		return this.equals(EventImpl.emptyEvent());
	}

}
