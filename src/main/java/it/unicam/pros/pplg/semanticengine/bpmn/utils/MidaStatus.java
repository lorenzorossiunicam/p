package it.unicam.pros.pplg.semanticengine.bpmn.utils;

import java.util.Collection;
import java.util.Stack;

import com.google.gson.JsonPrimitive;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MidaStatus {
	public final static String INIT = "1";
	public static final String END = "99";
	public static final String SAVEXMLERROR = "e1";
	public static final String READMODELERROR = "e2";
	public static final String INVALIDSESSIONID = "e3";
	public static final String INVALIDGUARD = "e4";
	public static final String MAXINSTANCE = "e5";
	public static final String INVALIDSYNTAX = "e5";

	private static Stack<String> status = new Stack<String>();
	private static JsonObject tokChange  = new JsonObject();
	private static JsonObject msgChange  = new JsonObject();

	public static String getStatus() {
		return status.peek();
	}

	public static void setStatus(String s) {
		status.push(s);
	}

	public static void resetTokChange() {
		tokChange = new JsonObject();
	}

	public static void resetMsgChange() {
		msgChange = new JsonObject();
	}

	public static void setMsgChange(int instance, String msgFlow) {
		if(msgChange.get(String.valueOf(instance)) == null) {
			msgChange.add(String.valueOf(instance), new JsonArray());
		}
		JsonArray tmp = (JsonArray)msgChange.get(String.valueOf(instance));
		tmp.add(new JsonPrimitive(msgFlow));
		msgChange.add(String.valueOf(instance), tmp);
	}

	public static void setTokChange(String procName, int instance, Collection<SequenceFlow> inc) {
		JsonObject change = new JsonObject();
		for (SequenceFlow s : inc) {
			change.addProperty(String.valueOf(instance), s.getId());
		}
		tokChange.add(procName, change);
	}

	public static JsonElement getTokChange() {
		return tokChange;
	}

	public static void setTokChange(String procName, int instance, String inc) {
		JsonObject change = new JsonObject();
		change.addProperty(String.valueOf(instance), inc);
		tokChange.add(procName, change);
	}



	public static JsonElement getMsgChange() {
		return msgChange;
	}

	public static void setStatus(String error, String throwingEl, int instance, String info) {
		JsonObject obj = new JsonObject();
		obj.addProperty("error", error);
		obj.addProperty("element", throwingEl);
		obj.addProperty("instance", instance);
		if(info!=null) {
			obj.addProperty("info", info);
		}
		status.add(obj.toString());
	}
}
