package it.unicam.pros.guidedsimulator.semanticengine.bpmn;
 
 

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.SemanticEngine;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.datastate.DataStoreState;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaProcConfiguration;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.ParticipantMultiplicity;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.BPMNMidaSemantics;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MICharateristics;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.DataUtil;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

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
