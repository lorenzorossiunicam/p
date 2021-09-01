package it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics;

import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.metrics.ModelProbabilities;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.TaskImpl;
import org.camunda.bpm.model.bpmn.instance.*;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

public class FootprintMatrix {

    private static BpmnModelInstance model;
    private static boolean isLTD = false;
    private Map<String,Map<String,FootprintRelations>> footprintMatrix;
    private Map<List<String>, int[]> loops = new HashMap<>();



    public Map<List<String>, int[]> getLoops() {
        return loops;
    }
    public Map<String,Map<String,FootprintRelations>> getFootprintMatrix() {return footprintMatrix;}

    private static Map<String[], Queue<Double>> ratioseries = new HashMap<>();

    public FootprintMatrix(BpmnModelInstance mi){
        this.model = mi;
        this.footprintMatrix = new HashMap<>();
        for(Task t : model.getModelElementsByType(Task.class)){
            footprintMatrix.put(t.getName(),new HashMap<>());
        }
        findDFR();
    }


    private void findDFR() {
        List<Task> tasks = new ArrayList<>(model.getModelElementsByType(Task.class));
        for (int i = 0; i < tasks.size() - 1; i++) {
            Task t1 = tasks.get(i);
            for (int j = i + 1; j < tasks.size(); j++) {
                Task t2 = tasks.get(j);
                int rel = findCommonRoot(t1, t2);
                switch (rel){
                    case 1 :
                        footprintMatrix.get(t1.getName()).put(t2.getName(), FootprintRelations.SEQUENCE);
                        break;
                    case 2 :
                        footprintMatrix.get(t2.getName()).put(t1.getName(), FootprintRelations.SEQUENCE);
                        break;
                    case 3 :
                        footprintMatrix.get(t1.getName()).put(t2.getName(), FootprintRelations.SEQUENCE);
                        footprintMatrix.get(t2.getName()).put(t1.getName(), FootprintRelations.SEQUENCE);
                        break;
                    case 4 :
//                        footprintMatrix.get(t1.getName()).put(t2.getName(), FootprintRelations.CHOICE);
//                        footprintMatrix.get(t2.getName()).put(t1.getName(), FootprintRelations.CHOICE);
                        break;
                    default:
                        break;
                }
            }
        }
        findLoops();
    }


    private void findLoops() {
        SimpleDirectedWeightedGraph<FlowNode, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<FlowNode, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (SequenceFlow e : model.getModelElementsByType(SequenceFlow.class)) {
            graph.addVertex(e.getSource());
            graph.addVertex(e.getTarget());
            graph.addEdge(e.getSource(), e.getTarget());
        }
        SzwarcfiterLauerSimpleCycles<FlowNode, DefaultWeightedEdge> f = new SzwarcfiterLauerSimpleCycles<FlowNode, DefaultWeightedEdge>(
                graph);
        for(List<FlowNode> c : f.findSimpleCycles()){
            String stPre = "", stPost = "";
            boolean pre = false, post = false;
            List<String> ciclo = new ArrayList<>();
            for (int i = 0; i< c.size(); i++){
                FlowNode n = c.get(i);
                if(n instanceof TaskImpl){
                    ciclo.add(n.getName());
                    if (pre){
                        stPre = n.getName();
                        pre = false;
                    }
                    if (post){
                        stPost = n.getName();
                        post = false;
                    }
                }
                if (n instanceof ExclusiveGateway){
                    if (ModelUtils.isJoin(n)){
                        pre = true;
                    }else{
                        post = true;
                    }
                }
            }
            int ipre, ipost;
            if (stPost == ""){
                if (stPre == ciclo.get(0)){
                    ipost = -1;
                }else{
                    ipost = 0;
                }
            }else{
                ipost = ciclo.indexOf(stPost);
            }
            if (stPre == ""){
                if (stPost == ciclo.get(0)){
                    ipre = -1;
                }else{
                    ipre = 0;
                }
            }else{
                ipre = ciclo.indexOf(stPre);
            }
            loops.put(ciclo, new int[]{ipre, ipost});
        }
        for(List<String> c : loops.keySet()){
            for(int i = 1; i<c.size(); i++){
                footprintMatrix.get(c.get(i - 1)).put(c.get(i), FootprintRelations.SEQUENCE);
            }
            footprintMatrix.get(c.get(c.size()-1)).put(c.get(0), FootprintRelations.SEQUENCE);
        }
    }




    /**
     *
     * @param t1
     * @param t2
     * @return 1, if t1->t2; 2, if t2->t1; 3, if t1||t2; 4, if t1#t2.
     */
    private int findCommonRoot(Task t1, Task t2) {
        List<Gateway> t1roots = new ArrayList<>(), t2roots = new ArrayList<>();
        boolean areInSequence = false;
        Set<FlowNode> visited = new HashSet<>();
        isLTD = false;
        areInSequence = getPredGateways(t1.getPreviousNodes().list(), t1roots, t2, visited);
        if (areInSequence) {
            if (isLTD) return -1;
            return 2;
        }
        visited = new HashSet<>();
        isLTD = false;
        areInSequence = getPredGateways(t2.getPreviousNodes().list(), t2roots, t1, visited);
        if (areInSequence) {
            if (isLTD) return -1;
            return 1;
        }
        for (int i=0; i<t1roots.size(); i++){
            for (int j=0; j<t2roots.size(); j++){
                if (t1roots.get(i).getId().equals(t2roots.get(j).getId())){
                    if (t1roots.get(i) instanceof ParallelGateway){
                        return 3;
                    } else {
                        return 4;
                    }
                }
            }
        }
        return -1;
    }


    private static boolean getPredGateways(List<FlowNode> preds, List<Gateway> roots, Task other,  Set<FlowNode> visited) {
        if (preds.isEmpty()) return false;
        List<FlowNode> toAdd = new ArrayList<>();
            for (FlowNode n : preds){
            visited.add(n);
            if (n instanceof Gateway && ModelUtils.isSplit(n)){
                roots.add((Gateway) n);
                toAdd.addAll(n.getPreviousNodes().list());
            } else if (n.getId().equals(other.getId())){
                return true;
            } else {
                if(n instanceof Task) isLTD = true;
                toAdd.addAll(n.getPreviousNodes().list());
            }
        }
        preds.addAll(toAdd);
        preds.removeAll(visited);
        return getPredGateways(preds, roots, other, visited);
    }

    public static void main(String[] args){
//        BpmnModelInstance mi = Bpmn.readModelFromFile(new File("m.bpmn"));
//
//        BPMNRediscoverability r = new BPMNRediscoverability(mi,"", Rediscoverability.RediscoverabilityAlgo.SPLIT_MINER);
//
//        SplitRelations rel = new SplitRelations(mi);
//        for (String[] r : rel.getParallels()){
//            System.out.println(r[0]+" - "+r[1]);
//        }
//        System.out.println(rel.getExclusives());
//        System.out.println(rel.getShort_loops());
//        EventLog l = new EventLogImpl("",null);
//        System.out.println(compareSplitRelations(rel, l, 0.0));
    }



    public static EventLog compareRelations(FootprintMatrix relations, EventLog log, Double tau) {
        Map<String,Map<String,FootprintRelations>> footprint = relations.getFootprintMatrix();
        for (Trace t : log.getTraces()){
            String curr, pred, last;
            for(int i = 0; i<t.getTrace().size(); i++){
                pred = t.get(i).getEventName();
                for(int j = i+1; j<t.getTrace().size(); j++){
                    curr = t.get(j).getEventName();
                    try {
                        if(footprint.get(pred).get(curr).equals(FootprintRelations.SEQUENCE))
                            footprint.get(pred).remove(curr);
                    } catch (NullPointerException e){
                        continue;
                    }
                }
            }
            for(int i = 2; i<t.getTrace().size(); i++){
                curr = t.get(i).getEventName();
                pred = t.get(i-1).getEventName();
                last = t.get(i-2).getEventName();
                try{
                    if (curr.equals(last)){//SHORT_LOOP
                        if(footprint.get(curr).get(pred).equals(FootprintRelations.LENGTH_2_LOOP))
                            footprint.get(curr).remove(pred);
                        if(footprint.get(pred).get(curr).equals(FootprintRelations.LENGTH_2_LOOP))
                            footprint.get(pred).remove(curr);
                    }
                }catch (NullPointerException e){
                    continue;
                }
            }
        }
        EventLog ret = new EventLogImpl("", null);

        for (String t1 : footprint.keySet()){
            for (String t2 : footprint.get(t1).keySet()){
                Trace trace = new TraceImpl(null);
                switch (footprint.get(t1).get(t2)){
                    case SEQUENCE :
                        trace.appendEvent(new EventImpl(null, null, t1, null, null, null, null, null, null, null, null));
                        trace.appendEvent(new EventImpl(null, null, t2, null, null, null, null, null, null, null, null));
                        break;
                    case LENGTH_2_LOOP :
                        trace.appendEvent(new EventImpl(null, null, t1, null, null, null, null, null, null, null, null));
                        trace.appendEvent(new EventImpl(null, null, t2, null, null, null, null, null, null, null, null));
                        trace.appendEvent(new EventImpl(null, null, t1, null, null, null, null, null, null, null, null));
                }
                ret.addTrace(trace);
            }
        }
        return ret;
    }
}
