package it.unicam.pros.purple.semanticengine.bpmn.configuration.datastate;

import java.util.Map;

import com.google.gson.JsonObject;

public interface DataState {
	
	@Override
	public String toString();
	
	public JsonObject toJson();
	
	public Map<String, Object> getScope();
}
