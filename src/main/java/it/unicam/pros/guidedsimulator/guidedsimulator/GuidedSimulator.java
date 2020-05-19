package it.unicam.pros.guidedsimulator.guidedsimulator;

import it.unicam.pros.guidedsimulator.evaluator.Delta;
import it.unicam.pros.guidedsimulator.evaluator.Evaluator;
import it.unicam.pros.guidedsimulator.evaluator.purpose.rediscoverability.BPMNRediscoverability;
import it.unicam.pros.guidedsimulator.evaluator.purpose.rediscoverability.Rediscoverability;
import it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis.BPMNWhatIfAnalysis;
import it.unicam.pros.guidedsimulator.gui.util.logger.SimLogAppender;
import it.unicam.pros.guidedsimulator.semanticengine.SemanticEngine;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.NodaEngine;
import it.unicam.pros.guidedsimulator.simulation.Simulator;
import it.unicam.pros.guidedsimulator.simulation.SimulatorImpl;
import it.unicam.pros.guidedsimulator.util.eventlogs.EventLog;

import it.unicam.pros.guidedsimulator.util.eventlogs.utils.LogIO;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GuidedSimulator {

    private static boolean interrupt = false;
    public static void setInterrupt(boolean val){
        interrupt = val;
    }
    public static boolean getInterrupt(){return interrupt;}

    private static EventLog generateLogsStream(Simulator simulator, Evaluator evaluator, Double tau){
        EventLog log = simulator.simulate(null);
        Delta d = evaluator.evaluate(log,tau);
        while(!d.isEmpty() && !interrupt){
            log = simulator.simulate(d);
            d = evaluator.evaluate(log,tau);
        }
        return log;
    }

    public static ByteArrayOutputStream rediscoverability(InputStream model, Rediscoverability.RediscoverabilityAlgo algo, double tau){
        //Parse a BPMN model from file
        BpmnModelInstance mi = Bpmn.readModelFromStream(model);
        if(mi.getModelElementsByType(Process.class).size()>1) {
            SimLogAppender.append(GuidedSimulator.class, SimLogAppender.Level.SEVERE, "Invalid Model");
            throw new IllegalArgumentException("Only process models");
        }

        SimLogAppender.append(GuidedSimulator.class, SimLogAppender.Level.INFO, "Model Uploaded");
        String process = "";
        for (Process p : mi.getModelElementsByType(Process.class)){
           process = p.getId();
        }
        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(GuidedSimulator.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        Evaluator evaluator = new BPMNRediscoverability(mi, process, algo);

        EventLog log = generateLogsStream(simulator,evaluator,tau);

        return LogIO.getAsStream(log);
    }

    public static EventLog whatif(BpmnModelInstance mi, Map<String, Double> sfProbability, Map<String, Double> actCosts, double tau, double maxTraces){

        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(GuidedSimulator.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        Evaluator evaluator = new BPMNWhatIfAnalysis(mi, sfProbability, actCosts, maxTraces);

        return generateLogsStream(simulator,evaluator,tau);
    }


    public static void main(String[] args) throws IOException {
        HashMap<String, Double> pr = new HashMap<String, Double>();
        pr.put("e9", 50.0);
        pr.put("se3",50.0);
          //  whatif(Bpmn.readModelFromFile(new File("p0.bpmn")), pr, 95);
//        rediscoverability("testXor.bpmn", "ltestXor.xes",Rediscoverability.RediscoverabilityAlgo.ALPHA, 1.0);
//        System.exit(0);
    }

}
