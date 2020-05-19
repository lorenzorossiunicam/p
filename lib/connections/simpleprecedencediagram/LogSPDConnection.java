/**
 * 
 */
package org.processmining.connections.simpleprecedencediagram;

import java.util.Collection;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.annotations.ConnectionObjectFactory;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.AbstractLogModelConnection;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDEdge;
import org.processmining.models.simpleprecedencediagram.SPDNode;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 26, 2009
 */
@Plugin(name = "Log SPD Connection Factory", parameterLabels = { "Log", "Event Classes", "SPD", "Relations" }, returnTypes = LogSPDConnection.class, returnLabels = "Log SPD connection", userAccessible = false)
@ConnectionObjectFactory
public class LogSPDConnection extends
		AbstractLogModelConnection<SPDNode, SPDEdge<? extends SPDNode, ? extends SPDNode>> {

	/**
	 * Default constructor
	 * 
	 * @param log
	 * @param classes
	 * @param graph
	 * @param relations
	 */
	public LogSPDConnection(XLog log, XEventClasses classes, SPD graph, Collection<Pair<SPDNode, XEventClass>> relations) {
		super(log, classes, graph, graph.getNodes(), relations);
	}

	/**
	 * Static factory to produce LogSPDConnection
	 * 
	 * @param context
	 * @param log
	 * @param classes
	 * @param graph
	 * @param relations
	 * @return
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2, 3 })
	public static LogSPDConnection logSPDConnectionFactory(PluginContext context, XLog log, XEventClasses classes,
			SPD graph, Collection<Pair<SPDNode, XEventClass>> relations) {
		LogSPDConnection logSPDConnection = new LogSPDConnection(log, classes, graph, relations);
		return logSPDConnection;
	}

}
