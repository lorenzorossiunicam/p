package org.processmining.plugins.etm.tests;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.etm.ETMPareto;
import org.processmining.plugins.etm.experiments.StandardLogs;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.serialization.ParetoFrontExport;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.parameters.ETMParamPareto;

public class MinimalExample {

	/**
	 * Minimal code to run the ETMb to discover a Pareto front
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		//We instantiate one of our default event logs, an external event log can also be loaded.
		XLog eventlog = StandardLogs.createDefaultLogWithNoise();

		//Initialize all parameters:
		int popSize = 100; //population size
		int eliteSize = 20; //elite size
		int nrRandomTrees = 2; //nr of random tree to create each generation
		double crossOverChance = 0.1; //chance applying crossover
		double chanceOfRandomMutation = 0.5; //change of applying a random mutation operator
		boolean preventDuplicates = true; //prevent duplicate process trees within a population (after change operations are applied)
		int maxGen = 10000; //maximum number of generation to run
		double targetFitness = 1; //target fitness to stop at when reached
		double frWeight = 10; //weight for replay fitness
		double maxF = 0.6; //stop alignment calculation for trees with a value below 0.6
		double maxFTime = 10; //allow maximum 10 seconds per trace alignment
		double peWeight = 5; //weight for precision
		double geWeight = 0.1; //weight for generalization
		double suWeight = 1; //weight for simplicity
		//the first null parameter is a ProM context, which does not need to be provided
		//the second null parameter is an array of seed process trees, which we do not provide here
		//the last `0' is the similarity weight
		ETMParamPareto etmParam = ETMParamFactory.buildETMParamPareto(eventlog, null, popSize, eliteSize,
				nrRandomTrees, crossOverChance, chanceOfRandomMutation, preventDuplicates, maxGen, targetFitness,
				frWeight, maxF, maxFTime, peWeight, geWeight, suWeight, null, 0);

		ETMPareto etm = new ETMPareto(etmParam); //Instantiate the ETM algorithm
		etm.run(); //Now actually run the ETM, this might take a while

		//Extract the resulting Pareto front
		ParetoFront paretoFront = etm.getResult();

		System.out.println("We have discovered a Pareto front of size " + paretoFront.size()); //output the size
		ParetoFrontExport.export(paretoFront, new File("myParetoFront.PTPareto")); //and write to file
	}

}
