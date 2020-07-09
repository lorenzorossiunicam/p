package it.unicam.pros.pplg.util.models.IO;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BPMNIO {

    public static void export(BPMNDiagram diagram, String filename) throws IOException {
        PluginContext pluginContext = new CLIPluginContext(new CLIContext(), null);
        BpmnExportPlugin exporter = new BpmnExportPlugin();
        exporter.export(pluginContext, diagram, new File(filename));
    }

    public static void export(BpmnModelInstance mi, String filename){
        Bpmn.writeModelToFile(new File(filename), mi);
    }

    public static BpmnModelInstance importXML(String xml){
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        return Bpmn.readModelFromStream(stream);
    }
}
