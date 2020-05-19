package org.processmining.plugins.etm.fitness.metrics;

import java.util.List;

import org.processmining.plugins.etm.fitness.FitnessAnnotation;
import org.processmining.plugins.etm.fitness.TreeFitnessAbstract;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo.Dimension;
import org.processmining.plugins.etm.model.narytree.NAryTree;

@FitnessAnnotation
//FIXME check all class contents
//FIXME Test Class thoroughly
public class SimplicityTreeSize extends TreeFitnessAbstract {

	public static final TreeFitnessInfo info = new TreeFitnessInfo(SimplicityTreeSize.class, "Ss",
			"Simplicity - absolute tree size", "Returns the size of the tree.", Dimension.SIMPLICITY, false);

	public double getFitness(NAryTree candidate, List<? extends NAryTree> population) {
		return candidate.size();
	}

	public TreeFitnessInfo getInfo() {
		return info;
	}
}
