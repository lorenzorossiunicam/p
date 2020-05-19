package org.processmining.plugins.transitionsystem.topetrinet.gui;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.petrify.PetrifyDotG;
import org.processmining.plugins.petrify.PetrifyDotSG;
import org.processmining.plugins.transitionsystem.topetrinet.TS2Petrinet;

public class TS2PetriNetGUIPlugin {
	@Plugin(name = "Genet (k-bounded synthesis/mining) ", parameterLabels = { "genet.sg" }, returnLabels = { "genet.g" }, returnTypes = { PetrifyDotG.class }, userAccessible = true)
	public static PetrifyDotG main(final PluginContext context, PetrifyDotSG dotSG) {
		TS2Petrinet converter = new TS2Petrinet();
		converter.convert(context, dotSG);
		String dotGName = dotSG.getName().replaceAll(".sg", ".g");
		return new PetrifyDotG(dotGName);

	}

}
