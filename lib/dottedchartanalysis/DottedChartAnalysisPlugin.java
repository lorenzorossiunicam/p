package org.processmining.plugins.dottedchartanalysis;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;

public class DottedChartAnalysisPlugin {

	@UITopiaVariant(uiLabel = "Analyze using Dotted Chart", affiliation = UITopiaVariant.EHV, author = "M.S. Song", email = "minseok.song@gmail.com", website = "http://is.tm.tue.nl/staff/msong/Site/")
	@Plugin(name = "Dotted Chart Analysis", parameterLabels = { "log" }, returnLabels = { "Dotted Chart Analysis" }, returnTypes = { DottedChartModel.class }, userAccessible = true, help = "Dotted chart analysis")
	public static DottedChartModel helloWorld(PluginContext context, XLog log) {
		//P.J. van Heumen: Do not create the DottedChartAnalysis immediately, since it also generates a GUI.
		//Generating the dotted chart model is enough. The user interface should only be generated at visualization time.
		if (log.isEmpty()) {
			JOptionPane.showMessageDialog(null, "The provided log contains no traces. No dotted chart is created.");
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return new DottedChartModel(context, log);
	}

}
