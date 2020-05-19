package org.processmining.plugins.ilpminer.templates;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;

import org.processmining.plugins.ilpminer.ILPMinerSolution;
import org.processmining.plugins.ilpminer.ILPMinerStrategyExtension;
import org.processmining.plugins.ilpminer.ILPMinerStrategyExtensionImpl;
import org.processmining.plugins.ilpminer.ILPModelCPLEX;
import org.processmining.plugins.ilpminer.ILPModelExtension;
import org.processmining.plugins.ilpminer.ILPModelJavaILP;
import org.processmining.plugins.ilpminer.templates.javailp.BasicPetriNetILPModel;

@ILPMinerStrategyExtension(author = "J.M.E.M. van der Werf", description = "Generates a Free Choice Petri net", name = "Free Choice", ParentExtensionClass = ILPModelExtension.class)
public class FreeChoiceILPModelExtension extends ILPModelExtension {
	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = BasicPetriNetILPModel.class)
	public void addBasicPetriNetLPSOLVE(Problem p, ILPModelJavaILP m) {
		//System.out.println("I have " + p.getVariablesCount() + " variables");
		int trans = (p.getVariablesCount() - 1) / 2;

		for (ILPMinerSolution s : m.getSolutions()) {
			for (int t1 = 0; t1 < trans; t1++) {
				for (int t2 = 0; t2 < trans; t2++) {
					if ((s.getOutputSet()[t1] == 1) && (s.getOutputSet()[t2] == 1)) {
						Linear l = new Linear();
						l.add(1, "y" + t1);
						l.add(-1, "y" + t2);
						p.add(l, Operator.EQ, 0);
					}
				}
			}
		}
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetSingleILPModel.class)
	public String addPetriNetSingleCPLEX(ILPModelCPLEX m) {
		// TODO: Very inefficient; don't use ||
		return "forall(t1 in Trans) forall(t2 in Trans) forall(p1 in Places) ctSharedInputImpliesEqualInput: x[p1][t1] == 0 || x[p1][t2] == 0 || sum(p2 in Places) ( x[p2][t1] != x[p2][t2] ) == 0 || s[p1] != 0;";
	}
}
