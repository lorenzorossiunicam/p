package it.unicam.pros.purple.semanticengine.bpmn.configuration;

import java.io.Serializable;
import java.util.*;

import com.google.gson.JsonObject;
import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import org.camunda.bpm.model.bpmn.instance.Process;

public class NodaCollabsConfiguration implements Configuration, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5957263397346384176L;
	private static final String SIGMA_I = "Sigma_i";
	private static final String SIGMA_M = "Sigma_m";

	private static int instanceID = 0;
	private Map<String, Map<Integer, NodaProcConfiguration>> sigmaI = new HashMap<String, Map<Integer, NodaProcConfiguration>>();
	private Map<String, Integer> sigmaM;
	private Map<String, MICharateristics> miPools = new HashMap<String, MICharateristics>();

	/**
	 * Creates
	 * 
	 * @param sI
	 */
	public NodaCollabsConfiguration(Map<String, Map<Integer, NodaProcConfiguration>> sI, Map<String, Integer> sM,
			Map<String, MICharateristics> miP) {
		sigmaI = sI;
		miPools = miP;
		sigmaM = sM;
	}

	@Override
	public Map<String, String> getGlobalData() {
		return new HashMap<String, String>();
	}

	@Override
	public Map<String, String> getLocalData() {
		return new HashMap<String, String>();
	}

	public void setSigmaM(Map<String, Integer> sigmaM) {
		this.sigmaM = sigmaM;
	}

	public Map<String, Map<Integer, NodaProcConfiguration>> getSigmaI() {
		return sigmaI;
	}

	public Map<String, MICharateristics> getMiPools() {
		return miPools;
	}

	public Map<String, Integer> getSigmaM() {
		return sigmaM;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	public JsonObject toJson() {
		JsonObject collConf = new JsonObject();

		JsonObject processes = new JsonObject();
		for (String p : sigmaI.keySet()) {
			JsonObject instances = new JsonObject();
			for (Integer i : sigmaI.get(p).keySet()) {
				instances.add(String.valueOf(i), sigmaI.get(p).get(i).toJson());
			}
			processes.add(p, instances);
		}
		collConf.add(SIGMA_I, processes);

		JsonObject messages = new JsonObject();
		for (String mF : sigmaM.keySet()) {
			messages.addProperty(mF, sigmaM.get(mF));
		}
		collConf.add(SIGMA_M, messages);

		return collConf;
	}

	@Override
	public Set<String> getInstances(String proc) {
		Set<String> ret = new HashSet<String>();
		for (Integer i : sigmaI.get(proc).keySet()) {
			ret.add(String.valueOf(i));
		}
		return ret;
	}

	@Override
	public Set<String> getProcesses() {
		return sigmaI.keySet();
	}

	private Integer getNewID() {
		return NodaCollabsConfiguration.instanceID++;
	}

	public void createInstance(Process process, String startEventID) throws MidaException {
		String p = process.getId();
		int min = miPools.get(p).getMin();
		int max = miPools.get(p).getMax();
		int live = miPools.get(p).getLive();
		if (live < max) {// CAN instantiate
			if (live < min) {// multiple instantiation
				for (int i = 0; i < min - live; i++) {
					int instID = getNewID();
					sigmaI.get(p).put(instID, new NodaProcConfiguration(process));
					Auxiliaries.inc(sigmaI.get(p).get(instID), startEventID);
					miPools.get(p).addLive();
				}
			} else {// single instantiation
				int instID = getNewID();
				sigmaI.get(p).put(instID, new NodaProcConfiguration(process));
				Auxiliaries.inc(sigmaI.get(p).get(instID), startEventID);
				miPools.get(p).addLive();
			}
		} else {// cannot instantiate
			MidaStatus.setStatus(MidaStatus.MAXINSTANCE);
			throw new MidaException(MidaStatus.MAXINSTANCE, p, -1, "Max instance reached");
		}

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NodaCollabsConfiguration that = (NodaCollabsConfiguration) o;
		return Objects.equals(valOfval(sigmaI),valOfval(that.sigmaI)) &&
				Objects.equals(sigmaM, that.sigmaM) &&
				Objects.equals(miPools, that.miPools);
	}

	private Set<NodaProcConfiguration> valOfval(Map<String, Map<Integer, NodaProcConfiguration>> sigmaI) {
		Set<NodaProcConfiguration> r = new HashSet<NodaProcConfiguration>();
		for(Map<Integer, NodaProcConfiguration> c: sigmaI.values()){
			r.addAll(c.values());
		}
		return r;
	}

	@Override
	public int hashCode() {
		return Objects.hash(valOfval(sigmaI), sigmaM, miPools);
	}
}
