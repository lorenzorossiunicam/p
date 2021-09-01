package it.unicam.pros.purple;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.evaluator.purpose.conformance.AlignCostConformance;
import it.unicam.pros.purple.evaluator.purpose.conformance.CustomNoise;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.*;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.AlphaRelations;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.FootprintMatrix;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.FootprintRelations;
import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.BPMNWhatIfAnalysis;
import it.unicam.pros.purple.gui.util.Constants;
import it.unicam.pros.purple.gui.util.logger.SimLogAppender;
import it.unicam.pros.purple.semanticengine.SemanticEngine;
import it.unicam.pros.purple.semanticengine.bpmn.NodaEngine;
import it.unicam.pros.purple.semanticengine.ptnet.PTNetUtil;
import it.unicam.pros.purple.semanticengine.ptnet.PnmlEngine;
import it.unicam.pros.purple.simulation.Simulator;
import it.unicam.pros.purple.simulation.SimulatorImpl;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.eventlogs.EventLog;

import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import java.io.*;
import java.lang.invoke.ConstantCallSite;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
     * @param tau the coverage of the simulation.
     * @return a stream containing the log.
     */
    public static ByteArrayOutputStream bpmnRediscoverability(InputStream model,  double tau) throws Exception {
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
        Evaluator evaluator = new BPMNRediscoverability(mi, process);

        EventLog log = generateLogsStream(simulator,evaluator,tau);

        return LogIO.getAsStream(log);
    }

    /**
     *
     * This method generates an event log for the rediscoverability purpose on the basis of an input BPMN model, and of a mining algorithm.
     *
     * @param model the model to simulate.
     * @param tau the coverage of the simulation.
     * @return a stream containing the log.
     */
    public static ByteArrayOutputStream pnmlRediscoverability(InputStream model,   double tau) throws Exception {
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
        Evaluator evaluator = new PnmlRediscoverability(mi);

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
    public static EventLog whatif(BpmnModelInstance mi, Map<String, Double> sfProbability, Map<String, Double> actCosts, double tau, double maxTraces) throws Exception {
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

    public static EventLog pnmlAlignCostConformance(InputStream model, double alignCost, int nTraces, double tau) throws Exception {
        //Parse a PNML model from file
        Petrinet mi = null;
        try {
            mi = PTNetUtil.importFile(model);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new PnmlEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        AlignCostConformance evaluator = new AlignCostConformance(alignCost,nTraces);

        EventLog log = simulator.simulate(null);
        Delta d = evaluator.evaluate(log,tau);
        while(!evaluator.done && !interrupt){
            log = simulator.simulate(d);
            d = evaluator.evaluate(log,tau);
        }
        log.addTraces(evaluator.getNoised());
        return log;
    }

    public static EventLog bpmnAlignCostConformance(InputStream model, double alignCost, int nTraces, double tau) throws Exception {
        //Create an engine from the given model

        BpmnModelInstance mi = Bpmn.readModelFromStream(model);
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        AlignCostConformance evaluator = new AlignCostConformance(alignCost,nTraces);

        EventLog log = simulator.simulate(null);
        Delta d = evaluator.evaluate(log,tau);
        while(!evaluator.done && !interrupt){
            log = simulator.simulate(d);
            d = evaluator.evaluate(log,tau);
        }
        log.addTraces(evaluator.getNoised());
        return log;
    }

    public static Couple<Set<Trace>, Map<Trace, int[]>> bpmnSimpleLog(BpmnModelInstance mi) throws Exception {
       return CustomTracesFrequency.generateSimpleLogAndLoops(new SimulatorImpl(new NodaEngine("",mi)),mi);
    }

    public static Couple<Set<Trace>, Map<Trace, int[]>> pnmlSimpleLog(Petrinet mi) throws Exception {
        return null;
    }

    public static EventLog bpmnCustomTraceFrequency(BpmnModelInstance mi, Map<Trace, Double> frequencies, Map<Trace, int[]> loopFrequencies, int minT, double tau){
        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        //Create the evaluator
        CustomTracesFrequency evaluator = new CustomTracesFrequency(frequencies, loopFrequencies, minT);
        evaluator.evaluate(null,tau);
        return evaluator.getLog();
    }

        public static EventLog bpmnCustomNoise(InputStream model, int nTraces, float[] noisesPerc, double tau) throws Exception {
        //Create an engine from the given model
            BpmnModelInstance mi = Bpmn.readModelFromStream(model);
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new NodaEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluato
        Set<String> minSet = new HashSet<>();
            for (Trace t :  bpmnSimpleLog(mi).getE()){
            minSet.add(LogUtil.toSimpleTrace(t));
        }
        CustomNoise evaluator = new CustomNoise(noisesPerc,nTraces, minSet);

        EventLog log = simulator.simulate(null);
        Delta d = evaluator.evaluate(log,tau);
        while(!evaluator.done && !interrupt){
            log = simulator.simulate(d);
            d = evaluator.evaluate(log,tau);
        }
        log.addTraces(evaluator.getNoised());
        return log;
    }

    public static EventLog pnmlCustomNoise(InputStream model, int nTraces, float[] noisesPerc, double tau) throws Exception {
        //Parse a PNML model from file
        Petrinet mi = null;
            mi = PTNetUtil.importFile(model);
        //Create an engine from the given model
        DateFormat df = new SimpleDateFormat("dd_MM_yy_HH_mm_ss");
        Date dateobj = new Date();
        SemanticEngine e = new PnmlEngine("EventLog"+df.format(dateobj), mi);
        SimLogAppender.append(PURPLE.class, SimLogAppender.Level.INFO, "Engine created");
        //Create the simulator
        SimulatorImpl simulator = new SimulatorImpl(e);
        //Create the evaluator
        CustomNoise evaluator = new CustomNoise(noisesPerc,nTraces, null);

        EventLog log = simulator.simulate(null);
        Delta d = evaluator.evaluate(log,tau);
        while(!evaluator.done && !interrupt){
            log = simulator.simulate(d);
            d = evaluator.evaluate(log,tau);
        }
        log.addTraces(evaluator.getNoised());
        return log;
    }

    public static void main(String[] args) throws Exception {
//
System.out.println(System.currentTimeMillis());
        InputStream s = new FileInputStream(new File("m.bpmn"));
        bpmnAlignCostConformance(s,5, 2000, 100);
        System.out.println(System.currentTimeMillis());
        Couple<Set<Trace>, Map<Trace, int[]>> out = bpmnSimpleLog(Bpmn.readModelFromStream(s));
        out.getE();
//        LogIO.saveXES(pnmlAlignCostConformance(s,3,2000,100), "p0.xes");

//        Map<Trace,Double> freq = new HashMap<>();
//        for(Trace t : log.getE()){
//            freq.put(t,100.0/log.getE().size());
//        }
//        Map<List<String>, Integer> fre = new HashMap<>();
//        for(List<String> c : log.getV()){
//            fre.put(c,5);
//        }
//        System.out.println(bpmnCustomTraceFrequency(mi,freq , fre, 10,100));
//        Map<String, Double> ret = new HashMap<>();
//            ret.put("uno",50.0);
//            ret.put("due",50.0);
//        ret = ModelProbabilities.calculateProbabilities(mi,ret);
//        System.out.println(ret);


//LogIO.saveXES(pnmlAlignCostConformance(PTNetUtil.importFile(new FileInputStream(new File("p0.pnml"))),2,1000,90),"ciao.xes");
//        InputStream stream = new FileInputStream("p0.pnml");
//        EventLog l = pnmlAlignCostConformance(stream,3, 50,90.0);
//        LogIO.saveXES(l,"log.xes");
//        System.exit(0);

    }


    private static EventLog generateLogsStream(Simulator simulator, Evaluator evaluator, Double tau) throws Exception {
        EventLog log = simulator.simulate(null);
        Delta d = evaluator.evaluate(log,tau);
        while(!d.isEmpty() && !interrupt){
            log = simulator.simulate(d);
            d = evaluator.evaluate(log,tau);
        }
        return log;
    }

}
