package org.processmining.plugins.ilpminer.templates.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVarMap;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import ilog.opl.IloOplDataHandler;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.ilpminer.ILPMinerSolution;
import org.processmining.plugins.ilpminer.ILPMinerStrategy;
import org.processmining.plugins.ilpminer.ILPModelCPLEX;
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
public class PetriNetSingleILPModel extends ILPModelCPLEX {
	protected static PetriNetILPModelSettings strategySettings;
	protected static JComboBox searchTypeBox;

	protected int trans = 0, lang = 0;
	protected int[] aMinusAPrime = {};
	protected int[][] aPrime = {};
	ArrayList<XEventClass> initialPlaces, notInitialPlaces;
	protected int numPlaces;
	protected ArrayList<int[]> cd = new ArrayList<int[]>();

	protected boolean initialPlace;

	/**
	 * Should be as low as possible due to problems occuring in the solver when
	 * summations go over Integer.MAX_VALUE. Worst case lower bound is: max
	 * trace length * number of transitions * number of words in language
	 */
	protected int slackWeight;

	public PetriNetSingleILPModel(IloOplFactory factory, Class<?>[] extensions,
			Map<SolverSetting, Object> solverSettings, ILPModelSettings settings) {
		super(factory, extensions, solverSettings, settings);
		strategySettings = (PetriNetILPModelSettings) settings;
	}

	public void customRead() {
		IloOplDataHandler handler = getDataHandler();
		handler.startElement("AMinusAPrime");
		handler.startArray();
		for (int w = 0; w < lang; w++) {
			handler.addIntItem(aMinusAPrime[w]);
		}
		handler.endArray();
		handler.endElement();

		handler.startElement("APrime");
		handler.startArray();
		for (int w = 0; w < lang; w++) {
			handler.startSet();
			for (int t = 0; t < trans; t++) {
				if (aPrime[w][t] > 0) {
					handler.startTuple();
					handler.addIntItem(t);
					handler.addIntItem(aPrime[w][t]);
					handler.endTuple();
				}
			}
			handler.endSet();
		}
		handler.endArray();
		handler.endElement();

		writePlaceData(handler);
	}

	protected void writePlaceData(IloOplDataHandler handler) {
		handler.startElement("Cds");
		handler.startArray();
		for (int i = 0; i < cd.size(); i++) {
			handler.startTuple();
			handler.addIntItem(cd.get(i)[0]);
			handler.addIntItem(cd.get(i)[1]);
			handler.endTuple();
		}
		handler.endArray();
		handler.endElement();
	}

	public String getModel() {
		return getVariables() + " minimize " + getObjective() + " subject to { " + getConstraints()
				+ getExtensionConstraints() + " }";
	}

	public void makeData() {
		trans = m.values().size();
		lang = l.last();
		aMinusAPrime = l.getLastTransitionVector();
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

		numPlaces = 0;
		switch (strategySettings.getSearchType()) {
			case PER_TRANSITION :
			case PRE_PER_TRANSITION :
			case POST_PER_TRANSITION :
				// we abuse the CausalDependency construct and use the From part to
				// state wether the place is befor (0) or after (1) the transition
				// in the To part
				for (XEventClass clazz : initialPlaces) {
					if (strategySettings.getSearchType() != SearchType.POST_PER_TRANSITION) {
						cd.add(new int[] { 0, m.get(clazz) });
						numPlaces++;
					}
					if (strategySettings.getSearchType() != SearchType.PRE_PER_TRANSITION) {
						cd.add(new int[] { 1, m.get(clazz) });
						numPlaces++;
					}
				}
				for (XEventClass clazz : notInitialPlaces) {
					if (strategySettings.getSearchType() != SearchType.POST_PER_TRANSITION) {
						cd.add(new int[] { 0, m.get(clazz) });
						numPlaces++;
					}
					if (strategySettings.getSearchType() != SearchType.PRE_PER_TRANSITION) {
						cd.add(new int[] { 1, m.get(clazz) });
						numPlaces++;
					}
				}
				break;
			default : // PER_CD
				if (strategySettings.separateInitialPlaces()) {
					for (XEventClass clazz : initialPlaces) {
						cd.add(new int[] { -1, m.get(clazz) });
						numPlaces++;
					}
				}
				for (Map.Entry<Pair<XEventClass, XEventClass>, Double> entry : r.getCausalDependencies().entrySet()) {
					if (entry.getValue() > 0) {
						cd.add(new int[] { m.get(entry.getKey().getFirst()), m.get(entry.getKey().getSecond()) });
						numPlaces++;
					}
				}
				break;
		}
	}

	protected void processModel(PluginContext context, IloOplSettings settings, IloCplex cp) {
		IloOplModelSource modelSource = factory.createOplModelSourceFromString(getModel(), "ProM6ILPMiner");
		IloOplModelDefinition def = factory.createOplModelDefinition(modelSource, settings);

		solve(context, def, cp);

		modelSource.end();
		def.end();
	}

	/**
	 * defines the variable types and bounds in the problem
	 */
	protected String getVariables() {
		return "range Lang = 0.." + (lang - 1) + ";" + "range Trans = 0.." + (trans - 1) + ";" + "range Places = 0.."
				+ (numPlaces - 1) + ";" + "range InitialPlaces = 0.." + (initialPlaces.size() - 1) + ";"
				+ "range OtherPlaces = " + (initialPlaces.size()) + ".." + (numPlaces - 1) + ";" + "int SlackWeight = "
				+ (l.getMaxWordLength() * trans * lang) + ";" + "tuple Letter { key int Trans; int Amount; }"
				+ "{Letter} APrime[Lang] = ...;" + "int AMinusAPrime[Lang] = ...;"
				+ "tuple CausalDependency { int From; int To; } CausalDependency Cds[Places] = ...;"
				+ "range Bin = 0..1;" + "dvar int c[Places] in Bin;" + "dvar int s[Places] in Bin;"
				+ "dvar int x[Places][Trans] in Bin;" + "dvar int y[Places][Trans] in Bin;";
	}

	/**
	 * adds an objective to the problem
	 */
	protected String getObjective() {
		return "sum(p in Places) (" + "c[p] + sum(w in Lang) (" + "c[p] + sum(<t,a> in APrime[w]) ("
				+ "a * ( x[p][t] - y[p][t] )" + ") + x[p][AMinusAPrime[w]] - y[p][AMinusAPrime[w]]"
				+ ") + s[p] * SlackWeight );";
	}

	/**
	 * adds the constraints to the problem
	 */
	protected String getConstraints() {
		return "forall(p in Places)" + "forall(w in Lang)" + "c[p] + sum(<t,a> in APrime[w]) ("
				+ "a * x[p][t] - a * y[p][t]" + ") - y[p][AMinusAPrime[w]] + s[p] * SlackWeight >= 0;"
				+ getPlaceConstraints();
	}

	protected String getPlaceConstraints() {
		if (strategySettings.separateInitialPlaces()) {
			switch (strategySettings.getSearchType()) {
				case PER_TRANSITION :
				case PRE_PER_TRANSITION :
				case POST_PER_TRANSITION :
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					return "forall(p in InitialPlaces) {" + "c[p] + s[p] >= 1;"
							+ "sum(t in Trans) x[p][t] - s[p] * SlackWeight <= 0;" + "y[p][Cds[p].To] + s[p] >= 1;"
							+ "}" + "forall(p in OtherPlaces) {" + "c[p] - s[p] <= 0;"
							+ "y[p][Cds[p].To] + s[p] >= 1 - Cds[p].From;"
							+ "x[p][Cds[p].To] + s[p] >= 1 * Cds[p].From;" + "}";
				default : // PER_CD
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					return "forall(p in InitialPlaces) {" + "c[p] + s[p] >= 1;"
							+ "sum(t in Trans) x[p][t] - s[p] * SlackWeight <= 0;" + "y[p][Cds[p].To] + s[p] >= 1;"
							+ "}" + "forall(p in OtherPlaces) {" + "c[p] - s[p] <= 0;"
							+ "x[p][Cds[p].From] + s[p] >= 1;" + "y[p][Cds[p].To] + s[p] >= 1;" + "}";
			}
		} else {
			switch (strategySettings.getSearchType()) {
				case PER_TRANSITION :
				case PRE_PER_TRANSITION :
				case POST_PER_TRANSITION :
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					return "forall(p in Places) {" + "y[p][Cds[p].To] + s[p] >= 1 - Cds[p].From;"
							+ "x[p][Cds[p].To] + s[p] >= 1 * Cds[p].From;" + "}";
				default : // PER_CD
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					return "forall(p in Places) {" + "x[p][Cds[p].From] + s[p] >= 1;" + "y[p][Cds[p].To] + s[p] >= 1;"
							+ "}";
			}
		}
	}

	/**
	 * converts the CPLEX string result in a solution (place representation) and
	 * adds it to the set of found solutions
	 * 
	 * @param opl
	 *            - opl model
	 */
	@SuppressWarnings("unchecked")
	protected ILPMinerSolution addSolution(IloOplModel opl) {
		double[] c = new double[numPlaces], s = new double[numPlaces];
		double[][] x = new double[numPlaces][trans], y = new double[numPlaces][trans];

		for (Iterator<IloOplElement> it = opl.getElementIterator(); it.hasNext();) {
			IloOplElement e = it.next();
			try {
				if (e.isDecisionVariable()) {
					IloIntVarMap map = e.asIntVarMap();
					if ("c".equals(e.getName())) {
						c = getValues(map, opl.getCplex(), numPlaces);
					} else if ("s".equals(e.getName())) {
						s = getValues(map, opl.getCplex(), numPlaces);
					} else if ("x".equals(e.getName())) {
						x = getValuess(map, opl.getCplex(), numPlaces, trans);
					} else if ("y".equals(e.getName())) {
						y = getValuess(map, opl.getCplex(), numPlaces, trans);
					}
				}
			} catch (Exception ex) {
				return null;
			}
		}

		for (int i = 0; i < numPlaces; i++) {
			if (s[i] == 0) {
				solutions.add(new ILPMinerSolution(x[i], y[i], c[i]));
			}
		}

		// we cant return a solution, since many were found at once. We don't
		// use this result in this strategy anyway, but for future use at least
		// you can see that this method was succesfull by the result
		return new ILPMinerSolution(new double[0], new double[0], 0);
	}

	protected double[][] getValuess(IloIntVarMap map, IloCplex cp, int expectedSize1stDim, int expectedSize2ndDim)
			throws IloException {
		double[][] list = new double[expectedSize1stDim][expectedSize2ndDim];
		if ((map.getNbDim() == 2) && (map.getSize() == expectedSize1stDim)) {
			for (int i = 0; i < map.getSize(); i++) {
				list[i] = getValues(map.getSub(i), cp, expectedSize2ndDim);
			}
		}
		return list;
	}

	protected double[] getValues(IloIntVarMap map, IloCplex cp, int expectedSize) throws UnknownObjectException,
			IloException {
		double[] list = new double[expectedSize];
		if ((map.getNbDim() == 1) && (map.getSize() == expectedSize)) {
			for (int i = 0; i < map.getSize(); i++) {
				list[i] = cp.getValue(map.get(i));
			}
		}
		return list;
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
