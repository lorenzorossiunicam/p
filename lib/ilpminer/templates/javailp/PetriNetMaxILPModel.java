package org.processmining.plugins.ilpminer.templates.javailp;

import java.util.Map;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;

import org.processmining.plugins.ilpminer.ILPMinerStrategy;
import org.processmining.plugins.ilpminer.ILPModelSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;

/**
 * Implements the abstract ILPModel class. Looks for a place for each causal
 * dependency in the log.
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategy(author = "T. van der Wiel", description = "The resulting Petri net allows at most the behavior in the log.", name = "Petri Net (Best Approximation)")
public class PetriNetMaxILPModel extends PetriNetILPModel {
	public PetriNetMaxILPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
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

	protected void addConstraints(Problem p) {
		for (int w = 0; w < lang; w++) {
			Linear l = new Linear();
			l.add(1, "c");
			for (int t = 0; t < trans; t++) {
				if (aPrime[w][t] > 0) {
					l.add(aPrime[w][t], "x" + t);
				}
				if (a[w][t] > 0) {
					l.add(-a[w][t], "y" + t);
				}
			}
			p.add(l, Operator.LE, 0);
		}
		addPlaceConstraints(p);
	}
}
