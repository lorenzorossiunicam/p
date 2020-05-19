package org.processmining.plugins.socialnetwork.analysis;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;

/**
 * @author Minseok Song
 * @version 1.0
 */
public class SocialNetworkAnalysisPlugin {

	@Plugin(name = "Visualize Social Network", level = PluginLevel.PeerReviewed, parameterLabels = { "Social Network" }, returnLabels = { "Social Network Visualization" }, returnTypes = { JComponent.class })
	@Visualizer
	public static JPanel invokeSNA(PluginContext context, SocialNetwork sn) {
		return new SocialNetworkAnalysisUI(context, sn);
	}
}
