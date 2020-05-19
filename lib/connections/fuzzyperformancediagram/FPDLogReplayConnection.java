/**
 * 
 */
package org.processmining.connections.fuzzyperformancediagram;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.models.performancemeasurement.dataelements.CaseKPIData;
import org.processmining.models.performancemeasurement.dataelements.FPDElementPerformanceMeasurementData;
import org.processmining.models.performancemeasurement.dataelements.TwoFPDNodesPerformanceData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 16, 2009
 */
public class FPDLogReplayConnection extends AbstractConnection {

	public final static String FPD = "FuzzyPerformanceDiagram";
	public final static String CASEKPIDATA = "CaseKPIData";
	public final static String GLOBALSETTINGSDATA = "GlobalSettingsData";
	public final static String ELEMENTPERFORMANCEMEASUREMENTDATA = "ElementPerformanceMeasurementData";
	public final static String TWONODESPERFORMANCEDATA = "TwoNodesPerformanceData";

	public FPDLogReplayConnection(FPD fpd, CaseKPIData caseKPIData, GlobalSettingsData globalSettingsData,
			FPDElementPerformanceMeasurementData elementPerformanceMeasurementData,
			TwoFPDNodesPerformanceData twoNodesPerformanceData) {
		super("Global setting of " + fpd.getLabel());
		put(FPD, fpd);
		put(CASEKPIDATA, caseKPIData);
		put(GLOBALSETTINGSDATA, globalSettingsData);
		put(ELEMENTPERFORMANCEMEASUREMENTDATA, elementPerformanceMeasurementData);
		put(TWONODESPERFORMANCEDATA, twoNodesPerformanceData);
	}
}
