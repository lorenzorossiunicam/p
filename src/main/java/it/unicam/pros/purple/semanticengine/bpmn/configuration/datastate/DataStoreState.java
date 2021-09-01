package it.unicam.pros.purple.semanticengine.bpmn.configuration.datastate;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Data;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.DataStoreReference;
import org.camunda.bpm.model.xml.instance.DomElement;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Objects;


public class DataStoreState implements DataState, Serializable {

private Bindings scope;
	
	public DataStoreState(BpmnModelInstance mi) {
		scope = Data.getEngine().createBindings();
		for(DataStoreReference d : mi.getModelElementsByType(DataStoreReference.class)) {
			initialiseVars(Data.getEngine(), d);
		}
	}

	@Override
	public String toString() {
		return toJson().getAsString();
	}
	
	public JsonObject toJson() {
		JsonObject ret = new JsonObject();
		for(String x : scope.keySet()) {
			ret.addProperty(x, scope.get(x).toString()); 
		}
		return ret;
	}
	
	private void initialiseVars(ScriptEngine engine, DataStoreReference d) {
		DomElement dataFields = d.getExtensionElements().getElements().iterator().next().getDomElement();
		for(DomElement field : dataFields.getChildElements()) {
			try {
				engine.eval("var "+field.getAttribute("dataField"), scope);
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Bindings getScope() {
		return scope;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DataStoreState that = (DataStoreState) o;
		return Objects.equals(scope, that.scope);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scope);
	}
}
