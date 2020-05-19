package org.processmining.plugins.ilpminer.fitness;

import java.util.Collection;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.ilpminer.ILPMinerSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverType;
import org.processmining.plugins.ilpminer.templates.PetriNetILPModelSettings;
import org.processmining.plugins.ilpminer.templates.PetriNetVariableFitnessILPModelSettings;
import org.processmining.plugins.ilpminer.templates.PetriNetILPModelSettings.SearchType;
import org.processmining.plugins.ilpminer.templates.javailp.PetriNetVariableFitnessILPModel;

//@Plugin(name = "tenfold", parameterLabels = { "Log 1.0", "Log 1.1", "Log 1.2", "Log 1.3", "Log 1.4", "Log 1.5", "Log 1.6", "Log 1.7", "Log 1.8", "Log 1.9" }, returnLabels = { "Fitness values" }, returnTypes = { ILPFitnessResult[][].class })
public class tenfoldCrossValidation {
	XLog[] logs;
	ILPFitnessResult[][] fres;
	
//	@UITopiaVariant(uiLabel = "10fold Cross Validation", affiliation = UITopiaVariant.EHV, author = "T. van der Wiel", email = "t.v.d.wiel@student.tue.nl")
//	@PluginVariant(variantLabel = "10fold Cross Validation", requiredParameterLabels = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })
	public ILPFitnessResult[][] doTransitionFitness(UIPluginContext context,
			XLog log10, XLog log11, XLog log12, XLog log13, XLog log14, XLog log15, XLog log16, XLog log17, XLog log18, XLog log19 ) {
		logs = new XLog[] {log10,log11,log12,log13,log14,log15,log16,log17,log18,log19};
		fres = new ILPFitnessResult[10][10];

		Collection<Pair<Integer, PluginParameterBinding>> plugins = context.getPluginManager().find(Plugin.class, Object[].class, context.getPluginContextType(), true, false, false, XLog.class, XLogInfo.class, ILPMinerSettings.class);
		Pair<Integer, PluginParameterBinding> plugin = plugins.iterator().next();

		plugins = context.getPluginManager().find(Plugin.class, ILPFitnessResult.class, context.getPluginContextType(), true, false, false, XLog.class, Petrinet.class);
		Pair<Integer, PluginParameterBinding> plugin2 = plugins.iterator().next();

		ILPMinerSettings settings = new ILPMinerSettings();
		settings.setSolverSetting(SolverSetting.TYPE, SolverType.JAVAILP_CPLEX);
		settings.setSolverSetting(SolverSetting.LICENSE_DIR, "");
		settings.setVariant(PetriNetVariableFitnessILPModel.class);
		settings.setModelSettings(PetriNetVariableFitnessILPModelSettings.fromPetriNetILPModelSettings(new PetriNetILPModelSettings(SearchType.PER_CD, true), 1));
		for(int j = 0; j < 10; j++){
			XLog log = combineLogs(j);
			for(double k = 1; k > 0.1; k -= 0.1) {
				((PetriNetVariableFitnessILPModelSettings)settings.getModelSettings()).setFitness(k);
				try {
					UIPluginContext c = context.createChildContext("ten fold crap");
					context.getPluginLifeCycleEventListeners().firePluginCreated(c);
					PluginExecutionResult pluginResult = plugin.getSecond().invoke(c, log, XLogInfoFactory.createLogInfo(log), settings);
					pluginResult.synchronize();
					Petrinet result = pluginResult.<Petrinet> getResult(plugin.getFirst());
					context.deleteChild(c);

					c = context.createChildContext("ten fold crap");
					context.getPluginLifeCycleEventListeners().firePluginCreated(c);
					pluginResult = plugin2.getSecond().invoke(c, log, result);
					pluginResult.synchronize();
					fres[j][(int) (10*k)-1] = pluginResult.<ILPFitnessResult> getResult(plugin2.getFirst());
					context.deleteChild(c);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
		return fres;
	}

	private XLog combineLogs(int skip) {
		XLog log = (XLog) logs[0].clone();
		log.clear();
		for(int i = 0; i < logs.length; i++) {
			if(i != skip) {
				log.addAll(logs[i]);
			}
		}
		return log;
	}
}
