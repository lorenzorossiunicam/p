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
 * Extends the ILPModelExtension class. Looks for a marked graph where a place
 * has at most one incoming and one outgoing arc. (i.e. adding a place )
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategyExtension(description = "All places have at most one incoming and at most one outgoing arc.", name = "Marked Graph", ParentExtensionClass = ILPModelExtension.class)
public class MarkedGraphILPModelExtension extends ILPModelExtension {
	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = PetriNetILPModel.class)
	public void addPetriNetLPSOLVE(Problem p, ILPModelJavaILP m) {
		// ctPlaceAtMostOneIngoingAndOutgoing: sum(t in Trans) x[t] <= 1 &&
		// sum(t in Trans) y[t] <= 1;
		Linear l1 = new Linear(), l2 = new Linear();
		for (int t = 0; t < ((PetriNetILPModel) m).trans; t++) {
			l1.add(1, "x" + t);
			l2.add(1, "y" + t);
		}
		p.add(l1, Operator.LE, 1);
		p.add(l2, Operator.LE, 1);
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = PetriNetSingleILPModel.class)
	public void addPetriNetSingleLPSOLVE(Problem p, ILPModelJavaILP m) {
		// ctPlaceAtMostOneIngoingAndOutgoing: sum(t in Trans) x[t] <= 1 &&
		// sum(t in Trans) y[t] <= 1;
		Linear l1 = new Linear(), l2 = new Linear();
		for (int t = 0; t < ((PetriNetSingleILPModel) m).trans; t++) {
			l1.add(1, "x" + ((PetriNetSingleILPModel) m).cdCount + "_" + t);
			l2.add(1, "y" + ((PetriNetSingleILPModel) m).cdCount + "_" + t);
		}
		l1.add(-((PetriNetSingleILPModel) m).slackWeight, "s" + ((PetriNetSingleILPModel) m).cdCount);
		l2.add(-((PetriNetSingleILPModel) m).slackWeight, "s" + ((PetriNetSingleILPModel) m).cdCount);
		p.add(l1, Operator.LE, 1);
		p.add(l2, Operator.LE, 1);
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetILPModel.class)
	public String addPetriNetCPLEX(ILPModelCPLEX m) {
		return "sum(t in Trans) x[t] <= 1;" + "sum(t in Trans) y[t] <= 1;";
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetSingleILPModel.class)
	public String addPetriNetSingleCPLEX(ILPModelCPLEX m) {
		return "forall(p in Places) {" + "sum(t in Trans) x[p][t] <= 1 + s[p] * SlackWeight;"
				+ "sum(t in Trans) y[p][t] <= 1 + s[p] * SlackWeight;" + "}";
	}
}
