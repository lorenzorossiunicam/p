package org.processmining.plugins.ilpminer.templates.cplex;

import ilog.opl.IloOplDataHandler;
import ilog.opl.IloOplFactory;

import java.util.ArrayList;
import java.util.Map;

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
@ILPMinerStrategy(description = "Constructs a Petri net that guarantees there are no tokens left in one ILP problem.", name = "Petri Net - Single ILP (Empty after Completion)")
public class EmptyAfterCompletionPetriNetSingleILPModel extends PetriNetSingleILPModel {
	protected ArrayList<Integer> maxWords;

	public EmptyAfterCompletionPetriNetSingleILPModel(IloOplFactory factory, Class<?>[] extensions,
			Map<SolverSetting, Object> solverSettings, ILPModelSettings settings) {
		super(factory, extensions, solverSettings, settings);
	}

	protected void writePlaceData(IloOplDataHandler handler) {
		super.writePlaceData(handler);

		handler.startElement("MaxWords");
		handler.startSet();
		for (int i : maxWords) {
			handler.addIntItem(i);
		}
		handler.endSet();
		handler.endElement();
	}

	public void makeData() {
		super.makeData();

		maxWords = new ArrayList<Integer>();
		for (int prefix = 0; prefix < aMinusAPrime.length; prefix++) {
			boolean isPrefix = false;
			for (int w = 0; w < aMinusAPrime.length; w++) {
				boolean isPrefixOfW = true;
				for (int t = 0; t < trans; t++) {
					if (aPrime[prefix][t] + ((aMinusAPrime[prefix] == t) ? 1 : 0) > aPrime[w][t]
							+ ((aMinusAPrime[w] == t) ? 1 : 0)) {
						isPrefixOfW = false;
					}
				}
				// we don't wan't to delete equal words, since they would both
				// be deleted (there should be equal onces in a prefix closed
				// language though)
				if (isPrefixOfW && (sum(aPrime[prefix]) < sum(aPrime[w]))) {
					isPrefix = true;
				}
			}
			if (!isPrefix) {
				maxWords.add(prefix);
			}
		}
	}

	private int sum(int[] values) {
		int sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		return sum;
	}

	/**
	 * defines the variable types and bounds in the problem
	 * 
	 * @param p
	 *            - problem
	 */
	protected String getVariables() {
		return super.getVariables() + "{int} MaxWords = ...;";
	}

	/**
	 * adds the constraints to the problem
	 */
	protected String getConstraints() {
		return super.getConstraints() + "forall(p in Places)" + "forall(w in MaxWords)"
				+ "c[p] + sum(<t,a> in APrime[w]) (" + "a * x[p][t] - a * y[p][t]"
				+ ") + x[p][AMinusAPrime[w]] - y[p][AMinusAPrime[w]] - s[p] * SlackWeight <= 0;";
	}
}
