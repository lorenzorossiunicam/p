package it.unicam.pros.purple;

import it.unicam.pros.purple.semanticengine.SemanticEngine;
import it.unicam.pros.purple.semanticengine.bpmn.NodaEngine;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.simulation.SimulatorImpl;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class TestLogWithMsg {

    public static void main(String[] args) throws Exception {


        BpmnModelInstance mi = Bpmn.readModelFromFile(new File("running_example.bpmn"));


        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        ModelUtils.setModel(mi);
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        EventLog log = new EventLogImpl("log", null);
        for(int i = 0; i < 10; i++){
            for (Map<String, Trace> p : simulator.randomSim().values()) {
                log.addTraces(p.values());
            }
        }
        System.out.println(log);
        LogIO.saveCollaborativeXES(log,"colLog.xes");
    }
}
