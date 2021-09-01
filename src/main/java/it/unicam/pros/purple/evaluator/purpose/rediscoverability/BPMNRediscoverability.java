package it.unicam.pros.purple.evaluator.purpose.rediscoverability;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.AlphaRelations;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.FootprintRelations;
import it.unicam.pros.purple.gui.util.logger.SimLogAppender;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;

import java.util.Map;


public class BPMNRediscoverability extends Rediscoverability {

	private Map<String, Map<String, FootprintRelations>> refMatrix;
	private int refRelations;

	public BPMNRediscoverability(BpmnModelInstance mi, String procName) {

			Process p1 = mi.getModelElementById(procName);
			this.refMatrix = AlphaRelations.getAlphaRelations(p1);

			SimLogAppender.append(BPMNRediscoverability.class, SimLogAppender.Level.INFO, "Relations found: "+refMatrix.toString());
			this.refRelations = refMatrix.keySet().size();
			for(String x : refMatrix.keySet()){
				this.refRelations += refMatrix.get(x).keySet().size();
			}

	}

	@Override
	public Delta evaluate(EventLog disc, Double tau) {
			return  new Delta(AlphaRelations.compareAlphaRelations(refMatrix, AlphaRelations.getAlphaRelations(disc), tau, refRelations));
	}



}

