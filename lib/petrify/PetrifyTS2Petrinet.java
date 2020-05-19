package org.processmining.plugins.petrify;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.OsUtil;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;

public class PetrifyTS2Petrinet {

//	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
//	@Plugin(name = "Transition System to Petrinet (Petrify)", parameterLabels = "Transition System", returnTypes = {
//			Petrinet.class, Marking.class }, returnLabels = { "Petrinet", "Marking" }, userAccessible = true, help = "", mostSignificantResult = 1)
	public Object[] convertToPetrinet(PluginContext context, TransitionSystem ts) throws Exception {

		if (OsUtil.isRunningWindows()) {
			// Assume Petrify Installed
			try {
				System.loadLibrary("petrify");
				System.loadLibrary("cygwin1");
			} catch (Exception e) {
				throw new Exception("Cannot load Petrify", e);
			}
		} else if (OsUtil.isRunningLinux()) {
			// Linux case
		} else if (OsUtil.isRunningMacOsX()) {
			// Macintosh case
		}

		return null;
	}

}
