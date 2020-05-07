package it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration;

import java.util.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;

import com.google.gson.JsonObject; 
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.datastate.DataObjState;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;

public class MidaProcConfiguration implements ProcConfiguration {
	private static final String SIGMA_E = "Sigma_e";
	private static final String SIGMA_DO = "Sigma_do";
	private static final String SIGMA_T = "Sigma_t";
	private static final String SIGMA_C = "Sigma_c";
	private Map<String, Integer> sigmaE = new HashMap<String, Integer>();
	private DataObjState sigmaDO;
	private Map<String, TaskState> sigmaT = new HashMap<String, TaskState>();
	private Map<String, Integer> sigmaC = new HashMap<String, Integer>();
	private ScriptEngine engine;


	private String procID;
	private Collection<IntActivity> intActivities = new HashSet<IntActivity>();

	public String getProcID() {
		return procID;
	}

	public MidaProcConfiguration(String procID) {
		BpmnModelInstance mi = ModelUtils.getModel();
		this.procID = procID;
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("javascript");
		Process p = mi.getModelElementById(procID);
		for (SequenceFlow edge : p.getChildElementsByType(SequenceFlow.class)) {
			sigmaE.put(edge.getId(), 0);
		}
		for (StartEvent sE : p.getChildElementsByType(StartEvent.class)) {
			if (ModelUtils.isNested(sE)) {
				continue;
			}
			sigmaE.put(sE.getId(), 0);
		}
		sigmaDO = new DataObjState(p);
		for (Task t : p.getChildElementsByType(Task.class)) {
			if (!ModelUtils.isAtomic(t)) {
				sigmaT.put(t.getId(), new TaskState());
			}
		}
	}
  
	
	public Map<String, Integer> getSigmaC() {
		return sigmaC;
	}
 

	public Map<String, Integer> getSigmaE() {
		return sigmaE;
	}

	public DataObjState getSigmaDO() {
		return sigmaDO;
	}

	public JsonObject toJson() {
		JsonObject procConf = new JsonObject();

		JsonObject tokens = new JsonObject();
		for (String sF : sigmaE.keySet()) {
			tokens.addProperty(sF, sigmaE.get(sF));
		}
		procConf.add(SIGMA_E, tokens);

		procConf.add(SIGMA_DO, sigmaDO.toJson());

		JsonObject taskState = new JsonObject();
		for (String t : sigmaT.keySet()) {
			taskState.add(t, sigmaT.get(t).toJson());
		}
		procConf.add(SIGMA_T, taskState);

		JsonObject counters = new JsonObject();
		for (String t : sigmaC.keySet()) {
			counters.addProperty(t, sigmaC.get(t));
		}
		procConf.add(SIGMA_C, counters);

		return procConf;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	public Collection<IntActivity> getIntActivities() {
		return intActivities;
	}

	public void addIntActivities(Collection<IntActivity> acts) {
		intActivities.addAll(acts);
	}

	public Map<String, TaskState> getSigmaT() {
		return sigmaT;
	}

	public void removeIntActivities(Collection<IntActivity> copies) {
		intActivities.removeAll(copies);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MidaProcConfiguration that = (MidaProcConfiguration) o;
		return Objects.equals(sigmaE, that.sigmaE) &&
				Objects.equals(sigmaDO, that.sigmaDO) &&
				Objects.equals(sigmaT, that.sigmaT) &&
				Objects.equals(sigmaC, that.sigmaC) &&
				Objects.equals(intActivities, that.intActivities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sigmaE, sigmaDO, sigmaT, sigmaC, intActivities);
	}
 
}
