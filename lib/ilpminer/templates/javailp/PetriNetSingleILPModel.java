package org.processmining.plugins.ilpminer.templates.javailp;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.VarType;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.ilpminer.ILPMinerSolution;
import org.processmining.plugins.ilpminer.ILPMinerStrategy;
import org.processmining.plugins.ilpminer.ILPModelJavaILP;
import org.processmining.plugins.ilpminer.ILPModelSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.ilpminer.templates.PetriNetILPModelSettings;
import org.processmining.plugins.ilpminer.templates.PetriNetILPModelSettings.SearchType;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Implements the abstract ILPModel class. Looks for a place for each causal
 * dependency in the log, combined in one ILP problem.
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategy(description = "Constructs a Petri Net with the selected search strategy in one ILP problem.", name = "Petri Net - Single ILP")
public class PetriNetSingleILPModel extends ILPModelJavaILP {
	/**
	 * Should be as low as possible due to problems occuring in the solver when
	 * summations go over Integer.MAX_VALUE. Lower bound is: max trace length *
	 * number of transitions
	 */
	public int slackWeight;

	protected static PetriNetILPModelSettings strategySettings;
	protected static JComboBox searchTypeBox;

	public int trans = 0, lang = 0, cdCount = 0;
	public int[][] a = {}, aPrime = {};
	public ArrayList<Integer> cd = null;
	protected ArrayList<XEventClass> initialPlaces, notInitialPlaces;

	protected boolean initialPlace;

	public PetriNetSingleILPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
		strategySettings = (PetriNetILPModelSettings) settings;
	}

	public Problem getModel() {
		Problem problem = new Problem();
		cdCount = 0;
		if (strategySettings.separateInitialPlaces()) {
			initialPlace = true;
			processInitialPlaces(problem);
		}
		initialPlace = false;
		switch (strategySettings.getSearchType()) {
			case BASIC : // this doesn't work as a single ILP problem since it's
				// incremental
				break;
			case PER_TRANSITION :
				processTransitions(problem, true);
				processTransitions(problem, false);
				break;
			case PRE_PER_TRANSITION :
				processTransitions(problem, true);
				break;
			case POST_PER_TRANSITION :
				processTransitions(problem, false);
				break;
			default :// PER_CD
				processCausalDependencies(problem);
				break;
		}
		return problem;
	}

	public void addModel(Problem p) {
		addObjective(p);
		addConstraints(p);
		addExtensionConstraints(p);
		addVariables(p);
	}

	public void makeData() {
		// store the log information provided in the parameters for future acces
		Object[] values = m.values().toArray();
		trans = values.length;
		lang = l.last();
		a = l.getTransitionCountMatrix();
		aPrime = l.getTransitionCountMatrix(1);

		// find all transitions that are not in the second part of a causal
		// dependency
		// start with everything and remove those that are in the second part of
		// a causal dependency
		initialPlaces = new ArrayList<XEventClass>(r.getEventClasses().getClasses());
		notInitialPlaces = new ArrayList<XEventClass>();
		for (Map.Entry<Pair<XEventClass, XEventClass>, Double> entry : r.getCausalDependencies().entrySet()) {
			if (entry.getValue() > 0) {
				int size = initialPlaces.size();
				initialPlaces.remove(entry.getKey().getSecond());
				if (initialPlaces.size() != size) {
					notInitialPlaces.add(entry.getKey().getSecond());
				}
			}
		}
	}

	protected void processModel(PluginContext context, SolverFactory factory) {
		slackWeight = l.getMaxWordLength() * trans * lang;
		addSolutions(solve(context));
	}

	protected void processInitialPlaces(Problem problem) {
		for (XEventClass c : initialPlaces) {
			cd = new ArrayList<Integer>();
			cd.add(m.get(c));
			cd.add(-1);

			addModel(problem);
			cdCount++;
		}
	}

	protected void processTransitions(Problem problem, boolean beforeTrans) {
		ArrayList<XEventClass> places = new ArrayList<XEventClass>(r.getEventClasses().getClasses());
		if (strategySettings.separateInitialPlaces() && beforeTrans) {
			places = notInitialPlaces;
		}

		for (XEventClass clazz : places) {
			cd = new ArrayList<Integer>();
			cd.add(m.get(clazz));
			// abuse cd to indicate wether we search
			// in front of (0) or after (1) a transition
			cd.add(beforeTrans ? 0 : 1);

			addModel(problem);
			cdCount++;
		}
	}

	protected void processCausalDependencies(Problem problem) {
		for (Map.Entry<Pair<XEventClass, XEventClass>, Double> entry : r.getCausalDependencies().entrySet()) {
			if (entry.getValue() > 0) {
				cd = new ArrayList<Integer>();
				cd.add(m.get(entry.getKey().getFirst()));
				cd.add(m.get(entry.getKey().getSecond()));

				addModel(problem);
				cdCount++;
			}
		}
	}

	/**
	 * defines the variable types and bounds in the problem
	 * 
	 * @param p
	 *            - problem
	 */
	protected void addVariables(Problem p) {
		for (int t = 0; t < trans; t++) {
			p.setVarType("x" + cdCount + "_" + t, VarType.INT);
			p.setVarLowerBound("x" + cdCount + "_" + t, 0);
			p.setVarUpperBound("x" + cdCount + "_" + t, 1);
			p.setVarType("y" + cdCount + "_" + t, VarType.INT);
			p.setVarLowerBound("y" + cdCount + "_" + t, 0);
			p.setVarUpperBound("y" + cdCount + "_" + t, 1);
		}
		p.setVarType("c" + cdCount, VarType.INT);
		p.setVarLowerBound("c" + cdCount, 0);
		p.setVarUpperBound("c" + cdCount, 1);
		p.setVarType("s" + cdCount, VarType.INT);
		p.setVarLowerBound("s" + cdCount, 0);
		p.setVarUpperBound("s" + cdCount, 1);
	}

	/**
	 * adds an objective to the problem
	 * 
	 * @param p
	 *            - problem
	 */
	protected void addObjective(Problem p) {
		// "c + sum(w in Lang) ( c + ( sum(t in trans) A[w][t] ) * (x[t] - y[t]) );";
		Linear l = p.getObjective();
		if (l == null) {
			l = new Linear();
		}
		l.add(1 + lang, "c" + cdCount);
		for (int t = 0; t < trans; t++) {
			int sum = 0;
			for (int w = 0; w < lang; w++) {
				sum += a[w][t];
			}
			l.add(sum, "x" + cdCount + "_" + t);
			l.add(-sum, "y" + cdCount + "_" + t);
		}
		l.add(slackWeight, "s" + cdCount);
		p.setObjective(l, OptType.MIN);
	}

	/**
	 * adds the constraints to the problem
	 * 
	 * @param p
	 *            - problem
	 */
	protected void addConstraints(Problem p) {
		// forall(w in Lang) ctMinimalRegion: c + ( sum(t in Trans) APrime[w][t]
		// * x[t] ) - ( sum(t in Trans) A[w][t] * y[t] ) >= 0;
		for (int w = 0; w < lang; w++) {
			Linear l = new Linear();
			l.add(1, "c" + cdCount);
			for (int t = 0; t < trans; t++) {
				if (aPrime[w][t] > 0) {
					l.add(aPrime[w][t], "x" + cdCount + "_" + t);
				}
				if (a[w][t] > 0) {
					l.add(-a[w][t], "y" + cdCount + "_" + t);
				}
			}
			l.add(slackWeight, "s" + cdCount);
			p.add(l, Operator.GE, 0);
		}
		addPlaceConstraints(p);
	}

	protected void addPlaceConstraints(Problem p) {
		if (initialPlace) {
			// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
			// => volgt uit volgende constraint(s)
			// ctToken: c == 1;
			Linear l = new Linear();
			l.add(1, "c" + cdCount);
			p.add(l, Operator.EQ, 1);
			// ctInitialPlace: sum(t in Trans) x[t] == 0;" + "y[CD.To] == 1;
			l = new Linear();
			for (int t = 0; t < trans; t++) {
				l.add(1, "x" + cdCount + "_" + t);
			}
			p.add(l, Operator.EQ, 0);
			l = new Linear();
			l.add(1, "y" + cdCount + "_" + cd.get(0).toString());
			p.add(l, Operator.EQ, 1);
		} else {
			if (strategySettings.separateInitialPlaces()) {
				// ctNoToken: c == 0;
				Linear l = new Linear();
				l.add(1, "c" + cdCount);
				p.add(l, Operator.EQ, 0);
			}
			switch (strategySettings.getSearchType()) {
				case BASIC : // this doesn't work as a single ILP problem since it's
					// incremental
					break;
				case PER_TRANSITION :
				case PRE_PER_TRANSITION :
				case POST_PER_TRANSITION :
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					Linear l = new Linear();
					if (cd.get(1) == 0) { // search the place in front of the
						// transition
						l.add(1, "y" + cdCount + "_" + cd.get(0).toString());
					} else { // cd.get(1) == 1; search the place after the
						// transition
						l.add(1, "x" + cdCount + "_" + cd.get(0).toString());
					}
					p.add(l, Operator.EQ, 1);
					break;
				default : // PER_CD
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					// ctCausalDependency: x[CD.From] == 1 && y[CD.To] == 1;
					l = new Linear();
					l.add(1, "x" + cdCount + "_" + cd.get(0).toString());
					p.add(l, Operator.EQ, 1);
					l = new Linear();
					l.add(1, "y" + cdCount + "_" + cd.get(1).toString());
					p.add(l, Operator.EQ, 1);
					break;
			}
		}
	}

	/**
	 * converts the Java-ILP result in a solution (place representation) and
	 * adds it to the set of found solutions
	 * 
	 * @param result
	 */
	protected void addSolutions(Result result) {
		if (result != null) {
			for (int i = 0; i < cdCount; i++) {
				if (result.get("s" + i).doubleValue() == 0) {
					solutions.add(makeSolution(result, i));
				}
			}
		}
	}

	/**
	 * converts the Java-ILP result in a solution (place representation)
	 * 
	 * @param result
	 * @return the converted solution
	 */
	protected ILPMinerSolution makeSolution(Result result, int causalDependency) {
		double[] x = new double[trans];
		double[] y = new double[trans];
		for (int t = 0; t < trans; t++) {
			x[t] = result.get("x" + causalDependency + "_" + t).doubleValue();
			y[t] = result.get("y" + causalDependency + "_" + t).doubleValue();
		}
		double c = result.get("c" + causalDependency).doubleValue();
		return new ILPMinerSolution(x, y, c);
	}

	public static Object[] getSettingsGUI(SlickerFactory f, Class<?> strategy) {
		JPanel panel = f.createRoundedPanel();
		searchTypeBox = f.createComboBox(new SearchType[] { SearchType.PER_CD, SearchType.PER_TRANSITION,
				SearchType.PRE_PER_TRANSITION, SearchType.POST_PER_TRANSITION });
		searchTypeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				strategySettings.setSearchType((SearchType) e.getItem());
			}
		});
		JCheckBox initialPlacesCBox = f.createCheckBox("Search for separate initial places", true);
		initialPlacesCBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				strategySettings.setSeparateInitialPlaces(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		strategySettings = new PetriNetILPModelSettings((SearchType) searchTypeBox.getSelectedItem(), initialPlacesCBox
				.isSelected());
		searchTypeBox.setSelectedItem(SearchType.PER_CD);
		panel.setLayout(new TableLayout(new double[][] { { 200, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, 30, 30 } }));
		panel.add(f.createLabel("<html><p>" + getAuthor(strategy) + "</p><p>" + getDescription(strategy)
				+ "</p><h3>ILP Variant Settings</h3></html>"), "0, 0, 1, 0");
		panel.add(f.createLabel("Number of places: "), "0, 1");
		panel.add(searchTypeBox, "1, 1");
		panel.add(initialPlacesCBox, "1, 2");
		return new Object[] { panel, strategySettings };
	}
}
