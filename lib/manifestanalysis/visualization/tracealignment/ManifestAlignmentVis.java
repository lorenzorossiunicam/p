/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.tracealignment;

import javax.swing.JComponent;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.manifestanalysis.conversion.LogCreatorManifestTraceAlignment;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.pnalignanalysis.visualization.tracealignment.ActivityColorMap;
import org.processmining.plugins.pnalignanalysis.visualization.tracealignment.TraceAlignmentDelegate;

/**
 * @author aadrians Jun 1, 2012
 * 
 */
@Plugin(name = "Trace Alignment of Manifests", returnLabels = { "Manifests, visualized as Trace Alignment" }, returnTypes = { JComponent.class }, parameterLabels = { "Manifests" }, userAccessible = false)
@Visualizer
public class ManifestAlignmentVis {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(final PluginContext context, Manifest manifest) {
		System.gc();

		try {
			XFactory factory = XFactoryRegistry.instance().currentDefault();
			XLog resultLog = factory.createLog();

			// create log for manifest
			ActivityColorMap acm = new ActivityColorMap();
			LogCreatorManifestTraceAlignment.createTraceAlignmentLogForManifest(manifest, factory, resultLog, acm);

			// create alignment from the log
			return TraceAlignmentDelegate.createAlignmentTree(context, resultLog, acm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			context.getFutureResult(0).cancel(true);
			return null;
		}

	}
}
