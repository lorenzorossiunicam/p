package it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics;

import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.metrics.ModelProbabilities;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

public class SplitRelations {

    private static BpmnModelInstance model;
    private static boolean isLTD = false;
    private Set<String[]> parallels;
    private Set<String[]> short_loops;
    private Map<String,Map<String,FootprintRelations>> footprintMatrix;

    public Set<String[]> getParallels() {
        return parallels;
    }
    public Set<String[]> getShort_loops() {
        return short_loops;
    }
    public Map<String,Map<String,FootprintRelations>> getFootprintMatrix() {return footprintMatrix;}

    private static Map<String[], Queue<Double>> ratioseries = new HashMap<>();

    public SplitRelations(BpmnModelInstance mi){
        this.model = mi;
        this.footprintMatrix = new HashMap<>();
        for(Task t : model.getModelElementsByType(Task.class)){
            footprintMatrix.put(t.getName(),new HashMap<>());
        }
        populateMatrix();
    }

    private void populateMatrix() {
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
        findShortLoops();
    }

    private Map<String, Double> findExclusives() {
        HashMap<String, Double> pr = new HashMap<String, Double>();
        for (ExclusiveGateway g : model.getModelElementsByType(ExclusiveGateway.class)){
            if (ModelUtils.isSplit(g)){
                int out = g.getOutgoing().size();
                for (SequenceFlow sf : g.getOutgoing()){
                    pr.put(sf.getId(), 100.0/out);
                }
            }
        }
        return ModelProbabilities.calculateProbabilities(model, pr);
    }

    private void findShortLoops() {
        SimpleDirectedWeightedGraph<FlowNode, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<FlowNode, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (SequenceFlow e : model.getModelElementsByType(SequenceFlow.class)) {
            graph.addVertex(e.getSource());
            graph.addVertex(e.getTarget());
            graph.addEdge(e.getSource(), e.getTarget());
        }
        SzwarcfiterLauerSimpleCycles<FlowNode, DefaultWeightedEdge> f = new SzwarcfiterLauerSimpleCycles<FlowNode, DefaultWeightedEdge>(
                graph);
        List<List<FlowNode>> cycl = f.findSimpleCycles();
        for(List<FlowNode> c : cycl){
            List<Task> tasksInCycle = new ArrayList<>();
            for (FlowNode n : c){
                if (n instanceof Task){
                    tasksInCycle.add((Task) n);
                }
            }
            if (tasksInCycle.size()==2){
                footprintMatrix.get(tasksInCycle.get(0).getName()).put(tasksInCycle.get(1).getName(), FootprintRelations.LENGTH_2_LOOP);
                footprintMatrix.get(tasksInCycle.get(1).getName()).put(tasksInCycle.get(0).getName(), FootprintRelations.LENGTH_2_LOOP);
            }
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



    public static EventLog compareSplitRelations(SplitRelations relations, EventLog log, Double tau) {
        Map<String,Map<String,FootprintRelations>> footprint = relations.getFootprintMatrix();
        System.out.println(footprint);
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
        System.out.println(ret);
        return ret;
//        if (log.getTraces().isEmpty()) return allTaskDelta();
//
//        Set<String[]> parallels = relations.getParallels();
//        Set<String[]> short_loops = relations.getShort_loops();
//        Map<String, Double> exclusives = relations.getExclusives();
//
//        Map<String, Double> exclInLog = new HashMap<>();
//        Map<String[], Integer[]> parInLog = new HashMap<>();
//
//        for(String act : exclusives.keySet()){
//            exclInLog.put(act, 0.0);
//        }
//        for(String[] par : parallels){
//            parInLog.put(par, new Integer[]{0,0});
//        }
//
//        int nTraces = log.size();
//        for (Trace t : log.getTraces()){
//            for (int i = 0; i<t.getTrace().size(); i++){
//                Event e = t.getTrace().get(i);
//                if (exclInLog.containsKey(e.getEventName())){
//                    exclInLog.put(e.getEventName(), exclInLog.get(e.getEventName())+1);
//                }
//                if (i == t.getTrace().size()-1) continue;
//                Event nextEv = t.getTrace().get(i+1);
//                for (String[] par : parallels){
//                    if (par[0].equals(e.getEventName()) && par[1].equals(nextEv.getEventName())){
//                        parInLog.put(par, new Integer[]{parInLog.get(par)[0]+1, parInLog.get(par)[1]});
//                    } else if (par[0].equals(nextEv.getEventName()) && par[1].equals(e.getEventName())){
//                        parInLog.put(par, new Integer[]{parInLog.get(par)[0], parInLog.get(par)[1]+1});
//                    }
//                }
//                if (i == t.getTrace().size()-2) continue;
//                Event nextNextEv = t.getTrace().get(i+2);
//                for(String[] loo : short_loops){
//                    if(loo[0].equals(e.getEventName()) && loo[1].equals(nextEv.getEventName()) && loo[0].equals(nextNextEv.getEventName())){
//                        short_loops.remove(loo);
//                    }
//                }
//
//            }
//        }
//
//        EventLog ret = new EventLogImpl("", null);
//        for(String s: exclusives.keySet()){
//            if (exclInLog.get(s)/nTraces <= exclusives.get(s) - tau){
//                Trace t = new TraceImpl(null);
//                t.appendEvent(new EventImpl(null, null, s, null, null, null, null, null, null, null, null));
//                ret.addTrace(t);
//            }
//        }
//        for(String[] couple : parInLog.keySet()){
//            double ratio = (Math.abs(parInLog.get(couple)[0] - parInLog.get(couple)[1])*1.0)/(parInLog.get(couple)[0] + parInLog.get(couple)[1] );
//
//            ratioseries.get(couple).add(ratio);
//            if (ratioseries.get(couple).size()>10)
//                ratioseries.get(couple).poll();
//            double varianza = getVarianza(ratioseries.get(couple));
//            if ((ratio < 1 && ratio > 0) || varianza <0.1) continue;
//
//            System.out.println(ratio+"  "+varianza+" "+couple[0]+"  "+couple[1]);
//            if (parInLog.get(couple)[0] + parInLog.get(couple)[1] == 0){
//                Trace t = new TraceImpl(null);
//                t.appendEvent(new EventImpl(null, null, couple[0], null, null, null, null, null, null, null, null));
//                t.appendEvent(new EventImpl(null, null, couple[1], null, null, null, null, null, null, null, null));
//                ret.addTrace(t);
//                t = new TraceImpl(null);
//                t.appendEvent(new EventImpl(null, null, couple[1], null, null, null, null, null, null, null, null));
//                t.appendEvent(new EventImpl(null, null, couple[0], null, null, null, null, null, null, null, null));
//                ret.addTrace(t);
//                continue;
//            }
//
//
//
//
//
//            if (parInLog.get(couple)[0] < parInLog.get(couple)[1]){
//                Trace t = new TraceImpl(null);
//                t.appendEvent(new EventImpl(null, null, couple[0], null, null, null, null, null, null, null, null));
//                t.appendEvent(new EventImpl(null, null, couple[1], null, null, null, null, null, null, null, null));
//                ret.addTrace(t);
//            } else if (parInLog.get(couple)[0] > parInLog.get(couple)[1]){
//                Trace t = new TraceImpl(null);
//                t.appendEvent(new EventImpl(null, null, couple[1], null, null, null, null, null, null, null, null));
//                t.appendEvent(new EventImpl(null, null, couple[0], null, null, null, null, null, null, null, null));
//                ret.addTrace(t);
//            }
//        }
//
//        for(String[] loop : short_loops){
//            Trace t = new TraceImpl(null);
//            t.appendEvent(new EventImpl(null, null, loop[0], null, null, null, null, null, null, null, null));
//            t.appendEvent(new EventImpl(null, null, loop[1], null, null, null, null, null, null, null, null));
//            t.appendEvent(new EventImpl(null, null, loop[0], null, null, null, null, null, null, null, null));
//            ret.addTrace(t);
//        }
//        System.out.println(ret);
//        return ret;

    }

    private static double getVarianza(Queue<Double> doubles) {
        int n = doubles.size();
        if (n < 10) return 1;
        double sum = 0;
        boolean nan = true;
        for (Double d : doubles){
            if (!d.isNaN()) nan = false;
            sum += d;
        }
        if (nan && n>=10) return 0;
        double mean = sum/n;
        double shiftSum = 0;
        for (Double d : doubles){
            shiftSum += Math.pow((d - mean), 2);
        }
        return shiftSum/n;
    }

    private static EventLog allTaskDelta() {
        EventLog ret = new EventLogImpl("", null);
        Trace t = null;
        for (Task act : model.getModelElementsByType(Task.class)){
            t = new TraceImpl(null);
            t.appendEvent(new EventImpl(null, null, act.getName(), null, null, null, null, null, null, null, null));
            ret.addTrace(t);
        }
        return ret;
    }
}
