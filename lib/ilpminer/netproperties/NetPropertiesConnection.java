package org.processmining.plugins.ilpminer.netproperties;

import org.processmining.framework.connections.annotations.ConnectionObjectFactory;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

@Plugin(name = "Average connector degree, Density and Petrinet Connection Factory", parameterLabels = { "Net Properties", "Petrinet" }, returnTypes = NetPropertiesConnection.class, returnLabels = "Average connector degree, Density and Petrinet Connection", userAccessible = false)
@ConnectionObjectFactory
public class NetPropertiesConnection extends AbstractConnection {

	public static String PROPERTIES = "Average connector degree and Density";
	public static String PNET = "Petrinet";

	public NetPropertiesConnection(NetPropertiesResult result, Petrinet net) {
		super("FitnessConnection");
		put(PROPERTIES, result);
		put(PNET, net);
	}
}
