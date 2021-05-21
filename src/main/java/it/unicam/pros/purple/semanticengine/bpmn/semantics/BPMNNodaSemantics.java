package it.unicam.pros.purple.semanticengine.bpmn.semantics;

import java.util.*;

import it.unicam.pros.purple.semanticengine.bpmn.elements.IntReceiveTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntTask;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import org.camunda.bpm.model.bpmn.impl.instance.EndEventImpl;
import org.camunda.bpm.model.bpmn.impl.instance.EventBasedGatewayImpl;
import org.camunda.bpm.model.bpmn.impl.instance.ExclusiveGatewayImpl;
import org.camunda.bpm.model.bpmn.impl.instance.IntermediateCatchEventImpl;
import org.camunda.bpm.model.bpmn.impl.instance.IntermediateThrowEventImpl;
import org.camunda.bpm.model.bpmn.impl.instance.ParallelGatewayImpl;
import org.camunda.bpm.model.bpmn.impl.instance.ReceiveTaskImpl;
import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.impl.instance.StartEventImpl;
import org.camunda.bpm.model.bpmn.impl.instance.TaskImpl;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.EventBasedGateway;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.Process;

import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.EndEventBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.EventBasedGatewayBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.ExclusiveGatewayJoinBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.ExclusiveGatewaySplitBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.IntermediateCatchEventBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.IntermediateThrowEventBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.MIIntReceiveTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.MIIntSendTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.MIIntTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.MIPTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.MISTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.MessageEndEventBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.MessageStartEventBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.NAReceiveTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.NASendTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.NATaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.ParallelGatewayJoinBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.ParallelGatewaySplitBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.ReceiveTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.SendTaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.StartEventBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.TaskBehaviour;
import it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda.TerminateEndEventBehaviour;

public class BPMNNodaSemantics implements Semantics {
 
	@Override
	public Map<Configuration, Set<Event>> getNexts(Map<Process, List<FlowNode>> pools, Configuration c) throws Exception {
		NodaCollabsConfiguration conf = (NodaCollabsConfiguration) c;
		Map<Configuration, Set<Event>> ret = new HashMap<Configuration, Set<Event>>();

		List<Process> processes = new ArrayList<Process>(pools.keySet());
		for (int i = 0; i < processes.size(); i++) {// iterate processes
			List<Integer> activeInstances = new ArrayList<Integer>(
					conf.getSigmaI().get(processes.get(i).getId()).keySet());
			for (int j = 0; j < activeInstances.size(); j++) {// iterate instances
				List<Object> nodes = new ArrayList<Object>(pools.get(processes.get(i)));
				nodes.addAll(
						ModelUtils.getProcessConf(processes.get(i), activeInstances.get(j), conf).getIntActivities());
				Collections.shuffle(nodes);
				for (int k = 0; k < nodes.size(); k++) {// iterate elements
					NodaCollabsConfiguration tmpConf = (NodaCollabsConfiguration) DeepCopy.copy(conf);
					Map<Configuration, Event> e = execute(nodes.get(k), processes.get(i), activeInstances.get(j), tmpConf);
					if (! e.isEmpty()) {
						// QUA FAI QUALCOSA
						//System.out.println(nodes.get(k)+" "+processes.get(i).getName()+" "+activeInstances.get(j));
						for(Configuration x : e.keySet()) {
							if(! ret.containsKey(x)) {
								ret.put(x, new HashSet<Event>());
							}
							ret.get(x).add(e.get(x));
						}
						break;
					}
				}
			}
		}
		return ret;
	}

	private Map<Configuration, Event> execute(Object object, Process process, int instance, NodaCollabsConfiguration conf)
			throws MidaException {
		if (object instanceof FlowNode) {
			FlowNode flowNode = (FlowNode) object;
			if (flowNode instanceof StartEventImpl) {// START EVENTS
				if ((ModelUtils.isMessageEvent(flowNode))) {// MSG START EVENT
					//System.out.println("MSGSTART");
					return MessageStartEventBehaviour.isActive((StartEvent) flowNode, conf, process, instance);
				} else {// START EVENT
					//System.out.println("START");
					return StartEventBehaviour.isActive((StartEvent) flowNode, conf, process, instance);
				}
			} else if (flowNode instanceof EndEventImpl) {// END EVENTS
				if (ModelUtils.isTerminate(flowNode)) {// TERMINATE END EVENT
					//System.out.println("TERMINATE");
					return TerminateEndEventBehaviour.isActive((EndEvent) flowNode, conf, process, instance);
				} else if ((ModelUtils.isMessageEvent(flowNode))) {// MSG END EVENT
					//System.out.println("MSGEND");
					return MessageEndEventBehaviour.isActive((EndEvent) flowNode, conf, process, instance);
				} else {// END EVENT
					//System.out.println("END");
					return EndEventBehaviour.isActive((EndEvent) flowNode, conf, process, instance);
				}
			} else if (flowNode instanceof TaskImpl) {// TASKS
				if (ModelUtils.isMultiInstance(flowNode)) {
					return miTask((Task) flowNode, conf, process, instance);
				}
//				if (ModelUtils.isAtomic((Task) flowNode)) {
//					if (flowNode instanceof SendTaskImpl) {// SENDTASKS
//						//System.out.println("SNDTASK");
//						return SendTaskBehaviour.isActive((SendTaskImpl) flowNode, conf, process, instance);
//					} else if (flowNode instanceof ReceiveTaskImpl) {// RECEIVETASKS
//						//System.out.println("RCVTASK");
//						return ReceiveTaskBehaviour.isActive((ReceiveTaskImpl) flowNode, conf, process, instance);
//					} else {
//						//System.out.println("TASK");
//						return TaskBehaviour.isActive((TaskImpl) flowNode, conf, process, instance);
//					}
//				}
				else {// NON-ATOMIC
					if (flowNode instanceof SendTaskImpl) {// SENDTASKS
						//System.out.println("NASNDTASK");
						return NASendTaskBehaviour.isActive((SendTaskImpl) flowNode, conf, process, instance);
					} else if (flowNode instanceof ReceiveTaskImpl) {// RECEIVETASKS
						//System.out.println("NARCVTASK");
						return NAReceiveTaskBehaviour.isActive((ReceiveTaskImpl) flowNode, conf, process, instance);
					} else {
						return NATaskBehaviour.isActive((TaskImpl) flowNode, conf, process, instance);
					}
				}
			} else if (flowNode instanceof IntermediateCatchEventImpl) {
				return IntermediateCatchEventBehaviour.isActive((IntermediateCatchEvent) flowNode, conf, process,
						instance);
			} else if (flowNode instanceof IntermediateThrowEventImpl) {
				return IntermediateThrowEventBehaviour.isActive((IntermediateThrowEvent) flowNode, conf, process,
						instance);
			} else if (flowNode instanceof ExclusiveGatewayImpl) {
				if (ModelUtils.isSplit(flowNode)) {
					return ExclusiveGatewaySplitBehaviour.isActive((ExclusiveGateway) flowNode, conf, process, instance);
				} else {
					return ExclusiveGatewayJoinBehaviour.isActive((ExclusiveGateway) flowNode, conf, process, instance);
				}
			} else if (flowNode instanceof ParallelGatewayImpl) {
				if (ModelUtils.isSplit(flowNode)) {
					return ParallelGatewaySplitBehaviour.isActive((ParallelGateway) flowNode, conf, process, instance);
				} else {
					return ParallelGatewayJoinBehaviour.isActive((ParallelGateway) flowNode, conf, process, instance);
				}
			} else if (flowNode instanceof EventBasedGatewayImpl) {
				return EventBasedGatewayBehaviour.isActive((EventBasedGateway) flowNode, conf, process, instance);
			}
		} else if (object instanceof IntTask) {
			IntTask t = (IntTask) object;
			return MIIntTaskBehaviour.isActive(t, conf, process, instance);
		} else if (object instanceof IntSendTask) {
			IntSendTask t = (IntSendTask) object;
			return MIIntSendTaskBehaviour.isActive(t, conf, process, instance);
		} else if (object instanceof IntReceiveTask) {
			IntReceiveTask t = (IntReceiveTask) object;
			return MIIntReceiveTaskBehaviour.isActive(t, conf, process, instance);
		}
		return null;
	}

	private Map<Configuration, Event> miTask(Task t, NodaCollabsConfiguration conf, Process process,  int instance)
			throws MidaException {
		if (ModelUtils.isSequential(t)) {
			return MISTaskBehaviour.isActive(t, conf, process,  instance);
		} else {
			return MIPTaskBehaviour.isActive(t, conf, process, instance);
		}
	}

}
