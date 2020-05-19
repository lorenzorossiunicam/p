package org.processmining.plugins.ilpminer.templates.javailp;

import java.util.Map;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;

import org.processmining.plugins.ilpminer.ILPMinerStrategy;
import org.processmining.plugins.ilpminer.ILPModelSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;

/**
 * Extends the PetriNetILPModel class. Looks for a place without tokens for each
 * causal dependency in the log and a place with one token for each transition.
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategy(description = "Minimizes the number of arcs in the net in one ILP problem.", name = "Petri Net - Single ILP (Fewest Arcs)")
public class FewestArcsSingleILPModel extends PetriNetSingleILPModel {
	public FewestArcsSingleILPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
	}

	protected void addObjective(Problem p) {
		// "c + sum(t in Trans) ( ( sum(w in Lang) A[w][t] ) * (x[t] - y[t]) );";
		Linear l = p.getObjective();
		if (l == null) {
			l = new Linear();
		}
		for (int t = 0; t < trans; t++) {
			l.add(1, "x" + cdCount + "_" + t);
			l.add(1, "y" + cdCount + "_" + t);
		}
		l.add(slackWeight, "s" + cdCount);
		p.setObjective(l, OptType.MIN);
	}
}
