/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.patternmining;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.manifestanalysis.conversion.Manifest2ModelPNRepResult;
import org.processmining.plugins.petrinet.manifestreplayer.conversion.Manifest2PNRepResult;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.visualization.patternmining.PNLogReplayPatternMinePanel;

/**
 * @author aadrians Jun 1, 2012
 * 
 */
@Plugin(name = "xFrequent Movement Sets Mining for Deviation Analysis", returnLabels = { "Visualized as Trace Alignment" }, returnTypes = { JComponent.class }, parameterLabels = { "Manifests" }, userAccessible = false)
@Visualizer
public class ManifestPatternMineVis {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(final PluginContext context, Manifest manifest) {
		System.gc();

		// create pnRep result from manifest
		PNRepResult pnRepResult = Manifest2PNRepResult.convert(manifest);

		// util class
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		// result
		XLog cLog = factory.createLog(manifest.getLog().getAttributes());
		XEventClass dummyEvClass = new XEventClass("DUMMY", -1);
		TransEvClassMapping map = new TransEvClassMapping(manifest.getEvClassifier(), dummyEvClass); // new mapping constructed from manifest
		
		// call method that create logs for PNRepResult
		Manifest2ModelPNRepResult.createLogMapForManifest(manifest, factory, cLog, dummyEvClass, map);

		// visualize it
		return new PNLogReplayPatternMinePanel(context, manifest.getNet(), manifest.getInitMarking(), cLog, map, pnRepResult);
	}
}
