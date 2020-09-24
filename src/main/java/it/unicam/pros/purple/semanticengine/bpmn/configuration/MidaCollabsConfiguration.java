package it.unicam.pros.purple.semanticengine.bpmn.configuration;

import java.util.*;

import javax.script.Bindings;
 
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.datastate.DataStoreState;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;

public class MidaCollabsConfiguration implements Configuration {

	private static final String SIGMA_I = "Sigma_i";
	private static final String SIGMA_M = "Sigma_m";
	private static final String SIGMA_DS = "Sigma_ds";

	private static int instanceID = 0;
	private Map<String, Map<Integer, MidaProcConfiguration>> sigmaI = new HashMap<String, Map<Integer, MidaProcConfiguration>>();
	private Map<String, Queue<String[]>> sigmaM;
	private DataStoreState sigmaDS;
	private Map<String, MICharateristics> miPools = new HashMap<String, MICharateristics>();


	/**
	 * Creates
	 *
	 */
	public MidaCollabsConfiguration(Map<String, Map<Integer, MidaProcConfiguration>> sI,
			Map<String, Queue<String[]>> sM, DataStoreState sDS, Map<String, MICharateristics> mP) {
		sigmaI = sI;
		sigmaM = sM;
		sigmaDS = sDS;
		miPools = mP;
	}

	@Override
	public Map<String, String> getGlobalData() {
		Bindings scope = sigmaDS.getScope();
		Map<String, String> ret = new HashMap<String, String>();
		for (String x : scope.keySet()) {
			ret.put(x, scope.get(x).toString());
		}
		return ret;
	}

	@Override
	public Map<String, String> getLocalData() {
		Map<String, String> ret = new HashMap<String, String>();
		for (String proc : sigmaI.keySet()) {
			for (Integer inst : sigmaI.get(proc).keySet()) {
				Bindings scope = sigmaI.get(proc).get(inst).getSigmaDO().getScope();
				for (String x : scope.keySet()) {
					ret.put(x, scope.get(x).toString());
				}
			}
		}
		return ret;
	}


	public Map<String, MICharateristics> getMiPools() {
		return miPools;
	}

	public void setMiPools(Map<String, MICharateristics> miPools) {
		this.miPools = miPools;
	}



	public void setSigmaM(Map<String, Queue<String[]>> sigmaM) {
		this.sigmaM = sigmaM;
	}

	public Map<String, Map<Integer, MidaProcConfiguration>> getSigmaI() {
		return sigmaI;
	}

	public Map<String, Queue<String[]>> getSigmaM() {
		return sigmaM;
	}

	public DataStoreState getSigmaDS() {
		return sigmaDS;
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
			JsonArray queue = new JsonArray();
			for (String[] msg : sigmaM.get(mF)) {
				queue.add(new JsonPrimitive(msg.toString()));
			}
			messages.add(mF, queue);
		}
		collConf.add(SIGMA_M, messages);

		collConf.add(SIGMA_DS, sigmaDS.toJson());

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
		return MidaCollabsConfiguration.instanceID++;
	}
	public void createInstance(String p, String startEventID) throws MidaException {
		int min = miPools.get(p).getMin();
		int max = miPools.get(p).getMax();
		int live = miPools.get(p).getLive();
		if (live < max) {// CAN instantiate
			if (live < min) {// multiple instantiation
				for (int i = 0; i < min - live; i++) {
					int instID = getNewID();
					sigmaI.get(p).put(instID, new MidaProcConfiguration(p));
					Auxiliaries.inc(sigmaI.get(p).get(instID), startEventID);
					miPools.get(p).addLive();
				}
			} else {// single instantiation
				int instID = getNewID();
				sigmaI.get(p).put(instID, new MidaProcConfiguration(p));
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
		MidaCollabsConfiguration that = (MidaCollabsConfiguration) o;
		return Objects.equals(valOfval(sigmaI), valOfval(that.sigmaI)) &&
				Objects.equals(sigmaM, that.sigmaM) &&
				Objects.equals(sigmaDS, that.sigmaDS) &&
				Objects.equals(miPools, that.miPools);
	}

	private Set<MidaProcConfiguration> valOfval(Map<String, Map<Integer, MidaProcConfiguration>> sigmaI) {
		Set<MidaProcConfiguration> r = new HashSet<MidaProcConfiguration>();
		for(Map<Integer, MidaProcConfiguration> c: sigmaI.values()){
			r.addAll(c.values());
		}
		return r;
	}

	@Override
	public int hashCode() {
		return Objects.hash(valOfval(sigmaI), sigmaM, sigmaDS, miPools);
	}
}
