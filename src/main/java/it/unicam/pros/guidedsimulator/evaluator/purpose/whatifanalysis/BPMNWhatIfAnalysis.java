package it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis;

import it.unicam.pros.guidedsimulator.evaluator.Delta;
import it.unicam.pros.guidedsimulator.evaluator.Evaluator;
import it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis.metrics.ModelProbabilities;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.eventlogs.EventLog;
import it.unicam.pros.guidedsimulator.util.eventlogs.utils.LogIO;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.deckfour.xes.model.XLog;

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
        if (disc.size() >= maxTraces) return null;
        return getDelta(disc, tau);
    }

    private Delta getDelta(EventLog log, Double tau) {
        tau = 100 - tau;

        return new Delta(ModelProbabilities.compareProbabilities(ModelProbabilities.calculateProbabilities(log), tasksProbabilities, tau));
    }

}
