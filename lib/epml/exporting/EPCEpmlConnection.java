package org.processmining.plugins.epml.exporting;

import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.epc.EPCGraph;
import org.processmining.plugins.epml.Epml;

/**
 * Created by napoli on 3/08/14.
 */
public class EPCEpmlConnection extends AbstractStrongReferencingConnection {

    public static final String EPC_GRAPH="EPCGraph";
    public static final String EPML="EPML";

    public EPCEpmlConnection(EPCGraph epcGraph, Epml epml) {
        super(epml.toString());
        put(EPC_GRAPH,epcGraph);
        putStrong(EPML,epml);

    }
}
