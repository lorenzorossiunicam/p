package org.processmining.plugins.ilpminer.templates.javailp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.SolverFactory;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.ilpminer.ILPMinerSolution;
import org.processmining.plugins.ilpminer.ILPMinerStrategy;
import org.processmining.plugins.ilpminer.ILPModelSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.ilpminer.templates.PetriNetILPModelSettings.SearchType;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Extends the WorkflowILPModel class. Looks for a workflow net where for every
 * two inputsets of a transition that have one or more places in common, these
 * sets are equal.
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategy(description = "<p>Constructs a workflow net with the special property that</p><p>if an input set of one transition contains a place that is also in an input set</p><p>of another transition, then these input sets are equal.</p>", name = "Petri Net (Free-Choice)")
public class FreeChoiceILPModel extends PetriNetILPModel {
	private Stack<Pair<Integer, Integer>> stack;
	private Map<ILPMinerSolution, Pair<Integer, Integer>> solutionMap = new HashMap<ILPMinerSolution, Pair<Integer, Integer>>();

	public FreeChoiceILPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
	}

	protected void processModel(PluginContext context, SolverFactory factory) {
		solutionMap = new HashMap<ILPMinerSolution, Pair<Integer, Integer>>();

		ArrayList<XEventClass> places = new ArrayList<XEventClass>(r.getEventClasses().getClasses());
		if (strategySettings.separateInitialPlaces()) {
			initialPlace = true;
			processInitialPlaces(context);
			places = notInitialPlaces;
		}

		// put all places on a stack
		stack = new Stack<Pair<Integer, Integer>>();
		switch (strategySettings.getSearchType()) {
			case BASIC : // this doesn't work for basic; in fact this option should not be in the options
				break;
			case PER_TRANSITION :
				for (XEventClass clazz : initialPlaces) {
					stack.push(new Pair<Integer, Integer>(m.get(clazz), 1));
				}
				for (XEventClass clazz : places) {
					stack.push(new Pair<Integer, Integer>(m.get(clazz), 0));
					stack.push(new Pair<Integer, Integer>(m.get(clazz), 1));
				}
				break;
			case PRE_PER_TRANSITION :
				for (XEventClass clazz : places) {
					stack.push(new Pair<Integer, Integer>(m.get(clazz), 0));
				}
				break;
			case POST_PER_TRANSITION :
				for (XEventClass clazz : r.getEventClasses().getClasses()) {
					stack.push(new Pair<Integer, Integer>(m.get(clazz), 1));
				}
				break;
			default :// PER_CD
				for (Map.Entry<Pair<XEventClass, XEventClass>, Double> entry : r.getCausalDependencies().entrySet()) {
					if (entry.getValue() > 0) {
						stack.push(new Pair<Integer, Integer>(m.get(entry.getKey().getFirst()), m.get(entry.getKey()
								.getSecond())));
					}
				}
				break;
		}
		// search for places without tokens.
		initialPlace = false;
		context.getProgress().setIndeterminate(false);
		int max = stack.size();
		context.getProgress().setMaximum(max);
		while (!stack.isEmpty()) {
			if (context.getProgress().isCancelled()) {
				return;
			}
			context.getProgress().setValue(max - stack.size());

			Pair<Integer, Integer> p = stack.pop();
			cd = new ArrayList<Integer>();
			cd.add(p.getFirst());
			cd.add(p.getSecond());

			addSolution(solve(context), p);
		}
		context.getProgress().setIndeterminate(true);
	}

	protected void addConstraints(Problem p) {
		super.addConstraints(p);
		// forall(s in Solutions) forall(t1 in Trans) forall(t2 in Trans) ctPlacePresetEquality: s.Y[t1] == 0 || s.Y[t2] == 0 || y[t1] == y[t2];
		for (ILPMinerSolution s : solutionMap.keySet()) {
			for (int t1 = 0; t1 < trans; t1++) {
				for (int t2 = 0; t2 < trans; t2++) {
					if ((s.getOutputSet()[t1] == 1) && (s.getOutputSet()[t2] == 1)) {
						Linear l = new Linear();
						l.add(1, "y" + t1);
						l.add(-1, "y" + t2);
						p.add(l, Operator.EQ, 0);
					}
				}
			}
		}
	}

	/**
	 * converts the Java-ILP result in a solution (place representation) and
	 * adds it to the set of found solutions, mapped to the causal dependency it
	 * represents. checks for earlier found solutions that have an outputset
	 * that is a subset of the new solution, removing these solutions and put
	 * the causal dependency back on the stack.
	 * 
	 * @param result
	 * @param cd
	 *            - the causal dependency used to find the result
	 */
	protected void addSolution(Result result, Pair<Integer, Integer> cd) {
		if (result != null) { // since this isn't null, makeSolution will succeed and no further check is needed
			ILPMinerSolution newsol = null;
			newsol = makeSolution(result);
			ArrayList<ILPMinerSolution> removing = new ArrayList<ILPMinerSolution>();
			for (int t = 0; t < trans; t++) {
				for (ILPMinerSolution s : solutionMap.keySet()) {
					if ((newsol.getOutputSet()[t] == 1) && (s.getOutputSet()[t] == 1)) {
						boolean hasDiff = false;
						for (int tc = 0; tc < trans; tc++) {
							if (newsol.getOutputSet()[t] != s.getOutputSet()[t]) {
								hasDiff = true;
							}
						}
						if (hasDiff) {
							// remove the solution later to keep the iterator correct
							removing.add(s);
						}
					}
				}
			}
			for (ILPMinerSolution rs : removing) {
				stack.add(solutionMap.get(rs));
				solutionMap.remove(rs);
			}
			solutionMap.put(newsol, cd);
		}
	}

	public Set<ILPMinerSolution> getSolutions() {
		// combine the initial places and the causal dependency places
		Set<ILPMinerSolution> allSols = new HashSet<ILPMinerSolution>();
		allSols.addAll(solutions);
		allSols.addAll(solutionMap.keySet());
		return allSols;
	}

	public static Object[] getSettingsGUI(SlickerFactory f, Class<?> strategy) {
		Object[] gui = PetriNetILPModel.getSettingsGUI(f, strategy);
		searchTypeBox.removeItem(SearchType.BASIC);
		return gui;
	}
}
