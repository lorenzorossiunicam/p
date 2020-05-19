package org.processmining.plugins.ywl;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.yawlfoundation.yawl.editor.net.NetGraph;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
@Plugin(name = "Visualize YAWL net", 
		returnLabels = { "Visualized Yawl Net" }, 
		returnTypes = { JComponent.class }, 
		parameterLabels = {"Yawl Net"}, 
		userAccessible = false)
@Visualizer
public class YwlNetGraphVisualization {
	
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, NetGraph graph){		
		// Call the garbage collector. This makes sure that no
		// connections are shown that should have been removed
		System.gc();		
		return getVisualizationPanel(graph);
	}

	public JComponent getVisualizationPanel(NetGraph graph) {
		YawlNetGraphVisualizationPanel graphVisPanel = new YawlNetGraphVisualizationPanel(graph);
		return graphVisPanel;
	}
	
}
