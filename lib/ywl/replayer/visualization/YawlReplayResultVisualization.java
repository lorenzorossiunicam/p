package org.processmining.plugins.ywl.replayer.visualization;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.yawlnet.YawlLogReplayResultConnection;
import org.processmining.models.graphbased.directed.yawl.YawlPerformanceResult;
import org.processmining.models.graphbased.directed.yawl.YawlReplayResult;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
@Plugin(name = "Visualize YAWL net replay result", 
		returnLabels = { "Visualized YAWL net Replay result" }, 
		returnTypes = { JComponent.class }, 
		parameterLabels = {"Yawl net replay result"}, 
		userAccessible = false)
@Visualizer
public class YawlReplayResultVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, YawlReplayResult replayResult){		
		// Call the garbage collector. This makes sure that no
		// connections are shown that should have been removed
		System.gc();
		
		return getVisualizationPanel(context, replayResult);
	}

	private JComponent getVisualizationPanel(PluginContext context, YawlReplayResult replayResult) {
		// create standard replay result (only consists of FlexReplayResult object
		YawlReplayResultVisPanel resultPanel = new YawlReplayResultVisPanel(context, replayResult.getYPD(), context.getProgress());

		// Check for available conformance measurement
		ConnectionManager cm = context.getConnectionManager();
		try {
			for (YawlLogReplayResultConnection replResultConn : cm.getConnections(YawlLogReplayResultConnection.class, context, replayResult)) {
				YawlPerformanceResult yawlPerformanceResult = replResultConn.getObjectWithRole(YawlLogReplayResultConnection.YAWLPERFORMANCERESULT);
				resultPanel.addPerformanceInfo(yawlPerformanceResult);
			}
		} catch (ConnectionCannotBeObtained e) {
			// No connections available
		}
		return resultPanel; 
	}
}
