package it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics;

import java.util.*;

import it.unicam.pros.purple.semanticengine.ptnet.PTNetUtil;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import org.camunda.bpm.model.bpmn.impl.instance.GatewayImpl;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public final class AlphaRelations {
    private static Map<String, Map<String, FootprintRelations>> matrix;

    public static EventLog compareAlphaRelations(Map<String, Map<String, FootprintRelations>> ref,
                                                 Map<String, Map<String, FootprintRelations>> disc, double tau, int refRelations) {


        EventLog missing = new EventLogImpl(null, null);

        for (String refAct1 : ref.keySet()) {
            if (disc.get(refAct1) == null) {// Activity not yet discovered
                Trace t = new TraceImpl(null);
                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                missing.addTrace(t);
            } else {
                Map<String, FootprintRelations> refRel = ref.get(refAct1);
                for (String refAct2 : refRel.keySet()) {
                    switch (refRel.get(refAct2)) {
                        case SEQUENCE:
                            if (disc.get(refAct1).get(refAct2) == null
                                    || disc.get(refAct1).get(refAct2) == FootprintRelations.PARALLEL
                                    || disc.get(refAct1).get(refAct2) == FootprintRelations.CHOICE) {
                                Trace t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                            }
                            break;
                        case PARALLEL:
                            if (disc.get(refAct1).get(refAct2) == null
                                    || disc.get(refAct1).get(refAct2) == FootprintRelations.CHOICE) {
                                Trace t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                                t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                            } else if (disc.get(refAct1).get(refAct2) == FootprintRelations.SEQUENCE) {
                                Trace t = new TraceImpl(null);
                                t.appendEvent(new EventImpl(null, null, refAct2, null, null, null, null, null, null, null, null));
                                t.appendEvent(new EventImpl(null, null, refAct1, null, null, null, null, null, null, null, null));
                                missing.addTrace(t);
                            }
                            break;
                        case CHOICE:
                            if (disc.get(refAct1).get(refAct2) == null
                                    || disc.get(refAct1).get(refAct2) == FootprintRelations.PARALLEL
                                    || disc.get(refAct1).get(refAct2) == FootprintRelations.SEQUENCE) {
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
        if(missing.size()/refRelations >= tau/100) return null;
        return missing;
    }

    public static Map<String, Map<String, FootprintRelations>> getAlphaRelations(Petrinet petrinet) {
        Collection<Transition> transitions = petrinet.getTransitions();
        matrix = new HashMap<String, Map<String, FootprintRelations>>(transitions.size());
        for (Transition t : transitions) {
            //matrix.put(t.getLabel(), new HashMap<String, FootprintRelations>(transitions.size()));
            matrix.put(PTNetUtil.getTransitionName(t), new HashMap<String, FootprintRelations>(transitions.size()));
            for (Transition next : t.getVisibleSuccessors()){
                matrix.get(PTNetUtil.getTransitionName(t)).put(PTNetUtil.getTransitionName(next),FootprintRelations.SEQUENCE);
                //matrix.get(t.getLabel()).put(next.getLabel(),FootprintRelations.SEQUENCE);
            }
        }
        return matrix;
    }

    /*
     * p must be Mida compliant
     */
    public static Map<String, Map<String, FootprintRelations>> getAlphaRelations(Process p) {
        Collection<Task> tasks = p.getChildElementsByType(Task.class);
        matrix = new HashMap<String, Map<String, FootprintRelations>>(tasks.size());
        for (Task t : tasks) {
            matrix.put(t.getName(), new HashMap<String, FootprintRelations>(tasks.size()));
        }
        for (Task t : tasks) {
			for (FlowNode next : t.getSucceedingNodes().list()) {
				if (next instanceof Task) {
					Task tmp = (Task) next;
					matrix.get(t.getName()).put(tmp.getName(), FootprintRelations.SEQUENCE);
				} else if (next instanceof GatewayImpl) {
					Set<Task> suceessorTasks = findSuccessorTasks(next);
					for (Task u : suceessorTasks) {
						matrix.get(t.getName()).put(u.getName(), FootprintRelations.SEQUENCE);
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

    public static Map<String, Map<String, FootprintRelations>> getAlphaRelations(EventLog log) {
        matrix = new HashMap<String, Map<String, FootprintRelations>>();
        for (Trace t : log.getTraces()){
            List<Event> trace = t.getTrace();
            for (int i = 0; i<trace.size()-1; i++){
                String init = trace.get(i).getEventName();
                String fin = trace.get(i+1).getEventName();
                if (matrix.get(init) == null){
                    matrix.put(init, new HashMap<String, FootprintRelations>());
                }
                if (matrix.get(fin) == null){
                    matrix.put(fin, new HashMap<String, FootprintRelations>());
                }
                if (matrix.get(fin).get(init) == null){
                    matrix.get(init).put(fin, FootprintRelations.SEQUENCE);
                }else{
                    matrix.get(fin).remove(init);
                }
            }
        }
        return matrix;
    }

    public static void main(String[] a) throws Exception {
        for (int i = 0; i<10; i++){
            doIt(i);
        }
    }

    private static void doIt(int kk) throws Exception {
        XLog lo =  LogIO.parseXES("C:\\Users\\lo_re\\Git\\Work\\Purpose Parametric Log Generator\\Validation\\Rediscoverability\\Order relations\\p"+kk+"ged_1k.xes");
        matrix = new HashMap<String, Map<String, FootprintRelations>>();
        System.out.println(lo.size());
        for (XTrace t : lo){
            for (int i = 0; i< t.size()-1; i++){
                String init = t.get(i).getAttributes().get("concept:name").toString();
                String fin = t.get(i+1).getAttributes().get("concept:name").toString();
                init = init.replaceAll("'","");
                fin = fin.replaceAll("'", "");
                if (matrix.get(init) == null){
                    matrix.put(init, new HashMap<String, FootprintRelations>());
                }
                if (matrix.get(fin) == null){
                    matrix.put(fin, new HashMap<String, FootprintRelations>());
                }
                //if (matrix.get(fin).get(init) == null){
                matrix.get(init).put(fin, FootprintRelations.SEQUENCE);
                //}
//                else{
//                    matrix.get(fin).remove(init);
//                }
            }
        }
        System.out.println(matrix);
        lo =  LogIO.parseXES("C:\\Users\\lo_re\\Git\\Work\\Purpose Parametric Log Generator\\Validation\\Rediscoverability\\Order relations\\p"+kk+"pplg.xes");
        Map<String, Map<String, FootprintRelations>> matrix1 = new HashMap<String, Map<String, FootprintRelations>>();
        for (XTrace t : lo){
            for (int i = 0; i< t.size()-1; i++){
                String init = t.get(i).getAttributes().get("concept:name").toString();
                String fin = t.get(i+1).getAttributes().get("concept:name").toString();
                if (matrix1.get(init) == null){
                    matrix1.put(init, new HashMap<String, FootprintRelations>());
                }
                if (matrix1.get(fin) == null){
                    matrix1.put(fin, new HashMap<String, FootprintRelations>());
                }
                //if (matrix1.get(fin).get(init) == null){
                matrix1.get(init).put(fin, FootprintRelations.SEQUENCE);
                //}
//                else{
//                    matrix1.get(fin).remove(init);
//                }
            }
        }
        System.out.println(matrix1);
        int i = 0;
        int missing = 0;
        for(String k : matrix1.keySet()){
            for(String k1 : matrix1.get(k).keySet()){
                i++;
                if(!matrix.containsKey(k) || !matrix.get(k).containsKey(k1) || !matrix1.get(k).get(k1).equals(matrix.get(k).get(k1))){
                    missing++;
                }
            }
        }
        System.out.println(1-((1.0*missing)/i));
    }
}
