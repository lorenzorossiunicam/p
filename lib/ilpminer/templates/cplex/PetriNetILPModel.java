package org.processmining.plugins.ilpminer.templates.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
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
 * dependency in the log.
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategy(description = "Constructs a Petri Net with the selected search strategy.", name = "Petri Net")
public class PetriNetILPModel extends ILPModelCPLEX {
	protected static PetriNetILPModelSettings strategySettings;
	protected static JComboBox searchTypeBox;

	protected int trans = 0, lang = 0;
	protected int[] aMinusAPrime = {};
	protected int[][] aPrime = {};
	protected ArrayList<Integer> cd = null;
	protected ArrayList<XEventClass> initialPlaces, notInitialPlaces;

	protected boolean initialPlace;

	public PetriNetILPModel(IloOplFactory factory, Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
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
		if ((strategySettings.getSearchType() != SearchType.BASIC) || initialPlace) {
			handler.startElement("CD");
			handler.startTuple();
			handler.addIntItem(cd.get(0));
			handler.addIntItem(cd.get(1));
			handler.endTuple();
			handler.endElement();
		}

		handler.startElement("Solutions");
		handler.startSet();
		for (ILPMinerSolution s : solutions) {
			handler.startTuple();
			handler.startArray();
			for (int i = 0; i < trans; i++) {
				handler.addIntItem(Math.round(s.getInputSet()[i]));
			}
			handler.endArray();
			handler.startArray();
			for (int i = 0; i < trans; i++) {
				handler.addIntItem(Math.round(s.getOutputSet()[i]));
			}
			handler.endArray();
			handler.addIntItem((int) Math.round(s.getTokens()));
			handler.endTuple();
		}
		handler.endSet();
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
	}

	protected void processModel(PluginContext context, IloOplSettings settings, IloCplex cp) {
		if (strategySettings.separateInitialPlaces()) {
			initialPlace = true;
			IloOplModelSource modelSource = factory.createOplModelSourceFromString(getModel(), "ProM6ILPMiner");
			IloOplModelDefinition def = factory.createOplModelDefinition(modelSource, settings);

			processInitialPlaces(context, def, cp);

			modelSource.end();
			def.end();
		}
		initialPlace = false;
		IloOplModelSource modelSource = factory.createOplModelSourceFromString(getModel(), "ProM6ILPMiner");
		IloOplModelDefinition def = factory.createOplModelDefinition(modelSource, settings);

		switch (strategySettings.getSearchType()) {
			case BASIC :
				processBasic(context, def, cp);
				break;
			case PER_TRANSITION :
			case PRE_PER_TRANSITION :
				processTransitions(context, def, cp);
				if (strategySettings.getSearchType() == SearchType.PRE_PER_TRANSITION) {
					break;
				}
			case POST_PER_TRANSITION :
				strategySettings.setSearchType(SearchType.POST_PER_TRANSITION);

				modelSource = factory.createOplModelSourceFromString(getModel(), "ProM6ILPMiner");
				def = factory.createOplModelDefinition(modelSource, settings);

				processTransitions(context, def, cp);
				break;
			default :// PER_CD
				processCausalDependencies(context, def, cp);
				break;
		}

		modelSource.end();
		def.end();
	}

	protected void processInitialPlaces(PluginContext context, IloOplModelDefinition def, IloCplex cp) {
		for (XEventClass c : initialPlaces) {
			if (context.getProgress().isCancelled()) {
				return;
			}
			cd = new ArrayList<Integer>();
			cd.add(-1);
			cd.add(m.get(c));

			solve(context, def, cp);
		}
	}

	protected void processBasic(PluginContext context, IloOplModelDefinition def, IloCplex cp) {
		boolean isOK = true;

		while (isOK && !context.getProgress().isCancelled()) {
			isOK = solve(context, def, cp) != null;
			isOK = isOK && (solutions.size() < Math.pow(r.getEventClasses().size(), 2));
		}
	}

	protected void processTransitions(PluginContext context, IloOplModelDefinition def, IloCplex cp) {
		ArrayList<XEventClass> places = new ArrayList<XEventClass>(r.getEventClasses().getClasses());
		if (strategySettings.separateInitialPlaces()) {
			places = notInitialPlaces;
		}

		int i = 1;
		context.getProgress().setIndeterminate(false);
		context.getProgress().setMaximum(places.size());
		for (XEventClass clazz : places) {
			if (context.getProgress().isCancelled()) {
				return;
			}
			context.getProgress().setValue(i);
			i++;

			cd = new ArrayList<Integer>();
			cd.add(m.get(clazz));
			cd.add(m.get(clazz));

			solve(context, def, cp);
		}
		context.getProgress().setIndeterminate(true);
	}

	protected void processCausalDependencies(PluginContext context, IloOplModelDefinition def, IloCplex cp) {
		int i = 1;
		context.getProgress().setIndeterminate(false);
		context.getProgress().setMaximum(r.getCausalDependencies().size());
		for (Map.Entry<Pair<XEventClass, XEventClass>, Double> entry : r.getCausalDependencies().entrySet()) {
			if (context.getProgress().isCancelled()) {
				return;
			}
			if (entry.getValue() > 0) {
				context.getProgress().setValue(i);
				i++;

				cd = new ArrayList<Integer>();
				cd.add(m.get(entry.getKey().getFirst()));
				cd.add(m.get(entry.getKey().getSecond()));

				solve(context, def, cp);
			}
		}
		context.getProgress().setIndeterminate(true);
	}

	/**
	 * defines the variable types and bounds in the problem
	 * 
	 * @param p
	 *            - problem
	 */
	protected String getVariables() {
		return "range Lang = 0.."
				+ (lang - 1)
				+ ";"
				+ "range Trans = 0.."
				+ (trans - 1)
				+ ";"
				+ "tuple Letter { key int Trans; int Amount; }"
				+ "{Letter} APrime[Lang] = ...;"
				+ "int AMinusAPrime[Lang] = ...;"
				+ (((strategySettings.getSearchType() != SearchType.BASIC) || initialPlace) ? "tuple CausalDependency { int From; int To; } CausalDependency CD = ...;"
						: "") + "tuple Solution { int X[Trans]; int Y[Trans]; int C; } {Solution} Solutions = ...;"
				+ "range Bin = 0..1;" + "dvar int c in Bin;" + "dvar int x[Trans] in Bin;"
				+ "dvar int y[Trans] in Bin;";
	}

	/**
	 * adds an objective to the problem
	 */
	protected String getObjective() {
		return "c + sum(w in Lang) (" + "c + sum(<t,a> in APrime[w]) (" + "a * ( x[t] - y[t] )"
				+ ") + x[AMinusAPrime[w]] - y[AMinusAPrime[w]]" + ");";
	}

	/**
	 * adds the constraints to the problem
	 */
	protected String getConstraints() {
		return "forall(w in Lang)" + "c + sum(<t,a> in APrime[w]) (" + "a * x[t] - a * y[t]"
				+ ") - y[AMinusAPrime[w]] >= 0;" + getPlaceConstraints();
	}

	protected String getPlaceConstraints() {
		if (initialPlace) {
			// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
			// => volgt uit volgende constraint(s)
			return "c == 1;" + "sum(t in Trans) x[t] == 0;" + "y[CD.To] == 1;";
		} else {
			String c = "";
			if (strategySettings.separateInitialPlaces()) {
				c = "c == 0;";
			}
			switch (strategySettings.getSearchType()) {
				case BASIC :
					return c + "sum(t in Trans) (x[t] + y[t]) >= 1;" + "forall(s in Solutions)"
							+ "2 * c * s.C + sum(t in Trans) (2 * x[t] * s.X[t] + 2 * y[t] * s.Y[t]) <"
							+ "c + s.c + sum(t in Trans) (x[t] + s.X[t] + y[t] + s.Y[t]);";
				case PER_TRANSITION :
				case PRE_PER_TRANSITION :
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					return c + "y[CD.To] == 1;";
				case POST_PER_TRANSITION :
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					return c + "x[CD.From] == 1;";
				default : // PER_CD
					// ctAtLeastOneArc: sum(t in Trans) (x[t] + y[t]) >= 1;
					// => volgt uit volgende constraint(s)
					return c + "x[CD.From] == 1;" + "y[CD.To] == 1;";
			}
		}
	}

	/**
	 * converts the CPLEX result in a solution (place representation) and adds
	 * it to the set of found solutions
	 * 
	 * @param opl
	 *            - opl model
	 */
	@SuppressWarnings("unchecked")
	protected ILPMinerSolution addSolution(IloOplModel opl) {
		double c = 0;
		double[] x = new double[trans], y = new double[trans];

		for (Iterator<IloOplElement> it = opl.getElementIterator(); it.hasNext();) {
			IloOplElement e = it.next();
			try {
				if (e.isDecisionVariable()) {
					if ("c".equals(e.getName())) {
						IloIntVar var = e.asIntVar();
						c = opl.getCplex().getValue(var);
					} else if ("x".equals(e.getName())) {
						IloIntVarMap map = e.asIntVarMap();
						x = getValues(map, opl.getCplex(), trans);
					} else if ("y".equals(e.getName())) {
						IloIntVarMap map = e.asIntVarMap();
						y = getValues(map, opl.getCplex(), trans);
					}
				}
			} catch (Exception ex) {
				return null;
			}
		}

		ILPMinerSolution s = new ILPMinerSolution(x, y, c);
		solutions.add(s);
		return s;
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
		searchTypeBox = f.createComboBox(new SearchType[] { SearchType.BASIC, SearchType.PER_CD,
				SearchType.PER_TRANSITION, SearchType.PRE_PER_TRANSITION, SearchType.POST_PER_TRANSITION });
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
