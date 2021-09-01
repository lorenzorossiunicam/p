package it.unicam.pros.purple.semanticengine.bpmn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.SemanticEngine;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MICharateristics;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.ParticipantMultiplicity;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import it.unicam.pros.purple.semanticengine.bpmn.semantics.BPMNNodaSemantics;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;

public class NodaEngine implements SemanticEngine {

	private BPMNNodaSemantics semantics;
	private String name;
	private BpmnModelInstance model;
	private Map<Process, List<FlowNode>> pools = new HashMap<Process, List<FlowNode>>();
 
	public NodaEngine(String name, BpmnModelInstance mi) { 
		model = mi;
		this.semantics = new BPMNNodaSemantics();
		this.name = name;
		for (Process p : mi.getModelElementsByType(Process.class)) {
			pools.put(p, new ArrayList<FlowNode>(p.getChildElementsByType(FlowNode.class)));
		}
	}

	@Override
	public Map<Configuration, Set<Event>> getNexts(Configuration c) throws Exception {
		return semantics.getNexts(pools, c);
	}

	@Override
	public Configuration getInitConf() {
		Map<String, Map<Integer, NodaProcConfiguration>> sigmaI = new HashMap<String, Map<Integer, NodaProcConfiguration>>();
		Map<String, Integer> sigmaM = new HashMap<String, Integer>();
		Map<String, MICharateristics> miPools = new HashMap<String, MICharateristics>();
		for (Process p : model.getModelElementsByType(Process.class)) {
			String procName = p.getId();//getAttributeValue("processRef");
			int min = 1, max = 1;
			if (p.getChildElementsByType(ParticipantMultiplicity.class).size() != 0) {
				ParticipantMultiplicity multiInstance = p.getChildElementsByType(ParticipantMultiplicity.class)
						.iterator().next();
				min = Integer.valueOf(multiInstance.getAttributeValue("minimum"));
				max = Integer.valueOf(multiInstance.getAttributeValue("maximum"));
			}
			Process proc = (Process) model.getModelElementById(procName);
			sigmaI.put(proc.getId(), new HashMap<Integer, NodaProcConfiguration>());
			miPools.put(proc.getId(), new MICharateristics(min, max));
		}
		for (MessageFlow msg : model.getModelElementsByType(MessageFlow.class)) {
			sigmaM.put(msg.getId(), 0);
		}
		return new NodaCollabsConfiguration(sigmaI, sigmaM, miPools);
	}

	@Override
	public Configuration initData(Configuration c) {
		return (NodaCollabsConfiguration) c;
	}

	@Override
	public Configuration initInstances(Configuration c) throws Exception {
		NodaCollabsConfiguration conf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		return rndTokensPlacement(conf);
	}

	private NodaCollabsConfiguration rndTokensPlacement(NodaCollabsConfiguration conf) throws MidaException {
		for (String proc : conf.getSigmaI().keySet()) {
			Process p = model.getModelElementById(proc);
			int qnt = 1;
			if (conf.getMiPools().get(proc) != null) {
				qnt = conf.getMiPools().get(proc).getMax();
			}
			List<String> starts = new ArrayList<String>();
			for (StartEvent s : p.getChildElementsByType(StartEvent.class)) {
				if (!ModelUtils.isMessageEvent(s)) {
					starts.add(s.getId());
				}
			}
			if (starts.size() < 1) continue;
			Collections.shuffle(starts);
			for (int i = 0; i < qnt; i++) {
				conf.createInstance(p, starts.iterator().next());
			}
		}
		return conf;
	}

	

	@Override
	public String getModelName() {
		return name;
	}
}
