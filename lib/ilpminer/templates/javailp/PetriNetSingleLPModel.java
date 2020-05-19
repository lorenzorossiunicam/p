package org.processmining.plugins.ilpminer.templates.javailp;

import java.util.Map;

import net.sf.javailp.Problem;
import net.sf.javailp.Result;

import org.processmining.plugins.ilpminer.ILPMinerSolution;
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
@ILPMinerStrategy(description = "<p>Generates a net by solving the relaxed problem and round the solution</p><p>to the nearest integers in one ILP problem.</p>", name = "Petri Net - Single ILP (Relaxed)")
public class PetriNetSingleLPModel extends PetriNetSingleILPModel {
	protected boolean causalDependencies;

	public PetriNetSingleLPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
	}

	protected void addVariables(Problem p) {
		// Default values will do, so just override super.addVariables with empty method
	}

	protected ILPMinerSolution makeSolution(Result result) {
		double[] x = new double[trans];
		double[] y = new double[trans];
		for (int t = 0; t < trans; t++) {
			x[t] = Math.round(result.get("x" + cdCount + "_" + t).doubleValue());
			y[t] = Math.round(result.get("y" + cdCount + "_" + t).doubleValue());
		}
		double c = Math.round(result.get("c" + cdCount).doubleValue());
		return new ILPMinerSolution(x, y, c);
	}
}
