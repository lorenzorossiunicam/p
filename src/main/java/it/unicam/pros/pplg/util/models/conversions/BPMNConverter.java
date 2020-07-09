package it.unicam.pros.pplg.util.models.conversions;

//import it.unicam.pros.guidedsimulator.util.mining.prom.framework.ContextsFactory;
//import it.unicam.pros.guidedsimulator.util.models.IO.BPMNIO;
//import org.camunda.bpm.model.bpmn.BpmnModelInstance;
//import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
//import org.processmining.framework.util.Pair;
//import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.semantics.petrinet.Marking;
//import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetConverter;
//import org.processmining.plugins.xpdl.importing.XpdlImportBpmn;
//
//import java.io.File;

public class BPMNConverter {

//
//    public static BpmnModelInstance toBPMNModelInstance(BPMNDiagram diagram) {
//        return BPMNIO.importXML(toXMLString(diagram));
//    }
//
//    public static String toXMLString(BPMNDiagram diagram){
//        BPMNExp.BpmnDefinitionsBuilder definitionsBuilder = new BPMNExp.BpmnDefinitionsBuilder(ContextsFactory.getPluginContext(), diagram);
//        BPMNExp exp = new BPMNExp(definitionsBuilder);
//        return exp.exportElements();
//    }
//
//    public static BPMNDiagram toBPMNDiagram(BpmnModelInstance mi) throws Exception {
//        BPMNIO.export(mi,"tmp.bpmn");
//        XpdlImportBpmn im = new XpdlImportBpmn();
//
//        im.importFile(ContextsFactory.getPluginContext(), new File("questo.bpmn"));
//        BPMNDiagram diagram = (BPMNDiagram) im.importFile(ContextsFactory.getPluginContext(), new File("tmp.bpmn"));
//        return diagram;
//    }
//
//    private static <T extends BpmnModelElementInstance> T createElement(BpmnModelInstance mi, BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
//        T element = mi.newInstance(elementClass);
//        element.setAttributeValue("id", id, true);
//        parentElement.addChildElement(element);
//        return element;
//    }
//
//    public static Pair<Petrinet, Marking> toPetrinet(BpmnModelInstance mi) throws Exception {
//        BPMN2PetriNetConverter conv = new BPMN2PetriNetConverter(toBPMNDiagram(mi));
//        return new Pair<Petrinet, Marking>(conv.getPetriNet(),conv.getMarking());
//    }

}
