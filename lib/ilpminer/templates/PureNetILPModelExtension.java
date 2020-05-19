package org.processmining.plugins.ilpminer.templates;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;

import org.processmining.plugins.ilpminer.ILPMinerStrategyExtension;
import org.processmining.plugins.ilpminer.ILPMinerStrategyExtensionImpl;
import org.processmining.plugins.ilpminer.ILPModelCPLEX;
import org.processmining.plugins.ilpminer.ILPModelExtension;
import org.processmining.plugins.ilpminer.ILPModelJavaILP;
import org.processmining.plugins.ilpminer.templates.javailp.PetriNetILPModel;
import org.processmining.plugins.ilpminer.templates.javailp.PetriNetSingleILPModel;

/**
 * Extends the ILPModelExtension class. Looks for a net where there are no self
 * loops. (i.e. adding a place )
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategyExtension(description = "No self loops are allowed.", name = "Pure Net")
public class PureNetILPModelExtension extends ILPModelExtension {
	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = PetriNetILPModel.class)
	public void addPetriNetLPSOLVE(Problem p, ILPModelJavaILP m) {
		// forall(t in Trans) ctNoSelfLoop: x[t] + y[t] <= 1;
		for (int t = 0; t < ((PetriNetILPModel) m).trans; t++) {
			Linear l = new Linear();
			l.add(1, "x" + t);
			l.add(1, "y" + t);
			p.add(l, Operator.LE, 1);
		}
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = PetriNetSingleILPModel.class)
	public void addPetriNetSingleLPSOLVE(Problem p, ILPModelJavaILP m) {
		// forall(t in Trans) ctNoSelfLoop: x[t] + y[t] <= 1;
		for (int t = 0; t < ((PetriNetSingleILPModel) m).trans; t++) {
			Linear l = new Linear();
			l.add(1, "x" + ((PetriNetSingleILPModel) m).cdCount + "_" + t);
			l.add(1, "y" + ((PetriNetSingleILPModel) m).cdCount + "_" + t);
			l.add(-((PetriNetSingleILPModel) m).slackWeight, "s" + ((PetriNetSingleILPModel) m).cdCount);
			p.add(l, Operator.LE, 1);
		}
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetILPModel.class)
	public String addPetriNetCPLEX(ILPModelCPLEX m) {
		return "forall(t in Trans) x[t] + y[t] <= 1;";
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetSingleILPModel.class)
	public String addPetriNetSingleCPLEX(ILPModelCPLEX m) {
		return "forall(p in Places) forall(t in Trans) x[p][t] + y[p][t] <= 1 + s[p] * SlackWeight;";
	}
}
