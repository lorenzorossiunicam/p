package it.unicam.pros.purple.semanticengine.bpmn.configuration.datastate;

import com.google.gson.JsonObject;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Data;
import org.camunda.bpm.model.bpmn.instance.DataObjectReference;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.DomElement;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Objects;

public class DataObjState implements DataState{
	
	private Bindings scope;
	
	public DataObjState(Process p) {
		scope = Data.getEngine().createBindings();
		for(DataObjectReference d : p.getChildElementsByType(DataObjectReference.class)) {
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
	
	public Bindings getScope() {
		return scope;
	}

	private void initialiseVars(ScriptEngine engine, DataObjectReference d) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DataObjState that = (DataObjState) o;
		return Objects.equals(scope, that.scope);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scope);
	}
}
