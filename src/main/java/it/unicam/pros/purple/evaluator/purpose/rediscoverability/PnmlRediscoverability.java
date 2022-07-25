package it.unicam.pros.purple.evaluator.purpose.rediscoverability;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.AlphaRelations;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.FootprintRelations;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import java.util.Map;


public class PnmlRediscoverability extends Rediscoverability {

	private Map<String, Map<String, FootprintRelations>> refMatrix;
	private int refRelations;

	public PnmlRediscoverability(Petrinet mi) {
		this.refMatrix = AlphaRelations.getAlphaRelations(mi);
		this.refRelations = refMatrix.keySet().size();
		for(String x : refMatrix.keySet()){
			this.refRelations += refMatrix.get(x).keySet().size();
		}
	}

	@Override
	public Delta evaluate(EventLog disc, Double tau) {
		return getDelta(AlphaRelations.getAlphaRelations(disc), tau);
	}
 
	private Delta getDelta(Map<String, Map<String, FootprintRelations>> disc, double tau) {
		return new Delta(AlphaRelations.compareAlphaRelations(refMatrix, disc, tau, refRelations));
	}


}

