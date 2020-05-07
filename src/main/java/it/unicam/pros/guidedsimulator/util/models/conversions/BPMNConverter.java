package it.unicam.pros.guidedsimulator.util.models.conversions;

import it.unicam.pros.guidedsimulator.util.mining.prom.discoverability.Alphas;
import it.unicam.pros.guidedsimulator.util.mining.prom.framework.ContextsFactory;
import it.unicam.pros.guidedsimulator.util.models.IO.BPMNIO;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.DefaultGraphCell;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.*;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.elements.ProMGraphPort;
import org.processmining.models.jgraph.views.JGraphPortView;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.*;
import org.processmining.plugins.bpmn.diagram.*;
import org.processmining.plugins.bpmn.plugins.BpmnImportPlugin;
import org.processmining.plugins.converters.BPMNUtils;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetConverter;
import org.processmining.plugins.xpdl.importing.XpdlImportBpmn;
import org.rapidprom.ioobjects.BPMNIOObject;
import org.xmlpull.v1.XmlPullParser;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class BPMNConverter {


    public static BpmnModelInstance toBPMNModelInstance(BPMNDiagram diagram) {
        return BPMNIO.importXML(toXMLString(diagram));
    }

    public static String toXMLString(BPMNDiagram diagram){
        BPMNExp.BpmnDefinitionsBuilder definitionsBuilder = new BPMNExp.BpmnDefinitionsBuilder(ContextsFactory.getPluginContext(), diagram);
        BPMNExp exp = new BPMNExp(definitionsBuilder);
        return exp.exportElements();
    }

    public static BPMNDiagram toBPMNDiagram(BpmnModelInstance mi) throws Exception {
        BPMNIO.export(mi,"tmp.bpmn");
        XpdlImportBpmn im = new XpdlImportBpmn();

        im.importFile(ContextsFactory.getPluginContext(), new File("questo.bpmn"));
        BPMNDiagram diagram = (BPMNDiagram) im.importFile(ContextsFactory.getPluginContext(), new File("tmp.bpmn"));
        return diagram;
    }

    private static <T extends BpmnModelElementInstance> T createElement(BpmnModelInstance mi, BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = mi.newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    public static Pair<Petrinet, Marking> toPetrinet(BpmnModelInstance mi) throws Exception {
        BPMN2PetriNetConverter conv = new BPMN2PetriNetConverter(toBPMNDiagram(mi));
        return new Pair<Petrinet, Marking>(conv.getPetriNet(),conv.getMarking());
    }

}
