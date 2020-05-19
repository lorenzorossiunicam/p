/**
 * 
 */
package org.processmining.connections.aggregatedactivitiesperformancediagram;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.performancemeasurement.GlobalSettingsData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 13, 2009
 */
public class AAPDLogReplayConnection extends AbstractConnection {
	public final static String AAPD = "AggregatedActivitiesPerformanceDiagram";
	public final static String GLOBALSETTINGSDATA = "GlobalSettingsData";

	/**
	 * Default constructor
	 * 
	 * @param aapd
	 * @param globalSettingsData
	 */
	public AAPDLogReplayConnection(AAPD aapd, GlobalSettingsData globalSettingsData) {
		super(aapd.getLabel());

		put(AAPD, aapd);
		put(GLOBALSETTINGSDATA, globalSettingsData);
	}
}
