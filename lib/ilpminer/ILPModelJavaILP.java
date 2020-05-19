package org.processmining.plugins.ilpminer;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryCPLEX;
import net.sf.javailp.SolverFactoryLpSolve;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverType;
import org.processmining.plugins.log.logabstraction.LogRelations;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * This abstract class provides a model for the ILP problem. It can be
 * overwritten for a specific ILP problem model.
 * 
 * @author T. van der Wiel
 * 
 */
public abstract class ILPModelJavaILP {
	protected Set<ILPMinerSolution> solutions = new HashSet<ILPMinerSolution>();
	protected Class<?>[] extensions;
	protected Map<XEventClass, Integer> m;
	protected PrefixClosedLanguage l;
	protected LogRelations r;
	protected Map<SolverSetting, Object> solverSettings;
	protected SolverFactory factory;

	public ILPModelJavaILP(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings, ILPModelSettings settings) {
		this.extensions = extensions;
		this.solverSettings = solverSettings;
	}

	/**
	 * Generates the model specific data from the generic data
	 * 
	 * @param indices
	 * @param l
	 * @param relations
	 */
	public abstract void makeData();

	/**
	 * Returns the model instantiated via the Java-ILP interface
	 * 
	 * @return problem
	 */
	public abstract Problem getModel();

	/**
	 * Builds the ILP problem and executes it.
	 * 
	 * @param indices
	 *            mapping between eventclasses and integers
	 * @param l
	 *            PFC Language
	 * @param relations
	 * @param context
	 * @throws IOException
	 */
	public void findPetriNetPlaces(Map<XEventClass, Integer> indices, PrefixClosedLanguage pfclang,
			LogRelations relations, PluginContext context) throws IOException {
		solutions = new HashSet<ILPMinerSolution>();
		factory = loadLibraries();
		factory.setParameter(Solver.VERBOSE, 0);
		factory.setParameter(Solver.TIMEOUT, 100);

		m = indices;
		l = pfclang;
		r = relations;
		makeData();

		context.getProgress().setCaption("Searching places...");
		// execute the model using the ILP model variant overwriting this class
		processModel(context, factory);
	}

	/**
	 * Loads the required jar and dll files (from the location) provided by the
	 * user via the settings if not loaded already and creates a solverfactory
	 * 
	 * @return solverfactory
	 * @throws IOException
	 */
	protected SolverFactory loadLibraries() throws IOException {
		SolverFactory factory;
		if (solverSettings.get(SolverSetting.TYPE) == SolverType.JAVAILP_CPLEX) {
			try {
				PackageManager.getInstance().findOrInstallPackages("CPlex");
				System.loadLibrary("cplex121");
				System.loadLibrary("opl63");
				factory = new SolverFactoryCPLEX();
			} catch (Exception e) {
				// resort to Java ILP
			}
		} // JAVAILP_LPSOLVE
		try {
			PackageManager.getInstance().findOrInstallPackages("LpSolve");
			System.loadLibrary("lpsolve55");
			System.loadLibrary("lpsolve55j");
			factory = new SolverFactoryLpSolve();
		} catch (Exception e) {
			throw new IOException("Unable to load required libraries.", e);
		}

		return factory;
	}

	/**
	 * Finds the solutions required for this model
	 * 
	 * @param context
	 * @param factory
	 */
	protected abstract void processModel(PluginContext context, SolverFactory factory);

	/**
	 * solves the model in the modeldefinition with this being the data source
	 * 
	 * @param context
	 */
	protected Result solve(PluginContext context) {
		context.log("Generating Java-ILP model");

		Problem problem = getModel();
		try {
			context.log("Solving...");

			Solver solver = factory.get();
			long solveTime = System.currentTimeMillis();
			Result result = solver.solve(problem);
			context.log("Solving time: " + (System.currentTimeMillis() - solveTime));
			context.log("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
			System.gc();
			return result;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * returns the solutions found with processModel
	 * 
	 * @return Solution list
	 */
	public Set<ILPMinerSolution> getSolutions() {
		return solutions;
	}

	private static ILPMinerStrategy getAnnotation(Class<?> strategy) throws ClassNotFoundException {
		return Class.forName(strategy.getName()).getAnnotation(ILPMinerStrategy.class);
	}

	public static String getName(Class<?> strategy) {
		try {
			return getAnnotation(strategy).name();
		} catch (Exception e) {
			return "[Unnamed strategy]";
		}
	}

	public static String getAuthor(Class<?> strategy) {
		try {
			return getAnnotation(strategy).author();
		} catch (Exception e) {
			return "T. van der Wiel";
		}
	}

	public static String getDescription(Class<?> strategy) {
		try {
			return getAnnotation(strategy).description();
		} catch (Exception e) {
			return "[No description available]";
		}
	}

	/**
	 * Explicitly override this method in each subclass, otherwise it will not
	 * correctly acces the static properties!
	 * 
	 * @param slickerfactory
	 *            to generate a nice layout
	 * @return Model specific settings panel, Empty model specific
	 *         ILPModelSettings object
	 */
	public static Object[] getSettingsGUI(SlickerFactory f, Class<?> strategy) {
		JPanel panel = f.createRoundedPanel();
		panel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } }));
		panel.add(f.createLabel("<html><p>" + getAuthor(strategy) + "</p><p>" + getDescription(strategy)
				+ "</p><h3>ILP Variant Settings</h3><p>There are no custom settings for this ILP variant.</p></html>"),
				"0, 0");
		return new Object[] { panel, null };
	}

	/**
	 * adds all the extensions constraints to the problem via reflection
	 * 
	 * @param problem
	 */
	public void addExtensionConstraints(Problem p) {
		for (Class<?> extension : extensions) {
			Method[] methods = extension.getMethods();
			for (Method m : methods) {
				if (m.isAnnotationPresent(ILPMinerStrategyExtensionImpl.class)) {
					ILPMinerStrategyExtensionImpl a = m.getAnnotation(ILPMinerStrategyExtensionImpl.class);
					if (ILPMinerStrategyManager.isSubclass(this.getClass(), a.ExtensionSuperClass())
							|| (this.getClass() == a.ExtensionSuperClass())) {
						try {
							m.invoke(extension.newInstance(), new Object[] { p, this });
						} catch (Exception e) {
						}
					}
				}
			}
		}
	}
}
