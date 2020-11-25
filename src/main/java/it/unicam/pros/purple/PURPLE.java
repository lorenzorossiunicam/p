package it.unicam.pros.purple;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.BPMNRediscoverability;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.PnmlRediscoverability;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.Rediscoverability;
import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.BPMNWhatIfAnalysis;
import it.unicam.pros.purple.gui.util.logger.SimLogAppender;
import it.unicam.pros.purple.semanticengine.SemanticEngine;
import it.unicam.pros.purple.semanticengine.bpmn.NodaEngine;
import it.unicam.pros.purple.semanticengine.ptnet.PTNetUtil;
import it.unicam.pros.purple.semanticengine.ptnet.PnmlEngine;
import it.unicam.pros.purple.simulation.Simulator;
import it.unicam.pros.purple.simulation.SimulatorImpl;
import it.unicam.pros.purple.util.eventlogs.EventLog;

import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * This class is the entry point of PURPLE. It provides the PURPLE functionalities for CLI applications.
 *
 *
 * @author Lorenzo Rossi
 */
public class PURPLE {

    private static boolean interrupt = false;

    /**
     *
     * Set the interrupt for the current activity.
     *
     * @param val the interruption value. If true the task will be interrupted.
     */
    public static void setInterrupt(boolean val){
        interrupt = val;
    }

    /**
     *
     * Shows if the current task has been interrupted.
     *
     * @return the interruption value.
     */
    public static boolean isInterrupted(){return interrupt;}

    /**
     *
     * This method generates an event log for the rediscoverability purpose on the basis of an input BPMN model, and of a mining algorithm.
     *
     * @param model the model to simulate.
     * @param algo the reference mining algorithm.
     * @param tau the coverage of the simulation.
     * @return a stream containing the log.
     */
    public static ByteArrayOutputStream bpmnRediscoverability(InputStream model, Rediscoverability.RediscoverabilityAlgo algo, double tau){
        //Parse a BPMN model from file
        BpmnModelInstance mi = Bpmn.readModelFromStream(model);
        if(mi.getModelElementsByType(Process.class).size()>1) {
            SimLogAppender.append(PURPLE.class, SimLogAppender.Level.SEVERE, "Invalid Model");
            throw new IllegalArgumentException("Only process models");
        }

        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Model Uploaded");
        String process = "";
        for (Process p : mi.getModelElementsByType(Process.class)){
           process = p.getId();
        }
        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        Evaluator evaluator = new BPMNRediscoverability(mi, process, algo);

        EventLog log = generateLogsStream(simulator,evaluator,tau);

        return LogIO.getAsStream(log);
    }

    /**
     *
     * This method generates an event log for the rediscoverability purpose on the basis of an input BPMN model, and of a mining algorithm.
     *
     * @param model the model to simulate.
     * @param algo the reference mining algorithm.
     * @param tau the coverage of the simulation.
     * @return a stream containing the log.
     */
    public static ByteArrayOutputStream pnmlRediscoverability(InputStream model, Rediscoverability.RediscoverabilityAlgo algo, double tau){
        //Parse a PNML model from file
        Petrinet mi = null;
        try {
            mi = PTNetUtil.importFile(model);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Model Uploaded");
        String process = "";

        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new PnmlEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        Evaluator evaluator = new PnmlRediscoverability(mi, algo);

        EventLog log = generateLogsStream(simulator,evaluator,tau);

        return LogIO.getAsStream(log);
    }

    /**
     *
     * This method generates an event log for the what-if analysis purpose on the basis of an input BPMN model and of a scenario.
     *
     * @param mi the BPMN model.
     * @param sfProbability
     * @param actCosts
     * @param tau
     * @param maxTraces
     * @return
     */
    public static EventLog whatif(BpmnModelInstance mi, Map<String, Double> sfProbability, Map<String, Double> actCosts, double tau, double maxTraces){

        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        Evaluator evaluator = new BPMNWhatIfAnalysis(mi, sfProbability, actCosts, maxTraces);

        return generateLogsStream(simulator,evaluator,tau);
    }


    public static void main(String[] args) throws IOException {

        ByteArrayOutputStream b = pnmlRediscoverability(new FileInputStream(new File("C:\\Users\\lo_re\\Desktop\\Rediscoverability\\p1.pnml")), Rediscoverability.RediscoverabilityAlgo.ALPHA, 1.0);
        System.out.println(b.toString());
//        System.exit(0);

    }


    private static EventLog generateLogsStream(Simulator simulator, Evaluator evaluator, Double tau){
        EventLog log = simulator.simulate(null);
        Delta d = evaluator.evaluate(log,tau);
        while(!d.isEmpty() && !interrupt){
            log = simulator.simulate(d);
            d = evaluator.evaluate(log,tau);
        }
        return log;
    }

}
