package it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.instance.DomElement;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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

    public static JsonObject extractParams(BpmnModelInstance mi){
        JsonObject ret = new JsonObject();
        for(Process process : mi.getModelElementsByType(Process.class)){
            Participant party = null;
            for(Participant participant : mi.getModelElementsByType(Participant.class)){
                if(participant.getProcess().equals(process)){
                    party = participant;
                    break;
                }
            }

            JsonObject proc = new JsonObject();
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
            proc.add("name", new JsonPrimitive(name));

            JsonObject activities = new JsonObject();
            for(Task t : mi.getModelElementsByType(Task.class)){
                activities.add(t.getId(), new JsonPrimitive(t.getName()));
            }
            proc.add("Activities", activities);
            JsonObject choices = new JsonObject();
            Set<Gateway> gateways = new HashSet<Gateway>();
            gateways.addAll(mi.getModelElementsByType(ExclusiveGateway.class));
            gateways.addAll(mi.getModelElementsByType(InclusiveGateway.class));
            for(Gateway xor: gateways){
                if(!ModelUtils.isSplit(xor)) {continue;}
                JsonArray outgoing = new JsonArray();
                for(SequenceFlow sf: xor.getOutgoing()){
                    outgoing.add(sf.getId());
                }
                if (outgoing.size()>0) choices.add(xor.getId(), outgoing);
            }
            if (choices.size()>0) proc.add("Choices", choices);

            if (proc.size()>0) ret.add(process.getId(),proc);
        }
        System.out.println(ret);
        return ret;
    }

    public static void main(String[] args){
        ScenariosParamsExtractor.extractParams(Bpmn.readModelFromFile(new File("caseStudiTesi.bpmn")));
    }
}
