package org.processmining.plugins.etm.mutation;

// =============================================================================
// Copyright 2006-2010 Daniel W. Dyer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// =============================================================================

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionaryOperator;

/**
 * Mutation operator for the trees of {@link Node}s.
 * 
 * @author jbuijs
 */
//FIXME check all class contents
//FIXME Test Class thoroughly
public class GuidedTreeMutationCoordinator implements EvolutionaryOperator<NAryTree> {
	private LinkedHashMap<TreeMutationAbstract, Double> smartMutators;
	private double totalChanceSmart = 0;
	private double chanceOfRandomMutation;
	private TreeMutationCoordinator dumbCoordinator;
	private boolean preventDuplicates;
	private CentralRegistry registry;

	/**
	 * Instantiates a new Guided mutation coordinator that selects trees to
	 * mutate and applies one of a given set of (weighted) mutators. Also
	 * distinguishes between guided mutators and random/stupid mutators.
	 * 
	 * @param registry
	 *            The central registry to use for general information
	 * @param chanceOfRandomMutation
	 *            Chance of applying one of the random mutators
	 * @param preventDuplicates
	 *            Boolean that indicates whether to keep mutating until a tree
	 *            is discovered that is not already in the current population
	 *            (TRUE) or whether duplicate trees are allowed (FALSE)
	 * @param smartMutators
	 *            Map with smart/guided mutators and their relative
	 *            weight/chance of application
	 * @param dumbCoordinator
	 *            Map with dumb/random mutators and their relative weight/chance
	 *            of application
	 */
	public GuidedTreeMutationCoordinator(CentralRegistry registry, double chanceOfRandomMutation,
			boolean preventDuplicates, LinkedHashMap<TreeMutationAbstract, Double> smartMutators,
			TreeMutationCoordinator dumbCoordinator) {
		this.registry = registry;
		this.chanceOfRandomMutation = chanceOfRandomMutation;
		this.preventDuplicates = preventDuplicates;
		this.smartMutators = smartMutators;
		this.dumbCoordinator = dumbCoordinator;
		calculateTotalChance();
	}

	/**
	 * Applies mutation functions to the tree, depending on the tree's fitness
	 * characteristics and the provided probabilities
	 */
	public List<NAryTree> apply(List<NAryTree> selectedCandidates, Random rng) {
		List<NAryTree> mutatedPopulation = new ArrayList<NAryTree>(selectedCandidates.size());

		for (NAryTree tree : selectedCandidates) {
			//First check if the fitness is set, if so, try to do something guided
			NAryTree mutatedTree = null;
			int nrTries = TreeMutationAbstract.MAXTRIES;
			if (registry.isFitnessKnown(tree)) {
				mutatedTree = apply(tree, rng);

				/*
				 * If we don't allow duplicates (e.g. first part is not true)
				 * then we continue applying until we find a tree that is not
				 * already in the mutated population
				 */
				do {
					mutatedTree = apply(tree, rng);
					nrTries--;
				} while (preventDuplicates && mutatedPopulation.contains(mutatedTree) && nrTries > 0);
			}

			//If the mutated tree is null we did not do any guided mutation
			//If the nrTries == 0 we tried guided but it failed, ergo do the dumb thing
			if (mutatedTree == null || nrTries == 0) {
				nrTries = TreeMutationAbstract.MAXTRIES;
				do {
					//apply mutation
					mutatedTree = dumbCoordinator.apply(tree, rng);
					nrTries--;
					/*
					 * And keep on trying if we don't allow duplicates but have
					 * a duplicate and did not try the max. nr. of times
					 */
				} while (preventDuplicates && mutatedPopulation.contains(mutatedTree) && nrTries > 0);
			}//end else !fitnessSet

			assert mutatedTree.isConsistent();

			//And add the mutated tree
			mutatedPopulation.add(mutatedTree);
		}

		return mutatedPopulation;
	}

	/**
	 * Applies mutation functions to the tree, depending on the tree's fitness
	 * characteristics and the provided probabilities
	 */
	public NAryTree apply(NAryTree tree, Random rng) {
		if (rng.nextDouble() > chanceOfRandomMutation) {
			//DO A SMART MUTATION
			NAryTree mutatedTree;
			boolean changed = false;
			int nrTries = 0;

			do {
				TreeMutationAbstract mutator = getSmartMutatorForChance(rng.nextDouble() * totalChanceSmart);
				//TODO remove debug code
				try {
					mutatedTree = mutator.mutate(tree);
				} catch (Exception e) {
					System.out.println("EXCEPTION at one of the guided mutators");
					//Break point here so we can investigate the cause of the error, by doing it again :)
					mutatedTree = mutator.mutate(tree);
				}

				changed = mutator.changedAtLastCall();
				nrTries++;

				mutatedTree.isConsistent();

				if (nrTries > 2 && changed == false) {
					//Going dumb... We tried to be smart long enough
					return dumbCoordinator.apply(tree, rng);
				}
			} while (!changed);

			assert mutatedTree.isConsistent();

			return mutatedTree;
		} else {
			//DELEGATE TO DUMB COORDINATOR
			return dumbCoordinator.apply(tree, rng);
		}

	}

	public LinkedHashMap<TreeMutationAbstract, Double> getSmartMutators() {
		return smartMutators;
	}

	private void calculateTotalChance() {
		totalChanceSmart = 0;
		for (Double weight : smartMutators.values()) {
			totalChanceSmart += weight;
		}
	}

	private TreeMutationAbstract getSmartMutatorForChance(double chance) {
		if (smartMutators.size() == 1)
			return smartMutators.keySet().iterator().next();

		double chanceSoFar = 0;
		for (Map.Entry<TreeMutationAbstract, Double> entry : smartMutators.entrySet()) {
			chanceSoFar += entry.getValue();
			if (chance <= chanceSoFar) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void addSmartMutator(TreeMutationAbstract mutator, Double chance) {
		smartMutators.put(mutator, chance);
		calculateTotalChance();
	}

	public boolean isPreventDuplicates() {
		return preventDuplicates;
	}

	public void setPreventDuplicates(boolean allowDuplicates) {
		this.preventDuplicates = allowDuplicates;
	}

}
