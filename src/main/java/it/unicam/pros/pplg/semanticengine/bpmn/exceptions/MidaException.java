package it.unicam.pros.pplg.semanticengine.bpmn.exceptions;

import com.google.gson.JsonObject;

public class MidaException extends Exception {

	private String error, el, info;
	private int instance;
	
	public MidaException(String error, String el, int instance, String info) {
		this.error = error;
		this.el = el;
		this.info = info;
		this.instance = instance;
	}
	
	public JsonObject getJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("error", error);
		obj.addProperty("instance", instance);
		obj.addProperty("element", el);
		obj.addProperty("info", info);
		return obj;
	}

	public String getError() {
		return error;
	}

	public String getEl() {
		return el;
	}

	public String getInfo() {
		return info;
	}

	public int getInstance() {
		return instance;
	}
	
	

}
