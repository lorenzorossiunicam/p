package org.processmining.plugins.ilpminer.templates.javailp;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.javailp.Constraint;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.ilpminer.ILPMinerSolution;
import org.processmining.plugins.ilpminer.ILPModelSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

//@ILPMinerStrategy(author = "J.M.E.M. van der Werf", description = "Constructs a Petri net", name = "JMW's Petri Net Generator")
public class BasicPetriNetILPModel extends PetriNetILPModel {

	@SuppressWarnings("unchecked")
	public BasicPetriNetILPModel(Class[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
	}

	protected ArrayList<Constraint> constraints = new ArrayList<Constraint>();

	/**
	 * This function gets the old model, and adds all extra constraints set by
	 * this miner.
	 */
	public Problem getModel() {

		Problem p = super.getModel();

		for (Constraint c : constraints) {
			p.add(c);
		}

		return p;
	}

	public void clearAdditionalConstraints() {
		constraints.clear();
	}

	protected Constraint addConstraintForMinimalOneArc() {
		Linear l = new Linear();
		for (int t = 0; t < trans; t++) {
			l.add(1, "x" + t);
			l.add(1, "y" + t);
		}

		return new Constraint(l, Operator.GE, 1);
	}

	/**
	 * This function implements the basic algorithm as described in the paper
	 * 
	 * J.M.E.M. van der Werf, B.F. van Dongen, C.A.J. Hurkens and A. Serebrenik,
	 * "Process Discovery Using Integer Linear Programming" Fundamenta
	 * Informatica 94 (3-4) pp 387-412, 2009
	 * 
	 * The algorithm constructs a basic ILP problem. For each solution found, an
	 * extra constraint is added to the ILP.
	 * 
	 * The algorithm stops after the ILP became infeasable or if there are |T|^2
	 * places found, where T is the number of transitions.
	 * 
	 */
	//@SuppressWarnings("unchecked")
	protected void processModel(PluginContext context, SolverFactory factory) {
		context.getProgress().setIndeterminate(false);

		if (generateWorkflow) {
			//set the progress bar
			context.getProgress().setMaximum(maxPlaces + 1);

			//add initial place
			addInitialPlace(factory);

			//increase progress bar.
			context.getProgress().inc();

		} else {

			context.getProgress().setMaximum(maxPlaces);
		}

		setFreeChoiceProperty();

		if (algorithmToUse == 0) {
			runIndependentPlacesAlgorithm(context, factory);
		} else if (algorithmToUse == 1) {
			runCausalDepenciesAlgorithm(context, factory);
		}
	}

	//************************************************************************************
	// ALL FOR FREE CHOICE STUFF
	//************************************************************************************

	protected void setFreeChoiceProperty() {
		generateFreeChoice = false;
		try {
			for (Class<?> c : extensions) {
				if (c.getSimpleName().equals("FreeChoiceExtension")) {
					generateFreeChoice = true;
				}
			}
		} catch (Exception e) {
			generateFreeChoice = false;
		}
	}

	/**
	 * This function checks whether a solution is allowed or not. If the free
	 * choice option is checked, not all solutions are allowed. Solutions that
	 * share an element in the output set, but do not have an identical output
	 * set are put in the "to be removed" solution list. This "to be removed"
	 * list is then returned.
	 * 
	 * @param sol
	 * @return the solutions not allowed for this solution
	 */
	protected ArrayList<ILPMinerSolution> solutionsNotAllowedWithThisOne(ILPMinerSolution sol) {
		ArrayList<ILPMinerSolution> tobeRemoved = new ArrayList<ILPMinerSolution>();

		for (int t = 0; t < trans; t++) {
			if (sol.getOutputSet()[t] > 0) {
				for (ILPMinerSolution s : solutions) {
					if (s.getOutputSet()[t] == sol.getOutputSet()[t]) {
						boolean different = false;
						for (int u = 0; u < trans; u++) {
							if (s.getOutputSet()[u] != sol.getOutputSet()[u]) {
								different = true;
							}
						}
						if (different) {
							tobeRemoved.add(s);
						}
					}
				}
			}
		}

		return tobeRemoved;
	}

	/**
	 * Creates a solution from any result. If result is null, null is returned.
	 * 
	 * @param result
	 */
	protected ILPMinerSolution makeSolution(Result result) {
		if (result != null) {
			return super.makeSolution(result);
		}
		return null;
	}

	/**
	 * Adds a new solution to the list of solutions.
	 * 
	 * @param result
	 * @return
	 * @see BasicPetriNetILPModelTEMP.addNewSolution
	 */
	protected ArrayList<ILPMinerSolution> addNewSolution(Result result) {
		return addNewSolution(makeSolution(result));
	}

	/**
	 * Adds a solution to the list of solutions. If the free choice property is
	 * set, the solutions not allowed by the given solution are removed. The set
	 * of removed solutions is returned.
	 * 
	 * @param sol
	 * @return
	 */
	protected ArrayList<ILPMinerSolution> addNewSolution(ILPMinerSolution sol) {
		ArrayList<ILPMinerSolution> tbr = new ArrayList<ILPMinerSolution>();

		if (sol != null) {
			if (generateFreeChoice) {
				tbr = solutionsNotAllowedWithThisOne(sol);
				for (ILPMinerSolution s : tbr) {
					solutions.remove(s);
				}
			}
			solutions.add(sol);
		}

		return tbr;
	}

	//************************************************************************************
	// ALL FOR WORKFLOW GENERATION
	//************************************************************************************

	/**
	 * This function adds the initial place. It searches for a single place that
	 * is marked, and has as many output arcs as possible.
	 * 
	 * It then sets the constraint for c to be 0 for all other solutions.
	 * 
	 */
	protected void addInitialPlace(SolverFactory factory) {
		Problem problem = getModel();

		Linear linit = new Linear();
		linit.add(1, "c");
		problem.add(new Constraint(linit, Operator.EQ, 1));

		for (int t = 0; t < trans; t++) {
			Linear xs = new Linear();
			xs.add(1, "x" + t);
			problem.add(new Constraint(xs, Operator.EQ, 0));
		}

		Solver solver = factory.get(); // you should use this solver only once for one problem
		Result result = solver.solve(problem);
		addNewSolution(result);

		Linear lcIsZero = new Linear();
		lcIsZero.add(1, "c");
		constraints.add(new Constraint(lcIsZero, Operator.EQ, 0));
	}

	//************************************************************************************
	// ALL FOR CAUSAL DEPENDENCIES
	//************************************************************************************

	/**
	 * This method is the main function for the generation of a Petri net using
	 * the causal dependencies in the log. It places all causal dependencies in
	 * a stack, and tries to find a solution for each causal dependency. In case
	 * a solution is removed, the causal dependency belonging to it is again
	 * added to the stack.
	 */
	protected void runCausalDepenciesAlgorithm(PluginContext context, SolverFactory factory) {
		int i = 0;

		Map<ILPMinerSolution, Pair<XEventClass, XEventClass>> solMapping = new HashMap<ILPMinerSolution, Pair<XEventClass, XEventClass>>();

		context.getProgress().setMaximum(r.getCausalDependencies().size());

		Stack<Pair<XEventClass, XEventClass>> causalDependencies = new Stack<Pair<XEventClass, XEventClass>>();

		for (Map.Entry<Pair<XEventClass, XEventClass>, Double> entry : r.getCausalDependencies().entrySet()) {
			if (entry.getValue() > 0) {
				causalDependencies.add(entry.getKey());
			}
		}

		while (!causalDependencies.empty() && !context.getProgress().isCancelled()) {
			Pair<XEventClass, XEventClass> cd = causalDependencies.pop();

			context.getProgress().setValue(i);
			i++;

			Problem problem = getModel();

			addCausalDependencyConstraint(problem, cd);

			Solver solver = factory.get(); // you should use this solver only once for one problem
			Result result = solver.solve(problem);

			ArrayList<ILPMinerSolution> tbr = addNewSolution(result);
			for (ILPMinerSolution s : tbr) {
				causalDependencies.add(solMapping.get(s));
			}

			System.out.println(result);
			context.getProgress().inc();
		}
		context.getProgress().setIndeterminate(true);
	}

	/**
	 * Adds the constraints of the given causal dependency to the problem
	 * 
	 * @param problem
	 * @param cd
	 */
	protected void addCausalDependencyConstraint(Problem problem, Pair<XEventClass, XEventClass> cd) {
		Linear lx = new Linear();
		Linear ly = new Linear();
		//add constraint for CD
		//the first puts a token in place, hence x
		lx.add(1, "x" + m.get(cd.getFirst()));
		//the second removes a token in place, hence y
		ly.add(1, "y" + m.get(cd.getSecond()));

		//System.out.println("CD: " + cd.getFirst() + " to " +cd.getSecond() );

		problem.add(new Constraint(lx, Operator.EQ, 1));
		problem.add(new Constraint(ly, Operator.EQ, 1));

		Linear l = new Linear();
		l.add(1, "c");
		problem.add(new Constraint(l, Operator.EQ, 0));
	}

	//************************************************************************************
	// ALL FOR INDEPENDENT PLACES
	//************************************************************************************

	/**
	 * Basic algorithm, generates a set of independent places.
	 */
	protected void runIndependentPlacesAlgorithm(PluginContext context, SolverFactory factory) {
		boolean condition = true;
		int nrPlaces = 1;

		// alter problem for settings.
		constraints.add(addConstraintForMinimalOneArc());

		while (condition && !context.getProgress().isCancelled()) {

			context.getProgress().setValue(nrPlaces);
			nrPlaces++;

			Problem problem = getModel();

			Solver solver = factory.get(); // you should use this solver only once for one problem
			Result result = solver.solve(problem);

			if (result == null) {
				condition = false;
				System.out.println("Problem infeasible");
			} else {
				//System.out.println(result);
				addNewSolution(result);

				addConstraintForSolution(result);
				condition = (solutions.size() <= maxPlaces);

				System.out.println(result);
			}

			context.getProgress().inc();
		}
		context.getProgress().setIndeterminate(true);
	}

	/**
	 * Adds the following constraint to problem p, where result = (x_i,y_i,c_i).
	 * 
	 * -c_i \cdot c + y\trans \cdot ( 1 - y_i ) - x\trans x_i \geq -c_i + 1 -
	 * 1\trans\cdot x_i
	 * 
	 * @param result
	 */
	protected void addConstraintForSolution(Result result) {

		Linear l = new Linear();
		l.add(-1 * result.get("c").doubleValue(), "c");

		double val = -1 * result.get("c").doubleValue() + 1;

		for (int t = 0; t < trans; t++) {
			l.add(-1 * result.get("x" + t).doubleValue(), "x" + t);
			l.add(1 - result.get("y" + t).doubleValue(), "y" + t);

			val -= result.get("x" + t).doubleValue();
		}

		constraints.add(new Constraint(l, Operator.GE, val));
	}

	//************************************************************************************
	// SETTINGS THAT CAN BE SET BY USER INTERFACE
	//************************************************************************************

	protected static boolean generateWorkflow = false;
	protected static int maxPlaces = 10;
	protected static boolean generateFreeChoice = false;

	//************************************************************************************
	// ALL ABOUT THE USER INTERFACE
	//************************************************************************************

	// 0 -> independent Places
	// 1 -> causal dependencies
	protected static int algorithmToUse = 0;

	protected static void setAlgorithmToUse(String algo) {
		if (algo.equals("causaldependencies")) {
			algorithmToUse = 1;
		} else {
			//algo.equals("independentplaces")
			algorithmToUse = 0;
		}
	}

	@SuppressWarnings("unchecked")
	public static Object[] getSettingsGUI(SlickerFactory f, Class strategy) {
		JPanel panel = f.createRoundedPanel();
		panel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
				{ 50, TableLayoutConstants.FILL } }));
		panel.add(f.createLabel("<html><p>" + getAuthor(strategy) + "</p><p>" + getDescription(strategy)
				+ "</p></html>"), "0, 0");
		JPanel optionPanel = f.createRoundedPanel();

		optionPanel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
		// wf, >|P|, algo 1, algo 2
				{ 50, 50, 25, 25, TableLayoutConstants.FILL } }));

		JCheckBox jWorkflow = f.createCheckBox("Single initial marked place", generateWorkflow);
		jWorkflow.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				generateWorkflow = (e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		optionPanel.add(jWorkflow, "0, 0");

		final NiceIntegerSlider jmaxPlaces = f.createNiceIntegerSlider("Max Places", 0, 50, maxPlaces,
				Orientation.HORIZONTAL);
		jmaxPlaces.setToolTipText("Max places: " + maxPlaces);
		jmaxPlaces.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				//JSlider source = (JSlider) e.getSource();
				maxPlaces = jmaxPlaces.getValue();
				jmaxPlaces.setToolTipText("Max places: " + maxPlaces);
			}
		});

		optionPanel.add(jmaxPlaces, "0, 1");

		JRadioButton indepAlgo = f.createRadioButton("Independent places");
		indepAlgo.setActionCommand("independentplaces");
		indepAlgo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BasicPetriNetILPModel.setAlgorithmToUse(e.getActionCommand());
			}
		});
		indepAlgo.setSelected(true);

		JRadioButton causalAlgo = f.createRadioButton("Use causal dependencies");
		causalAlgo.setActionCommand("causaldependencies");
		causalAlgo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BasicPetriNetILPModel.setAlgorithmToUse(e.getActionCommand());
			}
		});
		ButtonGroup algogroup = new ButtonGroup();
		algogroup.add(indepAlgo);
		algogroup.add(causalAlgo);

		optionPanel.add(indepAlgo, "0, 2");
		optionPanel.add(causalAlgo, "0, 3");

		panel.add(optionPanel, "0, 1");
		return new Object[] { panel, null };
	}

}