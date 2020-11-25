package it.unicam.pros.purple.evaluator.purpose.rediscoverability;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.AlphaRelations;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.AlphaRelations.Relations;
import it.unicam.pros.purple.gui.util.logger.SimLogAppender;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import java.util.Map;


public class PnmlRediscoverability extends Rediscoverability {

	private Map<String, Map<String, Relations>> refMatrix;
	private AlphaVersion algo;
	private int refRelations;

	public PnmlRediscoverability(Petrinet mi, RediscoverabilityAlgo algo) {
		this.refMatrix = AlphaRelations.getAlphaRelations(mi);
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
//		XLog log = LogIO.getXLog(disc);
//		Pair<Petrinet, Marking> pt = null;
//		try {
//			pt = Alphas.alpha(log, algo);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		Map<String, Map<String, Relations>> matrix2 =	AlphaRelations.getAlphaRelations(pt.getFirst());

		return getDelta(AlphaRelations.getAlphaRelations(disc), tau);
	}
 
	private Delta getDelta(Map<String, Map<String, Relations>> disc, double tau) {
		return new Delta(AlphaRelations.compareAlphaRelations(refMatrix, disc, tau, refRelations));
	}


}

