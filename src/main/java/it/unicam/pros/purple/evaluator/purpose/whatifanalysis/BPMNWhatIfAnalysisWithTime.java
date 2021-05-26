package it.unicam.pros.purple.evaluator.purpose.whatifanalysis;

import it.unicam.pros.purple.PURPLE;
import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.metrics.ModelProbabilities;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.TaskImpl;
import org.camunda.bpm.model.bpmn.instance.Task;

import java.util.HashMap;
import java.util.Map;

public class BPMNWhatIfAnalysisWithTime implements Evaluator {

    private Map<String, Double> tasksProbabilities = new HashMap<>();
    private double maxTraces;
    private Delta lastDelta;

    public BPMNWhatIfAnalysisWithTime(BpmnModelInstance mi, Map<String, Double> xorAndOrProbabilities, Map<String, Double> actCosts, Map<String, Long> actDur, long initDate, double maxTraces){
        ModelUtils.setCosts(actCosts);
        ModelUtils.setDurations(actDur);
        this.maxTraces = maxTraces;
        for(Task t : mi.getModelElementsByType(Task.class)){
            tasksProbabilities.put(t.getName(), 1.0);
        }
        //.tasksProbabilities = ModelProbabilities.calculateProbabilities(mi, xorAndOrProbabilities);
        ModelUtils.setInitDate(initDate);
    }

    @Override
    public Delta evaluate(EventLog disc, Double tau) {
        if (disc.size() >= maxTraces) {
            PURPLE.setInterrupt(true);}
        return getDelta(disc, tau);
    }

    private Delta getDelta(EventLog log, Double tau) {
        tau = 100 - tau;
        Delta delta = new Delta(ModelProbabilities.compareProbabilities(ModelProbabilities.calculateProbabilities(log), tasksProbabilities, tau));
        if (delta.isEmpty()) {
            EventLog missing = new EventLogImpl("",null);
            Trace t = new TraceImpl(null);
            t.appendEvent(new EventImpl("","","", null, null,null,null,null,null,null,null));
                    missing.addTrace(t);
            return new Delta(missing);
        }
        return delta;
    }

}
