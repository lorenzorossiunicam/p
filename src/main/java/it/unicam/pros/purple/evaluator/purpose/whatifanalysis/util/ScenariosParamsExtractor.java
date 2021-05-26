package it.unicam.pros.purple.evaluator.purpose.whatifanalysis.util;

import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;

import java.io.File;
import java.util.*;

public class ScenariosParamsExtractor {

//    public static JsonObject extractParams(BpmnModelInstance mi){
//        JsonObject ret = new JsonObject();
//        for(Process process : mi.getModelElementsByType(Process.class)){
//            JsonObject proc = new JsonObject();
//
//            String procName = "";
//            int min = 1, max = 1;
//            for (Participant p : mi.getModelElementsByType(Participant.class)) {
//                String pName = p.getAttributeValue("processRef");
//                if (pName.equals(process.getId())){
//                    procName = "Pool: "+p.getName() + " - ";
//
//                    if (p.getChildElementsByType(ParticipantMultiplicity.class).size() != 0) {
//                        ParticipantMultiplicity multiInstance = p.getChildElementsByType(ParticipantMultiplicity.class)
//                                .iterator().next();
//                        min = Integer.valueOf(multiInstance.getAttributeValue("minimum"));
//                        max = Integer.valueOf(multiInstance.getAttributeValue("maximum"));
//                    }
//                }
//
//            }
//
//            procName += "Process: "+process.getName();
//            proc.addProperty("min",min);
//            proc.addProperty("max",max);
//            JsonObject data = new JsonObject();
//            for(DataObjectReference d : process.getChildElementsByType(DataObjectReference.class)) {
//                DomElement dataFields = d.getExtensionElements().getElements().iterator().next().getDomElement();
//                for(DomElement field : dataFields.getChildElements()) {
//                    if(field.getAttribute("dataField").contains("=")) {
//                        String[] a = field.getAttribute("dataField").split("=");
//                        data.addProperty(a[0], a[1]);
//                    }
//                }
//            }
//            if(data.size()>0) proc.add("Data", data);
//            JsonObject choices = new JsonObject();
//            for(ExclusiveGateway xor: mi.getModelElementsByType(ExclusiveGateway.class)){
//                JsonObject conditions = new JsonObject();
//                for(SequenceFlow sf: xor.getOutgoing()){
//                    conditions.addProperty(sf.getId(), ModelUtils.getCondition(sf));
//                }
//                if (conditions.size()>0) choices.add(xor.getId(), conditions);
//            }
//            if (choices.size()>0) proc.add("Choices", choices);
//
//            if (proc.size()>0) ret.add(procName,proc);
//        }
//        System.out.println(ret);
//        return ret;
//    }

    public static Map<String,Scenario> extractParams(BpmnModelInstance mi){
        Map<String,Scenario> ret = new HashMap<String,Scenario>();

        for(Process process : mi.getModelElementsByType(Process.class)){
            Participant party = null;
            for(Participant participant : mi.getModelElementsByType(Participant.class)){
                if(participant.getProcess().equals(process)){
                    party = participant;
                    break;
                }
            }

            String name = "";
            if(party != null && party.getName()!= null){
                name = party.getName();
            }else{
                if(process.getName()!=null){
                    name = process.getName();
                }else{
                    name = process.getId();
                }
            }

            Collection<Task> tasks = mi.getModelElementsByType(Task.class);
            Map<String, String> activities = new HashMap<String, String>(tasks.size());
            for(Task t : tasks){
                activities.put(t.getId(), t.getName());
            }

            Set<Gateway> gateways = new HashSet<Gateway>();
            gateways.addAll(mi.getModelElementsByType(ExclusiveGateway.class));
            gateways.addAll(mi.getModelElementsByType(InclusiveGateway.class));
            Map<String, List<String>> choices = new HashMap<String, List<String>>(gateways.size());


            for(Gateway xor: gateways) {
                if (!ModelUtils.isSplit(xor)) {
                    continue;
                }
                Collection<SequenceFlow> sFlows = xor.getOutgoing();
                List<String> outgoing = new ArrayList<String>();
                for (SequenceFlow sf : sFlows) {
                    outgoing.add(sf.getId());
                }
                choices.put(xor.getId(), outgoing);
            }
            ret.put(process.getId(), new Scenario(name, activities, choices));
        }
        return ret;
    }

    public static void main(String[] args){
        ScenariosParamsExtractor.extractParams(Bpmn.readModelFromFile(new File("caseStudiTesi.bpmn")));
    }
}
