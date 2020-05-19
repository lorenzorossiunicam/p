package org.processmining.plugins.ilpminer.templates.javailp;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;

import org.processmining.plugins.ilpminer.ILPMinerStrategy;
import org.processmining.plugins.ilpminer.ILPModelSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.ilpminer.templates.PetriNetILPModelSettings;
import org.processmining.plugins.ilpminer.templates.PetriNetVariableFitnessILPModelSettings;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Implements the abstract ILPModel class. Looks for a place for each causal
 * dependency in the log.
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategy(description = "Constructs a Petri net that has a fitness higher or equal than the user specified value.", name = "Petri Net (Variable Fitness)")
public class PetriNetVariableFitnessILPModel extends PetriNetILPModel {
	protected static PetriNetVariableFitnessILPModelSettings fitness;
	protected static NiceDoubleSlider fitnessSlider;
	protected Random rand = new Random(7386496835173068365L);
	protected ArrayList<Integer> usedlang;

	public PetriNetVariableFitnessILPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
		fitness = (PetriNetVariableFitnessILPModelSettings) settings;
	}

	public void makeData() {
		super.makeData();
		generateUsedLang();
	}

	protected void generateUsedLang() {
		ArrayList<Integer> available = new ArrayList<Integer>();
		for (int i = 0; i < lang; i++) {
			available.add(i);
		}
		int total = 0;
		usedlang = new ArrayList<Integer>();
		while (total < fitness.getFitness() * l.getWeightsTotal()) {
			int x = rand.nextInt(available.size());
			usedlang.add(available.get(x));
			total += l.getWeight(available.get(x));
			available.remove(x);
		}
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
		for (int w : usedlang) {
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
			p.add(l, Operator.GE, 0);
		}

		addPlaceConstraints(p);
	}

	public static Object[] getSettingsGUI(SlickerFactory f, Class<?> strategy) {
		Object[] superGUI = PetriNetILPModel.getSettingsGUI(f, strategy);

		JPanel panel = f.createRoundedPanel();
		fitnessSlider = f.createNiceDoubleSlider("<html>Minimal fitness (<i>p<sub>SE</sub></i>):</html>", 0, 1, 1,
				Orientation.HORIZONTAL);
		fitnessSlider.setPreferredSize(new Dimension(350, 20));
		fitness = PetriNetVariableFitnessILPModelSettings.fromPetriNetILPModelSettings(
				(PetriNetILPModelSettings) superGUI[1], fitnessSlider.getValue());
		fitnessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				fitness.setFitness(fitnessSlider.getValue());
			}
		});
		panel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, 30} }));
		panel.add((Component) superGUI[0], "0, 0");
		panel.add(fitnessSlider, "0, 1");
		return new Object[] { panel, fitness };
	}
}
