package it.unicam.pros.purple.evaluator.purpose.whatifanalysis;

import it.unicam.pros.purple.PURPLE;
import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.metrics.ModelProbabilities;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Map;

public class BPMNWhatIfAnalysis implements Evaluator {

    private Map<String, Double> tasksProbabilities;
    private double maxTraces;

    public BPMNWhatIfAnalysis(BpmnModelInstance mi, Map<String, Double> xorAndOrProbabilities, Map<String, Double> actCosts, double maxTraces){
        ModelUtils.setCosts(actCosts);
        this.maxTraces = maxTraces;
        this.tasksProbabilities = ModelProbabilities.calculateProbabilities(mi, xorAndOrProbabilities);

    }

    @Override
    public Delta evaluate(EventLog disc, Double tau) {
        if (disc.size() >= maxTraces) {
            PURPLE.setInterrupt(true);}
        return getDelta(disc, tau);
    }

    private Delta getDelta(EventLog log, Double tau) {
        tau = 100 - tau;

        return new Delta(ModelProbabilities.compareProbabilities(ModelProbabilities.calculateProbabilities(log), tasksProbabilities, tau));
    }

}
