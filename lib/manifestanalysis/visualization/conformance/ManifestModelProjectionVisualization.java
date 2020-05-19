package org.processmining.plugins.manifestanalysis.visualization.conformance;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.manifestanalysis.conversion.Manifest2ModelPNRepResult;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.visualization.projection.LogSource;
import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;

@Plugin(name = "Project Manifest to Model for Conformance", level = PluginLevel.PeerReviewed, returnLabels = { "Manifests, projected onto model" }, returnTypes = { JComponent.class }, parameterLabels = { "Manifests" }, userAccessible = true)
@Visualizer
public class ManifestModelProjectionVisualization {
	
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Manifest manifest) {
		System.gc();
		
		// convert manifest to PNRepResult together with its mapping
		System.gc();
		PetrinetGraph net = manifest.getNet();
		Marking initMarking = manifest.getInitMarking();

		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog resultLog = factory.createLog(manifest.getLog().getAttributes());
		XEventClass dummyEvClass = new XEventClass("DUMMY", -1);
		TransEvClassMapping transMapping = new TransEvClassMapping(manifest.getEvClassifier(), dummyEvClass);
		
		PNRepResult replayResult = Manifest2ModelPNRepResult.convert(manifest);
		Manifest2ModelPNRepResult.createLogMapForManifest(manifest, factory, resultLog, dummyEvClass, transMapping);
		
		return new PNLogReplayProjectedVisPanel(context, net, initMarking, resultLog, transMapping, replayResult, new LogSource(manifest.getLog()));
	}
}
