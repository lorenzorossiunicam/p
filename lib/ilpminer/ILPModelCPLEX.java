package org.processmining.plugins.ilpminer;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.opl.IloCustomOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplSettings;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.log.logabstraction.LogRelations;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * This abstract class provides a model for the ILP problem. It can be
 * overwritten for a specific ILP problem model.
 * 
 * @author T. van der Wiel
 * 
 */
public abstract class ILPModelCPLEX extends IloCustomOplDataSource {
	protected Set<ILPMinerSolution> solutions = new HashSet<ILPMinerSolution>();
	protected Class<?>[] extensions;
	protected Map<XEventClass, Integer> m;
	protected PrefixClosedLanguage l;
	protected LogRelations r;
	protected Map<SolverSetting, Object> solverSettings;
	protected IloOplFactory factory;

	public ILPModelCPLEX(IloOplFactory factory, Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(factory);
		this.factory = factory;
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
	 * Builds the ILP problem and executes it.
	 * 
	 * @param indices
	 *            - mapping between eventclasses and integers
	 * @param l
	 *            - PFC Language
	 * @param relations
	 * @param context
	 * @throws IOException
	 * @throws IloException
	 */
	public void findPetriNetPlaces(Map<XEventClass, Integer> indices, PrefixClosedLanguage pfclang,
			LogRelations relations, PluginContext context) throws IOException, IloException {
		System.out.println("Start memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		solutions = new HashSet<ILPMinerSolution>();

		m = indices;
		l = pfclang;
		r = relations;
		makeData();

		context.getProgress().setCaption("Searching places...");
		// execute the model using the ILP model variant overwriting this class

		IloCplex cp = factory.createCplex();
		IloOplErrorHandler errHandler = factory.createOplErrorHandler();
		IloOplSettings settings = factory.createOplSettings(errHandler);
		cp.setOut(null);

		processModel(context, settings, cp);

		errHandler.end();
		settings.end();
		cp.end();
	}

	public abstract String getModel();

	/**
	 * Loads the required jar and dll files (from the location) provided by the
	 * user via the settings if not loaded already and creates a solverfactory
	 * 
	 * @return solverfactory
	 * @throws IOException
	 */
	public static void loadLibraries() throws IOException {
		try {
			PackageManager.getInstance().findOrInstallPackages("CPlex");
			System.loadLibrary("cplex121");
			System.loadLibrary("opl63");
		} catch (Exception e) {
			throw new IOException("Unable to load required libraries.");
		}
	}

	/**
	 * Finds the solutions required for this model
	 * 
	 * @param context
	 * @param settings
	 * @param cp
	 */
	protected abstract void processModel(PluginContext context, IloOplSettings settings, IloCplex cp);

	/**
	 * solves the model in the modeldefinition with this being the data source
	 * 
	 * @param context
	 * @param def
	 *            - opl model definition
	 * @param cp
	 */
	protected ILPMinerSolution solve(PluginContext context, IloOplModelDefinition def, IloCplex cp) {
		context.log("Generating CPLEX model");
		IloOplModel opl = factory.createOplModel(def, cp);
		opl.addDataSource(this);
		opl.generate();

		try {
			context.log("Solving...");
			long solveTime = System.currentTimeMillis();

			if (cp.solve()) {
				System.out.println("Solving time: " + (System.currentTimeMillis() - solveTime));
				System.out.println("Memory: "
						+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
				opl.postProcess();
				ILPMinerSolution sol = addSolution(opl);
				opl.end();
				System.gc();
				return sol;
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * converts the CPLEX result in a solution (place representation) and adds
	 * it to the set of found solutions
	 * 
	 * @param opl
	 *            - opl model
	 */
	protected abstract ILPMinerSolution addSolution(IloOplModel opl);

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
		panel
				.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
						{ TableLayoutConstants.FILL } }));
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
	public String getExtensionConstraints() {
		String constraints = "";
		for (Class<?> extension : extensions) {
			Method[] methods = extension.getMethods();
			for (Method m : methods) {
				if (m.isAnnotationPresent(ILPMinerStrategyExtensionImpl.class)) {
					ILPMinerStrategyExtensionImpl a = m.getAnnotation(ILPMinerStrategyExtensionImpl.class);
					if (ILPMinerStrategyManager.isSubclass(this.getClass(), a.ExtensionSuperClass())
							|| (this.getClass() == a.ExtensionSuperClass())) {
						try {
							constraints += (String) m.invoke(extension.newInstance(), new Object[] { this });
						} catch (Exception e) {
						}
					}
				}
			}
		}
		return constraints;
	}
}
