package it.unicam.pros.purple.evaluator.purpose.whatifanalysis.metrics;

import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.*;
import org.camunda.bpm.model.bpmn.instance.*;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.processtree.Block;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class ModelProbabilities {

    public static void main(String[] args) throws Exception {

        BpmnModelInstance mi = Bpmn.readModelFromFile(new File("m.bpmn"));

        HashMap<String, Double> pr = new HashMap<String, Double>();
        for (ExclusiveGateway g : mi.getModelElementsByType(ExclusiveGateway.class)){
            if (ModelUtils.isSplit(g)){
                int out = g.getOutgoing().size();
                for (SequenceFlow sf : g.getOutgoing()){
                    pr.put(sf.getId(), 100.0/out);
                }
            }
        }
//        Set<Gateway> gs = new HashSet<>();
//        gs.addAll(mi.getModelElementsByType(InclusiveGateway.class));
//        gs.addAll(mi.getModelElementsByType(ExclusiveGateway.class));
//
//        for(Gateway g : gs){
//            if (ModelUtils.isSplit(g)){
//                Collection<SequenceFlow> out = g.getOutgoing();
//int i = 0;
//                for(SequenceFlow sg : out){
//                    System.out.println(sg.getId()+" : ....");
//                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//                    Double prob = Double.valueOf(in.readLine());
//                    pr.put(sg.getId(), prob);
//
//                }
//            }
//        }


        Map<String, Double> original = calculateProbabilities(mi, pr);
//        XLog logPPLG = LogIO.parseXES("C:\\Users\\lo_re\\Desktop\\NEWIF\\p33pplg.xes");
//        Map<String, Double> ppgl = calculateProbabilities(logPPLG);
//        XLog logBIMP = LogIO.parseXES("C:\\Users\\lo_re\\Desktop\\NEWIF\\p33bimp.xes");
//        Map<String, Double> bimp = calculateProbabilities(logBIMP);
//        for(String s : bimp.keySet()){
//            bimp.put(s, bimp.get(s)/2);
//        }
         System.out.println(original);
//        System.out.println(ppgl);
//        System.out.println(bimp);
//
//        double  distPPLG = 0, distBIMP = 0, sum= 0.0;
//
//        for(String name : original.keySet()){
//            double or = original.get(name);
//            sum += or;
//            Double p = ppgl.get(name);
//            if(p == null) p = 0.0;
//            Double b = bimp.get(name);
//            if(b == null) b = 0.0;
//            distPPLG += Math.abs(p - or);
//            distBIMP += Math.abs(b - or);
//        }
//        System.out.println("BIMP "+(distBIMP)/original.keySet().size());
//        System.out.println("PPLG "+( (distPPLG)/original.keySet().size()));
//        rediscoverability("testXor.bpmn", "ltestXor.xes",Rediscoverability.RediscoverabilityAlgo.ALPHA, 1.0);
//        System.exit(0);
    }

    public static Map<String, Double> calculateProbabilities(BpmnModelInstance mi, Map<String, Double> xorAndOrProbabilities) {
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
            for(SequenceFlow prev : currNode.getIncoming()){
                if ((prev.getSource()  instanceof ExclusiveGatewayImpl || prev.getSource() instanceof InclusiveGatewayImpl || prev.getSource() instanceof EventBasedGatewayImpl) && ModelUtils.isSplit(prev.getSource())){
                    taskProb.put(currNode.getName(), prob);
                }
            }

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



    public static EventLog compareProbabilities(Map<String, Double> logProbs, Map<String, Double> requiredProbs, Double tau){
        EventLogImpl ret = new EventLogImpl(null, null);

        System.out.println("logProbs "+logProbs);
        System.out.println("reqProbs "+requiredProbs);

        for(String e : requiredProbs.keySet()){
            if (logProbs.get(e) == null || logProbs.get(e)  < requiredProbs.get(e) - tau/100.0 ){
                Trace t = new TraceImpl(null);
                t.appendEvent(new it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl(null, null, e, null, null, null, null, null, null, null, null));
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

    public static Map<String, Double> calculateProbabilities(XLog log){
        int nTraces = log.size();
        Map<String, Double> ret = new HashMap<String, Double>();
        for (XTrace trace : log){
            for (XEvent event : trace){
                String evName = event.getAttributes().get("concept:name").toString();
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
