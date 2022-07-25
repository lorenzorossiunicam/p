package it.unicam.pros.purple.semanticengine.bpmn;


import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.SemanticEngine;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MICharateristics;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.datastate.DataStoreState;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.BPMNMidaSemantics;
import it.unicam.pros.purple.semanticengine.bpmn.utils.DataUtil;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.*;

public class MidaEngine implements SemanticEngine {

	
	private BPMNMidaSemantics semantics;
	private String name;
	private BpmnModelInstance model;
	private Map<Process, List<FlowNode>> pools = new HashMap<Process, List<FlowNode>>();
	 
	
	public MidaEngine(String name, BpmnModelInstance mi) {
		model = mi;
		this.semantics = new BPMNMidaSemantics();
		this.name = name;
		for (Process p : mi.getModelElementsByType(Process.class)) {
			pools.put(p, new ArrayList<FlowNode>(p.getChildElementsByType(FlowNode.class)));
		}
	}
	
	@Override
	public Map<Configuration, Set<Event>> getNexts(Configuration c) throws Exception {
		return  semantics.getNexts(pools, c);
	}
	@Override
	public MidaCollabsConfiguration getInitConf() {
		Map<String, Map<Integer, MidaProcConfiguration>> sigmaI = new HashMap<String, Map<Integer, MidaProcConfiguration>>();
		Map<String, Queue<String[]>> sigmaM;
		DataStoreState sigmaDS;
		Map<String, MICharateristics> miPools = new HashMap<String, MICharateristics>();
		BpmnModelInstance mi = model;
		for (Participant p : mi.getModelElementsByType(Participant.class)) {
			String procName = p.getAttributeValue("processRef");
			int min = 1, max = 1;
			if (p.getChildElementsByType(ParticipantMultiplicity.class).size() != 0) {
				ParticipantMultiplicity multiInstance = p.getChildElementsByType(ParticipantMultiplicity.class)
						.iterator().next();
				min = Integer.valueOf(multiInstance.getAttributeValue("minimum"));
				max = Integer.valueOf(multiInstance.getAttributeValue("maximum"));
			}
			Process proc = (Process) mi.getModelElementById(procName);
			sigmaI.put(proc.getId(), new HashMap<Integer, MidaProcConfiguration>());
			miPools.put(proc.getId(), new MICharateristics(min, max));
		}

		sigmaM = new HashMap<String, Queue<String[]>>();
		for (MessageFlow msg : mi.getModelElementsByType(MessageFlow.class)) {
			sigmaM.put(msg.getId(), new LinkedList<String[]>());
		}

		sigmaDS = new DataStoreState(mi);
		return new MidaCollabsConfiguration(sigmaI, sigmaM, sigmaDS, miPools);
	}

	@Override
	public MidaCollabsConfiguration initData(Configuration c) { 
		MidaCollabsConfiguration conf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		return DataUtil.initData(conf);
	}

	@Override
	public MidaCollabsConfiguration initInstances(Configuration c) throws Exception {
		MidaCollabsConfiguration conf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		return rndTokensPlacement(conf);
	}


	public MidaCollabsConfiguration rndTokensPlacement(MidaCollabsConfiguration c) throws MidaException {
		for (String proc : c.getSigmaI().keySet()) {
			Process p = model.getModelElementById(proc);
			int qnt = 1;
			if (c.getMiPools().get(proc) != null) {
				qnt = c.getMiPools().get(proc).getMax();
			}
			List<String> starts = new ArrayList<String>();
			for (StartEvent s : p.getChildElementsByType(StartEvent.class)) {
				if (!ModelUtils.isMessageEvent(s)) {
					starts.add(s.getId());
				}
			}
			Collections.shuffle(starts);
			for (int i = 0; i < qnt; i++) {
				c.createInstance(proc, starts.iterator().next());
			}
		}
		return c;
	}
 
	
	@Override
	public String getModelName() { 
		return name;
	}
  
 
}
