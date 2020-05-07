package it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis.metrics;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.eventlogs.EventLog;
import it.unicam.pros.guidedsimulator.util.eventlogs.EventLogImpl;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.Trace;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.*;
import org.camunda.bpm.model.bpmn.instance.*;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ModelProbabilities {

    public static void main(String[] args) throws IOException {
        HashMap<String, Double> pr = new HashMap<String, Double>();
        pr.put("SequenceFlow_010ecf9", 50.0);
        pr.put("SequenceFlow_0og37pt",50.0);

        pr.put("SequenceFlow_11qbjm0", 50.0);
        pr.put("SequenceFlow_01au5mz",50.0);

        pr.put("SequenceFlow_0adsrmo", 50.0);
        pr.put("SequenceFlow_1f825iq",50.0);
        System.out.println(calculateProbabilities(Bpmn.readModelFromFile(new File("p0.bpmn")), pr));
//        rediscoverability("testXor.bpmn", "ltestXor.xes",Rediscoverability.RediscoverabilityAlgo.ALPHA, 1.0);
//        System.exit(0);
    }

    public static Map<String, Double> calculateProbabilities(BpmnModelInstance mi, Map<String, Double> xorAndOrProbabilities) {
//        Map<String, Double> tmp = new HashMap<String, Double>();
//        for(StartEvent sE : mi.getModelElementsByType(StartEvent.class)){
//            dfCalc(tmp, sE, 1, xorAndOrProbabilities, new HashSet<FlowNode>());
//        }
//        Map<String, Double> ret = new HashMap<String, Double>();
//        for (Task t : mi.getModelElementsByType(Task.class)){
//            ret.put(t.getName(), tmp.get(t.getId()));
//        }
//        return ret;
        Map<String, Double> tp = new HashMap<String, Double>();
        Map<String, Double> sfp = new HashMap<String, Double>();
        for(StartEvent sE : mi.getModelElementsByType(StartEvent.class)){
            dfsCalc(tp, sfp, sE, 1, xorAndOrProbabilities, new HashSet<FlowNode>());
        }
        return tp;
    }

    private static void dfsCalc(Map<String, Double> taskProb, Map<String, Double> sfProb, FlowNode currNode, double prob,
                               Map<String, Double> xorAndOrProbabilities, HashSet<FlowNode> done) {
        done.add(currNode);
        if (currNode instanceof TaskImpl)
            taskProb.put(currNode.getName(), prob);
        for (SequenceFlow out : currNode.getOutgoing()) {
            if((currNode instanceof ExclusiveGatewayImpl || currNode instanceof InclusiveGatewayImpl || currNode instanceof EventBasedGatewayImpl) && ModelUtils.isSplit(currNode) ){
                double newProb = prob*xorAndOrProbabilities.get(out.getId())/100;
                sfProb.put(out.getId(), newProb);
            }else {
                sfProb.put(out.getId(), prob);
            }
        }
        for (SequenceFlow out : currNode.getOutgoing()) {
            FlowNode next = out.getTarget();
            if(currNode instanceof  ParallelGatewayImpl || currNode instanceof StartEventImpl || currNode instanceof TaskImpl ||
                    currNode instanceof IntermediateCatchEventImpl || currNode instanceof IntermediateThrowEventImpl){
                dfsCalc(taskProb, sfProb, next, prob, xorAndOrProbabilities, done);
            }else if(currNode instanceof ExclusiveGatewayImpl || currNode instanceof InclusiveGatewayImpl || currNode instanceof EventBasedGatewayImpl) {
                if (ModelUtils.isSplit(currNode)) {
                    dfsCalc(taskProb, sfProb, next, prob*xorAndOrProbabilities.get(out.getId())/100, xorAndOrProbabilities, done);
                } else {
                    if(done.containsAll(currNode.getPreviousNodes().list())){
                        double newProb = 0;
                        for (SequenceFlow n : currNode.getIncoming()){
                            newProb += sfProb.get(n.getId());
                        }
                        dfsCalc(taskProb, sfProb, next, newProb, xorAndOrProbabilities, done);
                    }
                }
            }

        }
    }

//    private static void dfCalc(Map<String, Double> ret, FlowNode node, double prob,
//                               Map<String, Double> xorAndOrProbabilities, HashSet<FlowNode> done) { //It may not works with not structured models
//        ret.put(node.getId(), prob);
//        done.add(node);
//        for (SequenceFlow out : node.getOutgoing()) {
//            FlowNode next = out.getTarget();
//            if(node instanceof  ParallelGatewayImpl || node instanceof StartEventImpl || node instanceof TaskImpl ||
//                    node instanceof IntermediateCatchEventImpl || node instanceof IntermediateThrowEventImpl){
//                dfCalc(ret, next, prob, xorAndOrProbabilities, done);
//            }else if(node instanceof ExclusiveGatewayImpl || node instanceof InclusiveGatewayImpl || node instanceof EventBasedGatewayImpl) {
//                if (ModelUtils.isSplit(node)) {
//                    dfCalc(ret, next, prob*xorAndOrProbabilities.get(out.getId())/100, xorAndOrProbabilities, done);
//                } else {
//                    List<FlowNode> previous = node.getPreviousNodes().list();
//                    if(done.containsAll(previous)){
//                        double newProb = 0;
//                        for (FlowNode n : previous){
//                            newProb += ret.get(n.getId());
//                        }
//                        dfCalc(ret, next, newProb, xorAndOrProbabilities, done);
//                    }
//                }
//            }
//
//        }
//    }

    public static EventLog compareProbabilities(Map<String, Double> logProbs, Map<String, Double> requiredProbs, Double tau){
        EventLogImpl ret = new EventLogImpl(null, null);

        System.out.println("logProbs "+logProbs);
        System.out.println("reqProbs "+requiredProbs);

        for(String e : requiredProbs.keySet()){
            if (logProbs.get(e) == null || logProbs.get(e)  < requiredProbs.get(e) - tau/100.0 ){
                Trace t = new TraceImpl(null);
                t.appendEvent(new EventImpl(null, null, e, null, null, null, null, null, null, null, null));
                ret.addTrace(t);
            }

        }
        return ret;
    }

    public static Map<String, Double> calculateProbabilities(EventLog log){
        int nTraces = log.size();
        Map<String, Double> ret = new HashMap<String, Double>();
        for (Trace trace : log.getTraces()){
            for (Event event : trace.getTrace()){
                String evName = event.getEventName();
                if(ret.get(evName) == null){
                    ret.put(evName, 0.0);
                }
                ret.put(evName, ret.get(evName)+1.0);
            }
        }
        for(String e : ret.keySet()){
            ret.put(e, ret.get(e)/nTraces);
        }
        return ret;
    }

}
