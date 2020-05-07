package it.unicam.pros.guidedsimulator.evaluator.purpose.rediscoverability.metrics;

import java.util.*;

import it.unicam.pros.guidedsimulator.util.eventlogs.EventLog;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.Trace;
import org.camunda.bpm.model.bpmn.impl.instance.GatewayImpl;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.guidedsimulator.util.eventlogs.EventLogImpl;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.EventImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public final class AlphaRelations {
    private static Map<String, Map<String, Relations>> matrix;

    public static EventLog compareAlphaRelations(Map<String, Map<String, Relations>> ref,
                                                 Map<String, Map<String, Relations>> disc, double tau, int refRelations) {


        EventLog missing = new EventLogImpl(null, null);

        for (String refAct1 : ref.keySet()) {
            if (disc.get(refAct1) == null) {// Activity not yet discovered
                Trace t = new TraceImpl(null);
                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                missing.addTrace(t);
            } else {
                Map<String, Relations> refRel = ref.get(refAct1);
                for (String refAct2 : refRel.keySet()) {
                    switch (refRel.get(refAct2)) {
                        case SEQUENCE:
                            if (disc.get(refAct1).get(refAct2) == null
                                    || disc.get(refAct1).get(refAct2) == Relations.PARALLEL
                                    || disc.get(refAct1).get(refAct2) == Relations.CHOICE) {
                                Trace t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                            }
                            break;
                        case PARALLEL:
                            if (disc.get(refAct1).get(refAct2) == null
                                    || disc.get(refAct1).get(refAct2) == Relations.CHOICE) {
                                Trace t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                                t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                            } else if (disc.get(refAct1).get(refAct2) == Relations.SEQUENCE) {
                                Trace t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                            }
                            break;
                        case CHOICE:
                            if (disc.get(refAct1).get(refAct2) == null
                                    || disc.get(refAct1).get(refAct2) == Relations.PARALLEL
                                    || disc.get(refAct1).get(refAct2) == Relations.SEQUENCE) {
                                Trace t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, null, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                                t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, null, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                            }
                            break;
                    }
                }
            }
        }
        System.out.println("rel "+refRelations+" tau "+tau/100+" missin "+missing.size());
        if(missing.size()/refRelations >= tau/100) return null;
        return missing;
    }

    /*
     * p must be Mida compliant
     */
    public static Map<String, Map<String, Relations>> getAlphaRelations(Process p) {
        Collection<Task> tasks = p.getChildElementsByType(Task.class);
        matrix = new HashMap<String, Map<String, Relations>>(tasks.size());
        for (Task t : tasks) {
            matrix.put(t.getName(), new HashMap<String, Relations>(tasks.size()));
        }
        for (Task t : tasks) {
			for (FlowNode next : t.getSucceedingNodes().list()) {
				if (next instanceof Task) {
					Task tmp = (Task) next;
					matrix.get(t.getName()).put(tmp.getName(), Relations.SEQUENCE);
				} else if (next instanceof GatewayImpl) {
					Set<Task> suceessorTasks = findSuccessorTasks(next);
					for (Task u : suceessorTasks) {
						matrix.get(t.getName()).put(u.getName(), Relations.SEQUENCE);
					}
				}
			}
		}
        return matrix;
    }

    private static Set<Task> findSuccessorTasks(FlowNode n){
    	Set<Task> list = new HashSet<Task>();
    	for (FlowNode next : n.getSucceedingNodes().list()){
    		if (next instanceof Task){
    			list.add((Task) next);
			} else if (next instanceof GatewayImpl){
    			list.addAll(findSuccessorTasks(next));
			}
		}
    	return list;
	}

    public static Map<String, Map<String, Relations>> getAlphaRelations(Petrinet petrinet) {
        Collection<Transition> transitions = petrinet.getTransitions();
        matrix = new HashMap<String, Map<String, Relations>>(transitions.size());
        for (Transition t : transitions) {
            matrix.put(t.getLabel(), new HashMap<String, Relations>(transitions.size()));
            for (Transition next : t.getVisibleSuccessors()){
            	matrix.get(t.getLabel()).put(next.getLabel(),Relations.SEQUENCE);
			}
        }
		//TODO PARALLEL ...
        return matrix;
    }


    public enum Relations {
        SEQUENCE, PARALLEL, CHOICE, NONE
    }
}
