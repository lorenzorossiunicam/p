package it.unicam.pros.purple.util.eventlogs.utils;


import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.AlphaRelations;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.FootprintRelations;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.File;
import java.util.Map;

public class LogCompleteness {

    public static double measureCompleteness(XLog log, Map<String, Map<String, FootprintRelations>> relations){
        if (log == null || log.isEmpty()) return 0;
        int cardinality = 0;
        double score = 0;
        for (String act1 : relations.keySet()){
            for (String act2 : relations.get(act1).keySet()){
                cardinality++;
                score += checkRel(act1,act2,relations.get(act1).get(act2),log);
            }
        }
        return score/cardinality;
    }

    private static double checkRel(String act1, String act2, FootprintRelations rel, XLog log) {

        boolean seq = false, pred = false;
        for(XTrace t : log){
            for (int i = 0; i<t.size()-1; i++){
                String a1 = t.get(i).getAttributes().get("concept:name").toString(), a2 = t.get(i+1).getAttributes().get("concept:name").toString();
                if (a1.equals(act1)){
                    if(a2.equals(act2)){
                        seq = true;
                    }
                } else if (a1.equals(act2)){
                    if(a2.equals(act1)){
                        pred = true;
                    }
                }
            }
        }
        if(rel.equals(FootprintRelations.SEQUENCE) && seq && !pred) return 1;
        if(rel.equals(FootprintRelations.PARALLEL) && seq && pred) return 1;
        if((rel.equals(FootprintRelations.CHOICE)||rel.equals(FootprintRelations.NONE)) && !seq && !pred) return 1;
        System.out.println(act1+" - "+act2+" - "+rel);
        return 0;
    }

    public static void main(String[] args){
        XLog log1 = null, log2 = null;
        try {
            log1 = LogIO.parseXES("log.xes");
            log2 = LogIO.parseXES("log.xes");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(log1.size()+" - "+log2.size());
        BpmnModelInstance mi = Bpmn.readModelFromFile(new File("p4.bpmn"));
        String process = mi.getModelElementsByType(Process.class).iterator().next().getId();
        Map<String, Map<String, FootprintRelations>> relations = AlphaRelations.getAlphaRelations((Process) mi.getModelElementById(process));

        System.out.println(measureCompleteness(log1,relations));
        System.out.println(measureCompleteness(log2,relations));
    }
}
