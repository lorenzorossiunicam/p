package it.unicam.pros.pplg.evaluator.purpose.whatifanalysis;

import it.unicam.pros.pplg.PPLG;
import it.unicam.pros.pplg.evaluator.Delta;
import it.unicam.pros.pplg.evaluator.Evaluator;
import it.unicam.pros.pplg.evaluator.purpose.whatifanalysis.metrics.ModelProbabilities;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.pplg.util.eventlogs.EventLog;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Map;

public class BPMNWhatIfAnalysis implements Evaluator {

    private Map<String, Double> tasksProbabilities;
    private double maxTraces;

    public BPMNWhatIfAnalysis(BpmnModelInstance mi, Map<String, Double> xorAndOrProbabilities, Map<String, Double> actCosts, double maxTraces){
        ModelUtils.setCosts(actCosts);
        this.maxTraces = maxTraces;
        this.tasksProbabilities = ModelProbabilities.calculateProbabilities(mi, xorAndOrProbabilities);
        System.out.println(tasksProbabilities);

    }

    @Override
    public Delta evaluate(EventLog disc, Double tau) {
        if (disc.size() >= maxTraces) {PPLG.setInterrupt(true);}
        return getDelta(disc, tau);
    }

    private Delta getDelta(EventLog log, Double tau) {
        tau = 100 - tau;

        return new Delta(ModelProbabilities.compareProbabilities(ModelProbabilities.calculateProbabilities(log), tasksProbabilities, tau));
    }

}
