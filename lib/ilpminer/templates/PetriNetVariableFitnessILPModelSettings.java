package org.processmining.plugins.ilpminer.templates;

/**
 * Provides a storage for the petri net with variable fitness ILP model variant
 * settings
 * 
 * @author T. van der Wiel
 * 
 */
public class PetriNetVariableFitnessILPModelSettings extends PetriNetILPModelSettings {
	protected double fitness = 0;

	public PetriNetVariableFitnessILPModelSettings() {
	}

	public PetriNetVariableFitnessILPModelSettings(double fitness) {
		this.fitness = fitness;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public static PetriNetVariableFitnessILPModelSettings fromPetriNetILPModelSettings(
			PetriNetILPModelSettings petriNetILPModelSettings, double fitness) {
		PetriNetVariableFitnessILPModelSettings s = new PetriNetVariableFitnessILPModelSettings(fitness);
		s.setSearchType(petriNetILPModelSettings.getSearchType());
		s.setSeparateInitialPlaces(petriNetILPModelSettings.separateInitialPlaces());
		return s;
	}
}
