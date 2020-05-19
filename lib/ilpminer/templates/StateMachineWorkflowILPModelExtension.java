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
import org.processmining.plugins.ilpminer.templates.javailp.PetriNetILPModel;

/**
 * Extends the ILPModelExtension class. Looks for a state machine where each
 * transition has at most one input and one ooutput place.
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategyExtension(description = "All transitions have at most one incoming and at most one outgoing arc.", name = "State Machine", ParentExtensionClass = ILPModelExtension.class)
public class StateMachineWorkflowILPModelExtension extends ILPModelExtension {
	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = PetriNetILPModel.class)
	public void addPetriNetLPSOLVE(Problem p, ILPModelJavaILP m) {
		// forall(s in Solutions) ctTransitionAtMostOneIngoingAndOutgoing: sum(t
		// in Trans) s.X[t] * x[t] == 0 && sum(t in Trans) s.Y[t] * y[t] == 0;
		for (ILPMinerSolution s : m.getSolutions()) {
			Linear l = new Linear();
			for (int t = 0; t < ((PetriNetILPModel) m).trans; t++) {
				l.add(s.getInputSet()[t], "x" + t);
				l.add(s.getOutputSet()[t], "y" + t);
			}
			p.add(l, Operator.EQ, 0);
		}
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetILPModel.class)
	public String addPetriNetCPLEX(ILPModelCPLEX m) {
		return "forall(s in Solutions) {" + "sum(t in Trans) s.X[t] * x[t] == 0;"
				+ "sum(t in Trans) s.Y[t] * y[t] == 0;" + "}";
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetSingleILPModel.class)
	public String addPetriNetSingleCPLEX(ILPModelCPLEX m) {
		return "forall(t in Trans) {" + "sum(p in Places) x[p][t] <= 1 + s[p] * SlackWeight;"
				+ "sum(p in Places) y[p][t] <= 1 + s[p] * SlackWeight;" + "}";
	}
}
