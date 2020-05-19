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
 * Extends the ILPModelExtension class. Looks for a pure net where a place can
 * always contain at most one token. (i.e. adding a place )
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategyExtension(description = "Guarantees that a place can never contain more then one token.", name = "Elementary Net", ParentExtensionClass = PureNetILPModelExtension.class)
public class ElementaryNetILPModelExtension extends ILPModelExtension {
	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = PetriNetILPModel.class)
	public void addPetriNetLPSOLVE(Problem p, ILPModelJavaILP m) {
		for (int w = 0; w < ((PetriNetILPModel) m).lang; w++) {
			Linear l = new Linear();
			l.add(1, "c");
			for (int t = 0; t < ((PetriNetILPModel) m).trans; t++) {
				l.add(((PetriNetILPModel) m).a[w][t], "x" + t);
				l.add(-((PetriNetILPModel) m).a[w][t], "y" + t);
			}
			p.add(l, Operator.LE, 1);
		}
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = PetriNetSingleILPModel.class)
	public void addPetriNetSingleLPSOLVE(Problem p, ILPModelJavaILP m) {
		for (int w = 0; w < ((PetriNetSingleILPModel) m).lang; w++) {
			Linear l = new Linear();
			l.add(1, "c" + ((PetriNetSingleILPModel) m).cdCount);
			for (int t = 0; t < ((PetriNetSingleILPModel) m).trans; t++) {
				l.add(((PetriNetSingleILPModel) m).a[w][t], "x" + ((PetriNetSingleILPModel) m).cdCount + "_" + t);
				l.add(-((PetriNetSingleILPModel) m).a[w][t], "y" + ((PetriNetSingleILPModel) m).cdCount + "_" + t);
			}
			l.add(-((PetriNetSingleILPModel) m).slackWeight, "s" + ((PetriNetSingleILPModel) m).cdCount);
			p.add(l, Operator.LE, 1);
		}
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetILPModel.class)
	public String addPetriNetCPLEX(ILPModelCPLEX m) {
		return "c + sum(w in Lang) ( sum(<t,a> in APrime[w]) ( a * (x[t] - y[t])) + x[AMinusAPrime[w]] - y[AMinusAPrime[w]] ) <= 1;";
	}

	@ILPMinerStrategyExtensionImpl(ExtensionSuperClass = org.processmining.plugins.ilpminer.templates.cplex.PetriNetSingleILPModel.class)
	public String addPetriNetSingleCPLEX(ILPModelCPLEX m) {
		return "forall(p in Places) forall(w in Lang) c[p] + sum(<t,a> in APrime[w]) ( a * (x[p][t] - y[p][t])) + x[p][AMinusAPrime[w]] - y[p][AMinusAPrime[w]] <= 1 + s[p] * SlackWeight;";
	}
}