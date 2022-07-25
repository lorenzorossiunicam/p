package it.unicam.pros.purple.semanticengine.bpmn.configuration.datastate;

import com.google.gson.JsonObject;

import java.util.Map;

public interface DataState {
	
	@Override
	public String toString();
	
	public JsonObject toJson();
	
	public Map<String, Object> getScope();
}
