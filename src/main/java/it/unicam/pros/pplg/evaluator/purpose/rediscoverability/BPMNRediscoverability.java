package it.unicam.pros.pplg.evaluator.purpose.rediscoverability;

import java.util.Map;

import it.unicam.pros.pplg.evaluator.Delta;
import it.unicam.pros.pplg.gui.util.logger.SimLogAppender;
import it.unicam.pros.pplg.util.eventlogs.utils.LogIO;
import it.unicam.pros.pplg.util.eventlogs.EventLog;
import it.unicam.pros.pplg.util.mining.prom.discoverability.Alphas;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
 
import it.unicam.pros.pplg.evaluator.purpose.rediscoverability.metrics.AlphaRelations;
import it.unicam.pros.pplg.evaluator.purpose.rediscoverability.metrics.AlphaRelations.Relations;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;


public class BPMNRediscoverability extends Rediscoverability {

	private Map<String, Map<String, Relations>> refMatrix;
	private AlphaVersion algo;
	private int refRelations;

	public BPMNRediscoverability(BpmnModelInstance mi, String procName, RediscoverabilityAlgo algo) {
		Process p1 = mi.getModelElementById(procName);
		this.refMatrix = AlphaRelations.getAlphaRelations(p1);

		SimLogAppender.append(BPMNRediscoverability.class, SimLogAppender.Level.INFO, "Relations found: "+refMatrix.toString());
		this.refRelations = refMatrix.keySet().size();
		for(String x : refMatrix.keySet()){
			this.refRelations += refMatrix.get(x).keySet().size();
		}

		switch (algo){
			case ALPHA:
				this.algo = AlphaVersion.CLASSIC;
				break;
			case ALPHA_PLUS:
				this.algo = AlphaVersion.PLUS;
				break;
			case ALPHA_PLUS_PLUS:
				this.algo = AlphaVersion.PLUS_PLUS;
				break;
			case ALPA_SHARP:
				this.algo = AlphaVersion.SHARP;
				break;
			case ALPHA_DOLLAR:
				this.algo = AlphaVersion.DOLLAR;
		}

	}

	@Override
	public Delta evaluate(EventLog disc, Double tau) {
		XLog log = LogIO.getXLog(disc);
		Pair<Petrinet, Marking> pt = null;
		try {
			pt = Alphas.alpha(log, algo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Map<String, Relations>> matrix2 =	AlphaRelations.getAlphaRelations(pt.getFirst());
		return getDelta(matrix2, tau);
	}
 
	private Delta getDelta(Map<String, Map<String, Relations>> disc, double tau) {
		return new Delta(AlphaRelations.compareAlphaRelations(refMatrix, disc, tau, refRelations));
	}


}

