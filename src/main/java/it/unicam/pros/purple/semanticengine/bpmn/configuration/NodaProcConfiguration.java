package it.unicam.pros.purple.semanticengine.bpmn.configuration;

import java.io.Serializable;
import java.util.*;


import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;

import com.google.gson.JsonObject;

public class NodaProcConfiguration implements ProcConfiguration, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7499750068457672227L;
	private static final String SIGMA_E = "Sigma_e";
	private static final String SIGMA_T = "Sigma_t";
	private static final String SIGMA_C = "Sigma_c";
	private Map<String, Integer> sigmaE = new HashMap<String, Integer>();
	private Map<String, TaskState> sigmaT = new HashMap<String, TaskState>();
	private Map<String, Integer> sigmaC = new HashMap<String, Integer>(); 
	private String procID;
	private Collection<IntActivity> intActivities = new HashSet<IntActivity>();

	public String getProcID() {
		return procID;
	}


	public NodaProcConfiguration(Process p) {
		this.procID = p.getId();
		for (SequenceFlow edge : p.getChildElementsByType(SequenceFlow.class)) {
			sigmaE.put(edge.getId(), 0);
		}
		for (StartEvent sE : p.getChildElementsByType(StartEvent.class)) {
			if (ModelUtils.isNested(sE)) {
				continue;
			}
			sigmaE.put(sE.getId(), 0);
		}
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

	public JsonObject toJson() {
		JsonObject procConf = new JsonObject();

		JsonObject tokens = new JsonObject();
		for (String sF : sigmaE.keySet()) {
			tokens.addProperty(sF, sigmaE.get(sF));
		}
		procConf.add(SIGMA_E, tokens);


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
		NodaProcConfiguration that = (NodaProcConfiguration) o;
		return Objects.equals(sigmaE, that.sigmaE) &&
				Objects.equals(sigmaT, that.sigmaT) &&
				Objects.equals(sigmaC, that.sigmaC) &&
				Objects.equals(procID, that.procID) &&
				Objects.equals(intActivities, that.intActivities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sigmaE, sigmaT, sigmaC, procID, intActivities);
	}
}
