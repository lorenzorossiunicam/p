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
@ILPMinerStrategy(description = "Minimizes the number of arcs in the net.", name = "Petri Net (Fewest Arcs)")
public class FewestArcsILPModel extends PetriNetILPModel {
	public FewestArcsILPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
	}

	protected void addObjective(Problem p) {
		// "sum(t in Trans) (x[t] + y[t]);";
		Linear l = new Linear();
		for (int t = 0; t < trans; t++) {
			l.add(1, "x" + t);
			l.add(1, "y" + t);
		}
		p.setObjective(l, OptType.MIN);
	}
}
